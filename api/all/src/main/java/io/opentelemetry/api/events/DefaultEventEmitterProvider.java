/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

class DefaultEventEmitterProvider implements EventEmitterProvider {

  private static final EventEmitterProvider INSTANCE = new DefaultEventEmitterProvider();
  private static final EventEmitterBuilder NOOP_BUILDER = new NoopEventEmitterBuilder();

  static EventEmitterProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public EventEmitterBuilder eventEmitterBuilder(String instrumentationScopeName, String domain) {
    return NOOP_BUILDER;
  }

  private static class NoopEventEmitterBuilder implements EventEmitterBuilder {

    @Override
    public EventEmitterBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public EventEmitterBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public EventEmitter build() {
      return DefaultEventEmitter.getInstance();
    }
  }
}
