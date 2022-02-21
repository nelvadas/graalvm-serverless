# Deploy Serverless Fn on OCI 

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

- [Creating an Fn Project CLI Context to Connect to Oracle Cloud Infrastructure](https://docs.oracle.com/en-us/iaas/Content/Functions/Tasks/functionscreatefncontext.htm#Create_an_Fn_Project_CLI_Context_to_Connect_to_Oracle_Cloud_Infrastructure)


In the following lab, you will deploy the serverless functions built in [lab 01](../1) and [lab 02](../2/) to Oracle Cloud Infrastructure.



<div class="inline-container">
<img src="../images//noun_SH_File_272740_100.png">
</div>

## Prerequisites
Install [oci-cli](https://github.com/oracle/oci-cli) and setup your config file  

```sh
$ ca~ ~/.oci/config
[DEFAULT]
user=ocid1.user.oc1..axxxxgpa
fingerprint=
tenancy=ocid1.tenancy.oc1..aaaaaaaa...j3zny62a
region=us-ashburn-1
key_file=~/.oci/xxx-xx-xx-xx-xx.pem
```


## Create OCI Context
- Create a new `oci-emea-hol` context  
```sh
$ fn create  context oci-emea-hol --provider oracle
Successfully created context: oci-emea-hol
```
- Use the new context 

```sh
 $ fn use context oci-emea-hol
```
- Export Env variables

```sh 
export compartmentId=ocid1.compartment.oc1..aaaaaaaapf44pl6jve63655t7y2go67w23ivtn7o33kqd576kgobmuqq3j4q
export namespace=$(oci os ns get | jq -r '.data ')
export regionKey=$(oci iam region list | jq -r '.data | map(select(."name" =="us-ashburn-1"))| .[0] .key' | tr '[:upper:]' '[:lower:]' )
```


- update context  compartment Id

```sh 
fn update context oracle.compartment-id $compartmentId
Current context updated oracle.compartment-id with ocid1.compartment.oc1..aaaaaaaapf44pl6jve63655t7y2go67w23ivtn7o33kqd576kgobmuqq3j4q
```


- Set the API URL for `us-ashburn-1`region 
```sh 
$ fn update context api-url https://functions.us-ashburn-1.oci.oraclecloud.com
Current context updated api-url with https://functions.us-ashburn-1.oci.oraclecloud.com
```


- Define the context api url 

```sh
$ oci iam region list | jq -r '.data | map(select(."name" =="us-ashburn-1"))| .[0] .key'
IAD
```

- Define the registry base url 

```sh
$ fn update context registry ${regionKey}.ocir.io/$namespace/fibonacci
Current context updated registry with iad.ocir.io/idplwqm5vo15/fibonacci
```

- Verify the created context 

```sh
$ fn list context
CURRENT   NAME      PROVIDER API URL                                      REGISTRY
 default  default  http://localhost:8080                                  nelvadas
* oci-emea-hol oracle  https://functions.us-ashburn-1.oci.oraclecloud.com iad.ocir.io/idplwqm5vo15/fibonacci
```


### Fn Deployment 

- Login to the docker registry 
```sh
docker login iad.ocir.io
Authenticating with existing credentials...
Login Succeeded
```

- Create the graal-fn-demo application 

```
$ fn create app graal-fn-demo --annotation "oracle.com/oci/subnetIds=[\"ocid1.subnet.oc1.iad.aaaaaaaaxbbh4leyyhfid2unwptbzfxqjjp4573377m2bjlak2az4s5f3r7q\"]"
Successfully created app:  graal-fn-demo
```

- Move to [lab02](../2/) directory 
```sh
cd graalvm-serverless/2/graal-fn-demo/fibonaccinative
```
- Deploy the fibonaccinative function 

```sh
$ fn -v deploy --app graal-fn-demo
Deploying fibonaccinative to app: graal-fn-demo
Bumped to version 0.0.3
Using Container engine docker
Building image iad.ocir.io/idplwqm5vo15/fibonacci/fibonaccinative:0.0.3
FN_REGISTRY:  iad.ocir.io/idplwqm5vo15/fibonacci
Current Context:  oci-emea-hol
[+] Building 2.2s (24/24) FINISHED
 => [internal] load build definition from Dockerfile                                                                                                                                      0.0s
 => => transferring dockerfile: 37B                                                                                                                                                       0.0s
 => [internal] load .dockerignore                                                                                                                                                         0.0s
 => => transferring context: 2B                                                                                                                                                           0.0s
 => [internal] load metadata for gcr.io/distroless/base:latest                                                                                                                            0.5s
 => [internal] load metadata for docker.io/fnproject/fn-java-fdk-build:jdk11-1.0.145                                                                                                      2.0s
 => [internal] load metadata for docker.io/fnproject/fn-java-fdk:jre11-1.0.145                                                                                                            0.0s
 => [internal] load metadata for container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0                                                                                      0.0s
 => [auth] fnproject/fn-java-fdk-build:pull token for registry-1.docker.io                                                                                                                0.0s
 => [fdk 1/1] FROM docker.io/fnproject/fn-java-fdk:jre11-1.0.145                                                                                                                          0.0s
 => [graalvm 1/4] FROM container-registry.oracle.com/graalvm/native-image-ee:java11-21.3.0                                                                                                0.0s
 => [stage-3 1/4] FROM gcr.io/distroless/base@sha256:03dcbf61f859d0ae4c69c6242c9e5c3d7e1a42e5d3b69eb235e81a5810dd768e                                                                     0.0s
 => [internal] load build context                                                                                                                                                         0.0s
 => => transferring context: 883B                                                                                                                                                         0.0s
 => [build 1/6] FROM docker.io/fnproject/fn-java-fdk-build:jdk11-1.0.145@sha256:d1a3e06e1f3fb4c3aa4bbe776b90cb074f9e605eedac9ff1db70060873025165                                          0.0s
 => CACHED [stage-3 2/4] WORKDIR /function                                                                                                                                                0.0s
 => CACHED [graalvm 2/4] WORKDIR /function                                                                                                                                                0.0s
 => CACHED [build 2/6] WORKDIR /function                                                                                                                                                  0.0s
 => CACHED [build 3/6] ADD pom.xml pom.xml                                                                                                                                                0.0s
 => CACHED [build 4/6] RUN ["mvn", "package", "dependency:copy-dependencies", "-DincludeScope=runtime", "-DskipTests=true", "-Dmdep.prependGroupId=true", "-DoutputDirectory=target"]     0.0s
 => CACHED [build 5/6] ADD src src                                                                                                                                                        0.0s
 => CACHED [build 6/6] RUN ["mvn", "package"]                                                                                                                                             0.0s
 => CACHED [graalvm 3/4] COPY --from=build /function/target/*.jar target/                                                                                                                 0.0s
 => CACHED [graalvm 4/4] RUN /usr/bin/native-image     -H:+StaticExecutableWithDynamicLibC     --no-fallback     --allow-incomplete-classpath     --enable-url-protocols=https,http       0.0s
 => CACHED [stage-3 3/4] COPY --from=graalvm /function/func func                                                                                                                          0.0s
 => CACHED [stage-3 4/4] COPY --from=fdk /function/runtime/lib/* ./                                                                                                                       0.0s
 => exporting to image                                                                                                                                                                    0.0s
 => => exporting layers                                                                                                                                                                   0.0s
 => => writing image sha256:38fe1462a26664db8db5a5258b2650dca1f47fee2222469ef158af631f994469                                                                                              0.0s
 => => naming to iad.ocir.io/idplwqm5vo15/fibonacci/fibonaccinative:0.0.3                                                                                                                 0.0s

Use 'docker scan' to run Snyk tests against images to find vulnerabilities and learn how to fix them

Parts:  [iad.ocir.io idplwqm5vo15 fibonacci fibonaccinative:0.0.3]
Using Container engine docker to push
Pushing iad.ocir.io/idplwqm5vo15/fibonacci/fibonaccinative:0.0.3 to docker registry...The push refers to repository [iad.ocir.io/idplwqm5vo15/fibonacci/fibonaccinative]
a3cbf7755fa3: Pushed
b431edc3582e: Pushing [==================================================>]  37.27MB
312300c3200d: Pushed
0b3d0512394d: Pushed
5b1fa8e3e100: Pushed
...
0.0.3: digest: sha256:18c2d917d595ed5a8629b15818eefe694656c04ae3ee5eda21a10f9348ee3773 size: 1365
Updating function fibonaccinative using image iad.ocir.io/idplwqm5vo15/fibonacci/fibonaccinative:0.0.3...
Successfully created function: fibonaccinative with iad.ocir.io/idplwqm5vo15/fibonacci/fibonaccinative:0.0.3

Fn: HTTP Triggers are not supported on Oracle Functions

See 'fn <command> --help' for more information. Client version: 0.6.14
```

- Invoke the function 
```sh
$ echo -n "10" | fn -v invoke  graal-fn-demo fibonaccinative
55
```

- curl 

```sh

```



## Wrap Up

Congratulations! you just run a  Native Fibonacci serverless function on Oracle Cloud Infrastructure.


---
<a href="../1/">
    <img src="../images/noun_Next_511450_100.png"
        style="display: inline; height: 6em;" />
</a>



# References 
<https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm>

Fn UI 
<https://medium.com/hackernoon/playing-with-the-fn-project-8c6939cfe5cc>

