package com.github.diamond.web.model;

import java.util.ArrayList;
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
    private ArrayList<Config> configs;
    public Module()
    {

    }

    public Module(String name)
    {
        this.name=name;
        this.configs=new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(ArrayList<Config> configs) {
        this.configs = configs;
    }
}
