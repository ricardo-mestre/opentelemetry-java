/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.demo;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class AttributeEnrichingMetricExporterTest {

  @Test
  void demo() {
    InMemoryMetricExporter inMemoryMetricExporter = InMemoryMetricExporter.create();

    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.builder(
                        new AttributeEnrichingMetricExporter(
                            inMemoryMetricExporter, Attributes.builder().put("foo", "bar").build()))
                    .setInterval(Duration.ofSeconds(1000))
                    .build())
            .build();

    LongCounter counter = meterProvider.get("meter").counterBuilder("counter").build();

    counter.add(10);

    meterProvider.forceFlush();

    assertThat(inMemoryMetricExporter.getFinishedMetricItems())
        .hasSize(1)
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        longSum ->
                            longSum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(10)
                                        .hasAttributes(
                                            Attributes.builder().put("foo", "bar").build()))));
  }
}
