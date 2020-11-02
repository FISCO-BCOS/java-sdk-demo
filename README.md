![](docs/images/FISCO_BCOS_Logo.svg)

English / [Chinese](docs/README_CN.md)

# Java SDK

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Build Status](https://travis-ci.org/FISCO-BCOS/java-sdk-demo.svg?branch=master)](https://travis-ci.org/FISCO-BCOS/java-sdk-demo)
[![CodeFactor](https://www.codefactor.io/repository/github/fisco-bcos/java-sdk/badge)](https://www.codefactor.io/repository/github/fisco-bcos/java-sdk-demo)
[![GitHub All Releases](https://img.shields.io/github/downloads/FISCO-BCOS/java-sdk-demo/total.svg)](https://github.com/FISCO-BCOS/java-sdk-demo)

The benchmark test collection of Java SDK, based on [Java SDK](https://github.com/FISCO-BCOS/java-sdk) provides a stress test program for FISCO BCOS nodes.


## Functions

* Provide contract compilation function, convert Solidity contract files into Java contract files
* Provide transfer pressure test Demo
* Provide AMOP test Demo
* Provide CRUD contract stress test Demo

## manual

**Compile source code**

```bash
# clone the source code
$ git clone https://github.com/FISCO-BCOS/java-sdk-demo
$ cd java-sdk-demo

# compile the source code
$ ./gradlew build 
```

**Configure Demo**

Before using the Java SDK Demo, you must first configure the Java SDK, including certificate copy and port configuration. For details, please refer to [here](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/java_sdk/quick_start.html#sdk).

```bash
# Copy the certificate (suppose the SDK certificate is located in ~/fisco/nodes/127.0.0.1/sdk directory)
$ cp -r ~/fisco/nodes/127.0.0.1/sdk/* conf

# Copy configuration file
# Note:
#   The channel port of the FISCO BCOS blockchain system built by default is 20200. 
#   If you modify this port, please modify the [network.peers] configuration option in config.toml
$ cp conf/config-example.toml conf/config.toml
```

**Execute stress test Demo**

Java SDK Demo provides a series of stress testing programs, including serial transfer contract stress testing, parallel transfer contract stress testing, AMOP stress testing, etc. The specific usage examples are as follows:

```
# Enter the dist directory
$ cd dist

# Copy the sol file that needs to be converted to java code to the dist/contracts/solidity path
# convert sol, where ${packageName} is the generated java code package path
# The generated java code is located in the /dist/contracts/sdk/java directory
$ java -cp "apps/*:lib/*:conf/" org.fisco.bcos.sdk.demo.codegen.DemoSolcToJava ${packageName}

# Pressure test PerformanceOk contract:
# count: total transaction count
# tps: qps
# groupId: the group ID
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceOk [count] [tps] [groupId]

# Pressure test parallel transfer contract
# --------------------------
# Add accounts based on the Solidity parallel contract parallelok:
# groupID: the group ID
# count: total transaction count
# tps: qps
# file: the file of the generated account saved in
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupID] [add] [count] [tps] [file]
# Add accounts based on Precompiled parallel contract precompiled
# (Parameter meaning is the same as above)
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupID] [add] [count] [tps] [file]
# --------------------------
# Based on the Solidity parallel contract parallelok to initiate a transfer transaction stress test
# groupID: Group ID of pressure test
# count: total amount of transactions
# tps: qps
# file: User file for transfer
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupID] [transfer] [count] [tps] [file]
# 基于Precompiled并行合约Precompiled发起转账压测
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupID] [transfer] [count] [tps] [file]


# CRUD contract stress test:
# CRUD insert
# count: total amount of transactions
# tps: qps
# groupId: the groupId
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [insert] [count] [tps] [groupId]
# CRUD update
# (Parameter explanation is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [update] [count] [tps] [groupId]
# CRUD remove
# (Parameter explanation is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [remove] [count] [tps] [groupId]
# CRUD query
# (Parameter explanation is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [query] [count] [tps] [groupId]
```

## Join Our Community

The FISCO BCOS community is one of the most active open-source blockchain communities in China. It provides long-term technical support for both institutional and individual developers and users of FISCO BCOS. Thousands of technical enthusiasts from numerous industry sectors have joined this community, studying and using FISCO BCOS platform. If you are also interested, you are most welcome to join us for more support and fun.

## License
![license](http://img.shields.io/badge/license-Apache%20v2-blue.svg)

All contributions are made under the [Apache License 2.0](http://www.apache.org/licenses/). See [LICENSE](LICENSE).