/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 10)
@Threads(1)
@Fork(1)
public class LogsBenchmarks {

  private static final List<String> LOGGER_NAMES =
      IntStream.range(0, 10000).mapToObj(i -> "dummy-logger-name-" + i).collect(toList());

  @State(Scope.Benchmark)
  public static class LogEmitterState {

    private SdkLogEmitterProvider logEmitterProvider;

    @Param({"10", "100", "1000", "10000"})
    int numLoggers;

    @Setup
    public void setup() {
      logEmitterProvider =
          SdkLogEmitterProvider.builder()
              .setResource(Resource.getDefault())
              .addLogProcessor(
                  SimpleLogProcessor.create(
                      new LogExporter() {
                        @Override
                        public CompletableResultCode export(Collection<LogData> logs) {
                          return CompletableResultCode.ofSuccess();
                        }

                        @Override
                        public CompletableResultCode shutdown() {
                          return CompletableResultCode.ofSuccess();
                        }
                      }))
              .build();
    }
  }

  @Benchmark
  public void logEmitterWithLookup(LogEmitterState state) {
    String loggerName = LOGGER_NAMES.get(ThreadLocalRandom.current().nextInt(state.numLoggers));
    state.logEmitterProvider.logEmitterWithLookup(loggerName).logBuilder().emit();
  }

  @Benchmark
  public void logEmitterNoLookup(LogEmitterState state) {
    String loggerName = LOGGER_NAMES.get(ThreadLocalRandom.current().nextInt(state.numLoggers));
    state.logEmitterProvider.logEmitterNoLookup(loggerName).logBuilder().emit();
  }

  @Benchmark
  public void singleLogEmitter(LogEmitterState state) {
    String loggerName = LOGGER_NAMES.get(ThreadLocalRandom.current().nextInt(state.numLoggers));
    state
        .logEmitterProvider
        .singleLogEmitter()
        .logBuilder(InstrumentationLibraryInfo.create(loggerName, null))
        .emit();
  }
}
