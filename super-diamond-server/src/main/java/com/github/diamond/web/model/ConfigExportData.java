package com.github.diamond.web.model;

import java.util.ArrayList;
import java.util.Date;

/**
 * 项目导出数据定义
 * <p/>
 * Author: yuwang@iflytek.com
 * Date: 2015/10/20 13:50
 */
public class ConfigExportData {

    /**
     * 执行导出操作的用户.
     */
    private String exportUser;

    /**
     * 导出的时间.
     */
    private Date exportTime;

    /**
     * 项目编码.
     */
    private String projectCode;

    /**
     * 项目描述.
     */
    private String projectDesc;

    /**
     * 配置版本.
     */
    private String configVer;

    /**
     * 服务器IP.
     */
    private String serverIp;

    /**
     * 配置模块列表.
     */
    private ArrayList<Module> modules;

    public ConfigExportData() {
    }

    public ConfigExportData(String exportUser, Date exportTime, String projectCode, String projectDesc, String configVer, String serverIp) {
        this.exportUser = exportUser;
        this.exportTime = exportTime;
        this.projectCode = projectCode;
        this.projectDesc = projectDesc;
        this.configVer = configVer;
        this.serverIp = serverIp;
        this.modules = new ArrayList<>();
    }

    public ArrayList<Module> getModules() {
        return modules;
    }

    public void setModules(ArrayList<Module> modules) {
        this.modules = modules;
    }

    public String getExportUser() {
        return exportUser;
    }

    public void setExportUser(String exportUser) {
        this.exportUser = exportUser;
    }

    public Date getExportTime() {
        return exportTime;
    }

    public void setExportTime(Date exportTime) {
        this.exportTime = exportTime;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public String getConfigVer() {
        return configVer;
    }

    public void setConfigVer(String configVer) {
        this.configVer = configVer;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
