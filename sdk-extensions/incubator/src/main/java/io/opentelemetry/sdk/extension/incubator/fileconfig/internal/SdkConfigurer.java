/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimitsBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class SdkConfigurer {

  private static SdkConfigurer INSTANCE = new SdkConfigurer();

  private SdkConfigurer() {}

  public static SdkConfigurer getInstance() {
    return INSTANCE;
  }

  public OpenTelemetrySdk configureSdk(Sdk sdk) {
    io.opentelemetry.sdk.resources.Resource resource = configureResource(sdk.getResource());

    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            configureTracerProvider(resource, sdk.getAttributeLimits(), sdk.getTracerProvider())
                .build())
        .build();
  }

  static io.opentelemetry.sdk.resources.Resource configureResource(Resource resource) {
    Attributes resourceAttributes =
        configureAttributes(resource.getAttributes().getAdditionalProperties());

    resourceAttributes =
        resourceAttributes.toBuilder()
            .put("service.name", resource.getAttributes().getServiceName())
            .build();

    return io.opentelemetry.sdk.resources.Resource.getDefault()
        .merge(io.opentelemetry.sdk.resources.Resource.create(resourceAttributes));
  }

  static Attributes configureAttributes(Map<String, Object> attributes) {
    AttributesBuilder builder = io.opentelemetry.api.common.Attributes.builder();
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String) {
        builder.put(key, (String) value);
      } else if (value instanceof Long) {
        builder.put(key, (Long) value);
      } else if (value instanceof Double) {
        builder.put(key, (Double) value);
      } else if (value instanceof Boolean) {
        builder.put(key, (Boolean) value);
      } else {
        throw new ConfigurationException(
            "Unrecognized attribute value type " + value.getClass().toString());
      }
    }
    return builder.build();
  }

  static SdkTracerProviderBuilder configureTracerProvider(
      io.opentelemetry.sdk.resources.Resource resource,
      Limits limits,
      TracerProvider tracerProvider) {
    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder()
            .setResource(resource)
            .setSpanLimits(configureSpanLimits(limits, tracerProvider.getSpanLimits()));

    Map<String, SpanExporter> spanExporters =
        tracerProvider.getExporters().getAdditionalProperties().entrySet().stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> configureSpanExporter(entry.getKey(), entry.getValue())));

    tracerProvider.getSpanProcessors().stream()
        .map(processor -> configureSpanProcessor(spanExporters, processor))
        .forEach(builder::addSpanProcessor);

    return builder;
  }

  static io.opentelemetry.sdk.trace.SpanLimits configureSpanLimits(
      Limits limits, SpanLimits spanLimits) {
    SpanLimitsBuilder builder = io.opentelemetry.sdk.trace.SpanLimits.builder();

    Integer maxAttributeValueLength =
        defaultIfNull(
            spanLimits.getAttributeValueLengthLimit(), limits.getAttributeValueLengthLimit());
    if (maxAttributeValueLength != null) {
      builder.setMaxAttributeValueLength(maxAttributeValueLength);
    }

    Integer maxNumberOfAttributes =
        defaultIfNull(spanLimits.getAttributeCountLimit(), limits.getAttributeCountLimit());
    if (maxNumberOfAttributes != null) {
      builder.setMaxNumberOfAttributes(maxNumberOfAttributes);
    }

    if (spanLimits.getEventCountLimit() != null) {
      builder.setMaxNumberOfEvents(spanLimits.getEventCountLimit());
    }

    if (spanLimits.getLinkCountLimit() != null) {
      builder.setMaxNumberOfLinks(spanLimits.getLinkCountLimit());
    }

    if (spanLimits.getEventAttributeCountLimit() != null) {
      builder.setMaxNumberOfAttributesPerEvent(spanLimits.getEventAttributeCountLimit());
    }

    if (spanLimits.getLinkAttributeCountLimit() != null) {
      builder.setMaxNumberOfAttributesPerLink(spanLimits.getLinkAttributeCountLimit());
    }

    return builder.build();
  }

  @Nullable
  static <T> T defaultIfNull(@Nullable T first, @Nullable T second) {
    return first != null ? first : second;
  }

  static SpanExporter configureSpanExporter(String name, Object object) {
    if (object instanceof Otlp) {
      return configureOtlpSpanExporter((Otlp) object);
    } else if (object instanceof Zipkin) {
      return configureZipkinSpanExporter((Zipkin) object);
    } else if (object instanceof Jaeger) {
      return configureJaegerSpanExporter((Jaeger) object);
    }
    throw new ConfigurationException("Unrecognized span exporter [" + name + "]:" + object);
  }

  static SpanExporter configureOtlpSpanExporter(Otlp otlp) {
    return dummySpanExporter("otlp");
  }

  static SpanExporter configureZipkinSpanExporter(Zipkin zipkin) {
    return dummySpanExporter("zipkin");
  }

  static SpanExporter configureJaegerSpanExporter(Jaeger jaeger) {
    return dummySpanExporter("jaeger");
  }

  static SpanExporter dummySpanExporter(String name) {
    return new SpanExporter() {
      @Override
      public CompletableResultCode export(Collection<SpanData> spans) {
        return CompletableResultCode.ofSuccess();
      }

      @Override
      public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
      }

      @Override
      public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  static SpanProcessor configureSpanProcessor(
      Map<String, SpanExporter> spanExporters, Processor processor) {
    if (!processor.getName().equals("batch")) {
      throw new ConfigurationException(
          "Unrecognized span processor [" + processor.getName() + "]: " + processor);
    }
    ProcessorArgs args = processor.getArgs();
    SpanExporter spanExporter = spanExporters.get(args.getExporter());
    if (spanExporter == null) {
      throw new ConfigurationException(
          "Span processor configured with undefined exporter [" + args.getExporter() + "]");
    }

    BatchSpanProcessorBuilder builder = BatchSpanProcessor.builder(spanExporter);

    if (args.getScheduleDelay() != null) {
      builder.setScheduleDelay(args.getScheduleDelay(), TimeUnit.MILLISECONDS);
    }

    if (args.getExportTimeout() != null) {
      builder.setExporterTimeout(args.getExportTimeout(), TimeUnit.MILLISECONDS);
    }

    if (args.getMaxQueueSize() != null) {
      builder.setMaxQueueSize(args.getMaxQueueSize());
    }

    if (args.getMaxExportBatchSize() != null) {
      builder.setMaxExportBatchSize(args.getMaxExportBatchSize());
    }

    return builder.build();
  }
}
