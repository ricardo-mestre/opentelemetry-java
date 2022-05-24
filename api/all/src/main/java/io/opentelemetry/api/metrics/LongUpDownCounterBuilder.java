/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link LongUpDownCounter}. */
public interface LongUpDownCounterBuilder {
  /**
   * Sets the description for this instrument.
   *
   * @param description The description.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
   *     Description</a>
   */
  LongUpDownCounterBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * @param unit The unit. Instrument units must be 63 or fewer ASCII characters.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
   *     Unit</a>
   */
  LongUpDownCounterBuilder setUnit(String unit);

  /** Sets the counter for recording {@code double} values. */
  DoubleUpDownCounterBuilder ofDoubles();

  /**
   * Builds and returns an up down counter instrument with the configuration.
   *
   * @return The up down counter instrument.
   */
  LongUpDownCounter build();

  /**
   * Builds an asynchronous up down counter instrument with the given callback.
   *
   * <p>The callback will only be called when the instrument is being observed.
   *
   * <p>Callbacks are expected to abide by the following restrictions:
   *
   * <ul>
   *   <li>Run in a finite amount of time.
   *   <li>Safe to call repeatedly, across multiple threads.
   * </ul>
   *
   * @param callback A callback which observes measurements when invoked.
   */
  ObservableLongUpDownCounter buildWithCallback(Consumer<ObservableLongMeasurement> callback);
}
