super-diamond
=============

配置管理系统：提供系统参数配置管理，例如数据库的配置信息等，配置参数修改以后可以实时推送到客户端，方便系统动态修改运行参数。
系统参数配置分为三种profile（development、test、production）.项目profile请参考：http://melin.iteye.com/blog/1339060

super-diamond-server
--------------------

构建部署包：mvn install assembly:single -Pproduction，生成super-diamond-server-1.0.0-SNAPSHOT-bin.tar.gz文件，解压运行bin/server.sh start命令。

super-diamond-client
--------------------

```java
PropertiesConfiguration config = new PropertiesConfiguration("localhost", 5001, "test", "development");
config.getString("jdbc.url")
```

<b>spring 使用方式</b>

```xml
	<bean class="com.github.diamond.client.PropertiesConfigurationFactoryBean">
		<constructor-arg index="0" value="localhost" />
		<constructor-arg index="1" value="5001" />
		<constructor-arg index="2" value="test" />
		<constructor-arg index="3" value="development" />
	</bean>
```