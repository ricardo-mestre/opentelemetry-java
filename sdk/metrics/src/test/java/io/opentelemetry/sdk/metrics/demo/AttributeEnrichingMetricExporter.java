/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.demo;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.List;

public class AttributeEnrichingMetricExporter implements MetricExporter {

  private final MetricExporter delegate;
  private final Attributes attributes;

  public AttributeEnrichingMetricExporter(MetricExporter delegate, Attributes attributes) {
    this.delegate = delegate;
    this.attributes = attributes;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return delegate.getAggregationTemporality(instrumentType);
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    return delegate.export(
        metrics.stream()
            .map(
                metricData -> {
                  Data<?> data;
                  switch (metricData.getType()) {
                    case LONG_SUM:
                      List<LongPointDataWithAttributes> collect =
                          metricData.getLongSumData().getPoints().stream()
                              .map(
                                  point ->
                                      new LongPointDataWithAttributes(
                                          point,
                                          point.getAttributes().toBuilder()
                                              .putAll(attributes)
                                              .build()))
                              .collect(toList());
                      data = new SumDataWithPoints<>(metricData.getLongSumData(), collect);
                      break;
                      // TODO: enrich other metric types with attributes
                    default:
                      throw new IllegalStateException("Unsupported metric type");
                  }
                  return new MetricDataWithData(metricData, data);
                })
            .collect(toList()));
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  private static class MetricDataWithData extends DelegatingMetricData {

    private final Data<?> data;

    public MetricDataWithData(MetricData delegate, Data<?> data) {
      super(delegate);
      this.data = data;
    }

    @Override
    public Data<?> getData() {
      return data;
    }
  }

  private static class SumDataWithPoints<T extends PointData> extends DelegatingSumData<T> {

    private final Collection<? extends T> points;

    public SumDataWithPoints(SumData<T> delegate, Collection<? extends T> points) {
      super(delegate);
      this.points = points;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<T> getPoints() {
      return (Collection<T>) points;
    }
  }

  private static class LongPointDataWithAttributes extends DelegateLongPointData {

    private final Attributes attributes;

    public LongPointDataWithAttributes(LongPointData delegate, Attributes attributes) {
      super(delegate);
      this.attributes = attributes;
    }

    @Override
    public Attributes getAttributes() {
      return attributes;
    }
  }
}
