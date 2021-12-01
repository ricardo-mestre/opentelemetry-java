/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import javax.annotation.Nullable;

/** SDK implementation of {@link LogEmitter}. */
final class SdkLogEmitter implements LogEmitter {

  private final LogEmitterSharedState logEmitterSharedState;
  @Nullable private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  SdkLogEmitter(
      LogEmitterSharedState logEmitterSharedState,
      @Nullable InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public LogBuilder logBuilder() {
    if (instrumentationLibraryInfo == null) {
      throw new IllegalArgumentException(
          "Cannot call logBuilder() when instrumentationLibraryInfo is not set");
    }
    LogDataBuilder logDataBuilder =
        LogDataBuilder.create(
            logEmitterSharedState.getResource(),
            instrumentationLibraryInfo,
            logEmitterSharedState.getClock());
    return new SdkLogBuilder(logEmitterSharedState, logDataBuilder);
  }

  @Override
  public LogBuilder logBuilder(InstrumentationLibraryInfo instrumentationLibraryInfo) {
    LogDataBuilder logDataBuilder =
        LogDataBuilder.create(
            logEmitterSharedState.getResource(),
            instrumentationLibraryInfo,
            logEmitterSharedState.getClock());
    return new SdkLogBuilder(logEmitterSharedState, logDataBuilder);
  }

  // VisibleForTesting
  @Nullable
  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }
}
