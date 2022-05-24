/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

public interface LoggerBuilder {

  LoggerBuilder setSchemaUrl(String schemaUrl);

  LoggerBuilder setInstrumentationVersion(String instrumentationVersion);

  Logger build();
}
