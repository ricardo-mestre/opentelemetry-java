#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../../"

# freeze the spec & generator tools versions to make SemanticAttributes generation reproducible
# TODO: replace with tag after first release
SEMCONV_VERSION=c5f7a17b341a92055e7518067f6ed97426f5d0fe
SPEC_VERSION=v$SEMCONV_VERSION
SCHEMA_URL=https://opentelemetry.io/schemas/$SEMCONV_VERSION
GENERATOR_VERSION=0.18.0

cd ${SCRIPT_DIR}

rm -rf semantic-conventions || true
mkdir semantic-conventions
cd semantic-conventions

git init
git remote add origin https://github.com/open-telemetry/semantic-conventions.git
git fetch origin "$SEMCONV_VERSION"
git reset --hard FETCH_HEAD
cd ${SCRIPT_DIR}

docker run --rm \
  -v ${SCRIPT_DIR}/semantic-conventions/semantic_conventions:/source \
  -v ${SCRIPT_DIR}/templates:/templates \
  -v ${ROOT_DIR}/semconv/src/main/java/io/opentelemetry/semconv/trace/attributes/:/output \
  otel/semconvgen:$GENERATOR_VERSION \
  --only span,event,attribute_group,scope \
  -f /source code \
  --template /templates/SemanticAttributes.java.j2 \
  --output /output/SemanticAttributes.java \
  -Dsemconv=trace \
  -Dclass=SemanticAttributes \
  -DschemaUrl=$SCHEMA_URL \
  -Dpkg=io.opentelemetry.semconv.trace.attributes

docker run --rm \
  -v ${SCRIPT_DIR}/semantic-conventions/semantic_conventions:/source \
  -v ${SCRIPT_DIR}/templates:/templates \
  -v ${ROOT_DIR}/semconv/src/main/java/io/opentelemetry/semconv/resource/attributes/:/output \
  otel/semconvgen:$GENERATOR_VERSION \
  --only resource \
  -f /source code \
  --template /templates/SemanticAttributes.java.j2 \
  --output /output/ResourceAttributes.java \
  -Dclass=ResourceAttributes \
  -DschemaUrl=$SCHEMA_URL \
  -Dpkg=io.opentelemetry.semconv.resource.attributes

cd "$ROOT_DIR"
./gradlew spotlessApply
