/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

public interface EventEmitter {

  EventBuilder eventBuilder(String name);
}
