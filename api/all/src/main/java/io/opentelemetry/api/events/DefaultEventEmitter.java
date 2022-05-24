/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultEventEmitter implements EventEmitter {

  private static final EventEmitter INSTANCE = new DefaultEventEmitter();

  private static final EventBuilder NOOP_EVENT_BUILDER = new NoopEventBuilder();

  static EventEmitter getInstance() {
    return INSTANCE;
  }

  @Override
  public EventBuilder eventBuilder(String name) {
    return NOOP_EVENT_BUILDER;
  }

  private static final class NoopEventBuilder implements EventBuilder {

    @Override
    public EventBuilder setEpoch(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public EventBuilder setEpoch(Instant instant) {
      return this;
    }

    @Override
    public EventBuilder setContext(Context context) {
      return this;
    }

    @Override
    public EventBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public EventBuilder setSeverityText(String severityText) {
      return this;
    }

    @Override
    public EventBuilder setBody(String body) {
      return this;
    }

    @Override
    public EventBuilder setAttributes(Attributes attributes) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
