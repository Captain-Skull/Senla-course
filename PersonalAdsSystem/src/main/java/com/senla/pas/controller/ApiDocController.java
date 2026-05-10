package com.senla.pas.controller;

import com.senla.pas.config.OpenApiSpecBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/docs")
public class ApiDocController {

    private final OpenApiSpecBuilder specBuilder;

    @Autowired
    public ApiDocController(OpenApiSpecBuilder specBuilder) {
        this.specBuilder = specBuilder;
    }

    @GetMapping(value = "/openapi.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> openApiSpec() {
        return ResponseEntity.ok(specBuilder.build());
    }

    @GetMapping(value = "/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> swaggerUI() {
        byte[] html = buildSwaggerHtml().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private String buildSwaggerHtml() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <title>PersonalAdsSystem API</title>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css">
                </head>
                <body>
                  <div id="swagger-ui"></div>
                  <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                  <script>
                    window.onload = function() {
                      SwaggerUIBundle({
                        url: '/api/docs/openapi.json',
                        dom_id: '#swagger-ui',
                        presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset],
                        layout: 'BaseLayout',
                        persistAuthorization: true
                      });
                    };
                  </script>
                </body>
                </html>""";
    }
}