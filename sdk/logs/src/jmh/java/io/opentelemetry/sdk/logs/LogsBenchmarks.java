/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class LogsBenchmarks {

  private static SdkLogEmitterProvider logEmitterProvider;
  private static List<String> loggerNames;

  @Setup(Level.Trial)
  public void setUp() {
    logEmitterProvider = SdkLogEmitterProvider.builder()
        .setResource(Resource.getDefault())
        .addLogProcessor(SimpleLogProcessor.create(new LogExporter() {
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
    loggerNames = IntStream.range(0, 100).mapToObj(i -> "dummy-logger-name-" + i).collect(toList());
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    logEmitterProvider.shutdown().join(10, TimeUnit.SECONDS);
  }

  @Benchmark
  @Threads(1)
  public void oneThread() {
    emitLog();
  }

  @Benchmark
  @Threads(8)
  public void eightThreads() {
    emitLog();
  }

  private static void emitLog() {
    String loggerName = loggerNames.get(ThreadLocalRandom.current().nextInt(loggerNames.size()));
    logEmitterProvider.logEmitterBuilder(loggerName).build()
        .logBuilder()
        .setBody("message")
        .setEpoch(Instant.now())
        .setSeverity(Severity.INFO)
        .emit();
  }

}
