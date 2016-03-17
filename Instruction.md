super-diamond
=============

- 配置管理系统提供系统参数配置管理，例如数据库的配置信息等，配置参数修改以后可以实时推送到客户端(基于netty4)，
方便系统动态修改运行参数。
- 可以建多个项目，每个项目分为三种profile（development、test、production）， 能够控制profile 级别的权限。
- 所有参数均由development profile配置，test和production profile继承development profile配置，也可以覆盖其配置。
  test和production profile只提供修改功能。
- client备份配置信息到本地文件系统，如果server不可用，可以使用本地备份。client 能够定时重连server，保证client高可用。
- client支持自定义备份文件的本地路径。
- client支持本地环境变量解析替换功能。
- client 提供ConfigurationListener，当某个属性发生变化（add、update、clear）, ConfigurationListener能够接收到ConfigurationEvent。
- server 备份配置文件系统系统，如果数据库不用，能够保证对客户端提供数据（待完善）。
- server 支持配置跨工程替换（包含对加解密的处理）
- 支持php项目从superdiamond中获取配置参数。
- 支持服务端配置的导入导出功能（目前导入导出的格式为Json, properties）。
- 支持管理页面上配置列表中关键配置的隐藏与显示功能以及控制列表显示的记录数量。
- 支持项目中界面操作配置项更换模块。

系统功能截图：
![](https://raw.githubusercontent.com/melin/super-diamond/master/diamond.png "功能截图")
![](https://raw.githubusercontent.com/xiake2025/super-diamond/master/image.png "配置导出")

项目profile请参考：http://melin.iteye.com/blog/1339060

super-diamond-server 安装
--------------------
1. 下载super-diamond代码： git clone https://github.com/melin/super-diamond.git
2. 进入super-diamond目录，构建super-diamond父工程： mvn install
3. super-diamond-server中嵌入jetty运行，构建部署包：mvn install assembly:single -Pproduction，生成super-diamond-server-${version}-bin.tar.gz文件，
解压运行bin/server.sh start命令。
4. 在conf\META-INF\scripts目录中，提供mysql和oracle建表脚本，理论也支持其它数据库，在conf\META-INF\res\config-production.properties文件中修改数据库配置。
5. 在conf_user表中添加用户admin，密码000000的加密值为：670b14728ad9902aecba32e22fa4f6bd, mysql脚本：
insert into conf_user(id,USER_code,USER_NAME,PASSWORD,CREATE_TIME) values(1,'admin','admin','670b14728ad9902aecba32e22fa4f6bd',current_timestamp() );  
commit;  
6. 访问super-diamond-server，jetty默认端口为8090，可以在：conf/META-INF/res/jetty.properties中修改。
	http://localhost:8090/superdiamond

super-diamond-client
--------------------

客户端参考apache configuration，实现其中的部分功能。例如：
```java
public class PropertiesConfigurationTest {

	@Test
	public void testConfig() throws ConfigurationRuntimeException  {
		String config = "username = melin \r\n";
		config += "port=8000 \r\n";
		config += "reload=true \r\n";

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);

		Assert.assertEquals("melin", configuration.getString("username"));
		Assert.assertEquals(8000, configuration.getInt("port"));
		Assert.assertTrue(configuration.getBoolean("reload"));
	}

	@Test
	public void testInterpolator() throws ConfigurationRuntimeException  {
		String config = "app.home = /tmp/home \r\n";
		config += "zk.home=${app.home}/zk \r\n";
		config += "hbase.home=${app.home}/hbase \r\n";

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);

		Assert.assertEquals("/tmp/home", configuration.getString("app.home"));
		Assert.assertEquals("/tmp/home/zk", configuration.getString("zk.home"));
		Assert.assertEquals("/tmp/home/hbase", configuration.getString("hbase.home"));
	}

	@Test
	public void testSysProperties() throws ConfigurationRuntimeException  {
		String config = "javaVersion = ${sys:java.version} \r\n";

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);

		Assert.assertEquals(System.getProperty("java.version"), configuration.getString("javaVersion"));
	}

	@Test
	public void testSysEvns() throws ConfigurationRuntimeException  {
		String config = "javaHome = ${env:JAVA_HOME}/lib \r\n";

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load(config);

		Assert.assertEquals(System.getenv("JAVA_HOME") + "/lib", configuration.getString("javaHome"));
	}
}
```

客户端连接服务器端方式：
----------------------

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

客户端链接服务的参数projcode、profile、host和port可以通过环境变量和jvm参数两种方式设置，避免固定在工程配置文件中。

```shell
export SUPERDIAMOND_PROJCODE=javademo
export SUPERDIAMOND_PROFILE=production
export SUPERDIAMOND_MODULES=jdbc,common #多个模块之用逗号分隔，可以设置为空，获取所有模块配置。
export SPUERDIAMOND_HOST=192.168.0.1
export SPUERDIAMOND_PORT=8283
```
或者
```shell
 -Dsuperdiamond.projcode=javademo -Dsuperdiamond.profile=production -Dspuerdiamond.host=127.0.0.1 -Dspuerdiamond.port=8283 
```

```xml
<bean id="propertiesConfiguration" class="com.github.diamond.client.PropertiesConfigurationFactoryBean">
	<constructor-arg index="0" value="test" />
	<constructor-arg index="1" value="development" />
	<constructor-arg index="2" value="development" />
</bean>
```

```java
ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean.xml");
PropertiesConfiguration config = PropertiesConfigurationFactoryBean.getPropertiesConfiguration();
config.getString("jdbc.url")
```

Rest 接口获取配置：
----------------------
通过http获取配置信息，http url格式为：

- properties格式

	http://host:port/superdiamond/preview/${项目编码}/${profile}
	http://host:port/superdiamond/preview/${项目编码}/${module}[,${module}]/${profile} //支持设置多个module值，用逗号分割
	http://host:port/superdiamond/preview/${项目编码}/${module}/${key}/${profile}
- php config格式

	http://host:port/superdiamond/preview/${项目编码}/${profile}?format=php
	http://host:port/superdiamond/preview/${项目编码}/${module}[,${module}]/${profile}? format=php  //支持设置多个module值，用逗号分割
- json config格式

	http://host:port/superdiamond/preview/${项目编码}/${profile}?format=json 
	http://host:port/superdiamond/preview/${项目编码}/${module}[,${module}]/${profile}?  //支持设置多个module值，用逗号分割format=json


PHP项目应用：
----------------------
结合Phing从superdiamond获取配置参数。完整的phing build.xml配置请参考：
https://gist.github.com/melin/fa4818acc9fd55666b77

```xml
<!--
    Target: config 
    Description: 通过http方式从superdiamond中获取系统配置参数信息。
    development profile 获取配置存放在Application/Common/Conf/user-config.php文件中（ThinkPHP 3.2）
    test & production profile 获取配置存放在build/user-config.php文件中，在执行build target时，
    复制build/user-config.php文件中build/Application/Common/Conf/user-config.php位置
-->
<target name="config" depends="profile">
    <if>
        <equals arg1="development" arg2="${project.profile}" trim="true" />
        <then>
            <httpget url="http://172.16.81.73:8001/superdiamond/preview/App.EduSNS/${project.profile}?format=php"
                     dir="Application/Common/Conf" filename="user-config.php" />
        </then>
        <else>
            <httpget url="http://172.16.81.73:8001/superdiamond/preview/App.EduSNS/${project.profile}?format=php"
                     dir="build" filename="user-config.php" />
        </else>
    </if>
</target>
```
