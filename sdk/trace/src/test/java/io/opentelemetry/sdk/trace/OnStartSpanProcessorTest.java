/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.context.Context;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OnStartSpanProcessorTest {

  @Test
  void startOnly() {
    AtomicReference<Context> seenContext = new AtomicReference<>();
    AtomicReference<ReadWriteSpan> seenSpan = new AtomicReference<>();
    Context context = mock(Context.class);
    ReadWriteSpan inputSpan = mock(ReadWriteSpan.class);

    SpanProcessor processor =
        OnStartSpanProcessor.of(
            (ctx, span) -> {
              seenContext.set(ctx);
              seenSpan.set(span);
            });

    assertThat(processor.isStartRequired()).isTrue();
    assertThat(processor.isEndRequired()).isFalse();
    processor.onStart(context, inputSpan);
    assertThat(seenContext.get()).isSameAs(context);
    assertThat(seenSpan.get()).isSameAs(inputSpan);
  }
}
