/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.service.impl;

import com.github.diamond.web.dao.ModuleDao;
import com.github.diamond.web.model.ModuleConfigId;
import com.github.diamond.web.model.ModuleIdExist;
import com.github.diamond.web.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create on @2013-8-21 @下午8:18:44
 *
 * @author bsli@ustcinfo.com
 */
@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleDao moduleDao;

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

    public ModuleConfigId moduleConfigIdIsExist(String configName, String moduleName, int projectId) {
        return moduleDao.moduleConfigIdIsExist(configName, moduleName, projectId);
    }


    public ModuleIdExist moduleIdIsExist(String moduleName, int projectId) {
        boolean isExist = false;
        List<Integer> moduleIdList = moduleDao.getModuleIdList(moduleName, projectId);
        if (moduleIdList.size() != 0) {
            isExist = true;
            return new ModuleIdExist(isExist, moduleIdList.get(0));
        }
        return new ModuleIdExist(isExist, 0);

    }

    public List<Map<String, Object>> getModuleConfigData(int projectId, int[] moduleIds, String type) {
        List<Map<String, Object>> moduleConfigListData = moduleDao.getModuleConfigData(projectId, moduleIds, type);
        return moduleConfigListData;
    }
}
