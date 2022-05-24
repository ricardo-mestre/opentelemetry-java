/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

public interface EventEmitterProvider {

  default EventEmitter get(String instrumentationScopeName, String domain) {
    return eventEmitterBuilder(instrumentationScopeName, domain).build();
  }

  EventEmitterBuilder eventEmitterBuilder(String instrumentationScopeName, String domain);

  static EventEmitterProvider noop() {
    return DefaultEventEmitterProvider.getInstance();
  }
}
