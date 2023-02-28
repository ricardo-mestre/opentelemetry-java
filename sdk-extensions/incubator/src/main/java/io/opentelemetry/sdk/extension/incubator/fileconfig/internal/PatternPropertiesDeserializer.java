/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

@SuppressWarnings("serial")
public class PatternPropertiesDeserializer extends StdDeserializer<Object> {

  public PatternPropertiesDeserializer() {
    super((Class<?>) null);
  }

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String currentFieldName = p.getCurrentName();
    if (currentFieldName.startsWith("otlp")) {
      return p.getCodec().readValue(p, Otlp.class);
    } else if (currentFieldName.startsWith("zipkin")) {
      return p.getCodec().readValue(p, Zipkin.class);
    } else if (currentFieldName.startsWith("jaeger")) {
      return p.getCodec().readValue(p, Jaeger.class);
    }
    return p.getCodec().readValue(p, Object.class);
  }
}
