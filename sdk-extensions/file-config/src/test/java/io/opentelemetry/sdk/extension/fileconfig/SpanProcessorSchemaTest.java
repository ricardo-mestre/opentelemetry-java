/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

class SpanProcessorSchemaTest {

  private static final YamlJsonSchemaValidator validator =
      new YamlJsonSchemaValidator(
          new File(System.getProperty("otel.sdk-schema-dir")),
          "https://opentelemetry.io/schemas/sdkconfig",
          new File(System.getProperty("otel.sdk-schema-dir") + "/span_processor.json"),
          "/span_processor");

  @Test
  void simpleAllFields() {
    assertThat(validator.validate("simple-all-fields.yaml")).isEmpty();
  }

  @Test
  void simpleInvalidTypes() {
    assertThat(validator.validate("simple-invalid-types.yaml"))
        .containsExactlyInAnyOrder("$.args.exporter: string found, object expected");
  }

  @Test
  void batchAllFields() {
    assertThat(validator.validate("batch-all-fields.yaml")).isEmpty();
  }

  @Test
  void batchInvalidTypes() {
    assertThat(validator.validate("batch-invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.args.exporter: string found, object expected",
            "$.args.max_queue_size: string found, integer expected",
            "$.args.scheduled_delay_millis: string found, integer expected",
            "$.args.export_timeout_millis: string found, integer expected",
            "$.args.max_export_batch_size: string found, integer expected");
  }
}
