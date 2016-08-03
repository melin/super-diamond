[![Gitter](https://badges.gitter.im/super-diamond/Lobby.svg)](https://gitter.im/super-diamond/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Build Status](https://travis-ci.org/talywy/super-diamond.svg?branch=master)](https://travis-ci.org/talywy/super-diamond)

#SuperDiamond

###简介
SuperDiamond是集中化配置系统，用来将分散在各业务系统中的配置进行集中化管理，便于开发/测试/运维人员进行配置管理。原作者已暂停该项目的维护，我们fork了一份代码进行持续优化改进。

### 功能特性
#### 客户端
- 易于集成，只需要集成两行Spring配置即可。
- 配置本地缓存，配置拉取之后即使服务端宕机，客户端仍然可以使用最后一次拉取到的配置进行服务启动。
- 服务端连接参数支持构造函数、本地properties文件、JVM参数、环境变量多种配置方式，以便于满足不同场景下的集成需求。

#### 服务端
- 提供配置管理网站，通过可视化界面进行配置的管理。支持配置导出、导入(json,properties两种格式)。
- 配置通知机制，服务端配置发生变化后会触发客户端事件，便于配置热修改。
- 支持配置引用，对于公共配置可一处配置多出使用，修改时仅需修改一处。
- 提供客户端连接监控，便于监控客户端连接状态。
- 支持多账号使用，不同账号可分配不同项目权限。
- 除Java客户端外、还支持PHP客户端使用（PHP不支持配置变化事件通知）。

更多使用方式和说明，请参考 [https://github.com/talywy/super-diamond/wiki/Getting-Started](https://github.com/talywy/super-diamond/wiki/Getting-Started "Wiki")

### 系统部分功能截图
![](https://raw.githubusercontent.com/xiake2025/super-diamond/master/image/diamond_1.png "功能截图1")
![](https://raw.githubusercontent.com/xiake2025/super-diamond/master/image/diamond_2.png "功能截图2")

### 用到的组件
- netty4
- SpringMVC
- druid
- fastjson
- junit
- logback

### LICENSE
Copyright 2011-2016 - IFLYTEK, Released under The MIT License (MIT)