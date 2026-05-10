package com.senla.pas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenApiSpecBuilder {

    private final ObjectMapper objectMapper;

    private static final java.util.Set<String> PUBLIC_PATHS = java.util.Set.of(
            "/api/health",
            "/api/auth/register",
            "/api/auth/login",
            "/api/docs/openapi.json",
            "/api/docs/swagger-ui.html"
    );

    @Autowired
    public OpenApiSpecBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode build() {
        ObjectNode spec = objectMapper.createObjectNode();
        buildInfo(spec);
        buildServers(spec);
        buildPaths(spec);
        buildComponents(spec);
        return spec;
    }

    private void buildInfo(ObjectNode spec) {
        spec.put("openapi", "3.0.0");
        ObjectNode info = spec.putObject("info");
        info.put("title", "PersonalAdsSystem API");
        info.put("version", "1.0");
        info.put("description", "REST API для системы частных объявлений");
    }

    private void buildServers(ObjectNode spec) {
        spec.putArray("servers")
                .addObject()
                .put("url", "http://localhost:8081");
    }

    private void buildPaths(ObjectNode spec) {
        ObjectNode paths = spec.putObject("paths");
        buildAdPaths(paths);
        buildAuthPaths(paths);
        buildUserPaths(paths);
        buildChatPaths(paths);
        buildMessagePaths(paths);
        buildCommentPaths(paths);
        buildRatingPaths(paths);
        buildPaymentPaths(paths);
        buildSalePaths(paths);
        buildHealthPath(paths);

        applySecurityAndResponses(paths);
    }

    private void applySecurityAndResponses(ObjectNode paths) {
        paths.fields().forEachRemaining(pathEntry -> {
            String path = pathEntry.getKey();
            ObjectNode pathItem = (ObjectNode) pathEntry.getValue();

            pathItem.fields().forEachRemaining(opEntry -> {
                ObjectNode op = (ObjectNode) opEntry.getValue();
                ObjectNode responses = op.putObject("responses");

                boolean requiresAuth = !PUBLIC_PATHS.contains(path);

                if (requiresAuth) {
                    ArrayNode security = objectMapper.createArrayNode();
                    ObjectNode bearerAuth = objectMapper.createObjectNode();
                    bearerAuth.set("bearerAuth", objectMapper.createArrayNode());
                    security.add(bearerAuth);
                    op.set("security", security);

                    responses.set("401", ref("Unauthorized"));
                }

                responses.set("500", ref("InternalServerError"));
            });
        });
    }

    private ObjectNode ref(String componentName) {
        return objectMapper.createObjectNode()
                .put("$ref", "#/components/responses/" + componentName);
    }

    private void buildAuthPaths(ObjectNode paths) {
        ObjectNode register = paths.putObject("/api/auth/register").putObject("post");
        register.put("summary", "Регистрация пользователя");
        register.put("operationId", "register");
        addTag(register, "Аутентификация");
        addRequestBody(register, "RegisterRequest");
        ObjectNode registerResponses = register.putObject("responses");
        add201(registerResponses, "AuthResponse");
        registerResponses.set("400", ref("BadRequest"));
        registerResponses.set("409", ref("Conflict"));

        ObjectNode login = paths.putObject("/api/auth/login").putObject("post");
        login.put("summary", "Вход в систему");
        login.put("operationId", "login");
        addTag(login, "Аутентификация");
        addRequestBody(login, "LoginRequest");
        ObjectNode loginResponses = login.putObject("responses");
        add200(loginResponses, "AuthResponse");
        loginResponses.set("400", ref("BadRequest"));
        loginResponses.set("401", ref("Unauthorized"));
    }

    private void buildAdPaths(ObjectNode paths) {
        ObjectNode getAds = paths.putObject("/api/ads").putObject("get");
        getAds.put("summary", "Получить объявления с фильтрацией");
        getAds.put("operationId", "getAds");
        addTag(getAds, "Объявления");
        ArrayNode params = getAds.putArray("parameters");
        addQueryParam(params, "category", "string", false);
        addQueryParam(params, "searchText", "string", false);
        addQueryParam(params, "minPrice", "integer", false);
        addQueryParam(params, "maxPrice", "integer", false);
        addQueryParam(params, "sortBy", "string", false);
        addQueryParam(params, "sortDirection", "string", false);
        addQueryParamWithDefault(params, "page", "integer", "0");
        addQueryParamWithDefault(params, "size", "integer", "20");
        ObjectNode getAdsResponses = getAds.putObject("responses");
        add200Array(getAdsResponses, "AdResponse");

        ObjectNode adsNode = (ObjectNode) paths.get("/api/ads");
        if (adsNode == null) adsNode = paths.putObject("/api/ads");
        ObjectNode postAd = adsNode.putObject("post");
        postAd.put("summary", "Создать объявление");
        postAd.put("operationId", "createAd");
        addTag(postAd, "Объявления");
        addRequestBody(postAd, "CreateAdRequest");
        ObjectNode postAdResponses = postAd.putObject("responses");
        add201(postAdResponses, "AdResponse");
        postAdResponses.set("400", ref("BadRequest"));

        ObjectNode adById = paths.putObject("/api/ads/{adId}");

        ObjectNode getAdById = adById.putObject("get");
        getAdById.put("summary", "Получить объявление по ID");
        addTag(getAdById, "Объявления");
        addPathParam(getAdById.putArray("parameters"), "adId");
        ObjectNode getAdByIdResp = getAdById.putObject("responses");
        add200(getAdByIdResp, "AdResponse");
        getAdByIdResp.set("404", ref("NotFound"));

        ObjectNode putAd = adById.putObject("put");
        putAd.put("summary", "Обновить объявление");
        addTag(putAd, "Объявления");
        addPathParam(putAd.putArray("parameters"), "adId");
        addRequestBody(putAd, "UpdateAdRequest");
        ObjectNode putAdResp = putAd.putObject("responses");
        add200(putAdResp, "AdResponse");
        putAdResp.set("400", ref("BadRequest"));
        putAdResp.set("403", ref("Forbidden"));
        putAdResp.set("404", ref("NotFound"));

        ObjectNode deleteAd = adById.putObject("delete");
        deleteAd.put("summary", "Удалить объявление");
        addTag(deleteAd, "Объявления");
        addPathParam(deleteAd.putArray("parameters"), "adId");
        ObjectNode deleteAdResp = deleteAd.putObject("responses");
        add200(deleteAdResp, "AdResponse");
        deleteAdResp.set("403", ref("Forbidden"));
        deleteAdResp.set("404", ref("NotFound"));

        ObjectNode myAds = paths.putObject("/api/ads/my").putObject("get");
        myAds.put("summary", "Получить свои объявления");
        addTag(myAds, "Объявления");
        add200Array(myAds.putObject("responses"), "AdResponse");

        ObjectNode userAds = paths.putObject("/api/ads/user/{userId}").putObject("get");
        userAds.put("summary", "Получить объявления пользователя");
        addTag(userAds, "Объявления");
        addPathParam(userAds.putArray("parameters"), "userId");
        add200Array(userAds.putObject("responses"), "AdResponse");
    }

    private void buildUserPaths(ObjectNode paths) {
        ObjectNode me = paths.putObject("/api/users/me");

        ObjectNode getMe = me.putObject("get");
        getMe.put("summary", "Получить свой профиль");
        addTag(getMe, "Пользователи");
        add200(getMe.putObject("responses"), "UserResponse");

        ObjectNode putMe = me.putObject("put");
        putMe.put("summary", "Обновить профиль");
        addTag(putMe, "Пользователи");
        addRequestBody(putMe, "UpdateUserRequest");
        ObjectNode putMeResp = putMe.putObject("responses");
        add200(putMeResp, "UserResponse");
        putMeResp.set("400", ref("BadRequest"));
        putMeResp.set("409", ref("Conflict"));

        ObjectNode userById = paths.putObject("/api/users/{userId}");

        ObjectNode getUser = userById.putObject("get");
        getUser.put("summary", "Получить пользователя по ID");
        addTag(getUser, "Пользователи");
        addPathParam(getUser.putArray("parameters"), "userId");
        ObjectNode getUserResp = getUser.putObject("responses");
        add200(getUserResp, "UserResponse");
        getUserResp.set("404", ref("NotFound"));

        ObjectNode deleteUser = userById.putObject("delete");
        deleteUser.put("summary", "Удалить пользователя");
        addTag(deleteUser, "Пользователи");
        addPathParam(deleteUser.putArray("parameters"), "userId");
        ObjectNode deleteUserResp = deleteUser.putObject("responses");
        add200(deleteUserResp, "UserResponse");
        deleteUserResp.set("403", ref("Forbidden"));
        deleteUserResp.set("404", ref("NotFound"));

        ObjectNode allUsers = paths.putObject("/api/users").putObject("get");
        allUsers.put("summary", "Получить всех пользователей (Admin)");
        addTag(allUsers, "Пользователи");
        add200Array(allUsers.putObject("responses"), "UserResponse");

        ObjectNode filterUsers = paths.putObject("/api/users/filter").putObject("get");
        filterUsers.put("summary", "Пользователи отфильтрованные по рейтингу");
        addTag(filterUsers, "Пользователи");
        ArrayNode fp = filterUsers.putArray("parameters");
        addQueryParam(fp, "direction", "string", false);
        addQueryParam(fp, "minRating", "number", false);
        addQueryParam(fp, "maxRating", "number", false);
        add200Array(filterUsers.putObject("responses"), "UserResponse");
    }

    private void buildChatPaths(ObjectNode paths) {
        ObjectNode myChats = paths.putObject("/api/chats").putObject("get");
        myChats.put("summary", "Получить свои чаты");
        addTag(myChats, "Чаты");
        add200Array(myChats.putObject("responses"), "ChatResponse");

        ObjectNode chatById = paths.putObject("/api/chats/{chatId}").putObject("get");
        chatById.put("summary", "Получить чат по ID");
        addTag(chatById, "Чаты");
        addPathParam(chatById.putArray("parameters"), "chatId");
        ObjectNode chatByIdResp = chatById.putObject("responses");
        add200(chatByIdResp, "ChatResponse");
        chatByIdResp.set("403", ref("Forbidden"));
        chatByIdResp.set("404", ref("NotFound"));

        ObjectNode createChat = paths.putObject("/api/chats/ad/{adId}").putObject("post");
        createChat.put("summary", "Открыть или создать чат по объявлению");
        addTag(createChat, "Чаты");
        addPathParam(createChat.putArray("parameters"), "adId");
        ObjectNode createChatResp = createChat.putObject("responses");
        add200(createChatResp, "ChatResponse");
        createChatResp.set("400", ref("BadRequest"));
        createChatResp.set("403", ref("Forbidden"));
        createChatResp.set("404", ref("NotFound"));
    }

    private void buildMessagePaths(ObjectNode paths) {
        ObjectNode messages = paths.putObject("/api/chats/{chatId}/messages");

        ObjectNode getMessages = messages.putObject("get");
        getMessages.put("summary", "Получить сообщения чата");
        addTag(getMessages, "Сообщения");
        addPathParam(getMessages.putArray("parameters"), "chatId");
        ObjectNode getMsgResp = getMessages.putObject("responses");
        add200Array(getMsgResp, "MessageResponse");
        getMsgResp.set("403", ref("Forbidden"));

        ObjectNode sendMessage = messages.putObject("post");
        sendMessage.put("summary", "Отправить сообщение");
        addTag(sendMessage, "Сообщения");
        addPathParam(sendMessage.putArray("parameters"), "chatId");
        addRequestBody(sendMessage, "MessageRequest");
        ObjectNode sendMsgResp = sendMessage.putObject("responses");
        add201(sendMsgResp, "MessageResponse");
        sendMsgResp.set("400", ref("BadRequest"));
        sendMsgResp.set("403", ref("Forbidden"));

        ObjectNode messageById = paths.putObject("/api/chats/{chatId}/messages/{messageId}");

        ObjectNode getMessage = messageById.putObject("get");
        getMessage.put("summary", "Получить сообщение по ID");
        addTag(getMessage, "Сообщения");
        addTwoPathParams(getMessage.putArray("parameters"), "chatId", "messageId");
        ObjectNode getMsgByIdResp = getMessage.putObject("responses");
        add200(getMsgByIdResp, "MessageResponse");
        getMsgByIdResp.set("403", ref("Forbidden"));
        getMsgByIdResp.set("404", ref("NotFound"));

        ObjectNode updateMessage = messageById.putObject("put");
        updateMessage.put("summary", "Обновить сообщение");
        addTag(updateMessage, "Сообщения");
        addTwoPathParams(updateMessage.putArray("parameters"), "chatId", "messageId");
        addRequestBody(updateMessage, "MessageRequest");
        ObjectNode updateMsgResp = updateMessage.putObject("responses");
        add200(updateMsgResp, "MessageResponse");
        updateMsgResp.set("403", ref("Forbidden"));
        updateMsgResp.set("404", ref("NotFound"));

        ObjectNode deleteMessage = messageById.putObject("delete");
        deleteMessage.put("summary", "Удалить сообщение");
        addTag(deleteMessage, "Сообщения");
        addTwoPathParams(deleteMessage.putArray("parameters"), "chatId", "messageId");
        ObjectNode deleteMsgResp = deleteMessage.putObject("responses");
        add200(deleteMsgResp, "MessageResponse");
        deleteMsgResp.set("403", ref("Forbidden"));
        deleteMsgResp.set("404", ref("NotFound"));

        ObjectNode readMessage = paths.putObject("/api/chats/{chatId}/messages/{messageId}/read")
                .putObject("patch");
        readMessage.put("summary", "Отметить сообщение как прочитанное");
        addTag(readMessage, "Сообщения");
        addTwoPathParams(readMessage.putArray("parameters"), "chatId", "messageId");
        ObjectNode readMsgResp = readMessage.putObject("responses");
        add200(readMsgResp, "MessageResponse");
        readMsgResp.set("403", ref("Forbidden"));
        readMsgResp.set("404", ref("NotFound"));
    }

    private void buildCommentPaths(ObjectNode paths) {
        ObjectNode comments = paths.putObject("/api/ads/{adId}/comments");

        ObjectNode getComments = comments.putObject("get");
        getComments.put("summary", "Получить комментарии объявления");
        addTag(getComments, "Комментарии");
        addPathParam(getComments.putArray("parameters"), "adId");
        add200Array(getComments.putObject("responses"), "CommentResponse");

        ObjectNode addComment = comments.putObject("post");
        addComment.put("summary", "Добавить комментарий");
        addTag(addComment, "Комментарии");
        addPathParam(addComment.putArray("parameters"), "adId");
        addRequestBody(addComment, "CommentRequest");
        ObjectNode addCommentResp = addComment.putObject("responses");
        add201(addCommentResp, "CommentResponse");
        addCommentResp.set("400", ref("BadRequest"));
        addCommentResp.set("404", ref("NotFound"));

        ObjectNode commentById = paths.putObject("/api/ads/{adId}/comments/{commentId}");

        ObjectNode updateComment = commentById.putObject("put");
        updateComment.put("summary", "Обновить комментарий");
        addTag(updateComment, "Комментарии");
        addTwoPathParams(updateComment.putArray("parameters"), "adId", "commentId");
        addRequestBody(updateComment, "CommentRequest");
        ObjectNode updateCommentResp = updateComment.putObject("responses");
        add200(updateCommentResp, "CommentResponse");
        updateCommentResp.set("403", ref("Forbidden"));
        updateCommentResp.set("404", ref("NotFound"));

        ObjectNode deleteComment = commentById.putObject("delete");
        deleteComment.put("summary", "Удалить комментарий");
        addTag(deleteComment, "Комментарии");
        addTwoPathParams(deleteComment.putArray("parameters"), "adId", "commentId");
        ObjectNode deleteCommentResp = deleteComment.putObject("responses");
        add200(deleteCommentResp, "CommentResponse");
        deleteCommentResp.set("403", ref("Forbidden"));
        deleteCommentResp.set("404", ref("NotFound"));
    }

    private void buildRatingPaths(ObjectNode paths) {
        ObjectNode ratings = paths.putObject("/api/users/{userId}/ratings");

        ObjectNode getRatings = ratings.putObject("get");
        getRatings.put("summary", "Получить рейтинги пользователя");
        addTag(getRatings, "Рейтинги");
        addPathParam(getRatings.putArray("parameters"), "userId");
        add200Array(getRatings.putObject("responses"), "RatingResponse");

        ObjectNode addRating = ratings.putObject("post");
        addRating.put("summary", "Поставить оценку продавцу");
        addTag(addRating, "Рейтинги");
        addPathParam(addRating.putArray("parameters"), "userId");
        addRequestBody(addRating, "RatingRequest");
        ObjectNode addRatingResp = addRating.putObject("responses");
        add201(addRatingResp, "RatingResponse");
        addRatingResp.set("400", ref("BadRequest"));
        addRatingResp.set("404", ref("NotFound"));
    }

    private void buildPaymentPaths(ObjectNode paths) {
        ObjectNode myPayments = paths.putObject("/api/payments/my").putObject("get");
        myPayments.put("summary", "Получить свои платежи");
        addTag(myPayments, "Платежи");
        add200Array(myPayments.putObject("responses"), "PaymentResponse");

        ObjectNode paymentById = paths.putObject("/api/payments/{paymentId}").putObject("get");
        paymentById.put("summary", "Получить платёж по ID");
        addTag(paymentById, "Платежи");
        addPathParam(paymentById.putArray("parameters"), "paymentId");
        ObjectNode paymentByIdResp = paymentById.putObject("responses");
        add200(paymentByIdResp, "PaymentResponse");
        paymentByIdResp.set("403", ref("Forbidden"));
        paymentByIdResp.set("404", ref("NotFound"));

        ObjectNode paymentsByAd = paths.putObject("/api/payments/ad/{adId}").putObject("get");
        paymentsByAd.put("summary", "Получить платежи по объявлению");
        addTag(paymentsByAd, "Платежи");
        addPathParam(paymentsByAd.putArray("parameters"), "adId");
        ObjectNode paymentsByAdResp = paymentsByAd.putObject("responses");
        add200Array(paymentsByAdResp, "PaymentResponse");
        paymentsByAdResp.set("403", ref("Forbidden"));

        ObjectNode activePayment = paths.putObject("/api/payments/ad/{adId}/active").putObject("get");
        activePayment.put("summary", "Получить активное продвижение объявления");
        addTag(activePayment, "Платежи");
        addPathParam(activePayment.putArray("parameters"), "adId");
        ObjectNode activePaymentResp = activePayment.putObject("responses");
        add200(activePaymentResp, "PaymentResponse");
        activePaymentResp.set("403", ref("Forbidden"));
        activePaymentResp.set("404", ref("NotFound"));

        ObjectNode createPayment = paths.putObject("/api/payments").putObject("post");
        createPayment.put("summary", "Оплатить продвижение объявления");
        addTag(createPayment, "Платежи");
        addRequestBody(createPayment, "PaymentRequest");
        ObjectNode createPaymentResp = createPayment.putObject("responses");
        add201(createPaymentResp, "PaymentResponse");
        createPaymentResp.set("400", ref("BadRequest"));
        createPaymentResp.set("403", ref("Forbidden"));
        createPaymentResp.set("404", ref("NotFound"));
        createPaymentResp.set("423", ref("Locked"));
    }

    private void buildSalePaths(ObjectNode paths) {
        ObjectNode mySales = paths.putObject("/api/sales/my-sales").putObject("get");
        mySales.put("summary", "История своих продаж");
        addTag(mySales, "История продаж");
        add200Array(mySales.putObject("responses"), "SaleHistoryResponse");

        ObjectNode myPurchases = paths.putObject("/api/sales/my-purchases").putObject("get");
        myPurchases.put("summary", "История своих покупок");
        addTag(myPurchases, "История продаж");
        add200Array(myPurchases.putObject("responses"), "SaleHistoryResponse");

        ObjectNode saleById = paths.putObject("/api/sales/{saleId}").putObject("get");
        saleById.put("summary", "Получить запись о продаже по ID");
        addTag(saleById, "История продаж");
        addPathParam(saleById.putArray("parameters"), "saleId");
        ObjectNode saleByIdResp = saleById.putObject("responses");
        add200(saleByIdResp, "SaleHistoryResponse");
        saleByIdResp.set("403", ref("Forbidden"));
        saleByIdResp.set("404", ref("NotFound"));

        ObjectNode buyDirect = paths.putObject("/api/sales/ad/{adId}/buy").putObject("post");
        buyDirect.put("summary", "Купить объявление напрямую");
        addTag(buyDirect, "История продаж");
        addPathParam(buyDirect.putArray("parameters"), "adId");
        ObjectNode buyDirectResp = buyDirect.putObject("responses");
        add201(buyDirectResp, "SaleHistoryResponse");
        buyDirectResp.set("400", ref("BadRequest"));
        buyDirectResp.set("403", ref("Forbidden"));
        buyDirectResp.set("404", ref("NotFound"));
        buyDirectResp.set("423", ref("Locked"));

        ObjectNode buyViaChat = paths.putObject("/api/sales/chat/{chatId}/buy").putObject("post");
        buyViaChat.put("summary", "Завершить сделку через чат");
        addTag(buyViaChat, "История продаж");
        addPathParam(buyViaChat.putArray("parameters"), "chatId");
        addRequestBody(buyViaChat, "SaleHistoryRequest");
        ObjectNode buyViaChatResp = buyViaChat.putObject("responses");
        add201(buyViaChatResp, "SaleHistoryResponse");
        buyViaChatResp.set("400", ref("BadRequest"));
        buyViaChatResp.set("403", ref("Forbidden"));
        buyViaChatResp.set("404", ref("NotFound"));
        buyViaChatResp.set("423", ref("Locked"));
    }

    private void buildHealthPath(ObjectNode paths) {
        ObjectNode health = paths.putObject("/api/health").putObject("get");
        health.put("summary", "Проверка работоспособности");
        add200(health.putObject("responses"), "HealthResponse");
    }

    private void buildComponents(ObjectNode spec) {
        ObjectNode components = spec.putObject("components");
        buildSecuritySchemes(components);
        buildReusableResponses(components);
        buildSchemas(components);
    }

    private void buildSecuritySchemes(ObjectNode components) {
        ObjectNode bearerAuth = components.putObject("securitySchemes").putObject("bearerAuth");
        bearerAuth.put("type", "http");
        bearerAuth.put("scheme", "bearer");
        bearerAuth.put("bearerFormat", "JWT");
    }

    private void buildReusableResponses(ObjectNode components) {
        ObjectNode responses = components.putObject("responses");
        addReusableResponse(responses, "BadRequest", "400 Bad Request");
        addReusableResponse(responses, "Unauthorized", "401 Unauthorized");
        addReusableResponse(responses, "Forbidden", "403 Forbidden");
        addReusableResponse(responses, "NotFound", "404 Not Found");
        addReusableResponse(responses, "Conflict", "409 Conflict");
        addReusableResponse(responses, "Locked", "423 Locked");
        addReusableResponse(responses, "InternalServerError", "500 Internal Server Error");
    }

    private void addReusableResponse(ObjectNode responses, String name, String description) {
        responses.putObject(name)
                .put("description", description)
                .putObject("content")
                .putObject("application/json")
                .putObject("schema")
                .put("$ref", "#/components/schemas/ErrorResponse");
    }

    private void buildSchemas(ObjectNode components) {
        ObjectNode schemas = components.putObject("schemas");
        buildEnumSchemas(schemas);
        buildRequestSchemas(schemas);
        buildResponseSchemas(schemas);
        buildErrorSchema(schemas);
    }

    private void buildEnumSchemas(ObjectNode schemas) {
        addEnum(schemas, "AdCategory",
                "ELECTRONICS", "TRANSPORT", "REAL_ESTATE", "CLOTHING",
                "HOME_AND_GARDEN", "SERVICES", "HOBBIES", "PETS", "JOBS", "OTHER");
        addEnum(schemas, "AdSort", "DATE", "PRICE", "TITLE", "RATING");
        addEnum(schemas, "SortDirection", "ASC", "DESC");
        addEnum(schemas, "PromotionPlan", "DAY", "THREE_DAYS", "WEEK", "MONTH");
    }

    private void addEnum(ObjectNode schemas, String name, String... values) {
        ObjectNode schema = schemas.putObject(name);
        schema.put("type", "string");
        ArrayNode enumValues = schema.putArray("enum");
        for (String v : values) enumValues.add(v);
    }

    private void buildRequestSchemas(ObjectNode schemas) {
        ObjectNode reg = schemas.putObject("RegisterRequest");
        reg.put("type", "object");
        reg.putArray("required").add("username").add("email").add("password");
        ObjectNode regProps = reg.putObject("properties");
        regProps.putObject("username").put("type", "string").put("minLength", 3).put("maxLength", 100);
        regProps.putObject("email").put("type", "string").put("format", "email");
        regProps.putObject("password").put("type", "string").put("minLength", 6);

        ObjectNode login = schemas.putObject("LoginRequest");
        login.put("type", "object");
        login.putArray("required").add("usernameOrEmail").add("password");
        ObjectNode loginProps = login.putObject("properties");
        loginProps.putObject("usernameOrEmail").put("type", "string");
        loginProps.putObject("password").put("type", "string");

        ObjectNode createAd = schemas.putObject("CreateAdRequest");
        createAd.put("type", "object");
        createAd.putArray("required").add("title").add("description").add("category").add("price");
        ObjectNode createAdProps = createAd.putObject("properties");
        createAdProps.putObject("title").put("type", "string").put("maxLength", 100);
        createAdProps.putObject("description").put("type", "string").put("maxLength", 3000);
        createAdProps.putObject("category").put("$ref", "#/components/schemas/AdCategory");
        createAdProps.putObject("price").put("type", "integer").put("minimum", 0);

        ObjectNode updateAd = schemas.putObject("UpdateAdRequest");
        updateAd.put("type", "object");
        ObjectNode updateAdProps = updateAd.putObject("properties");
        updateAdProps.putObject("title").put("type", "string").put("maxLength", 100);
        updateAdProps.putObject("description").put("type", "string").put("maxLength", 3000);
        updateAdProps.putObject("category").put("$ref", "#/components/schemas/AdCategory");
        updateAdProps.putObject("price").put("type", "integer").put("minimum", 0);
        updateAdProps.putObject("isActive").put("type", "boolean");

        ObjectNode updateUser = schemas.putObject("UpdateUserRequest");
        updateUser.put("type", "object");
        ObjectNode updateUserProps = updateUser.putObject("properties");
        updateUserProps.putObject("newUsername").put("type", "string");
        updateUserProps.putObject("newEmail").put("type", "string").put("format", "email");
        updateUserProps.putObject("newPassword").put("type", "string");
        updateUserProps.putObject("newAboutMe").put("type", "string");

        ObjectNode comment = schemas.putObject("CommentRequest");
        comment.put("type", "object");
        comment.putArray("required").add("content");
        comment.putObject("properties").putObject("content").put("type", "string");

        ObjectNode message = schemas.putObject("MessageRequest");
        message.put("type", "object");
        message.putArray("required").add("content");
        message.putObject("properties").putObject("content").put("type", "string");

        ObjectNode rating = schemas.putObject("RatingRequest");
        rating.put("type", "object");
        rating.putArray("required").add("rating");
        rating.putObject("properties").putObject("rating")
                .put("type", "integer").put("minimum", 1).put("maximum", 5);

        ObjectNode payment = schemas.putObject("PaymentRequest");
        payment.put("type", "object");
        payment.putArray("required").add("adId").add("plan");
        ObjectNode paymentProps = payment.putObject("properties");
        paymentProps.putObject("adId").put("type", "integer").put("format", "int64");
        paymentProps.putObject("plan").put("$ref", "#/components/schemas/PromotionPlan");

        ObjectNode sale = schemas.putObject("SaleHistoryRequest");
        sale.put("type", "object");
        sale.putObject("properties").putObject("price").put("type", "integer").put("minimum", 1);
    }

    private void buildResponseSchemas(ObjectNode schemas) {
        ObjectNode userShort = schemas.putObject("UserShortResponse");
        userShort.put("type", "object");
        ObjectNode userShortProps = userShort.putObject("properties");
        userShortProps.putObject("id").put("type", "integer").put("format", "int64");
        userShortProps.putObject("username").put("type", "string");
        userShortProps.putObject("averageRating").put("type", "number").put("format", "double");

        ObjectNode user = schemas.putObject("UserResponse");
        user.put("type", "object");
        ObjectNode userProps = user.putObject("properties");
        userProps.putObject("id").put("type", "integer").put("format", "int64");
        userProps.putObject("username").put("type", "string");
        userProps.putObject("email").put("type", "string");
        userProps.putObject("aboutMe").put("type", "string");
        userProps.putObject("averageRating").put("type", "number");
        userProps.putObject("createdAt").put("type", "string").put("format", "date-time");
        userProps.putObject("roles").put("type", "array").putObject("items").put("type", "string");

        ObjectNode auth = schemas.putObject("AuthResponse");
        auth.put("type", "object");
        ObjectNode authProps = auth.putObject("properties");
        authProps.putObject("accessToken").put("type", "string");
        authProps.putObject("tokenType").put("type", "string");
        authProps.putObject("user").put("$ref", "#/components/schemas/UserResponse");

        ObjectNode ad = schemas.putObject("AdResponse");
        ad.put("type", "object");
        ObjectNode adProps = ad.putObject("properties");
        adProps.putObject("id").put("type", "integer").put("format", "int64");
        adProps.putObject("title").put("type", "string");
        adProps.putObject("description").put("type", "string");
        adProps.putObject("category").put("$ref", "#/components/schemas/AdCategory");
        adProps.putObject("user").put("$ref", "#/components/schemas/UserShortResponse");
        adProps.putObject("price").put("type", "integer");
        adProps.putObject("isActive").put("type", "boolean");
        adProps.putObject("isPremium").put("type", "boolean");
        adProps.putObject("createdAt").put("type", "string").put("format", "date-time");

        ObjectNode commentResp = schemas.putObject("CommentResponse");
        commentResp.put("type", "object");
        ObjectNode commentRespProps = commentResp.putObject("properties");
        commentRespProps.putObject("id").put("type", "integer").put("format", "int64");
        commentRespProps.putObject("username").put("type", "string");
        commentRespProps.putObject("adId").put("type", "integer").put("format", "int64");
        commentRespProps.putObject("content").put("type", "string");
        commentRespProps.putObject("sendAt").put("type", "string").put("format", "date-time");

        ObjectNode messageResp = schemas.putObject("MessageResponse");
        messageResp.put("type", "object");
        ObjectNode messageRespProps = messageResp.putObject("properties");
        messageRespProps.putObject("id").put("type", "integer").put("format", "int64");
        messageRespProps.putObject("senderUsername").put("type", "string");
        messageRespProps.putObject("chatId").put("type", "integer").put("format", "int64");
        messageRespProps.putObject("content").put("type", "string");
        messageRespProps.putObject("sendAt").put("type", "string").put("format", "date-time");
        messageRespProps.putObject("isRead").put("type", "boolean");

        ObjectNode chatResp = schemas.putObject("ChatResponse");
        chatResp.put("type", "object");
        ObjectNode chatRespProps = chatResp.putObject("properties");
        chatRespProps.putObject("id").put("type", "integer").put("format", "int64");
        chatRespProps.putObject("adId").put("type", "integer").put("format", "int64");
        chatRespProps.putObject("adTitle").put("type", "string");
        chatRespProps.putObject("buyer").put("$ref", "#/components/schemas/UserShortResponse");
        chatRespProps.putObject("seller").put("$ref", "#/components/schemas/UserShortResponse");
        chatRespProps.putObject("createdAt").put("type", "string").put("format", "date-time");

        ObjectNode ratingResp = schemas.putObject("RatingResponse");
        ratingResp.put("type", "object");
        ObjectNode ratingRespProps = ratingResp.putObject("properties");
        ratingRespProps.putObject("id").put("type", "integer").put("format", "int64");
        ratingRespProps.putObject("reviewer").put("$ref", "#/components/schemas/UserShortResponse");
        ratingRespProps.putObject("recipient").put("$ref", "#/components/schemas/UserShortResponse");
        ratingRespProps.putObject("rating").put("type", "integer");
        ratingRespProps.putObject("createdAt").put("type", "string").put("format", "date-time");

        ObjectNode paymentResp = schemas.putObject("PaymentResponse");
        paymentResp.put("type", "object");
        ObjectNode paymentRespProps = paymentResp.putObject("properties");
        paymentRespProps.putObject("id").put("type", "integer").put("format", "int64");
        paymentRespProps.putObject("adId").put("type", "integer").put("format", "int64");
        paymentRespProps.putObject("user").put("$ref", "#/components/schemas/UserShortResponse");
        paymentRespProps.putObject("plan").put("$ref", "#/components/schemas/PromotionPlan");
        paymentRespProps.putObject("amount").put("type", "integer");
        paymentRespProps.putObject("confirmedAt").put("type", "string").put("format", "date-time");
        paymentRespProps.putObject("expireAt").put("type", "string").put("format", "date-time");

        ObjectNode saleResp = schemas.putObject("SaleHistoryResponse");
        saleResp.put("type", "object");
        ObjectNode saleRespProps = saleResp.putObject("properties");
        saleRespProps.putObject("id").put("type", "integer").put("format", "int64");
        saleRespProps.putObject("adId").put("type", "integer").put("format", "int64");
        saleRespProps.putObject("adTitle").put("type", "string");
        saleRespProps.putObject("seller").put("$ref", "#/components/schemas/UserShortResponse");
        saleRespProps.putObject("buyer").put("$ref", "#/components/schemas/UserShortResponse");
        saleRespProps.putObject("price").put("type", "integer");
        saleRespProps.putObject("soldAt").put("type", "string").put("format", "date-time");

        ObjectNode health = schemas.putObject("HealthResponse");
        health.put("type", "object");
        ObjectNode healthProps = health.putObject("properties");
        healthProps.putObject("status").put("type", "string");
        healthProps.putObject("timestamp").put("type", "string").put("format", "date-time");
        healthProps.putObject("service").put("type", "string");
        healthProps.putObject("version").put("type", "string");
    }

    private void buildErrorSchema(ObjectNode schemas) {
        ObjectNode error = schemas.putObject("ErrorResponse");
        error.put("type", "object");
        ObjectNode errorProps = error.putObject("properties");
        errorProps.putObject("status").put("type", "integer");
        errorProps.putObject("error").put("type", "string");
        errorProps.putObject("message").put("type", "string");
        errorProps.putObject("timestamp").put("type", "string").put("format", "date-time");
        ObjectNode fieldErrors = errorProps.putObject("fieldErrors");
        fieldErrors.put("type", "array");
        ObjectNode fieldItem = fieldErrors.putObject("items");
        fieldItem.put("type", "object");
        ObjectNode fieldItemProps = fieldItem.putObject("properties");
        fieldItemProps.putObject("field").put("type", "string");
        fieldItemProps.putObject("message").put("type", "string");
    }

    private void addTag(ObjectNode op, String tag) {
        op.putArray("tags").add(tag);
    }

    private void addRequestBody(ObjectNode op, String schemaRef) {
        op.putObject("requestBody")
                .put("required", true)
                .putObject("content")
                .putObject("application/json")
                .putObject("schema")
                .put("$ref", "#/components/schemas/" + schemaRef);
    }

    private void add200(ObjectNode responses, String schemaRef) {
        responses.putObject("200")
                .put("description", "Success")
                .putObject("content")
                .putObject("application/json")
                .putObject("schema")
                .put("$ref", "#/components/schemas/" + schemaRef);
    }

    private void add201(ObjectNode responses, String schemaRef) {
        responses.putObject("201")
                .put("description", "Created")
                .putObject("content")
                .putObject("application/json")
                .putObject("schema")
                .put("$ref", "#/components/schemas/" + schemaRef);
    }

    private void add200Array(ObjectNode responses, String schemaRef) {
        responses.putObject("200")
                .put("description", "Success")
                .putObject("content")
                .putObject("application/json")
                .putObject("schema")
                .put("type", "array")
                .putObject("items")
                .put("$ref", "#/components/schemas/" + schemaRef);
    }

    private void addPathParam(ArrayNode params, String name) {
        params.addObject()
                .put("name", name)
                .put("in", "path")
                .put("required", true)
                .putObject("schema")
                .put("type", "integer")
                .put("format", "int64");
    }

    private void addTwoPathParams(ArrayNode params, String first, String second) {
        addPathParam(params, first);
        addPathParam(params, second);
    }

    private void addQueryParam(ArrayNode params, String name, String type, boolean required) {
        params.addObject()
                .put("name", name)
                .put("in", "query")
                .put("required", required)
                .putObject("schema")
                .put("type", type);
    }

    private void addQueryParamWithDefault(ArrayNode params, String name, String type, String defaultValue) {
        ObjectNode param = params.addObject();
        param.put("name", name)
                .put("in", "query")
                .put("required", false);
        param.putObject("schema")
                .put("type", type)
                .put("default", defaultValue);
    }
}