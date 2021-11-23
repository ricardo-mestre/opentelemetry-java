/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static io.opentelemetry.sdk.internal.ThrowableUtil.propagateIfFatal;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class ThrowableUtilTest {

  @Test
  void propagateIfFatal_Throws() {
    assertThatCode(() -> propagateIfFatal(new RuntimeException("Error!")))
        .doesNotThrowAnyException();
    assertThatCode(() -> propagateIfFatal(new Exception("Error!"))).doesNotThrowAnyException();
    assertThatCode(() -> propagateIfFatal(new VirtualMachineError("Error!") {}))
        .isInstanceOf(VirtualMachineError.class);
    assertThatCode(() -> propagateIfFatal(new ThreadDeath())).isInstanceOf(ThreadDeath.class);
    assertThatCode(() -> propagateIfFatal(new LinkageError())).isInstanceOf(LinkageError.class);
  }
}
