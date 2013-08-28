super-diamond
=============

- 配置管理系统提供系统参数配置管理，例如数据库的配置信息等，配置参数修改以后可以实时推送到客户端(基于netty4)，
方便系统动态修改运行参数。
- 可以建多个项目，每个项目分为三种profile（development、test、production）， 能够控制profile 级别的权限。
- 所有参数均由development profile配置，test和production profile继承development profile配置，也可以覆盖其配置。
  test和production profile只提供修改功能。
- client 备份配置信息到本地文件系统，如果server不可用，可以使用本地备份。client 能够定时重连server，保证client高可用。
- client 提供ConfigurationListener，当某个属性发生变化（add、update、clear）, ConfigurationListener能够接收到ConfigurationEvent。
- server 备份配置文件系统系统，如果数据库不用，能够保证对客户端提供数据（待完善）。

系统功能截图：http://melin.iteye.com/picture/127359

项目profile请参考：http://melin.iteye.com/blog/1339060

super-diamond-server
--------------------

super-diamond-server中嵌入tomcat运行，构建部署包：mvn install assembly:single -Pproduction，生成super-diamond-server-1.0.0-SNAPSHOT-bin.tar.gz文件，
解压运行bin/server.sh start命令。

在conf\META-INF\scripts目录中，提供mysql和oracle建表脚本，理论也支持其它数据库，在conf\META-INF\res\config-production.properties文件中修改数据库配置。

super-diamond-client
--------------------

```java
PropertiesConfiguration config = new PropertiesConfiguration("localhost", 5001, "test", "development");
config.addConfigurationListener(new ConfigurationListenerTest());
config.getString("jdbc.url")
```

<b>spring 使用方式</b>

```xml
<bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
	<property name="properties" ref="propertiesConfiguration" />
</bean>
	
<bean id="propertiesConfiguration" class="com.github.diamond.client.PropertiesConfigurationFactoryBean">
	<constructor-arg index="0" value="localhost" />
	<constructor-arg index="1" value="5001" />
	<constructor-arg index="2" value="test" />
	<constructor-arg index="3" value="development" />
</bean>
```

```java
ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
PropertiesConfiguration config = PropertiesConfigurationFactoryBean.getPropertiesConfiguration();
config.getString("jdbc.url")
```
