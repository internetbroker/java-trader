# Java Trader交易框架文档

[TOC]

## 简介
java-trader项目目标是成为一个基于Java的开源期货交易框架, 有这些特点:
+ 分布式管理界面, 支持交易服务的集中管理
+ 基于纯Java的行情和交易接口, 内建支持JCTP, 支持运行时指定JCTP版本, 具体指定方式参见PluginService的配置项
+ 行情/交易代码全部在同一个JVM中, 使用disrputor实现低延时的线程间事件传递.
+ 使用动态ClassLoader加载交易策略实现类, 允许运行时动态更新
+ 支持基于GROOVY的脚本式策略编程, 可运行时动态更新, 支持自定义函数插件式扩展
+ 交易策略通过简单的分组和配置参数调整, 动态组合动态调整, 最大限度复用已有的开平止盈止损策略
+ 支持账户视图(AccountView), 允许主动限制策略的仓位和资金

## 构建
构建环境需求: JDK 11/12, GRADLE 5.4, bash(Linux或CYGWIN)

java-trader的构建过程需要一些手动编译和安装依赖包的操作:
+ 安装jctp依赖包到本地MVN Repository:

```
cd jars
./mvn.sh
```

+ 构建工程

```
gradle clean build install
```

+本地复制文件到traderHome

```
gradle localDeploy
```

## 准备运行环境

+ 准备JDK 11/12
+ 准备traderHome/etc/trader.xml
+ (*自动完成, 通过localDeploy)准备JCTP插件包: 从plugin-jctp-****/build/distributions目录，将jctp-version-platform.zip文件释放到 ~/traderHome/plugin目录， 注意需要为每个JCTP插件都释放一次
+ 准备crontab 运行环境变量, 写在 crontab 中

```
SHELL=/bin/bash
JAVA_HOME=XXXXXX
TRADER_HOME=YYYYYY
PATH=XXXXXX:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
```

+ 准备CTP 最新API的权限(Linux)

```
chmod +s /usr/sbin/dmidecode
```

+ 从 Eclipse打开

```
gradle eclipse

ECLIPSE 导入项目: 
1 File/Import...
2 Select "Existing Projects into Workspace"
3 Select root directory: java-trader
4 Select projects, finish... 
```


## 如何运行和监控

### 运行目录与配置文件

java-trader的运行目录为 ~/traderHome, 缺省在当前用户下, 目录结构如下:

```
traderHome
    |-apps
    |   |-trader
    |       |-trader.sh
    |       |-trader-service.sh
    |       |-trader-ui.sh
    |       |-trader-services-1.0.0.jar
    |
    |-etc(配置文件目录)
    |   |-trader.xml (配置文件, 可以有多个. 启动时需要选择一个)
    |   |-trader-key.ini (加密密钥文件, 第一次运行时会自动生成)
    |   |-trader-ui.xml (管理服务的配置文件)
    |-log(日志目录)
    |-data(数据目录)
    |   |-marketData(临时行情数据目录)
    |   |   |-20181010 (按交易日区分的行情数据)
    |   |   |   |- mdProducer-1
    |   |   |   |- mdProducer-2
    |   |   |-20181011
    |   |-repository (整理归档后的行情数据)
    |   |-work (工作目录)
    |-plugin (插件根目录, 插件目录内容需要手工准备)
    |   |-jctp-6.3.13-linux_x64(JCTP插件目录)
    |   |-jctp-6.3.13-win32_x64(JCTP插件目录)
    |   |-jctp-6.3.13-win32_x86(JCTP插件目录)
    |   |-jctp-6.3.15-linux_x64(JCTP插件目录)
    |   |-jctp-6.3.15-win32_x64(JCTP插件目录)
    |   |-jctp-6.3.15-win32_x86(JCTP插件目录)
```

配置文件为XML格式, 按照不同的Service实现, 每个属性有有不同的含义

### 启动-关闭与命令行参数
java-trader 每次开市前10分钟需要手工启动, 休市后自动停止, 自动停止时间可以在ShutdownTriggerService配置端设置

java-trader使用命令行方式启动和监控, 支持的命令行如下

```
trader.sh [-Dproperty=value] action subaction

#加密文本
trader.sh crypto encrypt <PLAIN_TEXT>

#解密文本
trader.sh crypto encrypt <ENCRYPTED_TEXT>

#启动H2行情数据库, 如果本机有多个交易程序, 必须启动数据库
trader.sh h2db start

#导入行情数据
trader.sh marketData import

#压缩行情数据
trader.sh repository archive

#启动java-trader服务
trader.sh service start

#java-trader服务当前状态
trader.sh service status

#查看插件列表
trader.sh plugin list

#支持的property
trader.home: 指定另一个traderHome目录
trader.configFile: 指定另一个trader配置文件
```


