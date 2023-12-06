/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The EventBuilder is used to {@link #emit()} events. */
public interface EventBuilder {

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, String value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, long value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, double value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, boolean value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, String... value) {
    return put(key, AnyValue.of(Arrays.stream(value).map(AnyValue::of).collect(toList())));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, long... value) {
    return put(key, AnyValue.of(Arrays.stream(value).mapToObj(AnyValue::of).collect(toList())));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, double... value) {
    return put(key, AnyValue.of(Arrays.stream(value).mapToObj(AnyValue::of).collect(toList())));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, boolean... value) {
    List<AnyValue<?>> values = new ArrayList<>(value.length);
    for (boolean val : value) {
      values.add(AnyValue.of(val));
    }
    return put(key, AnyValue.of(values));
  }

  /** Put the given {@code attributeKey} and {@code value} in the payload. */
  @SuppressWarnings("unchecked")
  default <T> EventBuilder put(AttributeKey<T> attributeKey, T value) {
    if (attributeKey == null || attributeKey.getKey().isEmpty() || value == null) {
      return this;
    }
    String key = attributeKey.getKey();
    switch (attributeKey.getType()) {
      case STRING:
        return put(key, (String) value);
      case BOOLEAN:
        return put(key, (boolean) value);
      case LONG:
        return put(key, (long) value);
      case DOUBLE:
        return put(key, (double) value);
      case STRING_ARRAY:
        return put(
            key, AnyValue.of(((List<String>) value).stream().map(AnyValue::of).collect(toList())));
      case BOOLEAN_ARRAY:
        return put(
            key, AnyValue.of(((List<Boolean>) value).stream().map(AnyValue::of).collect(toList())));
      case LONG_ARRAY:
        return put(
            key, AnyValue.of(((List<Long>) value).stream().map(AnyValue::of).collect(toList())));
      case DOUBLE_ARRAY:
        return put(
            key, AnyValue.of(((List<Double>) value).stream().map(AnyValue::of).collect(toList())));
    }
    throw new IllegalStateException("Unknown attribute type: " + attributeKey.getType());
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  EventBuilder put(String key, AnyValue<?> value);

  /** Put all the {@code attributes} in the payload. */
  @SuppressWarnings("unchecked")
  default EventBuilder putAll(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach((attributeKey, value) -> put((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp}t, using the instant.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(Instant instant);

  /** Set the context. */
  EventBuilder setContext(Context context);

  /** Set the severity. */
  EventBuilder setSeverity(Severity severity);

  /**
   * Set the attributes.
   *
   * <p>Event {@link io.opentelemetry.api.common.Attributes} provide additional details about the
   * Event which are not part of the well-defined {@link AnyValue} {@code payload}.
   */
  EventBuilder setAttributes(Attributes attributes);

  /** Emit an event. */
  void emit();
}
