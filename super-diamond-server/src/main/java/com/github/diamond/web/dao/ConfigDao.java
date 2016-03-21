package com.github.diamond.web.dao;

import com.github.diamond.web.model.Config;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ConfigDao {
    List<Map<String, Object>> queryDevelopmentConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow);

    List<Map<String, Object>> queryTestConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow);

    List<Map<String, Object>> queryProductionConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow);

    List<Map<String, Object>> queryBuildConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow);

    int queryConfigCount(int projectId, int moduleId);

    List<Map<String, Object>> queryConfigs(String projectCode, String type);

    Map<String, Object> queryValue(String projectCode, String module, String key);

    void insertConfig(String configKey, String configValue, String configDesc, boolean isShow, int projectId, int moduleId, String user);

    void updateConfig(String type, int configId, String configKey,
                      String configValue, String configDesc, boolean isShow,
                      int projectId, int moduleId, String user);

    void deleteConfig(int id);

    void moveConfig(int id, int newModuleId);

    List<Map<String, Object>> getExportConfig(int projectId, int moduleId, int configId, String type, String valueField);

    List<Map<String, Object>> queryCommonConfigs(int projectId, String type);
}
