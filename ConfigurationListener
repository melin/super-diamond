/**
     * superdiamond动态监听配置文集的修改
     */
    class ConfigChangeHandler extends ApplicationObjectSupport implements ConfigurationListener {

        private final Map<String, List<String>> classInfo = new ConcurrentHashMap<String, List<String>>();
        private final Logger logger = LoggerFactory.getLogger(com.springtour.app.config.ConfigChangeHandler.class);
        private final boolean useClassCache = false;
        private boolean beanScaned = false;

        private boolean inited = false;

        /**
         * 对应在spring的xml文件中注入,至于对于superdiamond的地址，用户和密码等只需要写在配置文件中，会自动去发现配置，无需注入到spring中
         * <bean id="configChangeHandle" class="com.ynhuang.test.ConfigChangeHandler"/>
         *
         * 	<bean id="propertiesConfiguration" class="com.github.diamond.client.PropertiesConfigurationFactoryBean">
         * 		<constructor-arg  type="java.util.List">
         * 			<list>
         * 				<ref bean="configChangeHandle"/>
         * 			</list>
         * 		</constructor-arg>
         * 	</bean>
         */
        public ConfigChangeHandler() {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    logger.info("开启配置变更事件处理。");
                    ApplicationContext context = getApplicationContext();
                    while(context == null){
                        try{
                            Thread.sleep(300);
                        }catch (Exception e) {}
                        context = getApplicationContext();
                    }
                    inited = true;
                    logger.info("配置变更事件初始化完成。");
                }
            }).start();

        }

        @Override
        public void configurationChanged(ConfigurationEvent event) {
            String propertyName = event.getPropertyName();
            Object propertyValue = event.getPropertyValue();
            ApplicationContext context = getApplicationContext();
            logger.info("收到配置变更事件："+propertyName+"->"+propertyValue);
            if(inited && context != null){
                if(context instanceof AbstractRefreshableApplicationContext){
                    ConfigurableListableBeanFactory beanFactory
                            = ((AbstractRefreshableApplicationContext) context).getBeanFactory();
                    if(useClassCache){
                        if(!beanScaned){
                            synchronized (this) {
                                if(!beanScaned){
                                    String[] names = beanFactory.getBeanDefinitionNames();
                                    for (String name : names) {
                                        if(isVailedBeanName(name)){
                                            processBean(name,context, propertyName, propertyValue);
                                        }
                                    }
                                    beanScaned = true;
                                }
                            }
                        }
                        //从缓存里检查类
                        Set<String> cachedBean = classInfo.keySet();
                        for (String name : cachedBean) {
                            if(isVailedBeanName(name)){
                                try {
                                    processBean(name,context, propertyName, propertyValue);
                                } catch (BeansException e) {
                                    logger.warn("无法访问bean:"+name,e);
                                } catch (Exception e) {
                                    logger.warn("设置bean属性出错:"+name,e);
                                }
                            }
                        }
                    }
                }
            }else{
                logger.info("配置变更监听初始化未完成。无法处理变动");
            }
        }

        private boolean isVailedBeanName(String name){
            if(name==null || name.trim().length()<1){
                return false;
            }else{
                String str=name.toLowerCase();
                return !name.startsWith("org.")&&!str.contains("proxy")&&!str.contains("factory");
            }
        }

        private void processBean(String name,ApplicationContext ctx,String propName,Object propValue){
            Object bean = ctx.getBean(name);
            if(bean.getClass().getAnnotation(AutoUpdateConfigBean.class) != null){
                if(useClassCache){
                    List<String> fieldNames = classInfo.get(name);
                    if(fieldNames==null){
                        fieldNames = new ArrayList<String>();
                        classInfo.put(name, new ArrayList<String>());
                    }
                }
                if(propName!=null && propValue!=null ){
                    Field[] fields = bean.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        AutoUpdateConfigField anno = field.getAnnotation(AutoUpdateConfigField.class);
                        if(anno !=null){
                            if(propName.equals(anno.propName())){
                                logger.debug("发现bean:"+ name +" " +bean.getClass().getName());
                                field.setAccessible(true);
                                String type = field.getType().toString();
                                try {
                                    if (type.endsWith("String")) {
                                        field.set(bean, propValue);
                                    } else if (type.endsWith("int") || type.endsWith("Integer")) {
                                        field.set(bean,Integer.parseInt((String)propValue));
                                    } else if (type.endsWith("boolean") || type.endsWith("Boolean")) {
                                        field.set(bean,Boolean.parseBoolean((String)propValue));
                                    } else {

                                    }
                                } catch (Exception e) {
                                    logger.error("配置变更设置bean["+name+"]属性发生异常", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
