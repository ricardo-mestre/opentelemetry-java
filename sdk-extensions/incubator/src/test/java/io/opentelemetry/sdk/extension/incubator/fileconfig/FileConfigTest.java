/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class FileConfigTest {

  @Test
  void foo() throws FileNotFoundException {
    Yaml yaml = new Yaml();
    Object load = yaml.load(new FileInputStream(
        "/Users/jberg/code/open-telemetry/opentelemetry-java/sdk-extensions/incubator/file-config-natural.yaml"));
    System.out.println(load);
  }

  @Test
  void validateSchema_Simple() throws IOException {
    FileInputStream schemaFile = new FileInputStream("/Users/jberg/code/open-telemetry/opentelemetry-java/sdk-extensions/incubator/simple-schema.json");
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    JsonSchemaFactory factory = JsonSchemaFactory.builder(
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
    )
        .objectMapper(mapper)
        .build();
    JsonSchema schema = factory.getSchema(schemaFile);

    for (File file : new File("/Users/jberg/code/open-telemetry/opentelemetry-java/sdk-extensions/incubator/simpe-schemas").listFiles()) {
      System.out.println("Validating schema of: " + file.getAbsolutePath());
      Set<ValidationMessage> validate = schema.validate(mapper.readTree(new FileInputStream(file)));
      if (validate.isEmpty()) {
        System.out.println("Valid.");
      } else {
        validate.forEach(System.out::println);
      }
    }
  }

  @Test
  void validateSchema_Otel() throws IOException {
    FileInputStream schemaFile = new FileInputStream("/Users/jberg/code/open-telemetry/opentelemetry-java/sdk-extensions/incubator/otel-file-schema.json");
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    JsonSchemaFactory factory = JsonSchemaFactory.builder(
            // V202012 has a bug where items is not validated
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
        )
        .objectMapper(mapper)
        .build();
    JsonSchema schema = factory.getSchema(schemaFile);
    Yaml yaml = new Yaml();

    for (File file : new File("/Users/jberg/code/open-telemetry/opentelemetry-java/sdk-extensions/incubator/otel-config-schemas").listFiles()) {
      Object yamlObj = yaml.load(new FileInputStream(file));
      String yamlStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(yamlObj);

      System.out.println("Validating schema of: " + file.getAbsolutePath());
      Set<ValidationMessage> validate = schema.validate(mapper.readTree(yamlStr));
      if (validate.isEmpty()) {
        System.out.println("Valid.");
      } else {
        validate.forEach(System.out::println);
      }
    }
  }

  @Test
  void fileConfig() {
    Resource resource =
        Resource.builder()
            .put("key1", "value")
            .put("key2", 5)
            .setSchemaUrl("http://schema.com")
            .build();

    OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .addSpanProcessor(
                    BatchSpanProcessor.builder(
                            OtlpGrpcSpanExporter.builder()
                                .setEndpoint("grpc")
                                .setEndpoint("http://localhost:4317")
                                .addHeader("api-key", System.getenv("API_KEY"))
                                .setCompression("gzip")
                                .setTimeout(Duration.ofMillis(30_000))
                                .build())
                        .setMaxQueueSize(100)
                        .setScheduleDelay(Duration.ofMillis(1_000))
                        .setExporterTimeout(Duration.ofMillis(30_000))
                        .setMaxExportBatchSize(200)
                        .build())
                .setSpanLimits(
                    SpanLimits.builder()
                        .setMaxNumberOfAttributes(10)
                        .setMaxAttributeValueLength(100)
                        .setMaxNumberOfAttributesPerEvent(5)
                        .setMaxNumberOfAttributesPerLink(5)
                        .setMaxNumberOfEvents(10)
                        .setMaxNumberOfLinks(4)
                        .build())
                .build())
        .setMeterProvider(SdkMeterProvider.builder().build())
        .setLogEmitterProvider(SdkLogEmitterProvider.builder().build())
        .setPropagators(
            ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CBaggagePropagator.getInstance(),
                    W3CTraceContextPropagator.getInstance(),
                    new FooPropagator("bar", "qux"))));
  }

  private static final class FooPropagator implements TextMapPropagator {

    private final String foo;
    private final String baz;

    private FooPropagator(String foo, String baz) {
      this.foo = foo;
      this.baz = baz;
    }

    @Override
    public Collection<String> fields() {
      return Arrays.asList(foo, baz);
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
      // Do nothing
    }

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
      return context;
    }
  }
}
