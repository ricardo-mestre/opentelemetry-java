/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class OkHttpSender implements HttpSender {

  private final OkHttpClient client;
  private final HttpUrl url;
  private final boolean compressionEnabled;
  private final Supplier<Map<String, String>> headerSupplier;

  OkHttpSender(
      String endpoint,
      boolean compressionEnabled,
      Supplier<Map<String, String>> headerSupplier,
      @Nullable SSLSocketFactory socketFactory,
      @Nullable X509TrustManager trustManager) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (socketFactory != null && trustManager != null) {
      builder.sslSocketFactory(socketFactory, trustManager);
    }
    this.client = builder.build();
    this.url = HttpUrl.get(endpoint);
    this.compressionEnabled = compressionEnabled;
    this.headerSupplier = headerSupplier;
  }

  @Override
  public void send(
      Consumer<OutputStream> marshaler,
      int contentLength,
      Consumer<Throwable> onFailure,
      Consumer<HttpResponse> onResponse) {
    Request.Builder requestBuilder = new Request.Builder().url(url);
    headerSupplier.get().forEach(requestBuilder::addHeader);
    RequestBody body = new ProtoRequestBody(marshaler, contentLength);
    if (compressionEnabled) {
      requestBuilder.addHeader("Content-Encoding", "gzip");
      requestBuilder.post(gzipRequestBody(body));
    } else {
      requestBuilder.post(body);
    }

    client
        .newCall(requestBuilder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                onFailure.accept(e);
              }

              @Override
              public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                  onResponse.accept(
                      new HttpResponse() {
                        @Override
                        public int statusCode() {
                          return response.code();
                        }

                        @Override
                        public String statusMessage() {
                          return response.message();
                        }

                        @Override
                        public byte[] responseBody() throws IOException {
                          return body.bytes();
                        }
                      });
                }
              }
            });
  }

  @Override
  public CompletableResultCode shutdown() {
    client.dispatcher().cancelAll();
    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
    return CompletableResultCode.ofSuccess();
  }

  private static class ProtoRequestBody extends RequestBody {

    private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

    private final Consumer<OutputStream> marshaler;
    private final int contentLength;

    /** Creates a new {@link io.opentelemetry.exporter.internal.okhttp.ProtoRequestBody}. */
    public ProtoRequestBody(Consumer<OutputStream> marshaler, int contentLength) {
      this.marshaler = marshaler;
      this.contentLength = contentLength;
    }

    @Override
    public long contentLength() {
      return contentLength;
    }

    @Override
    public MediaType contentType() {
      return PROTOBUF_MEDIA_TYPE;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
      marshaler.accept(bufferedSink.outputStream());
    }
  }

  private static RequestBody gzipRequestBody(RequestBody requestBody) {
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return requestBody.contentType();
      }

      @Override
      public long contentLength() {
        return -1;
      }

      @Override
      public void writeTo(BufferedSink bufferedSink) throws IOException {
        BufferedSink gzipSink = Okio.buffer(new GzipSink(bufferedSink));
        requestBody.writeTo(gzipSink);
        gzipSink.close();
      }
    };
  }
}
