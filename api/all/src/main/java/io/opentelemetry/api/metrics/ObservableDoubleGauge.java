/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/**
 * A reference to an observable metric registered with {@link
 * DoubleGaugeBuilder#buildWithCallback(Consumer)}.
 */
public interface ObservableDoubleGauge extends AutoCloseable {
  /**
   * Remove the callback associated with this instrument. After this is called, callbacks won't be
   * invoked on future collections. Subsequent calls to {@link #close()} will have no effect.
   *
   * @see DoubleGaugeBuilder#buildWithCallback(Consumer)
   */
  @Override
  default void close() {}
}
