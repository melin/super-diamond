package com.github.diamond.web.model;

import java.util.List;

/**
 * 模块定义
 *
 * Author: yuwang@iflytek.com
 * Date: 2015/10/20 13:46
 */
public class Module {

    /**
     * 模块名称
     */
    private String name;

    /**
     * 配置列表
     */
    private List<Config> configs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }
}
