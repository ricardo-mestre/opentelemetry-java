/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.Closeable;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcLogExporterTest
    extends AbstractGrpcTelemetryExporterTest<LogRecordData, ResourceLogs> {

  OtlpGrpcLogExporterTest() {
    super("log", ResourceLogs.getDefaultInstance());
  }

  @Test
  void testSetRetryPolicyOnDelegate() {
    assertThatCode(
            () ->
                RetryUtil.setRetryPolicyOnDelegate(
                    OtlpGrpcLogExporter.builder(), RetryPolicy.getDefault()))
        .doesNotThrowAnyException();
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcLogExporter.builder().build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(OkHttpGrpcExporter.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<LogRecordData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcLogExporter.builder());
  }

  @Override
  protected LogRecordData generateFakeTelemetry() {
    return TestLogRecordData.builder()
        .setResource(Resource.create(Attributes.builder().put("testKey", "testValue").build()))
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.builder("instrumentation").setVersion("1").build())
        .setEpoch(Instant.now())
        .setSeverity(Severity.ERROR)
        .setSeverityText("really severe")
        .setBody("message")
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  @Override
  protected Marshaler[] toMarshalers(List<LogRecordData> telemetry) {
    return ResourceLogsMarshaler.create(telemetry);
  }
}
