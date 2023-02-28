/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import org.junit.jupiter.api.Test;

class FileConfigTest {

  @Test
  void foo() {
    FileConfig fileConfig =
        FileConfig.create(FileConfigTest.class.getResourceAsStream("/fileconfig/config.yaml"));

    System.out.println(fileConfig.getOpenTelemetryConfiguration());
    System.out.println(fileConfig.sdk());
  }
}
