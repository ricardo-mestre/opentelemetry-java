/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultLoggerTest {

  @Test
  void buildAndEmit() {
    assertThatCode(
            () ->
                DefaultLogger.getInstance(false)
                    .logRecordBuilder()
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> DefaultLogger.getInstance(false).eventBuilder("event-name"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Cannot emit event from Logger without event domain. Please use LoggerBuilder#setEventDomain(String) when obtaining Logger.");
    assertThatCode(
            () ->
                DefaultLogger.getInstance(true)
                    .logRecordBuilder()
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                DefaultLogger.getInstance(true)
                    .eventBuilder("event-name")
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
  }
}
