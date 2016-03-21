package com.github.diamond.web.service;

import com.github.diamond.web.model.ModuleConfigId;
import com.github.diamond.web.model.ModuleIdExist;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ModuleService {
    List<Map<String, Object>> queryModules(int projectId);

    int save(int projectId, String name);

    boolean delete(int moduleId, int projectId);

    ModuleConfigId moduleConfigIdIsExist(String configName, String moduleName, int projectId);

    ModuleIdExist moduleIdIsExist(String moduleName, int projectId);

    List<Map<String, Object>> getModuleConfigData(int projectId, int[] moduleIds, String type);
}
