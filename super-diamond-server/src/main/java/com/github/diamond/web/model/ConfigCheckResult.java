package com.github.diamond.web.model;

/**
 * 配置检查结果类
 *
 * Author: yuwang@iflytek.com
 * Date: 2015/11/10 11:24
 */
public class ConfigCheckResult {



    /**
     * 检查是否成功 0表示失败，1和2表示成功，1表示无重复数据，2表示数据重复
     */
    private int checkSuccess;

    /**
     * 检查ID
     */
    private String checkId;

    /**
     * 检查结果消息
     */
    private String message;

    public int getCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(int checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
