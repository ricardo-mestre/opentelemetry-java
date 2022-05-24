/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

public interface EventEmitterBuilder {

  EventEmitterBuilder setSchemaUrl(String schemaUrl);

  EventEmitterBuilder setInstrumentationVersion(String instrumentationVersion);

  EventEmitter build();
}
