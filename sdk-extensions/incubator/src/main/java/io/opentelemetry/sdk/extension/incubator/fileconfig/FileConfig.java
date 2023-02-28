/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.OpenTelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.SdkConfigurer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.yaml.snakeyaml.Yaml;

public class FileConfig {

  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
  private static final Yaml YAML = new Yaml();
  private static final JsonSchema jsonSchema = initializeJsonSchema();

  private final OpenTelemetryConfiguration openTelemetryConfiguration;

  private FileConfig(OpenTelemetryConfiguration openTelemetryConfiguration) {
    this.openTelemetryConfiguration = openTelemetryConfiguration;
  }

  public static FileConfig create(InputStream inputStream) {
    return new FileConfig(validateAndParse(inputStream));
  }

  OpenTelemetryConfiguration getOpenTelemetryConfiguration() {
    return openTelemetryConfiguration;
  }

  private static OpenTelemetryConfiguration validateAndParse(InputStream inputStream) {
    String content;
    try {
      content = readInputStream(inputStream);
    } catch (RuntimeException e) {
      throw new ConfigurationException("Error reading file config input stream", e);
    }

    Set<String> validationErrors = validate(content);
    if (!validationErrors.isEmpty()) {
      throw new ConfigurationException(
          "Validate errors detected: \n"
              + validationErrors.stream().map(s -> "\t" + s).collect(joining("\n")));
    }

    return parse(content);
  }

  private static JsonSchema initializeJsonSchema() {
    Map<String, String> uriMapping =
        Stream.of("jaeger", "otlp", "zipkin")
            .collect(
                Collectors.toMap(
                    s -> "https://opentelemetry.io/otelconfig/" + s + ".json",
                    s -> "classpath:/fileconfig/" + s + ".json"));

    JsonSchemaFactory jsonSchemaFactory =
        JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012))
            .addMetaSchema(JsonMetaSchema.getV6())
            .objectMapper(MAPPER)
            .addUriMappings(uriMapping)
            .build();
    return jsonSchemaFactory.getSchema(loadResource("/fileconfig/schema.json"));
  }

  private static InputStream loadResource(String path) {
    return FileConfig.class.getResourceAsStream(path);
  }

  private static String readInputStream(InputStream inputStream) {
    // Load yaml and write it as string to resolve anchors
    try {
      Object yamlObj = YAML.load(inputStream);
      return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(yamlObj);
    } catch (JsonProcessingException e) {
      throw new ConfigurationException("Unable to read file config", e);
    }
  }

  private static Set<String> validate(String yaml) {
    try {
      return jsonSchema.validate(MAPPER.readTree(yaml)).stream()
          .map(ValidationMessage::toString)
          .collect(toSet());
    } catch (IOException e) {
      throw new ConfigurationException("Unable to validate file config", e);
    }
  }

  private static OpenTelemetryConfiguration parse(String yaml) {
    try {
      return MAPPER.readValue(yaml, new TypeReference<OpenTelemetryConfiguration>() {});
    } catch (IOException e) {
      throw new ConfigurationException("Unable to parse file config", e);
    }
  }

  public OpenTelemetrySdk sdk() {
    return SdkConfigurer.getInstance().configureSdk(openTelemetryConfiguration.getSdk());
  }
}
