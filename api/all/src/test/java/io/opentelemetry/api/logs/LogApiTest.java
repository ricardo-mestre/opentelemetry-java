/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventEmitter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LogApiTest {

  @Test
  void demo() {
    OpenTelemetry openTelemetry = OpenTelemetry.noop();

    // Obtain an event emitter, with events scoped to the domain "otel.jfr"
    // Event names are exepcted to be unique with a domain
    EventEmitter eventEmitter = openTelemetry.getEventEmitter("my-event-emitter-scope", "otel.jfr");

    // Obtain a logger with additional scope details
    eventEmitter =
        openTelemetry
            .eventEmitterBuilder("my-event-emitter-scope", "otel.jfr")
            .setInstrumentationVersion("1.0.0")
            .build();

    // Emit a simple event named my-event
    eventEmitter.eventBuilder("my-event").emit();

    // Emit an event with attributes
    eventEmitter
        .eventBuilder("jdk.execution_sample")
        .setAttributes(
            Attributes.builder()
                .put("thread.name", "sampled-thread-1")
                .put("thread.state", Thread.currentThread().getState().toString())
                .put(AttributeKey.stringArrayKey("stack.trace"), stackTrace())
                .build())
        .emit();

    // Obtain a logger and emit a low level log record
    // NOTE: the API for emitting log records is only intended to be used by log appender adapters
    // to adapt logs from existing log frameworks (Log4j, Logback) to OpenTelemetry
    Logger logger = openTelemetry.getLoggerProvider().get("my-logger");
    logger
        .logRecordBuilder()
        .setSeverity(Severity.DEBUG)
        .setBody("My application log message")
        .emit();
  }

  private static List<String> stackTrace() {
    return Stream.of(Thread.currentThread().getStackTrace())
        .map(
            stackTraceElement ->
                stackTraceElement.getClassName()
                    + "."
                    + stackTraceElement.getMethodName()
                    + " "
                    + stackTraceElement.getLineNumber())
        .collect(Collectors.toList());
  }
}
