/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.demo;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.util.Collection;

public class DelegatingSumData<T extends PointData> implements SumData<T> {

  private final SumData<T> delegate;

  public DelegatingSumData(SumData<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Collection<T> getPoints() {
    return delegate.getPoints();
  }

  @Override
  public boolean isMonotonic() {
    return delegate.isMonotonic();
  }

  @Override
  public AggregationTemporality getAggregationTemporality() {
    return delegate.getAggregationTemporality();
  }
}
