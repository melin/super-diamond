package com.github.diamond.web.service;

import com.github.diamond.web.model.Config;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ConfigService {
    List<Map<String, Object>> queryConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow);

    int queryConfigCount(int projectId, int moduleId);

    String queryConfigs(String projectCode, String type, String format);

    String queryConfigs(String projectCode, String type, String[] encryptPropNameArr, String format);

    String queryConfigs(String projectCode, String[] modules, String type, String format);

    String queryConfigs(String projectCode, String[] modules, String[] encryptPropNameArr, String type, String format);

    String queryValue(String projectCode, String module, String key, String type);

    void insertConfig(String configKey, String configValue, String configDesc, boolean isShow, int projectId, int moduleId, String user);

    void updateConfig(String type, int configId, String configKey,
                      String configValue, String configDesc, boolean isShow,
                      int projectId, int moduleId, String user);

    void deleteConfig(int id, int projectId);

    void moveConfig(int id, int newModuleId, int projectId);

    Config getExportConfig(int projectId, int moduleId, int configId, String type);

    Map<String, String> getCommonConfigMap(int projectId, String type);

    Map<String, String> replaceCommonConfigs(int projectId, String type, Map<String, String> commonCofigStore);

    Map<String, String> replaceCommonConfigs(int projectId, String type, Map<String, String> commonCofigStore, String[] encryptPropNameArr);

    boolean checkConfigKeyExist(String configKey, int projectId);
}
