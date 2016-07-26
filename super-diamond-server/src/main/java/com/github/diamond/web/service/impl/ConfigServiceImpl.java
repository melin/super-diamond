/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.diamond.utils.PlaceHolderUtil;
import com.github.diamond.utils.ProjectIdUtil;
import com.github.diamond.web.dao.ConfigDao;
import com.github.diamond.web.dao.ProjectDao;
import com.github.diamond.web.model.Config;
import com.github.diamond.web.service.ConfigService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create on @2013-8-23 @上午10:26:17.
 *
 * @author bsli@ustcinfo.com
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private final static String ERROR_REPLACE_TOKEN = "RPF:";

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ConfigDao configDao;


    public List<Map<String, Object>> queryConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow) {
        List<Map<String, Object>> pageConfigList;
        Map<String, String> commonStore = new HashMap<>();
        Map<String, String> store;
        if ("development".equals(type)) {
            List<Map<String, Object>> originalConfigList = configDao.queryDevelopmentConfigs(projectId, type, moduleId, offset, limit, isShow);
            pageConfigList = doQueryConfigs(projectId, type, originalConfigList, commonStore, "REAL_CONFIG_VALUE");
        } else if ("test".equals(type)) {
            List<Map<String, Object>> originalConfigList = configDao.queryTestConfigs(projectId, type, moduleId, offset, limit, isShow);
            pageConfigList = doQueryConfigs(projectId, type, originalConfigList, commonStore, "REAL_TEST_VALUE");
        } else if ("production".equals(type)) {
            List<Map<String, Object>> originalConfigList = configDao.queryProductionConfigs(projectId, type, moduleId, offset, limit, isShow);
            pageConfigList = doQueryConfigs(projectId, type, originalConfigList, commonStore, "REAL_PRODUCTION_VALUE");
        } else {
            List<Map<String, Object>> originalConfigList = configDao.queryBuildConfigs(projectId, type, moduleId, offset, limit, isShow);
            pageConfigList = doQueryConfigs(projectId, type, originalConfigList, commonStore, "REAL_BUILD_VALUE");
        }
        return pageConfigList;
    }

    private List<Map<String, Object>> doQueryConfigs(int projectId, String type, List<Map<String, Object>> originalConfigList, Map<String, String> commonStore, String realConfigValKey) {
        Map<String, String> store;
        List<Map<String, Object>> commonProjectId = projectDao.queryMultiCommonProjectId();
        if (commonProjectId != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
            commonStore = getCommonConfigMap(commonProjectId, type);
        }
        store = replaceByCommonConfigs(projectId, type, commonStore);

        for (Map<String, Object> config : originalConfigList) {
            int index = String.valueOf(config.get("CONFIG_DESC")).indexOf("\r\n");
            if (index != -1) {
                String desc = StringUtils.replace(String.valueOf(config.get("CONFIG_DESC")), "\r\n", "<br/>");
                config.remove("CONFIG_DESC");
                config.put("CONFIG_DESC", desc);
            }
            String str = PlaceHolderUtil.findPlaceHolderVar(store.get(config.get("CONFIG_KEY")));
            if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
                StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                String realValue = strSubstitutor.replace(store.get(config.get("CONFIG_KEY")));
                // add
                if (PlaceHolderUtil.findPlaceHolderVar(realValue) != null) { //发现有${}【但除了${env:}或${sys:}】符号，则认为替换失败
                    realValue = ERROR_REPLACE_TOKEN + realValue;
                }
                config.put(realConfigValKey, realValue);
            } else {

                // 对于公共项目内自身引用的处理
                StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                String realValue = strSubstitutor.replace(store.get(config.get("CONFIG_KEY")));
                // add
                if (PlaceHolderUtil.findPlaceHolderVar(realValue) != null) { // 发现有${}【但除了${env:}或${sys:}】符号，则认为替换失败
                    realValue = ERROR_REPLACE_TOKEN + realValue;
                }
                config.put(realConfigValKey, realValue);
//                config.put(realConfigValKey, store.get(config.get("CONFIG_KEY")));
            }
        }

        return originalConfigList;
    }

    public int queryConfigCount(int projectId, int moduleId, boolean isShow) {
        return configDao.queryConfigCount(projectId, moduleId, isShow);
    }

    public String queryConfigs(String projectCode, String type, String format) {
        Map<String, String> commonStore = new HashMap<>();
        Map<String, String> store = new HashMap<>();
        List<Map<String, Object>> commonProjectId = projectDao.queryMultiCommonProjectId();
        int projectId = projectDao.getProjectIdByProjectCode(projectCode);
        if (commonProjectId != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
            commonStore = getCommonConfigMap(commonProjectId, type);
        }

        if (projectId != -1) {
            store = replaceByCommonConfigs(projectId, type, commonStore);
        } else {
            //todo 抛异常
        }
        List<Map<String, Object>> configs = configDao.queryConfigs(projectCode, type);
        if ("development".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("CONFIG_VALUE");
                configs.get(i).put("CONFIG_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("CONFIG_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("CONFIG_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("CONFIG_VALUE");
                    configs.get(i).put("CONFIG_VALUE", realValue);
                    configs.get(i).put("REAL_CONFIG_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_CONFIG_VALUE", "");
                }
            }
        } else if ("production".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("PRODUCTION_VALUE");
                configs.get(i).put("PRODUCTION_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("PRODUCTION_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("PRODUCTION_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("PRODUCTION_VALUE");
                    configs.get(i).put("PRODUCTION_VALUE", realValue);
                    configs.get(i).put("REAL_PRODUCTION_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_PRODUCTION_VALUE", "");
                }
            }
        } else if ("test".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("TEST_VALUE");
                configs.get(i).put("TEST_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("TEST_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("TEST_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("TEST_VALUE");
                    configs.get(i).put("TEST_VALUE", realValue);
                    configs.get(i).put("REAL_TEST_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_TEST_VALUE", "");
                }
            }
        } else if ("build".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("BUILD_VALUE");
                configs.get(i).put("BUILD_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("BUILD_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("BUILD_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("BUILD_VALUE");
                    configs.get(i).put("BUILD_VALUE", realValue);
                    configs.get(i).put("REAL_BUILD_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_BUILD_VALUE", "");
                }
            }
        }
        if ("php".equals(format)) {
            return viewConfigPhp(configs, type);
        } else if ("json".equals(format)) {
            return viewConfigJson(configs, type);
        } else {
            return viewConfig(configs, type);
        }
    }

    public String queryConfigs(String projectCode, String[] modules, String type, String format) {
        Map<String, String> commonStore = new HashMap<>();
        Map<String, String> store = new HashMap<>();
        List<Map<String, Object>> commonProjectId = projectDao.queryMultiCommonProjectId();
        int projectId = projectDao.getProjectIdByProjectCode(projectCode);
        if (commonProjectId != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
            commonStore = getCommonConfigMap(commonProjectId, type);
        }

        if (projectId != -1) {
            store = replaceByCommonConfigs(projectId, type, commonStore);
        } else {
            //todo 抛异常
        }

        List<Map<String, Object>> configs = configDao.queryConfigs(projectCode, type, modules);
        if ("development".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("CONFIG_VALUE");
                configs.get(i).put("CONFIG_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("CONFIG_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("CONFIG_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("CONFIG_VALUE");
                    configs.get(i).put("CONFIG_VALUE", realValue);
                    configs.get(i).put("REAL_CONFIG_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_CONFIG_VALUE", "");
                }
            }
        } else if ("production".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("PRODUCTION_VALUE");
                configs.get(i).put("PRODUCTION_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("PRODUCTION_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("PRODUCTION_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("RODUCTION_VALUE");
                    configs.get(i).put("RODUCTION_VALUE", realValue);
                    configs.get(i).put("REAL_PRODUCTION_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_PRODUCTION_VALUE", "");
                }
            }
        } else if ("test".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("TEST_VALUE");
                configs.get(i).put("TEST_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("TEST_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("TEST_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("TEST_VALUE");
                    configs.get(i).put("TEST_VALUE", realValue);
                    configs.get(i).put("REAL_TEST_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_TEST_VALUE", "");
                }
            }
        } else if ("build".equals(type)) {
            for (int i = 0; i < configs.size(); i++) {
                int index = String.valueOf(configs.get(i).get("CONFIG_DESC")).indexOf("\r\n");
                if (index != -1) {
                    String desc = StringUtils.replace(String.valueOf(configs.get(i).get("CONFIG_DESC")), "\r\n", "<br/>");
                    configs.get(i).remove("CONFIG_DESC");
                    configs.get(i).put("CONFIG_DESC", desc);
                }
                configs.get(i).remove("BUILD_VALUE");
                configs.get(i).put("BUILD_VALUE", store.get(configs.get(i).get("CONFIG_KEY")));
                String str = PlaceHolderUtil.findPlaceHolderVar(String.valueOf(configs.get(i).get("BUILD_VALUE")));
                if (str != null && !ProjectIdUtil.isIdExistsInCommonId(projectId, commonProjectId)) {
//                    String realValue = PlaceHolderUtil.replaceVarWithValue(store, str, String.valueOf(configs.get(i).get("BUILD_VALUE")));
                    StrSubstitutor strSubstitutor = new StrSubstitutor(store);
                    String realValue = strSubstitutor.replace(store.get(String.valueOf(configs.get(i).get("CONFIG_KEY"))));
                    configs.get(i).remove("BUILD_VALUE");
                    configs.get(i).put("BUILD_VALUE", realValue);
                    configs.get(i).put("REAL_BUILD_VALUE", realValue);
                } else {
                    configs.get(i).put("REAL_BUILD_VALUE", "");
                }
            }
        }

        if ("php".equals(format)) {
            return viewConfigPhp(configs, type);
        } else if ("json".equals(format)) {
            return viewConfigJson(configs, type);
        } else {
            return viewConfig(configs, type);
        }
    }

    public String queryValue(String projectCode, String module, String key, String type) {
        Map<String, Object> config = configDao.queryValue(projectCode, module, key);
        if ("development".equals(type)) {
            return (String) config.get("CONFIG_VALUE");
        } else if ("production".equals(type)) {
            return (String) config.get("PRODUCTION_VALUE");
        } else if ("test".equals(type)) {
            return (String) config.get("TEST_VALUE");
        } else if ("build".equals(type)) {
            return (String) config.get("BUILD_VALUE");
        } else {
            return "";
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void insertConfig(String configKey, String configValue, String configDesc, boolean isShow, int projectId, int moduleId, String user) {
        configDao.insertConfig(configKey, configValue, configDesc, isShow, projectId, moduleId, user);
        projectDao.updateVersion(projectId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateConfig(String type, int configId, String configKey,
                             String configValue, String configDesc, boolean isShow,
                             int projectId, int moduleId, String user) {
        configDao.updateConfig(type, configId, configKey, configValue, configDesc, isShow, projectId, moduleId, user);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteConfig(int id, int projectId) {
        configDao.deleteConfig(id);
        projectDao.updateVersion(projectId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void moveConfig(int id, int newModuleId, int projectId) {
        configDao.moveConfig(id, newModuleId);
        projectDao.updateVersion(projectId);
    }

    private String viewConfig(List<Map<String, Object>> configs, String type) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");

        boolean versionFlag = true;
        for (Map<String, Object> map : configs) {
            if (versionFlag) {
                if ("development".equals(type)) {
                    stringBuilder.append("#version = " + map.get("DEVELOPMENT_VERSION") + "\r\n");
                } else if ("production".equals(type)) {
                    stringBuilder.append("#version = " + map.get("PRODUCTION_VERSION") + "\r\n");
                } else if ("test".equals(type)) {
                    stringBuilder.append("#version = " + map.get("TEST_VERSION") + "\r\n");
                } else if ("build".equals(type)) {
                    stringBuilder.append("#version = " + map.get("BUILD_VERSION") + "\r\n");
                }
                versionFlag = false;
            }

            String desc = (String) map.get("CONFIG_DESC");
            desc = desc.replaceAll("\r\n", " ");
            if (StringUtils.isNotBlank(desc)) {
                stringBuilder.append("#" + desc + "\r\n");
            }

            if ("development".equals(type)) {
                stringBuilder.append(map.get("CONFIG_KEY") + " = " + map.get("CONFIG_VALUE") + "\r\n");
            } else if ("production".equals(type)) {
                stringBuilder.append(map.get("CONFIG_KEY") + " = " + map.get("PRODUCTION_VALUE") + "\r\n");
            } else if ("test".equals(type)) {
                stringBuilder.append(map.get("CONFIG_KEY") + " = " + map.get("TEST_VALUE") + "\r\n");
            } else if ("build".equals(type)) {
                stringBuilder.append(map.get("CONFIG_KEY") + " = " + map.get("BUILD_VALUE") + "\r\n");
            }
        }

        return stringBuilder.toString();
    }

    private String viewConfigPhp(List<Map<String, Object>> configs, String type) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?php\r\n"
                + "return array(\r\n"
                + "\t//profile = " + type + "\r\n");

        boolean versionFlag = true;
        for (Map<String, Object> map : configs) {
            if (versionFlag) {
                if ("development".equals(type)) {
                    stringBuilder.append("\t//version = " + map.get("DEVELOPMENT_VERSION") + "\r\n");
                } else if ("production".equals(type)) {
                    stringBuilder.append("\t//version = " + map.get("PRODUCTION_VERSION") + "\r\n");
                } else if ("test".equals(type)) {
                    stringBuilder.append("\t//version = " + map.get("TEST_VERSION") + "\r\n");
                } else if ("build".equals(type)) {
                    stringBuilder.append("\t//version = " + map.get("BUILD_VALUE") + "\r\n");
                }

                versionFlag = false;
            }

            String desc = (String) map.get("CONFIG_DESC");
            if (StringUtils.isNotBlank(desc)) {
                stringBuilder.append("\t//" + desc + "\r\n");
            }
            if ("development".equals(type)) {
                stringBuilder.append("\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("CONFIG_VALUE")));
            } else if ("production".equals(type)) {
                stringBuilder.append("\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("PRODUCTION_VALUE")));
            } else if ("test".equals(type)) {
                stringBuilder.append("\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("TEST_VALUE")));
            } else if ("build".equals(type)) {
                stringBuilder.append("\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("BUILD_VALUE")));
            }
        }
        stringBuilder.append(");\r\n");
        return stringBuilder.toString();
    }

    private String convertType(Object value) {
        String conf = String.valueOf(value).trim();
        if ("true".equals(conf) || "false".equals(conf)) {
            return conf + ",\r\n";
        } else if (isNumeric(conf)) {
            return conf + ",\r\n";
        } else {
            return "'" + conf + "',\r\n";
        }
    }

    private String viewConfigJson(List<Map<String, Object>> configs, String type) {
        Map<String, Object> confMap = new LinkedHashMap<String, Object>();
        boolean versionFlag = true;
        for (Map<String, Object> map : configs) {
            if (versionFlag) {
                if ("development".equals(type)) {
                    confMap.put("version", map.get("DEVELOPMENT_VERSION"));
                } else if ("production".equals(type)) {
                    confMap.put("version", map.get("PRODUCTION_VERSION"));
                } else if ("test".equals(type)) {
                    confMap.put("version", map.get("TEST_VERSION"));
                } else if ("build".equals(type)) {
                    confMap.put("version", map.get("BUILD_VALUE"));
                }

                versionFlag = false;
            }

            if ("development".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("CONFIG_VALUE"));
            } else if ("production".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("PRODUCTION_VALUE"));
            } else if ("test".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("TEST_VALUE"));
            } else if ("build".equals(type)) {
                confMap.put(map.get("CONFIG_KEY").toString(), map.get("BUILD_VALUE"));
            }
        }

        return JSONUtils.toJSONString(confMap);
    }

    public static final boolean isNumeric(String str) {
        if (str != null && !"".equals(str.trim())) {
            return str.matches("^[0-9]*$");
        } else {
            return false;
        }
    }

    @Transactional
    public Config getExportConfig(int projectId, int moduleId, int configId, String type) {
        String configKey = null;
        String configValue = null;
        String configDesc = null;

        String valueField;

        if ("development".equals(type)) {
            valueField = "CONFIG_VALUE";
        } else if ("production".equals(type)) {
            valueField = "PRODUCTION_VALUE";
        } else if ("test".equals(type)) {
            valueField = "TEST_VALUE";
        } else if ("build".equals(type)) {
            valueField = "BUILD_VALUE";
        } else {
            valueField = "CONFIG_VALUE";
        }
        List<Map<String, Object>> configs = configDao.getExportConfig(projectId, moduleId, configId, type, valueField);
        for (Map<String, Object> config : configs) {
            configKey = config.get("CONFIG_KEY").toString();
            configValue = config.get(valueField).toString();
            configDesc = config.get("CONFIG_DESC").toString();
        }

        return new Config(configKey, configValue, configDesc);
    }


    public Map<String, String> getCommonConfigMap(List<Map<String, Object>> multiProjectId, String type) {
        Map<String, String> store = new HashMap<>();
        for (int id = 0; id < multiProjectId.size(); id++) {
            int projectId = Integer.valueOf(String.valueOf(multiProjectId.get(id).get("ID")));
            List<Map<String, Object>> allConfigList = configDao.queryConfigs(projectId, type);
            String projCode = projectDao.getProjectCodeByProjectId(projectId);
            Map<String, String> storeTmp = new HashMap<>();
            if ("development".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "CONFIG_VALUE", null);
            } else if ("production".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "PRODUCTION_VALUE", null);
            } else if ("test".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "TEST_VALUE", null);
            } else if ("build".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "BUILD_VALUE", null);
            }

            if ("development".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "CONFIG_VALUE");
            } else if ("production".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "PRODUCTION_VALUE");
            } else if ("test".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "TEST_VALUE");
            } else if ("build".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "BUILD_VALUE");
            }
        }

        return store;
    }

    private void doConfigValReplace(Map<String, String> store, List<Map<String, Object>> allConfigList, String projCode, Map<String, String> storeTmp, String configValueProfileKey) {

        for (Map<String, Object> config : allConfigList) {
            String configVal = String.valueOf(config.get(configValueProfileKey));
            String str = PlaceHolderUtil.findPlaceHolderVar(configVal);
            if (str != null && storeTmp.size() != 0) {
                StrSubstitutor strSubstitutor = new StrSubstitutor(storeTmp);
                String realValue = strSubstitutor.replace(configVal);
                store.put(projCode + ":" + String.valueOf(config.get("CONFIG_KEY")), realValue);
            } else {
                store.put(projCode + ":" + String.valueOf(config.get("CONFIG_KEY")), configVal);
            }
        }

    }

    public Map<String, String> getCommonConfigMap(List<Map<String, Object>> multiProjectId, String type, String[] encryptPropNameArr) {
        Map<String, String> store = new HashMap<>();
        for (int id = 0; id < multiProjectId.size(); id++) {
            int projectId = Integer.valueOf(String.valueOf(multiProjectId.get(id).get("ID")));
            List<Map<String, Object>> allConfigList = configDao.queryConfigs(projectId, type);
            String projCode = projectDao.getProjectCodeByProjectId(projectId);
            Map<String, String> storeTmp = new HashMap<>();
            if ("development".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "CONFIG_VALUE", encryptPropNameArr);
            } else if ("production".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "PRODUCTION_VALUE", encryptPropNameArr);
            } else if ("test".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "TEST_VALUE", encryptPropNameArr);
            } else if ("build".equals(type)) {
                handleConfigVal(allConfigList, projCode, storeTmp, "BUILD_VALUE", encryptPropNameArr);
            }

            if ("development".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "CONFIG_VALUE");
            } else if ("production".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "PRODUCTION_VALUE");
            } else if ("test".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "TEST_VALUE");
            } else if ("build".equals(type)) {
                doConfigValReplace(store, allConfigList, projCode, storeTmp, "BUILD_VALUE");
            }
        }

        return store;
    }

    private void handleConfigVal(List<Map<String, Object>> allConfigList, String projCode, Map<String, String> storeTmp, String configValueProfileKey, String[] encryptPropNameArr) {

        for (Map<String, Object> config : allConfigList) {
            String configVal = String.valueOf(config.get(configValueProfileKey));
            String value;
            if (encryptPropNameArr != null && ArrayUtils.contains(encryptPropNameArr, projCode + ":" + config.get("CONFIG_KEY"))) {
                value = "$[" + configVal + "]";
            } else {
                value = configVal;
            }
            storeTmp.put(String.valueOf(config.get("CONFIG_KEY")), value);
        }
    }


    public Map<String, String> replaceByCommonConfigs(int projectId, String type, Map<String, String> commonCofigStore) {
        Map<String, String> store;
        List<Map<String, Object>> allConfigList = configDao.queryConfigs(projectId, type);

        if ("production".equals(type)) {
            store = doConfigReplace(commonCofigStore, allConfigList, "PRODUCTION_VALUE");
        } else if ("test".equals(type)) {
            store = doConfigReplace(commonCofigStore, allConfigList, "TEST_VALUE");
        } else if ("build".equals(type)) {
            store = doConfigReplace(commonCofigStore, allConfigList, "BUILD_VALUE");
        } else { // defualt use "development"
            store = doConfigReplace(commonCofigStore, allConfigList, "CONFIG_VALUE");
        }

        return store;
    }

    private Map<String, String> doConfigReplace(Map<String, String> commonConfigs, List<Map<String, Object>> configList, String configValueProfileKey) {
        Map<String, String> store = new HashMap<>();

        for (Map<String, Object> config : configList) {
            String configVal = String.valueOf(config.get(configValueProfileKey));
            String str = PlaceHolderUtil.findPlaceHolderVar(configVal);
            if (str != null) {

                if (commonConfigs == null || commonConfigs.size() == 0) {
//                    store.put(String.valueOf(config.get("CONFIG_KEY")), ERROR_REPLACE_TOKEN + configVal);
                    store.put(String.valueOf(config.get("CONFIG_KEY")), configVal);
                } else {
                    StrSubstitutor strSubstitutor = new StrSubstitutor(commonConfigs);
                    String realValue = strSubstitutor.replace(configVal);
                    store.put(String.valueOf(config.get("CONFIG_KEY")), realValue);
                }
            } else {
                store.put(String.valueOf(config.get("CONFIG_KEY")), configVal);
            }
        }

        return store;
    }

    public boolean checkConfigKeyExist(String configKey, int projectId) {
        return configDao.checkConfigKeyExist(configKey, projectId);
    }

    public Map<String, Object> queryConfigByConfigId(int configId) {
        return configDao.queryConfigByConfigId(configId);
    }
}
