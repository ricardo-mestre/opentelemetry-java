/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfoBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;

final class SdkLoggerBuilder implements LoggerBuilder {

  private final ComponentRegistry<SdkLogger> registry;
  private final InstrumentationScopeInfoBuilder scopeBuilder;

  SdkLoggerBuilder(ComponentRegistry<SdkLogger> registry, String instrumentationScopeName) {
    this.registry = registry;
    this.scopeBuilder = InstrumentationScopeInfo.builder(instrumentationScopeName);
  }

  @Override
  public SdkLoggerBuilder setSchemaUrl(String schemaUrl) {
    scopeBuilder.setSchemaUrl(schemaUrl);
    return this;
  }

  @Override
  public SdkLoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    scopeBuilder.setVersion(instrumentationScopeVersion);
    return this;
  }

  @Override
  public SdkLogger build() {
    return registry.get(scopeBuilder.build());
  }
}
