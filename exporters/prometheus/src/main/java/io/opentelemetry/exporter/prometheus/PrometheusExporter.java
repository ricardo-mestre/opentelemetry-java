package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.prometheus.client.Collector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PrometheusExporter extends Collector implements MetricExporter {

  private final Object lock = new Object();

  Collection<MetricData> metricData = Collections.emptyList();

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    synchronized (lock) {
      this.metricData = metrics;
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    synchronized (lock) {
      Collection<MetricData> allMetrics = metricData;
      List<MetricFamilySamples> allSamples = new ArrayList<>(allMetrics.size());
      for (MetricData metricData : allMetrics) {
        allSamples.add(MetricAdapter.toMetricFamilySamples(metricData));
      }
      return allSamples;
    }
  }
}
