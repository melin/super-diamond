package com.github.diamond.web.model;

/**
 * Created by xzwang on 2015/10/27.
 */
public class ModuleConfigId {
    /**
     *项目名和模块名是否已经存在的标志.
     */
    private boolean exist;

    /**
     *配置ID.
     */
    private int configId;

    /**
      *模块ID.
     */
    private int moduleId;

    public ModuleConfigId(boolean exist, int configId, int moduleId) {
        this.exist = exist;
        this.configId = configId;
        this.moduleId = moduleId;
    }

    public ModuleConfigId() {

    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }
}
