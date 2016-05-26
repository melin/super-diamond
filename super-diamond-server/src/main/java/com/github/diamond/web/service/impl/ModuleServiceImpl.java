/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.dao.ConfigDao;
import com.github.diamond.web.dao.ModuleDao;
import com.github.diamond.web.model.Config;
import com.github.diamond.web.model.ConfigCheckResult;
import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.Module;
import com.github.diamond.web.model.ModuleConfigId;
import com.github.diamond.web.model.ModuleIdExist;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ModuleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Create on @2013-8-21 @下午8:18:44
 *
 * @author bsli@ustcinfo.com
 */
@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleDao moduleDao;

    @Autowired
    private ConfigDao configDao;

    public List<Map<String, Object>> queryModules(int projectId) {
        return moduleDao.queryModules(projectId);
    }

    public int save(int projectId, String name) {
        return moduleDao.save(projectId, name);
    }

    public String findName(int moduleId) {
        return moduleDao.findName(moduleId);
    }

    public boolean delete(int moduleId, int projectId) {
        return moduleDao.delete(moduleId, projectId);
    }

    public ModuleConfigId configIdIsExist(String configName, String moduleName, int projectId) {
        return moduleDao.configIdIsExist(configName, moduleName, projectId);
    }


    public ModuleIdExist moduleIdIsExist(String moduleName, int projectId) {
        boolean isExist = false;
        List<Integer> moduleIdList = moduleDao.getModuleIdList(moduleName, projectId);
        if (moduleIdList.size() != 0 && moduleIdList.get(0) != null) {
            isExist = true;
            return new ModuleIdExist(isExist, moduleIdList.get(0));
        }
        return new ModuleIdExist(isExist, 0);

    }

    public void fillConfigExportJsonData(int projectId, int[] moduleIds, String type, ConfigExportData configExportData) {
        List<Map<String, Object>> moduleConfigListData = moduleDao.getModuleConfigData(projectId, moduleIds, type);
        for (int i = 0; i < moduleIds.length; i++) {
            Module module = new Module();
            module.setConfigs(new ArrayList<Config>());
            for (int j = 0; j < moduleConfigListData.size(); j++) {
                if (moduleIds[i] == Integer.parseInt(moduleConfigListData.get(j).get("MODULE_ID").toString())) {
                    if (!(moduleConfigListData.get(j).get("MODULE_NAME").toString().equals(module.getName()))) {
                        module.setName(moduleConfigListData.get(j).get("MODULE_NAME").toString());
                        configExportData.getModules().add(module);
                    }
                    Config config = new Config();
                    config.setKey(moduleConfigListData.get(j).get("CONFIG_KEY").toString());
                    config.setIsShow((Integer) moduleConfigListData.get(j).get("IS_SHOW") > 0 ? true : false);
                    if ("development".equals(type)) {
                        config.setValue(moduleConfigListData.get(j).get("CONFIG_VALUE").toString());
                    } else if ("test".equals(type)) {
                        config.setValue(moduleConfigListData.get(j).get("TEST_VALUE").toString());
                    } else if ("build".equals(type)) {
                        config.setValue(moduleConfigListData.get(j).get("BUILD_VALUE").toString());
                    } else {
                        config.setValue(moduleConfigListData.get(j).get("PRODUCTION_VALUE").toString());
                    }
                    config.setDescription(moduleConfigListData.get(j).get("CONFIG_DESC").toString());
                    module.getConfigs().add(config);
                }
            }
        }
    }

    public String getConfigExportPropertiesInfo(int projectId, int[] moduleIds, String type) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Map<String, Object>> getModuleData = moduleDao.getModuleConfigData(projectId, moduleIds, type);
        for (int i = 0; i < moduleIds.length; i++) {
            String moduleName = "";
            for (int j = 0; j < getModuleData.size(); j++) {
                if (moduleIds[i] == Integer.parseInt(getModuleData.get(j).get("MODULE_ID").toString())) {
                    if (!(getModuleData.get(j).get("MODULE_NAME").toString().equals(moduleName))) {
                        stringBuilder.append("\n");
                        moduleName = getModuleData.get(j).get("MODULE_NAME").toString();
                        stringBuilder.append("\n" + "#ModuleName:" + moduleName);
                    }
                    if ("development".equals(type)) {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            stringBuilder.append("\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#"));
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            stringBuilder.append("\n" + "#IS_NOT_SHOW");
                        }
                        stringBuilder.append("\n" + getModuleData.get(j).get("CONFIG_KEY").toString()
                                + "=" + getModuleData.get(j).get("CONFIG_VALUE").toString());
                    } else if ("test".equals(type)) {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            stringBuilder.append("\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#"));
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            stringBuilder.append("\n" + "#IS_NOT_SHOW");
                        }
                        stringBuilder.append("\n" + getModuleData.get(j).get("CONFIG_KEY").toString() + "="
                                + getModuleData.get(j).get("TEST_VALUE").toString());
                    } else if ("build".equals(type)) {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            stringBuilder.append("\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#"));
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            stringBuilder.append("\n" + "#IS_NOT_SHOW");
                        }
                        stringBuilder.append("\n" + getModuleData.get(j).get("CONFIG_KEY").toString() + "="
                                + getModuleData.get(j).get("BUILD_VALUE").toString());
                    } else {
                        if (StringUtils.isNotBlank(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")))) {
                            stringBuilder.append("\n" + "#"
                                    + StringUtils.replace(String.valueOf(getModuleData.get(j).get("CONFIG_DESC")), "\r\n", "\r\n#"));
                        }
                        if ((Integer) getModuleData.get(j).get("IS_SHOW") == 0) {
                            stringBuilder.append("\n" + "#IS_NOT_SHOW");
                        }
                        stringBuilder.append("\n" + getModuleData.get(j).get("CONFIG_KEY").toString()
                                + "=" + getModuleData.get(j).get("PRODUCTION_VALUE").toString());
                    }
                }
            }
        }
        return stringBuilder.append("\n").toString();
    }

    public ConfigExportData getExportData(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        String exportDataStr = new String(fileBytes);
        ConfigExportData exportData = new ConfigExportData();
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
        return exportData;
    }

    public void getConfigCheckResult(ConfigExportData exportData, MultipartFile file, String type, int projectId, ConfigCheckResult checkResult) throws IOException {
        List<Map<String, String>> saveRepeatData = new ArrayList<>();
        ArrayList<Module> modules = exportData.getModules();
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        ArrayList<Module> moduleTmpList = new ArrayList<>();
        //如果该配置已经存在,将重复的数据添加到saveRepeatData变量中
        for (Module moduleTmp : modules) {
            ArrayList<Config> configs = moduleTmp.getConfigs();
            String moduleName = moduleTmp.getName();          //得到模型name
            for (Config configTmp : configs) {
                String configKey = configTmp.getKey();
                ModuleConfigId moduleConfigId = configIdIsExist(configKey, moduleName, projectId);
                if (moduleConfigId.isExist()) {
                    ModuleIdExist moduleIdExist = moduleIdIsExist(moduleName, projectId);
                    if (moduleIdExist.isExist() && moduleIdExist.getModuleId() == moduleConfigId.getModuleId()) {
                        Map<String, String> singleRepeatData = new HashMap<>();
                        singleRepeatData.put(moduleName, configKey);
                        saveRepeatData.add(singleRepeatData);
                    } else {
                        String moduleNameTemp = findName(moduleConfigId.getModuleId());
                        Map<String, String> singleRepeatData = new HashMap<>();
                        singleRepeatData.put(moduleNameTemp, configKey);
                        saveRepeatData.add(singleRepeatData);
                        Module repeatModule = new Module(moduleNameTemp);
                        ArrayList<Config> configListTemp = new ArrayList<>();
                        configListTemp.add(configTmp);
                        repeatModule.setConfigs(configListTemp);
                        moduleTmpList.add(repeatModule);
                    }
                }
            }
        }
        exportData.getModules().addAll(moduleTmpList);
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
                    ModuleIdExist moduleIdExist = moduleIdIsExist(moduleName, projectId);
                    if (moduleIdExist.isExist()) {
                        configDao.insertConfig(configKey, configValue, configDesc, configTmp.getIsShow(),
                                projectId, moduleIdExist.getModuleId(), user.getUserCode());
                    } else {
                        int moduleId = moduleDao.save(projectId, moduleName);
                        configDao.insertConfig(configKey, configValue, configDesc, configTmp.getIsShow(),
                                projectId, moduleId, user.getUserCode());
                    }
                }
            }
            checkResult.setCheckSuccess(2);
        }
    }

    public String getHandlerResult(String checkId, int operation, int projectId, String type,
                                   HttpSession session, ConfigExportData exportData,
                                   HashMap<String, ConfigExportData> IMPORT_CONFIG_MAP) {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
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
                        ModuleConfigId moduleConfigId = configIdIsExist(configKey, moduleName, projectId);
                        ModuleIdExist moduleIdExist = moduleIdIsExist(moduleName, projectId);
                        //找出不存在的配置，执行插入操作
                        if (!moduleConfigId.isExist()) {
                            if (moduleIdExist.isExist()) {
                                configDao.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
                                        projectId, moduleIdExist.getModuleId(), user.getUserCode());
                            } else {
                                int moduleId = save(projectId, moduleName);
                                configDao.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
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
                        ModuleConfigId moduleConfigId = configIdIsExist(configKey, moduleName, projectId);
                        ModuleIdExist moduleIdExist = moduleIdIsExist(moduleName, projectId);
                        if (moduleConfigId.isExist()) {
                            configDao.updateConfig(type, moduleConfigId.getConfigId(), configKey, configValue,
                                    configDesc, config.getIsShow(), projectId, moduleConfigId.getModuleId(),
                                    user.getUserCode());
                        } else { //找出不存在的配置，执行插入操作
                            if (moduleIdExist.isExist()) {
                                configDao.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
                                        projectId, moduleIdExist.getModuleId(), user.getUserCode());
                            } else {
                                int moduleId = save(projectId, moduleName);
                                configDao.insertConfig(configKey, configValue, configDesc, config.getIsShow(),
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

    public boolean isExistModuleName(int projectId, String name) {
        return moduleDao.isExistModuleName(projectId, name);
    }
}
