package com.github.diamond.web.model;

/**
 * 配置定义.
 * <p/>
 * Author: yuwang@iflytek.com
 * Date: 2015/10/20 13:46
 */
public class Config {

    /**
     * 配置key.
     */
    private String key;

    /**
     * 配置值.
     */
    private String value;

    /**
     * 配置描述.
     */
    private String description;

    private boolean isShow;

    public Config() {
        this.isShow = true;
    }

    public Config(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsShow() {
        return isShow;
    }

    public void setIsShow(boolean isShow) {
        this.isShow = isShow;
    }
}
