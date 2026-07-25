package com.apu.asc.config;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScalarUiController {

  @GetMapping(value = "/scalar", produces = MediaType.TEXT_HTML_VALUE)
  public String scalarApiReference() {
    return """
        <!doctype html>
        <html>
          <head>
            <title>APU-ASC API Reference</title>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <style>
              body {
                margin: 0;
                padding: 0;
              }
            </style>
          </head>
          <body>
            <script
              id="api-reference"
              data-url="/v3/api-docs"
              src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
          </body>
        </html>
        """;
  }
}
