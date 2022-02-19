#
# Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM fnproject/fn-java-fdk-build:jdk11-1.0.145 as build
WORKDIR /function
ENV MAVEN_OPTS=-Dmaven.repo.local=/usr/share/maven/ref/repository
ADD pom.xml pom.xml
RUN ["mvn", "package", "dependency:copy-dependencies", "-DincludeScope=runtime", "-DskipTests=true", "-Dmdep.prependGroupId=true", "-DoutputDirectory=target"]
ADD src src
RUN ["mvn", "package"]

FROM container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0 as graalvm
WORKDIR /function
COPY --from=build /function/target/*.jar target/
RUN /usr/bin/native-image \
    -H:+StaticExecutableWithDynamicLibC \
    --no-fallback \
    --allow-incomplete-classpath \
    --enable-url-protocols=https,http \
    --report-unsupported-elements-at-runtime \
    -H:Name=func \
    -classpath "target/*"\
    com.fnproject.fn.runtime.EntryPoint

# need socket library from Fn FDK
FROM fnproject/fn-java-fdk:jre11-1.0.145 as fdk

FROM gcr.io/distroless/base
WORKDIR /function
COPY --from=graalvm /function/func func
COPY --from=fdk /function/runtime/lib/* ./
ENTRYPOINT ["./func", "-XX:MaximumHeapSizePercent=80", "-Djava.library.path=/function"]
CMD [ "com.example.fn.Fibonaccinative::handleRequest" ]
