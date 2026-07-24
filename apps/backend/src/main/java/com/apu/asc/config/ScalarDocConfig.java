package com.apu.asc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
public class ScalarDocConfig {

    @GetMapping(value = "/docs", produces = "text/html")
    public String scalarDocHtml() {
        return """
            <!doctype html>
            <html>
              <head>
                <title>APU Automotive Service Centre API Reference</title>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <link rel="icon" type="image/svg+xml" href="https://scalar.com/favicon.svg" />
              </head>
              <body>
                <script
                  id="api-reference"
                  data-url="/v3/api-docs"
                  data-theme="purple"
                  src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
              </body>
            </html>
            """;
    }
}
