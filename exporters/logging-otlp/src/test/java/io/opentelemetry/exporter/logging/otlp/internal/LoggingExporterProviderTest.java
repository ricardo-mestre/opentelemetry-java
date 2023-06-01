/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConfigPropertiesBridge;
import org.junit.jupiter.api.Test;

class LoggingExporterProviderTest {

  @Test
  void logRecordExporterProvider() {
    LoggingLogRecordExporterProvider provider = new LoggingLogRecordExporterProvider();
    assertThat(provider.getName()).isEqualTo("logging-otlp");
    assertThat(provider.createExporter(ConfigPropertiesBridge.getEmptyInstance()))
        .isInstanceOf(OtlpJsonLoggingLogRecordExporter.class);
  }

  @Test
  void metricExporterProvider() {
    LoggingMetricExporterProvider provider = new LoggingMetricExporterProvider();
    assertThat(provider.getName()).isEqualTo("logging-otlp");
    assertThat(provider.createExporter(ConfigPropertiesBridge.getEmptyInstance()))
        .isInstanceOf(OtlpJsonLoggingMetricExporter.class);
  }

  @Test
  void spanExporterProvider() {
    LoggingSpanExporterProvider provider = new LoggingSpanExporterProvider();
    assertThat(provider.getName()).isEqualTo("logging-otlp");
    assertThat(provider.createExporter(ConfigPropertiesBridge.getEmptyInstance()))
        .isInstanceOf(OtlpJsonLoggingSpanExporter.class);
  }
}
