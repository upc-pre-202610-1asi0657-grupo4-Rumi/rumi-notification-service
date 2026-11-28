package com.rumi.notification;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The exported OpenAPI document must keep the Spanish texts readable. UTF-8 read as cp1252 (the default
 * encoding of a Windows console) turns every accented letter into two characters that start with U+00C3,
 * so the document must contain neither that character nor its JSON escape. Both are built at run time, so
 * this file can be searched for them too.
 */
class DocsEncodingTest {

    private static final Path OPENAPI = Path.of("docs/openapi.json");
    private static final String MOJIBAKE_CHARACTER = String.valueOf((char) 0x00C3);
    private static final String MOJIBAKE_ESCAPE = "\\" + "u00" + "c3";

    @Test
    void theExportedOpenApiDocumentHasNoDoubleEncodedCharacters() throws IOException {
        String document = Files.readString(OPENAPI, StandardCharsets.UTF_8);

        assertThat(document)
                .as("docs/openapi.json was written with the wrong encoding: export it again reading UTF-8")
                .doesNotContain(MOJIBAKE_ESCAPE)
                .doesNotContain(MOJIBAKE_CHARACTER);
    }

    @Test
    void theExportedOpenApiDocumentKeepsTheSpanishAlertTexts() throws IOException {
        String document = Files.readString(OPENAPI, StandardCharsets.UTF_8);
        String escapedAnomaly = "Anomal" + "\\" + "u00eda estructural cr" + "\\" + "u00edtica";

        assertThat(document).contains(escapedAnomaly);
    }
}
