package com.github.diamond.web.dao;

import com.github.diamond.web.model.ModuleConfigId;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ModuleDao {
    List<Map<String, Object>> queryModules(int projectId);

    int save(int projectId, String name);

    String findName(int moduleId);

    boolean delete(int moduleId, int projectId);

    ModuleConfigId configIdIsExist(String configName, String moduleName, int projectId);

    List<Integer> getModuleIdList(String moduleName, int projectId);

    List<Map<String, Object>> getModuleConfigData(int projectId, int[] moduleIds, String type);

    boolean isExistModuleName(int projectId, String name);
}
