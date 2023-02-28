import com.sun.codemodel.JDefinedClass
import com.sun.codemodel.JMethod
import org.jsonschema2pojo.AbstractAnnotator
import org.jsonschema2pojo.GenerationConfig

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
  id("org.jsonschema2pojo") version "1.1.3"
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.incubator")

dependencies {
  api(project(":sdk:all"))

  compileOnly(project(":sdk:trace-shaded-deps"))

  annotationProcessor("com.google.auto.value:auto-value")

  // io.opentelemetry.sdk.extension.incubator.fileconfig
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.networknt:json-schema-validator:1.0.76")

  // io.opentelemetry.sdk.extension.incubator.metric.viewconfig
  implementation(project(":sdk-extensions:autoconfigure-spi"))
  implementation("org.snakeyaml:snakeyaml-engine")

  // io.opentelemetry.sdk.extension.trace.zpages
  implementation(project(":semconv"))
  compileOnly("com.sun.net.httpserver:http")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))

  testImplementation("com.google.guava:guava-testlib")
}

class Annotator(config: GenerationConfig) : AbstractAnnotator(config) {
  override fun anySetter(setter: JMethod, clazz: JDefinedClass) {
    setter.annotate(clazz.owner().directClass("com.fasterxml.jackson.databind.annotation.JsonDeserialize"))
      .param("contentUsing", clazz.owner().directClass("io.opentelemetry.sdk.extension.incubator.fileconfig.internal.PatternPropertiesDeserializer"))
  }
}

jsonSchema2Pojo {
  sourceFiles = setOf(file(project.projectDir.toString() + "/src/main/resources/fileconfig"))

  targetPackage = "io.opentelemetry.sdk.extension.incubator.fileconfig.internal"
  isIncludeGeneratedAnnotation = false
  includeSetters = false
  removeOldOutput = true
  setCustomAnnotator(Annotator::class.java)
}
