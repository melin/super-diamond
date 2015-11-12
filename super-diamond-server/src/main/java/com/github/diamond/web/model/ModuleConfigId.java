package com.github.diamond.web.model;

/**
 * Created by xzwang on 2015/10/27.
 */
public class ModuleConfigId {
    /*
    项目名和模块名是否已经存在的标志
     */
    private boolean exist;

    /*
    配置ID
     */
    private Long ConfigId;

    /*
    模块ID
     */
    private Long moduleId;

    public ModuleConfigId(boolean exist, Long ConfigId, Long moduleId)
    {
        this.exist=exist;
        this.ConfigId=ConfigId;
        this.moduleId=moduleId;
    }
    public ModuleConfigId()
    {

    }
    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public Long getConfigId() {
        return ConfigId;
    }

    public void setConfigId(Long configId) {
        this.ConfigId = configId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
}