## 插件

插件是可以运行时加载和更新的动态扩展库, 基于Java 动态Classloader机制实现, 每个插件的目录结构如下

```
/traderHome/plugin
    |
    /pluginDir
        |- plugin.properties
        |-classes(*.class)
        |-lib(*.jar)
```

plugin.properties是一个标准java properties文件, 包含这样几个属性:
* id: 全局唯一插件id
* exposedInterfaces: (可选)逗号,分隔的导出接口
* 其它属性: 用于插件管理查询时的统一查询语法, 类似JMX query

id: 唯一ID, 例如：

```
id=jctp-6.3.15-linux_x64
```

title: 可显示名称, 例如：

```
title=我是插件
```

exposedInterfaces: 导出接口列表, 以","分隔, 例如：

例如

```
exposedInterfaces=trader.service.md.MarketDataProducerFactory
```
注: 这个属性的值会自动合并所有标记为Discoverable的实现类的interfaceClass的值.


目前, 支持的标准导出接口有:
* trader.service.md.MarketDataProducerFactory : 行情网关接口
* trader.service.trade.TxnSessionFactory : 交易网关接口
* trader.service.tradlet.Tradlet : 交易策略接口

## 交易小程序(Tradlet)的实体关系
* 策略微服务(Tradlet)是一个交易策略微代码的接口, 每个Tradlet实现都需要完成一个独立的功能, 例如止损, 动态止盈, 开仓, 超时撤销报单等等. Tradlet 实现类可以通过插件机制实现动态加载和动态更新.
* 策略组(TradleGroup)是Tradlet的组合, 最终形成一个可以完整的交易策略, 每个策略组在配置文件中有一个对应的配置项, 支持动态更新和启用禁用.
* 交易剧本(Playbook)是一个完整的开仓平仓过程记录, 交易框架提供辅助函数用于实现自动报单超时和平仓超时处理. 交易剧本和账户视图打交道, 而非直接操作真实账户.
* 账户视图(AccountView)是一个虚拟账户, 理论上支持两种模式: 1) 从一个真实账户保留部分资金和仓位限制 2) 从多个真实账户虚拟合成一个账户视图, 目前只实现了第一种模式, 第二种模式过于复杂暂不实现.

### GROOVY 脚本支持

GROOVY 脚本支持是通过标准交易小程序实现, 如下:

```
        <tradletGroup id="group_ru" ><![CDATA[
#INI格式, 单行注释从#开始
[common]
state=disabled
exchangeable=ru1901
account=sim-account1

[GROOVY]
//GROOVY 代码正文
def onInit(){
    println("Hello world from onInit()");
}

def onNewBar(series){
}

        ]]></tradletGroup>

```

#### Groovy脚本的事件函数
Groovy脚本通过事件函数被调用, 支持这样一些事件函数:
* onInit()
* onTick(MarketData tick)
* onNewBar(TimeSeries series)
* onNoopSecond()

这些事件函数与Tradlet小程序接口的事件函数完全相同

#### Groovy脚本的函数支持
Groovy脚本可以访问事件函数, 这些事件函数运行时被动态加载, 支持通过插件方式扩展, 实现代码参见 Java package trader.service.tradlet.script.func下的所有的标准函数. 自定义函数通过Discoverable annotation实现自动发现.

## 多线程模型
基于disruptor低延时事件分发机制, 实现行情和交易事件的多线程处理. 同时存在多个disruptor线程, 构成完整的交易处理逻辑.
线程接力模型:
1 行情/交易API发送异步事件, 作为Multiple Producer
2 AsyncEventService 的 Main consumer 线程: 快速处理, 更新状态.
3 对于需要策略处理的事件, 异步派发到交易策略组, 这时候可以作为Single Producer派发, 进一步降低延时
4 每个交易策略组, 运行在各自的disrputor consumer线程中, 处理行情和交易事件

