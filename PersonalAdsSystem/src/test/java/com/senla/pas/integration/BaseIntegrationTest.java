package com.senla.pas.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senla.pas.config.SecurityConfig;
import com.senla.pas.config.TestAppConfig;
import com.senla.pas.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.test.context.jdbc.Sql;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Tag("integration")
@WebAppConfiguration
@ContextConfiguration(classes = {TestAppConfig.class, SecurityConfig.class})
@Sql(scripts = "/db/test-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIntegrationTest {

    private static final String DEFAULT_JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final String DEFAULT_JWT_EXPIRATION_MS = "3600000";

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("pas_test_db")
            .withUsername("pas_test_user")
            .withPassword("pas_test_password");

    static {
        if (System.getProperty("api.version") == null && System.getenv("DOCKER_API_VERSION") == null) {
            System.setProperty("api.version", "1.44");
        }
        postgres.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("test.db.url", postgres::getJdbcUrl);
        registry.add("test.db.username", postgres::getUsername);
        registry.add("test.db.password", postgres::getPassword);

        registry.add("jwt.secret", () -> System.getProperty("jwt.secret", DEFAULT_JWT_SECRET));
        registry.add("jwt.expiration.ms", () -> System.getProperty("jwt.expiration.ms", DEFAULT_JWT_EXPIRATION_MS));
    }

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    protected RequestPostProcessor authUser(Long id, String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
        CustomUserDetails userDetails = new CustomUserDetails(id, username, "n/a", authorities);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }

    protected String json(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    protected Long responseId(MvcResult result) throws UnsupportedEncodingException, JsonProcessingException {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode idNode = node.get("id");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asLong();
        }
        JsonNode userNode = node.get("user");
        if (userNode != null && userNode.get("id") != null && !userNode.get("id").isNull()) {
            return userNode.get("id").asLong();
        }
        throw new IllegalStateException("Response does not contain id or user.id: " + result.getResponse().getContentAsString());
    }
}
