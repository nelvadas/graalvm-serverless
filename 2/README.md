# Optimizing Serverless Application with GraalVM Enterprise 

<div class="inline-container">

<span><img src="../images/noun_Stopwatch_14262_100.png"> </span>
<span style="color:blue;font-weight:bold"></span>
<strong>
  Estimated time: 15 minutes
</strong>
</div>

<div class="inline-container">
<img src="../images/noun_Book_3652476_100.png">
<strong>References:</strong>
</div>

- [Building your Frist Function with Fn](https://github.com/fnproject/fn#your-first-function)
- [Fn Tutorials](https://fnproject.io/tutorials/)


In the following lab, we will follow various patterns to optimize our Serverless function with GraalVM 





<div class="inline-container">
<img src="../images//noun_SH_File_272740_100.png">
</div>

## GraalVM EE Container images
Oracle provides a couple of Initializations images for Fn with GraalVM EE.


### Base Images
Enterprises Container images are subject to licence agreement. Login with your Oracle account

```sh
$ docker login -u <YOUR-ORACLE-USERNAME>   container-registry.oracle.com
Password:
Login Succeeded
```

Pull the  [container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0](container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0) Docker image. 
You can choose a nearest download mirror ( Franckfurt or Amsterdam for mine)


```sh
$ docker pull container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0
java11-21.3.0: Pulling from graalvm/native-image-ee
58c4eaffce77: Already exists
8a552bbc4c81: Already exists
85e7262af4f1: Already exists
Digest: sha256:3755ccd604283a1f2135927bee89a3c17d7a5155ae6baef534bffd1fef6ec427
Status: Downloaded newer image for container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0
container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0
```

### Fn init Images

To initialize a Native FaaS with GraalVM Enteprise, you can rely on the following repositories to build your Own initializer and builder images 

- [GraalVM Enterprise Images for Oracle Cloud Infrastructure and Functions](https://github.com/shaunsmith/graalvm-ee-docker)
- [GraalVM Fn Init Image builder](hhttps://github.com/shaunsmith/graalvm-fn-init-images)


### Others Docker Images

Depending of your environment you may take different URL options to download the GraalVM EE Docker images.

- [GraalVM Enterprise Images Compact JDK](https://container-registry.oracle.com/ords/f?p=113:1:107989510949953:::1:P1_BUSINESS_AREA:88&cs=3NIoF3MM1WggfxVBVGJgZOvIPOb6zDYVDZWMnhbx5o93XuXWp6AP3CWcSTJUHrtM72arGtgfe820zTeqy8qwyUw)
- [GraalVM Enterprise Images for Oracle Cloud Infrastructure and Functions](https://github.com/shaunsmith/graalvm-ee-docker)
- [GraalVM Community Edition Images](https://github.com/graalvm/container/pkgs/container/graalvm-ce/versions)




## GraalVM FaaS 

### Initialize a Native Faas Application
For the purpose of this workshop `(only)` you can rely on the following temporal [Builder](https://github.com/nelvadas/graalvm-serverless/pkgs/container/fn-java-graalvm-ee-init) Docker image to initialize your native fibonaci function.
:warning: Theses images are not the official one. For any support request you should refer to Oracle.

```sh 
docker pull ghcr.io/nelvadas/fn-java-graalvm-ee-init:jdk11-1.0.145-21.3.0
```

Now create a native Fibonaci function with the following command 

```sh
$ fn init --init-image ghcr.io/nelvadas/fn-java-graalvm-ee-init:jdk11-1.0.145-21.3.0 --version 0.0.2 --trigger http --name fibonacciNative
Running init-image: ghcr.io/nelvadas/fn-java-graalvm-ee-init:jdk11-1.0.145-21.3.0
Executing docker command: run --rm -e FN_FUNCTION_NAME=fibonaccinative ghcr.io/nelvadas/fn-java-graalvm-ee-init:jdk11-1.0.145-21.3.0
func.yaml created.
```

A new function is generated with the following descriptor 
```yaml
$ cat func.yaml
schema_version: 20180708
name: fibonaccinative
version: 0.0.2
triggers:
- name: fibonaccinative
  type: http
  source: /fibonaccinative
```

A new MultiStage build  Dockerfile  is provided to build the function

```dockerfile
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
```

Replace the content of the `Fibonaccinative.java` with the business logic built at the [Lab 01](../1/graal-fn-demo/fibonacci/src/main/java/com/oracle/graalvm/fn/FibonacciFunction.java) 

Update the `FibonaccinativeTest`  accordingly.




### Rebuild the function 

With this new configuration, the function is built from the Docker file.

```sh 
$ $ fn build -v
Using Container engine docker
Building image nelvadas/fibonaccinative:0.0.2
FN_REGISTRY:  nelvadas
Current Context:  default
[+] Building 255.2s (24/24) FINISHED
 => [internal] load build definition from Dockerfile                                                                                                                                  0.0s
 => => transferring dockerfile: 1.83kB                                                                                                                                                0.0s
 => [internal] load .dockerignore                                                                                                                                                     0.0s
 => => transferring context: 2B                                                                                                                                                       0.0s
 => [internal] load metadata for gcr.io/distroless/base:latest                                                                                                                        0.5s
 => [internal] load metadata for container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0                                                                                  0.0s
 => [internal] load metadata for docker.io/fnproject/fn-java-fdk-build:jdk11-1.0.145                                                                                                  1.4s
 => [internal] load metadata for docker.io/fnproject/fn-java-fdk:jre11-1.0.145                                                                                                        0.0s
 => [auth] fnproject/fn-java-fdk-build:pull token for registry-1.docker.io                                                                                                            0.0s
 => [stage-3 1/4] FROM gcr.io/distroless/base@sha256:03dcbf61f859d0ae4c69c6242c9e5c3d7e1a42e5d3b69eb235e81a5810dd768e                                                                 0.0s
 => [graalvm 1/4] FROM container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0                                                                                            0.0s
 => [internal] load build context                                                                                                                                                     0.0s
 => => transferring context: 7.58kB                                                                                                                                                   0.0s
 => CACHED [fdk 1/1] FROM docker.io/fnproject/fn-java-fdk:jre11-1.0.145                                                                                                               0.0s
 => [build 1/6] FROM docker.io/fnproject/fn-java-fdk-build:jdk11-1.0.145@sha256:d1a3e06e1f3fb4c3aa4bbe776b90cb074f9e605eedac9ff1db70060873025165                                      0.0s
 => CACHED [build 2/6] WORKDIR /function                                                                                                                                              0.0s
 => [build 3/6] ADD pom.xml pom.xml                                                                                                                                                   0.0s
 => [build 4/6] RUN ["mvn", "package", "dependency:copy-dependencies", "-DincludeScope=runtime", "-DskipTests=true", "-Dmdep.prependGroupId=true", "-DoutputDirectory=target"]       39.3s
 => [build 5/6] ADD src src                                                                                                                                                           0.0s
 => [build 6/6] RUN ["mvn", "package"]                                                                                                                                                8.7s
 => CACHED [graalvm 2/4] WORKDIR /function                                                                                                                                            0.0s
 => [graalvm 3/4] COPY --from=build /function/target/*.jar target/                                                                                                                    0.0s
 => [graalvm 4/4] RUN /usr/bin/native-image     -H:+StaticExecutableWithDynamicLibC     --no-fallback     --allow-incomplete-classpath     --enable-url-protocols=https,http     -  204.3s
 => CACHED [stage-3 2/4] WORKDIR /function                                                                                                                                            0.0s
 => [stage-3 3/4] COPY --from=graalvm /function/func func                                                                                                                             0.1s
 => [stage-3 4/4] COPY --from=fdk /function/runtime/lib/* ./                                                                                                                          0.0s
 => exporting to image                                                                                                                                                                0.2s
 => => exporting layers                                                                                                                                                               0.2s
 => => writing image sha256:38fe1462a26664db8db5a5258b2650dca1f47fee2222469ef158af631f994469                                                                                          0.0s
 => => naming to docker.io/nelvadas/fibonaccinative:0.0.2                                                                                                                             0.0s

Use 'docker scan' to run Snyk tests against images to find vulnerabilities and learn how to fix them

Function nelvadas/fibonaccinative:0.0.2 built successfully.
```

Notice the native image utility call. 
```
...
gaalvm 3/4] COPY --from=build /function/target/*.jar target/                                                                                                                    0.0s
 => [graalvm 4/4] RUN /usr/bin/native-image     -H:+StaticExecutableWithDynamicLibC     --no-fallback     --allow-incomplete-classpath     --enable-url-protocols=https,http     -  204.3s
 ```

The new version is built , image is  about 6X ```smaller`` than the first image generated.

```sh 
$ docker images | grep nelvadas/fibonacci
docker images | grep nelvadas/fibonacci
nelvadas/fibonaccinative                                0.0.2                                                   38fe1462a266   4 minutes ago   57.5MB
nelvadas/fibonacci                                      0.0.1                                                   b1c83e51fe5f   2 days ago      294MB
```


### Local Deployment

Deploy the function on the local running server using fn deploy

```sh
$ fn deploy --app graal-fn-demo --local --no-bump
Deploying fibonaccinative to app: graal-fn-demo
Using Container engine docker
Building image nelvadas/fibonaccinative:0.0.2 ..
Updating function fibonaccinative using image nelvadas/fibonaccinative:0.0.2...
Successfully created function: fibonaccinative with nelvadas/fibonaccinative:0.0.2
Successfully created trigger: fibonaccinative
Trigger Endpoint: http://localhost:8080/t/graal-fn-demo/fibonaccinative
```

The no-bump argument assumes external version management .

The functions list of the graal-fn-demo applications is updated with the new function

```sh
$ $ fn list functions graal-fn-demo
NAME  IMAGE    ID
fibonacci nelvadas/fibonacci:0.0.1 01FVX74J8XNG8G00GZJ0000003
fibonaccinative nelvadas/fibonaccinative:0.0.2 01FW6W5FAVNG8G00GZJ000000A
```

### Invoke the function

 Inspect the function to confirm the changes

```sh
$ fn inspect function graal-fn-demo fibonaccinative
{
 "annotations": {
  "fnproject.io/fn/invokeEndpoint": "http://localhost:8080/invoke/01FW6W5FAVNG8G00GZJ000000A"
 },
 "app_id": "01FVX21T29NG8G00GZJ0000002",
 "created_at": "2022-02-18T16:55:40.635Z",
 "id": "01FW6W5FAVNG8G00GZJ000000A",
 "idle_timeout": 30,
 "image": "nelvadas/fibonaccinative:0.0.2",
 "memory": 128,
 "name": "fibonaccinative",
 "timeout": 30,
 "updated_at": "2022-02-18T16:55:40.635Z"
}
```

```sh
$ curl  -X POST --data 8  http://localhost:8080/t/graal-fn-demo/fibonaccinative
21
```

### Fn Performance with GraalVM AOT

Let's run a benchmark on the new AOT serverless function with `hey`()
For each function invocation, a docker container is created to serve the request.

Create an Body file for the benchmark; for this we want to get Fibonaci(100)

```sh
$ echo 100 > data.txt
$ cat data.txt
100
```

Now run 1000 requests with 100 cucurrent calls.

```sh
$ hey -n 1000 -c 100  -m POST -D ../../../data.txt  http://localhost:8080/t/graal-fn-demo/fibonaccinative

Summary:
  Total: 8.5006 secs
  Slowest: 5.6738 secs
  Fastest: 0.0085 secs
  Average: 0.6565 secs
  Requests/sec: 117.6393

  Total data: 2000 bytes
  Size/request: 2 bytes

Response time histogram:
  0.009 [1] |
  0.575 [708] |■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  1.142 [106] |■■■■■■
  1.708 [50] |■■■
  2.275 [29] |■■
  2.841 [43] |■■
  3.408 [27] |■■
  3.974 [20] |■
  4.541 [4] |
  5.107 [4] |
  5.674 [8] |


Latency distribution:
  10% in 0.0234 secs
  25% in 0.0438 secs
  50% in 0.1610 secs
  75% in 0.7606 secs
  90% in 2.3400 secs
  95% in 2.9496 secs
  99% in 4.8928 secs

Details (average, fastest, slowest):
  DNS+dialup: 0.0008 secs, 0.0085 secs, 5.6738 secs
  DNS-lookup: 0.0002 secs, 0.0000 secs, 0.0031 secs
  req write: 0.0001 secs, 0.0000 secs, 0.0030 secs
  resp wait: 0.6555 secs, 0.0084 secs, 5.6637 secs
  resp read: 0.0001 secs, 0.0000 secs, 0.0005 secs

Status code distribution:
  [200] 1000 responses
  ```

For Reference here are the metrics for the initial Version 
```
$ hey -n 1000 -c 100  -m POST -D ../../../data.txtdata.txt  http://localhost:8080/t/graal-fn-demo/fibonaccinative

Summary:
  Total: 32.8704 secs
  Slowest: 19.9812 secs
  Fastest: 0.0212 secs
  Average: 2.1615 secs
  Requests/sec: 30.4226

  Total data: 1980 bytes
  Size/request: 2 bytes

Response time histogram:
  0.021 [1] |
  2.017 [734] |■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  4.013 [68] |■■■■
  6.009 [47] |■■■
  8.005 [35] |■■
  10.001 [24] |■
  11.997 [18] |■
  13.993 [9] |
  15.989 [14] |■
  17.985 [11] |■
  19.981 [6] |


Latency distribution:
  10% in 0.1277 secs
  25% in 0.2228 secs
  50% in 0.4512 secs
  75% in 1.7373 secs
  90% in 7.1375 secs
  95% in 11.4712 secs
  99% in 17.8666 secs

Details (average, fastest, slowest):
  DNS+dialup: 0.0009 secs, 0.0212 secs, 19.9812 secs
  DNS-lookup: 0.0004 secs, 0.0000 secs, 0.0056 secs
  req write: 0.0001 secs, 0.0000 secs, 0.0034 secs
  resp wait: 2.1604 secs, 0.0211 secs, 19.9667 secs
  resp read: 0.0001 secs, 0.0000 secs, 0.0007 secs

Status code distribution:
  [200] 966 responses
  [502] 1 responses

Error distribution:
  [33] Post "http://localhost:8080/t/graal-fn-demo/fibonacci": context deadline exceeded (Client.Timeout exceeded while awaiting headers)


```


### Metrics Comparison Table 



In average we can comple `30.42` req/s  with a latency  `~17.87` for 99% of the requests.




## Wrap Up

Congratulations! you just initialize a Native Fibonacci serverless function with Fn SDK
Build the function with GraalVM EE and benchmarks the performance of this function toward those of the same function running with Open JDK 11. 



Next, we'll try to create a Optimize the function with GraalVM Enterprise features..

---
<a href="../1/">
    <img src="../images/noun_Next_511450_100.png"
        style="display: inline; height: 6em;" />
</a>



# References 
<https://docs.oracle.com/en-us/iaas/Content/Functions/Tasks/functionsusingwithfncli.htm>

Fn UI 
<https://medium.com/hackernoon/playing-with-the-fn-project-8c6939cfe5cc>
