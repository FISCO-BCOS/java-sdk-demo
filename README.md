![](./docs/images/FISCO_BCOS_Logo.svg)

[English](./docs/README_EN.md) / 中文

# Java SDK Demo

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Build Status](https://travis-ci.org/FISCO-BCOS/java-sdk-demo.svg?branch=master)](https://travis-ci.org/FISCO-BCOS/java-sdk-demo)
[![CodeFactor](https://www.codefactor.io/repository/github/fisco-bcos/java-sdk/badge)](https://www.codefactor.io/repository/github/fisco-bcos/java-sdk-demo)
[![GitHub All Releases](https://img.shields.io/github/downloads/FISCO-BCOS/java-sdk-demo/total.svg)](https://github.com/FISCO-BCOS/java-sdk-demo)


Java SDK的基准测试集合，基于[Java SDK](https://github.com/FISCO-BCOS/java-sdk)提供了对FISCO BCOS节点的压测程序。


## 功能

* 提供多合约并行转账压测Demo
* 提供转账压测Demo
* 提供AMOP测试Demo
* 提供KV合约压测Demo
* 提供合约编译功能，将Solidity合约文件转换成Java合约文件
* 提供合约编译功能，将Liquid合约文件编译后的WASM和ABI文件转换成Java合约文件

## 使用手册

**编译源码**

```bash
# 下载源码
$ git clone https://github.com/FISCO-BCOS/java-sdk-demo
$ cd java-sdk-demo

# 编译源码
$ bash gradlew build 
```

**配置Demo**

使用Java SDK Demo之前，首先要配置Java SDK，包括证书拷贝以及端口配置，详细请参考[这里](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/develop/sdk/java_sdk/quick_start.html#sdk).

```bash
# 拷贝证书(设SDK证书位于~/fisco/nodes/127.0.0.1/sdk目录)
$ cp -r ~/fisco/nodes/127.0.0.1/sdk/* conf

# 拷贝配置文件
# 注:
#   默认搭建的FISCO BCOS区块链系统RPC端口是20200，若修改了该端口，请同步修改config.toml中的[network.peers]配置选项
$ cp conf/config-example.toml conf/config.toml
```

**执行压测Demo**

Java SDK Demo提供了一系列压测程序，包括串行转账合约压测、并行转账合约压测、AMOP压测等，具体使用示例如下:
注意：下面的压力测试程序均为EVM的节点执行环境，节点配置详情请参考：[节点配置](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/tutorial/air/config.html)

```shell
# 进入dist目录
$ cd dist

# 多合约-合约内并行转账合约:
# groupId: 压测的群组ID
# userCount: 创建账户的个数，建议（4～32个）
# count: 压测的交易总量
# qps: 压测QPS
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceDMC [groupId] [userCount] [count] [qps]

# 多合约-跨合约并行转账
# groupId: 压测的群组ID
# userCount: 创建账户的个数，建议（4～32个）
# count: 压测的交易总量
# qps: 压测QPS
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTransferDMC [groupId] [userCount] [count] [qps]

# 压测串行转账合约:
# count: 压测的交易总量
# tps: 压测QPS
# groupId: 压测的群组ID
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceOk [count] [tps] [groupId]

# 压测并行转账合约
# --------------------------
# 基于Solidity并行合约parallelok添加账户:
# groupID: 压测的群组ID
# count: 压测的交易总量
# tps: 压测QPS
# file: 保存生成账户的文件名
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupID] [add] [count] [tps] [file]
# 基于Precompiled并行合约precompiled添加账户
# (参数含义同上)
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupID] [add] [count] [tps] [file]
# --------------------------
# 基于Solidity并行合约parallelok发起转账交易压测
# groupID: 压测的群组ID
# count: 压测的交易总量
# tps: 压测的QPS
# file: 转账用户文件
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupID] [transfer] [count] [tps] [file]
# 基于Precompiled并行合约Precompiled发起转账压测
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupID] [transfer] [count] [tps] [file]


# KVTable合约压测
# 压测KV set
# count: 压测的交易总量
# tps: 压测QPS
# groupId: 压测群组
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceKVTable [set] [count] [tps] [groupId]
# 压测KV get
# (参数解释同上)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceKVTable [get] [count] [tps] [groupId]
```

**以下是WASM环境的压力测试**

```shell
# 压测并行转账合约
# --------------------------
# 基于Liquid并行合约parallelok添加账户:
# groupID: 压测的群组ID
# count: 压测的交易总量
# tps: 压测QPS
# file: 保存生成账户的文件名
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelLiquidPerf [groupId] [add] [count] [tps] [file]

# 基于Liquid并行合约parallelok发起转账交易压测
# groupID: 压测的群组ID
# count: 压测的交易总量
# tps: 压测的QPS
# file: 转账用户文件
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelLiquidPerf [groupId] [transfer] [count] [tps] [file]
```

## 贡献代码
欢迎参与FISCO BCOS的社区建设：
- 点亮我们的小星星(点击项目左上方Star按钮)。
- 提交代码(Pull requests)，参考我们的[代码贡献流程](CONTRIBUTING_CN.md)。
- [提问和提交BUG](https://github.com/FISCO-BCOS/java-sdk-demo/issues)。

## 加入我们的社区

FISCO BCOS开源社区是国内活跃的开源社区，社区长期为机构和个人开发者提供各类支持与帮助。已有来自各行业的数千名技术爱好者在研究和使用FISCO BCOS。如您对FISCO BCOS开源技术及应用感兴趣，欢迎加入社区获得更多支持与帮助。

## License

![license](https://img.shields.io/badge/license-Apache%20v2-blue.svg)

Web3SDK的开源协议为[Apache License 2.0](http://www.apache.org/licenses/). 详情参考[LICENSE](../LICENSE)。