/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link EventEmitter} is the entry point into an event pipeline.
 *
 * <p>Example usage emitting events:
 *
 * <p>
 *
 * <pre>{@code
 * class MyClass {
 *   private final EventEmitter eventEmitter = openTelemetryEventEmitterProvider.eventEmitterBuilder("scope-name")
 *         .build();
 *
 *   void doWork() {
 *     eventEmitter.builder("namespace.my-event")
 *       .put("key1", "value1")
 *       .put("key2", "value2")
 *       .emit();
 *     // do work
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface EventEmitter {

  /**
   * Return a {@link EventBuilder} to emit an event.
   *
   * @param eventName the event name, which defines the class or type of event. Events names SHOULD
   *     include a namespace to avoid collisions with other event names.
   */
  EventBuilder builder(String eventName);
}
