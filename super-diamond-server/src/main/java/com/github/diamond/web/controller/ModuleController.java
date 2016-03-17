/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import com.alibaba.fastjson.JSON;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.Config;
import com.github.diamond.web.model.ConfigCheckResult;
import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.Module;
import com.github.diamond.web.model.ModuleConfigId;
import com.github.diamond.web.model.ModuleIdExist;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ModuleService;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.impl.ConfigServiceImpl;
import com.github.diamond.web.service.impl.ModuleServiceImpl;
import com.github.diamond.web.service.impl.ProjectServiceImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;


/**
 * Create on @2013-8-22 @下午5:25:00
 *
 * @author bsli@ustcinfo.com
 */

@Controller
public class ModuleController extends BaseController {

    private static final HashMap<String, ConfigExportData> IMPORT_CONFIG_MAP = new HashMap<>();
    private static Date DATE = new Date();

    @Autowired
    private ConfigService configService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;

    /**
     * 配置导入检查.
     *
     * @param file      配置文件
     * @param projectId 项目编号
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/module/import/{type}/{projectId}/{currentPage}")
    @ResponseBody
    public String importModuleCheck(@RequestParam("file") MultipartFile file,
                                    @PathVariable int projectId,
                                    MultipartHttpServletRequest request) throws ServletException, IOException {

        final ConfigCheckResult checkResult = new ConfigCheckResult() {
            {
                setCheckId(UUID.randomUUID().toString());
            }
        };
        ConfigExportData exportData = new ConfigExportData();
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        try {
            String exportDataStr;
            if (file.isEmpty()) {
                checkResult.setCheckSuccess(0);
                checkResult.setMessage("导入的文件为空");
            } else {
                byte[] fileBytes = file.getBytes();
                exportDataStr = new String(fileBytes);

                if (file.getOriginalFilename().toString().indexOf(".json") > 0) {
                    exportData = JSON.parseObject(exportDataStr, ConfigExportData.class);
                } else {
                    ArrayList<Module> moduleList = new ArrayList<>();
                    InputStream in = file.getInputStream();
                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);
                    String str;
                    Boolean firstNote = true;
                    Boolean newModuleFlag;
                    Boolean defaultNoteFlag = false;
                    String note = "";
                    ArrayList<Config> configList = null;
                    Module module = null;
                    Config config = null;
                    while ((str = br.readLine()) != null) {
                        if (!"".equals(str)) {
                            int index = str.indexOf("#");
                            if (index == 0) {
                                firstNote = false;
                                String content = str.trim().substring(1);
                                String moduleName = null;
                                if (content.indexOf(":") != -1 && content.indexOf("ModuleName") != -1 && index + content.indexOf("ModuleName") < 3) {
                                    moduleName = "ModuleName".equals(content.substring(0, content.indexOf(":")).trim())
                                            || "ModuleName".equals(content.substring(0, content.indexOf("：")).trim())
                                            ? content.substring(content.indexOf(":") + 1).trim() : null;
                                }
                                if (content.indexOf("IS_NOT_SHOW") != -1 && index + content.indexOf("IS_NOT_SHOW") < 3) {
                                    defaultNoteFlag = true;
                                    config = new Config();
                                    config.setIsShow(false);
                                }
                                if (moduleName != null) {
                                    newModuleFlag = true;
                                    if (module != null) {
                                        module.setConfigs(configList);
                                        moduleList.add(module);
                                        module = null;
                                    }
                                } else {
                                    newModuleFlag = false;
                                }
                                if (newModuleFlag) {
                                    module = new Module();
                                    module.setName(moduleName);
                                    configList = new ArrayList<>();
                                } else {
                                    if (!defaultNoteFlag) {
                                        note += str.substring(1);
                                    }
                                }
                            } else {
                                if (!firstNote) {
                                    if (defaultNoteFlag) {
                                        config.setDescription(note);
                                        note = "";
                                        config.setKey(str.substring(0, str.indexOf("=")));
                                        config.setValue(str.substring(str.indexOf("=") + 1));
                                        configList.add(config);
                                        config = null;
                                        defaultNoteFlag = false;
                                    } else {
                                        config = new Config();
                                        config.setDescription(note);
                                        note = "";
                                        config.setKey(str.substring(0, str.indexOf("=")));
                                        config.setValue(str.substring(str.indexOf("=") + 1));
                                        configList.add(config);
                                        config = null;
                                    }
                                } else {
                                    //todo 抛异常
                                }
                            }
                        }
                    }
                    if (module != null) {
                        module.setConfigs(configList);
                        moduleList.add(module);
                    }
                    exportData.setModules(moduleList);
                }
                DATE = new Date();

                List<Map<String, String>> saveRepeatData = new ArrayList<>();
                ArrayList<Module> modules = exportData.getModules();

                for (Module moduleTmp : modules) {
                    ArrayList<Config> configs = moduleTmp.getConfigs();
                    String moduleName = moduleTmp.getName();          //得到模型name
                    for (Config configTmp : configs) {
                        String configKey = configTmp.getKey();
                        ModuleConfigId moduleConfigId = moduleService.moduleConfigIdIsExist(configKey, moduleName, projectId);
                        if (moduleConfigId.isExist()) { //该模型与配置已经存在,将重复的数据添加到saveRepeatData变量中
                            Map<String, String> singleRepeatData = new HashMap<>();

                            singleRepeatData.put(moduleName, configKey);
                            saveRepeatData.add(singleRepeatData);
                            //configService.updateConfig(type, moduleConfigId.getProjectId(), configKey, configValue, configDesc,
                            // projectId, moduleConfigId.getModuleId(), user.getUserCode());
                        }  //该模型与配置还不存在
                        //configService.insertConfig(configKey, configValue, configDesc, projectId,
                        // moduleConfigId.getModuleId(), user.getUserCode());
                    }
                }
                if (saveRepeatData.size() != 0) {
                    String message = "重复的配置信息如下：" + "\n";
                    for (Map<String, String> m : saveRepeatData) {
                        for (String key : m.keySet()) {
                            message += ("模块名：" + key + "\t" + "配置名：" + m.get(key) + "\t" + "\n");
                        }
                    }
                    checkResult.setCheckSuccess(1);
                    checkResult.setMessage(message);
                } else {
                    for (Module moduleTmp : modules) {
                        ArrayList<Config> configs = moduleTmp.getConfigs();
                        String moduleName = moduleTmp.getName();          //得到模型name
                        for (Config configTmp : configs) {
                            String configKey = configTmp.getKey();
                            String configValue = configTmp.getValue();
                            String configDesc = configTmp.getDescription();
                            ModuleIdExist moduleIdExist = moduleService.moduleIdIsExist(moduleName, projectId);
                            if (moduleIdExist.isExist()) {
                                configService.insertConfig(configKey, configValue, configDesc, configTmp.getIsShow(),
                                        projectId, moduleIdExist.getModuleId(), user.getUserCode());
                            } else {
                                int moduleId = moduleService.save(projectId, moduleName);
                                configService.insertConfig(configKey, configValue, configDesc, configTmp.getIsShow(),
                                        projectId, moduleId, user.getUserCode());
                            }
                        }
                    }
                    checkResult.setCheckSuccess(2);
                }
            }
        } catch (Exception ex) {
            checkResult.setCheckSuccess(0);
            checkResult.setMessage(ex.getMessage());
        }

        if (checkResult.getCheckSuccess() == 1) {
            IMPORT_CONFIG_MAP.put(checkResult.getCheckId(), exportData);
        }
        return URLEncoder.encode(JSON.toJSONString(checkResult), "utf-8");
        //return JSON.toJSONString(checkResult);
    }

    @RequestMapping("/module/import/perform/{checkId}/{operation}/{projectId}/{type}")
    @ResponseBody
    public String importModulePerform(@PathVariable String checkId,
                                      @PathVariable int operation,
                                      @PathVariable int projectId,
                                      @PathVariable String type,
                                      HttpSession session) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        Date dateNow = new Date();
        if ((dateNow.getTime() - DATE.getTime()) % nd % nh / nm >= 10) {
            IMPORT_CONFIG_MAP.remove(checkId);
        }

        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        ConfigExportData exportData = IMPORT_CONFIG_MAP.get(checkId);
        if (exportData == null) {
            return "error: import data is null";
        } else {
            if (operation == 1) {
                ArrayList<Module> modules = exportData.getModules();
                for (Module module : modules) {
                    ArrayList<Config> configs = module.getConfigs();
                    String moduleName = module.getName();          //得到模型name
                    for (Config config : configs) {
                        String configKey = config.getKey();
                        String configValue = config.getValue();
                        String configDesc = config.getDescription();
                        ModuleConfigId moduleConfigId = moduleService.moduleConfigIdIsExist(configKey, moduleName, projectId);
                        ModuleIdExist moduleIdExist = moduleService.moduleIdIsExist(moduleName, projectId);
                        if (!moduleConfigId.isExist()) { //找出不存在的配置，执行插入操作
                            if (moduleIdExist.isExist()) {
                                configService.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
                                        projectId, moduleIdExist.getModuleId(), user.getUserCode());
                            } else {
                                int moduleId = moduleService.save(projectId, moduleName);
                                configService.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
                                        projectId, moduleId, user.getUserCode());
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
                        ModuleConfigId moduleConfigId = moduleService.moduleConfigIdIsExist(configKey, moduleName, projectId);
                        ModuleIdExist moduleIdExist = moduleService.moduleIdIsExist(moduleName, projectId);
                        if (moduleConfigId.isExist()) {
                            configService.updateConfig(type, moduleConfigId.getConfigId(), configKey, configValue,
                                    configDesc, config.getIsShow(), projectId, moduleConfigId.getModuleId(),
                                    user.getUserCode());
                        } else { //找出不存在的配置，执行插入操作
                            if (moduleIdExist.isExist()) {
                                configService.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
                                        projectId, moduleIdExist.getModuleId(), user.getUserCode());
                            } else {
                                int moduleId = moduleService.save(projectId, moduleName);
                                configService.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
                                        projectId, moduleId, user.getUserCode());
                            }
                        }
                    }
                }
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
    public String save(String type, int projectId, String name) {
        moduleService.save(projectId, name);
        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/module/delete/{type}/{projectId}/{moduleId}")
    public String delete(@PathVariable String type, @PathVariable int projectId,
                         @PathVariable int moduleId, HttpSession session) {
        boolean result = moduleService.delete(moduleId, projectId);
        if (!result) {
            session.setAttribute("message", "模块已经被配置项关联，不能删除！");
            return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + moduleId;
        } else {
            session.setAttribute("message", "删除成功！");
            return "redirect:/profile/" + type + "/" + projectId;
        }
    }

    @RequestMapping("/module/exportJson/{type}/{projectId}/{userName}/{moduleIds}")
    @ResponseBody
    public String exportJson(@PathVariable String type, @PathVariable int projectId, @PathVariable String userName, @PathVariable int[] moduleIds) {

        ConfigExportData configExportData = projectService.getConfigExportData(projectId, userName);

        List<Map<String, Object>> getModuleData = moduleService.getModuleConfigData(projectId, moduleIds, type);

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
                    config.setIsShow((Integer) getModuleData.get(j).get("IS_SHOW") > 0 ? true : false);
                    if ("development".equals(type)) {
                        config.setValue(getModuleData.get(j).get("CONFIG_VALUE").toString());
                    } else if ("test".equals(type)) {
                        config.setValue(getModuleData.get(j).get("TEST_VALUE").toString());
                    } else if ("build".equals(type)) {
                        config.setValue(getModuleData.get(j).get("BUILD_VALUE").toString());
                    } else {
                        config.setValue(getModuleData.get(j).get("PRODUCTION_VALUE").toString());
                    }
                    config.setDescription(getModuleData.get(j).get("CONFIG_DESC").toString());
                    module.getConfigs().add(config);
                }
            }
        }
        String json = JSON.toJSONString(configExportData, true);
        return json;
    }

    @RequestMapping("/module/exportProperties/{type}/{projectId}/{userName}/{moduleIds}")
    @ResponseBody
    public String exportProperties(@PathVariable String type,
                                   @PathVariable int projectId,
                                   @PathVariable String userName,
                                   @PathVariable int[] moduleIds) {

        String propertiesString = "";

        List<Map<String, Object>> getModuleData = moduleService.getModuleConfigData(projectId, moduleIds, type);

        for (int i = 0; i < moduleIds.length; i++) {
            String moduleName = "";
            for (int j = 0; j < getModuleData.size(); j++) {
                if (moduleIds[i] == Integer.parseInt(getModuleData.get(j).get("MODULE_ID").toString())) {
                    if (!(getModuleData.get(j).get("MODULE_NAME").toString().equals(moduleName))) {
                        propertiesString += "\n";
                        moduleName = getModuleData.get(j).get("MODULE_NAME").toString();
                        propertiesString += "\n" + "#ModuleName : " + moduleName;
                    }
                    if ("development".equals(type)) {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            propertiesString += "\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#");
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            propertiesString += "\n" + "#IS_NOT_SHOW";
                        }
                        propertiesString += ("\n" + getModuleData.get(j).get("CONFIG_KEY").toString()
                                + "=" + getModuleData.get(j).get("CONFIG_VALUE").toString());
                    } else if ("test".equals(type)) {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            propertiesString += "\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#");
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            propertiesString += "\n" + "#IS_NOT_SHOW";
                        }
                        propertiesString += ("\n" + getModuleData.get(j).get("CONFIG_KEY").toString() + "="
                                + getModuleData.get(j).get("TEST_VALUE").toString());
                    } else if ("build".equals(type)) {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            propertiesString += "\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#");
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            propertiesString += "\n" + "#IS_NOT_SHOW";
                        }
                        propertiesString += ("\n" + getModuleData.get(j).get("CONFIG_KEY").toString() + "="
                                + getModuleData.get(j).get("BUILD_VALUE").toString());
                    } else {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            propertiesString += "\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#");
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            propertiesString += "\n" + "#IS_NOT_SHOW";
                        }
                        propertiesString += ("\n" + getModuleData.get(j).get("CONFIG_KEY").toString()
                                + "=" + getModuleData.get(j).get("PRODUCTION_VALUE").toString());
                    }
                }
            }
        }
        propertiesString += "\n";
        return propertiesString;
    }
}
