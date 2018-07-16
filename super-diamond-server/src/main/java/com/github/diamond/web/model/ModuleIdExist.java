package com.github.diamond.web.model;

/**
 * Created by xzwang on 2015/11/10.
 */
public class ModuleIdExist {

    /**
     * 判断moduleId是否存在.
     */

    private boolean isExist;


    /**
     * 模型Id.
     */
    private int moduleId;

    public ModuleIdExist(boolean isExist, int moduleId) {
        this.isExist = isExist;
        this.moduleId = moduleId;
    }

    public boolean isExist() {
        return isExist;
    }

    public void setIsExist(boolean isExist) {
        this.isExist = isExist;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }
}
