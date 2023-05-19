/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.demo;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import java.util.List;

public class DelegateLongPointData implements LongPointData {

  private final LongPointData delegate;

  public DelegateLongPointData(LongPointData delegate) {
    this.delegate = delegate;
  }

  @Override
  public long getValue() {
    return delegate.getValue();
  }

  @Override
  public long getStartEpochNanos() {
    return delegate.getStartEpochNanos();
  }

  @Override
  public long getEpochNanos() {
    return delegate.getEpochNanos();
  }

  @Override
  public Attributes getAttributes() {
    return delegate.getAttributes();
  }

  @Override
  public List<LongExemplarData> getExemplars() {
    return delegate.getExemplars();
  }
}