注: 关于 Single Producer vs Multiple Producer的性能对比, 简单的说有3倍的性能差距, 参见: [Disruptor Getting Started](https://github.com/LMAX-Exchange/disruptor/wiki/Getting-Started)

### 异步事件处理服务
异步事件处理服务(AsyncEventService) 同时接收全部行情和交易事件, 作为disruptor的多producer存在, 单独启动行情数据处理线程, 分发行情数据.
K线处理服务(TAService)和 账户报单交易服务(TradeService) 由于延时很低, 直接在disruptor的consumer线程被调用.

### 交易策略组
交易策略服务(TradeletService)负责维护与某个账户视图(AccountView)相关的交易策略组(TradletGroup), 每个交易策略组运行关联的账户线程或运行在一个独立的 disruptor consumer 线程中. 每个策略组在处理行情切片数据时, K线与账户的状态更新确保已经完成.

## 标准服务以及相关的配置

### BasisService

BasisServic是基础服务配置

可配置项:
1. httpPort: HTTP 端口
2. exportAddr: 外部连接地址, 缺省为hostname
3. mgmtURL: 管理服务URL, 格式为: ws://addr:port, 为空表明独立运行, 不接受管理

```
    <BasisService exportAddr="" httpPort="10081" mgmtURL="ws://localhost:10080" />
```


### PluginService

PluginService是插件服务， 负责统一加载插件

可配置项: 
1. attachedPlugins 附加的插件ID列表, 每行一个Plugin Id. JCTP插件至少需要指定一个, 如下:

```
    <PluginService>
        <attachedPlugins><![CDATA[
            jctp-6.3.15-${jtrader.platform}
        ]]></attachedPlugins>
    </PluginService>
```


### AsyncEventService

AsyncEventService是异步消息处理服务, 负责统一处理行情和交易接口的事件 

可配置项: 
1. disruptor等待策略, 缓冲区大小 

```
    <AsyncEventService>
		<disruptor waitStrategy="BlockingWait" ringBufferSize="65536" />
    </AsyncEventService>
```

### MarketDataService

MarketDataService是行情消息处理服务, 负责连接多个行情数据源, 整理成为统一的行情TICK数据, 并单独保存原始行情数据

配置项有:
1. producer: 行情数据源, provider目前支持ctp, 可以通过插件支持别的数据源实现(飞马, 易胜等等)
2. subscriptions: 订阅的行情品种逗号分隔的品种列表; 使用 $PrimaryContracts代表主力合约

```
	<MarketDataService saveData="true">
	    <producer id="zsqh_sh_uniconn1" provider="ctp" ><![CDATA[
			frontUrl=tcp://000.000.000.000:41213
			brokerId=0000
			username=000000000
			password=000000
	    ]]></producer>
	    <producer id="zsqh_sh_telecom1" provider="ctp" ><![CDATA[
			frontUrl=tcp://000.000.000.000:41213
			brokerId=0000
			username=000000000
			password=000000
	    ]]></producer>

		<!--$PrimaryContracts会被自动替换为实际的主力合约-->
	    <subscriptions>
	    	$PrimaryContracts
	    </subscriptions>
	</MarketDataService>
```

### ShutdownTriggerService
设置自动停止时间

```
	<ShutdownTriggerService time="15:25,02:32" />
```

### TradeService
交易账户和连接管理服务, 支持同时配置多个Account

一个Account配置对应一个实际的交易账户, 每个交易账户通过 provider属性指定连接API类型: ctp, femas, xtp 等等. 对于不支持的交易API, 需要通过插件机制动态扩展.

需要加密的参数, 例如 userId, password可以使用密文(通过加密命令行得到)

```
    <TradeService>
        <account id="accountId" provider="ctp" ><![CDATA[

[brokerMarginRatio]

[connectionProps]
frontUrl=tcp://<HOST>:PORT
brokerId=8888
userId={key_AYYzfYzKmZ82qguwhEHpmB}BvcnR1voKo94TWrgkfRfJk
investorId={key_AYYzfYzKmZ82qguwhEHpmB}BvcnR1voKo94TWrgkfRfJk
password={key_AYYzfYzKmZ82qguwhEHpmB}DeCabtP6eqBfGwQLPjcqLd

]]>
        </account>
    </TradeService>
```


### TradletService
Tradlet/TradletGroup的加载和运行时管理服务

```
<TradletService>
        <!-- 定义无法自动发现需要明确加载的Tradlet实现类名 -->
        <tradlets><![CDATA[
            trader.service.tradlet.impl.StopLossTradlet
            trader.service.tradlet.impl.MACD135Tradlet
        ]]></tradlets>

        <tradletGroup id="group_au" ><![CDATA[
#This is comment
[common]
state=disabled
exchangeable=au1906
account=sim-account1

[MACD135]

[StopLoss]
{
    "default": {
        "priceSteps": [{
            "priceBase": "4t",
            "duration": "30s"
        }, {
            "priceBase": "8t",
            "duration": "1s"
        }],
        "endTime": "14:55:00"
    }
}
        ]]></tradletGroup>
</TradletService>
```

## REST API
java-trader 作为一个纯后台WEB应用, 对前端提供的REST API实现类都保存在 package trader.api中. 关于REST API的详细描述, 可以通过swagger在如下URL获得所有API的参考:

http://localhost:10080/swagger-ui.html

基础服务API:
+ Config API
+ Node API
+ Log API
+ Plugin API

交易服务API:
+ MarketData API
+ Trade API
+ Tradlet API

## 取得联系

QQ群: 654906364

QQ: 1933214924

