/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.assertj.MetricAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

class PushVsPullTest {

  @Test
  void pushBased() {
    InMemoryMetricExporter exporter = InMemoryMetricExporter.create(AggregationTemporality.DELTA);
    SdkMeterProvider provider =
        SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(exporter).newMetricReaderFactory())
            .build();

    provider.get("meter").counterBuilder("foo").build().add(10);

    // Verify that after a flush we have data
    provider.forceFlush();
    assertThat(exporter.getFinishedMetricItems())
        .satisfiesExactly(
            metricData ->
                MetricAssertions.assertThat(metricData)
                    .hasName("foo")
                    .hasLongSum()
                    .points()
                    .hasSize(1));

    // Need to reset first
    exporter.reset();

    // Subsequent call for metrics should not have data because no new recordings
    provider.forceFlush();
    assertThat(exporter.getFinishedMetricItems()).hasSize(0);
  }

  @Test
  void pullBased() {
    InMemoryMetricReader reader = InMemoryMetricReader.createDelta();
    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    provider.get("meter").counterBuilder("foo").build().add(10);

    // Verify that we have data
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metricData ->
                MetricAssertions.assertThat(metricData)
                    .hasName("foo")
                    .hasLongSum()
                    .points()
                    .hasSize(1));

    // Subsequent call to collect metrics should not have data because no new recordings have
    // occurred
    assertThat(reader.collectAllMetrics()).hasSize(0);
  }
}
