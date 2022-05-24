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

public interface EventBuilder {

  EventBuilder setEpoch(long timestamp, TimeUnit unit);

  EventBuilder setEpoch(Instant instant);

  EventBuilder setContext(Context context);

  EventBuilder setSeverity(Severity severity);

  EventBuilder setSeverityText(String severityText);

  EventBuilder setBody(String body);

  EventBuilder setAttributes(Attributes attributes);

  void emit();
}
