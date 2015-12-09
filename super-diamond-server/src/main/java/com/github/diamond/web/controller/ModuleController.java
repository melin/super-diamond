/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */
package com.github.diamond.web.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.*;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.web.service.ModuleService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.List;


/**
 * Create on @2013-8-22 @下午5:25:00
 *
 * @author bsli@ustcinfo.com
 */

@Controller
public class ModuleController extends BaseController {

    private final static HashMap<String, ConfigExportData> IMPORT_CONFIG_MAP = new HashMap<>();
    private static Date DATE = new Date();

    @Autowired
    private ConfigService configService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;

    /**
     * 配置导入检查
     *
     * @param file      配置文件
     * @param projectId 项目编号
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/module/import/{type}/{projectId}/{currentPage}")
    @ResponseBody
    public String importModuleCheck(@RequestParam("file") MultipartFile file, @PathVariable long projectId) throws ServletException, IOException {

        final ConfigCheckResult checkResult = new ConfigCheckResult() {{
            setCheckId(UUID.randomUUID().toString());
        }};
        ConfigExportData exportData = null;
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        try {
            String exportDataStr;
            if (file.isEmpty()) {
                checkResult.setCheckSuccess(0);
                checkResult.setMessage("导入的文件为空");
            } else {
                byte[] fileBytes = file.getBytes();
                exportDataStr = new String(fileBytes);

                exportData = JSON.parseObject(exportDataStr, ConfigExportData.class);

                DATE = new Date();

                List<Map<String, String>> saveRepeatData = new ArrayList<>();
                ArrayList<Module> modules = exportData.getModules();

                // TODO： 一次性取出来进行对比
                for (Module module : modules) {
                    ArrayList<Config> configs = module.getConfigs();
                    String moduleName = module.getName();          //得到模型name
                    for (Config config : configs) {
                        String configKey = config.getKey();
                        ModuleConfigId moduleConfigId = moduleService.isExist(configKey, moduleName);
                        if (moduleConfigId.isExist()) { //该模型与配置已经存在,将重复的数据添加到saveRepeatData变量中
                            Map<String, String> singleRepeatData = new HashMap<>();

                            singleRepeatData.put(moduleName, configKey);
                            saveRepeatData.add(singleRepeatData);

                            //configService.updateConfig(type, moduleConfigId.getProjectId(), configKey, configValue, configDesc, projectId, moduleConfigId.getModuleId(), user.getUserCode());
                        }  //该模型与配置还不存在

                    }
                }

                if (saveRepeatData.size() != 0) {
                    String message = "重复的配置信息如下：" + "\n";
                    for (Map<String, String> m : saveRepeatData) {
                        for (String key : m.keySet())
                            message += ("模块名：" + key + "  " + "配置名：" + m.get(key) + "  " + "\n");
                    }
                    checkResult.setCheckSuccess(1);
                    checkResult.setMessage(message);
                    { // TODO: 处理新增模块的记录
                        for (Module module : modules) {
                            ArrayList<Config> configs = module.getConfigs();
                            String moduleName = module.getName();          //得到模型name
                            for (Config config : configs) {
                                String configKey = config.getKey();
                                String configValue = config.getValue();
                                String configDesc = config.getDescription();
                                ModuleIdExist moduleIdExist = moduleService.isExist(moduleName, projectId);
                                if (moduleIdExist.isExist()) {
                                    configService.insertConfig(configKey, configValue, configDesc, projectId, moduleIdExist.getModuleId(), user.getUserCode());
                                } else {
                                    long moduleId = moduleService.save(projectId, moduleName);
                                    configService.insertConfig(configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
                                }
                            }
                        }
                        checkResult.setCheckSuccess(2);
                    }
                }
            }
        } catch (Exception ex) {
            checkResult.setCheckSuccess(0);
            checkResult.setMessage(ex.getMessage());
        }

        if (checkResult.getCheckSuccess() == 1) {
            IMPORT_CONFIG_MAP.put(checkResult.getCheckId(), exportData);
        }

        return JSON.toJSONString(checkResult);
    }

    @RequestMapping("/module/import/perform/{checkId}/{operation}/{projectId}/{type}")
    @ResponseBody
    public String importModulePerform(@PathVariable String checkId, @PathVariable int operation, @PathVariable long projectId, @PathVariable String type, HttpSession session) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        Date dateNow = new Date();
        if ((dateNow.getTime() - DATE.getTime()) % nd % nh / nm >= 10) { // TODO: 新增一个计时器，定时删除不再访问的配置检查数据，设定的时间为十分钟
            IMPORT_CONFIG_MAP.remove(checkId);
        }

        ConfigExportData exportData = IMPORT_CONFIG_MAP.get(checkId);
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        if (exportData == null) {
            return "error import data is null";
        } else {// TODO: 执行具体的操作，插入数据库什么的,注意用事务保证数据可以整体操作成功
            if (operation == 1) {
                ArrayList<Module> modules = exportData.getModules();
                for (Module module : modules) {
                    ArrayList<Config> configs = module.getConfigs();
                    String moduleName = module.getName();          //得到模型name
                    for (Config config : configs) {
                        String configKey = config.getKey();
                        String configValue = config.getValue();
                        String configDesc = config.getDescription();
                        ModuleConfigId moduleConfigId = moduleService.isExist(configKey, moduleName);
                        ModuleIdExist moduleIdExist = moduleService.isExist(moduleName, projectId);
                        if (!moduleConfigId.isExist()) { //找出不存在的配置，执行插入操作
                            if (moduleIdExist.isExist()) {
                                configService.insertConfig(configKey, configValue, configDesc, projectId, moduleIdExist.getModuleId(), user.getUserCode());

                            } else {
                                long moduleId = moduleService.save(projectId, moduleName);
                                configService.insertConfig(configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
                            }
                        }
                    }
                }
                IMPORT_CONFIG_MAP.remove(checkId);
                session.setAttribute("message", "导入成功");
                return "{\"data\":\"success\"}";
            } else if (operation == 2) {
                ArrayList<Module> modules = exportData.getModules();
                for (Module module : modules) {
                    ArrayList<Config> configs = module.getConfigs();
                    String moduleName = module.getName();          //得到模型name
                    for (Config config : configs) {
                        String configKey = config.getKey();
                        String configValue = config.getValue();
                        String configDesc = config.getDescription();
                        ModuleConfigId moduleConfigId = moduleService.isExist(configKey, moduleName);
                        ModuleIdExist moduleIdExist = moduleService.isExist(moduleName, projectId);
                        if (moduleConfigId.isExist()) {
                            configService.updateConfig(type, moduleConfigId.getConfigId(), configKey, configValue, configDesc, projectId, moduleConfigId.getModuleId(), user.getUserCode());
                        } else { //找出不存在的配置，执行插入操作
                            if (moduleIdExist.isExist()) {
                                configService.insertConfig(configKey, configValue, configDesc, projectId, moduleIdExist.getModuleId(), user.getUserCode());
                            } else {
                                long moduleId = moduleService.save(projectId, moduleName);
                                configService.insertConfig(configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
                            }
                        }
                    }
                }
                ;
                IMPORT_CONFIG_MAP.remove(checkId);
                session.setAttribute("message", "导入成功");
                return "{\"data\":\"success\"}";
            } else {
                IMPORT_CONFIG_MAP.remove(checkId);
                return "{\"data\":\"success\"}";
            }
        }
    }

    @RequestMapping("/module/save")
    public String save(String type, Long projectId, String name) {
        moduleService.save(projectId, name);
        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/module/delete/{type}/{projectId}/{moduleId}")
    public String delete(@PathVariable String type, @PathVariable Long projectId,
                         @PathVariable Long moduleId, HttpSession session) {
        boolean result = moduleService.delete(moduleId, projectId);
        if (!result) {
            session.setAttribute("message", "模块已经被配置项关联，不能删除！");
            return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + moduleId;
        } else {
            session.setAttribute("message", "删除成功！");
            return "redirect:/profile/" + type + "/" + projectId;
        }
    }


    @RequestMapping("/module/export/{type}/{projectId}/{userName}/{moduleIds}")
    @ResponseBody
    public String export(HttpServletResponse response, @PathVariable String type, @PathVariable long projectId, @PathVariable String userName, @PathVariable long[] moduleIds) {

        ConfigExportData configExportData = projectService.getConfigExportData(projectId, userName);

        List<Map<String, Object>> getModuleData = moduleService.getModuleConfigData(projectId, moduleIds);

        for (int i = 0; i < moduleIds.length; i++) {
            Module module = new Module();
            module.setConfigs(new ArrayList<Config>());
            for (int j = 0; j < getModuleData.size(); j++) {
                if (moduleIds[i] == Integer.parseInt(getModuleData.get(j).get("MODULE_ID").toString())) {
                    if (!(getModuleData.get(j).get("MODULE_NAME").toString().equals(module.getName()))) {
                        module.setName(getModuleData.get(j).get("MODULE_NAME").toString());
                        configExportData.getModules().add(module);
                    }
                    Config config = new Config();
                    config.setKey(getModuleData.get(j).get("CONFIG_KEY").toString());
                    config.setValue(getModuleData.get(j).get("CONFIG_VALUE").toString());
                    config.setDescription(getModuleData.get(j).get("CONFIG_DESC").toString());
                    try {
                        module.getConfigs().add(config);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        String json = JSON.toJSONString(configExportData, true);
        return json;
    }
}
