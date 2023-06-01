/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.sdk.common.config.ConfigProperties;
import io.opentelemetry.sdk.internal.DefaultConfigProperties;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class ConfigPropertiesBridge
    implements io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties {

  private static final ConfigPropertiesBridge EMPTY =
      new ConfigPropertiesBridge(DefaultConfigProperties.create(Collections.emptyMap()));

  private final ConfigProperties delegate;

  private ConfigPropertiesBridge(ConfigProperties delegate) {
    this.delegate = delegate;
  }

  public static ConfigPropertiesBridge create(ConfigProperties delegate) {
    return new ConfigPropertiesBridge(delegate);
  }

  public static ConfigPropertiesBridge createForTest(Map<String, String> properties) {
    return create(DefaultConfigProperties.createForTest(properties));
  }

  public static ConfigPropertiesBridge getEmptyInstance() {
    return EMPTY;
  }

  @Nullable
  @Override
  public String getString(String name) {
    return delegate.getString(name);
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    return delegate.getBoolean(name);
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    return delegate.getInt(name);
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    return delegate.getLong(name);
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    return delegate.getDouble(name);
  }

  @Nullable
  @Override
  public Duration getDuration(String name) {
    return delegate.getDuration(name);
  }

  @Override
  public List<String> getList(String name) {
    return delegate.getList(name);
  }

  @Override
  public Map<String, String> getMap(String name) {
    return delegate.getMap(name);
  }
}
