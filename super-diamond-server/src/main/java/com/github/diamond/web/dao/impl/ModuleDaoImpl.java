package com.github.diamond.web.dao.impl;

import com.github.diamond.web.dao.ModuleDao;
import com.github.diamond.web.model.ModuleConfigId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
@Repository
public class ModuleDaoImpl implements ModuleDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> queryModules(int projectId) {
        String sql = "SELECT * FROM CONF_PROJECT_MODULE a WHERE a.PROJ_ID = ? order by a.MODULE_ID";
        return jdbcTemplate.queryForList(sql, projectId);
    }

    public int save(int projectId, String name) {
        String sql = "SELECT MAX(MODULE_ID)+1 FROM CONF_PROJECT_MODULE";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class);
        if(id == null){
            id = 1;
        }
        sql = "INSERT INTO CONF_PROJECT_MODULE(MODULE_ID, PROJ_ID, MODULE_NAME) values(?, ?, ?)";
        jdbcTemplate.update(sql, id, projectId, name);
        return id;
    }

    public String findName(int moduleId) {
        String sql = "SELECT MODULE_NAME FROM CONF_PROJECT_MODULE WHERE MODULE_ID=?";
        return jdbcTemplate.queryForObject(sql, String.class, moduleId);
    }

    public boolean delete(int moduleId, int projectId) {
        String sql = "select count(*) from CONF_PROJECT_CONFIG where MODULE_ID = ? and PROJECT_ID = ? and DELETE_FLAG <> 1";

        int count = jdbcTemplate.queryForObject(sql, Integer.class, moduleId, projectId);
        if (count == 0) {
            sql = "delete from CONF_PROJECT_MODULE where MODULE_ID = ? and PROJ_ID = ?";
            jdbcTemplate.update(sql, moduleId, projectId);
            return true;
        } else {
            return false;
        }
    }

   /* @Transactional
    public ModuleConfigId moduleConfigIdIsExist(String configName, String moduleName, int projectId) {
        List<Integer> moduleIds = null;
        boolean isExist = false;
        Integer configId = -1;
        Integer moduleId = -1;
        String sql = "select MODULE_ID from CONF_PROJECT_MODULE where MODULE_NAME=? and PROJ_ID=?";
        try {
            moduleIds = jdbcTemplate.queryForList(sql, Integer.class, moduleName, projectId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (moduleIds.size() != 0) {
            moduleId = moduleIds.get(0);
            String querySql = "select CONFIG_ID from CONF_PROJECT_CONFIG where MODULE_ID=? and CONFIG_KEY=? and DELETE_FLAG=0";
            List<Integer> configs = null;
            try {
                configs = jdbcTemplate.queryForList(querySql, Integer.class, moduleId, configName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (configs.size() != 0 && configs.get(0) != null) {
                isExist = true;
                configId = configs.get(0);
            }
            return new ModuleConfigId(isExist, configId, moduleId);
        } else {
            return new ModuleConfigId(isExist, configId, moduleId);
        }
    }*/

    @Transactional
    public ModuleConfigId configIdIsExist(String configName, String moduleName, int projectId) {
        boolean isExist = false;
        Integer configId = -1;
        Integer moduleId = -1;
        String querySql = "select CONFIG_ID, MODULE_ID from CONF_PROJECT_CONFIG where PROJECT_ID=? and CONFIG_KEY=? and DELETE_FLAG=0";
        List<Map<String,Object>> configList = jdbcTemplate.queryForList(querySql, projectId, configName);
        if(configList.size() != 0){
            isExist = true;
            return new ModuleConfigId(isExist, (int)configList.get(0).get("CONFIG_ID"), (int)configList.get(0).get("MODULE_ID"));
        } else {
            return new ModuleConfigId(isExist, configId, moduleId);
        }
    }

    public List<Integer> getModuleIdList(String moduleName, int projectId) {
        String sql = "select MODULE_ID from CONF_PROJECT_MODULE where MODULE_NAME=? and PROJ_ID=?";
        List<Integer> moduleIdList = null;
        try {
            moduleIdList = jdbcTemplate.queryForList(sql, Integer.class, moduleName, projectId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return moduleIdList;
    }

    @Transactional
    public List<Map<String, Object>> getModuleConfigData(int projectId, int[] moduleIds, String type) {
        List<Integer> moduleId = new ArrayList<>();
        for (int i = 0; i < moduleIds.length; i++) {
            moduleId.add(moduleIds[i]);
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", projectId);
        paramMap.put("moduleIds", moduleId);
        String sql = null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        if ("development".equals(type)) {
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.CONFIG_VALUE,b.CONFIG_DESC,b.IS_SHOW "
                    + "FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID "
                    + "AND b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID "
                    + "in (:moduleIds) ORDER BY a.MODULE_ID, b.CONFIG_ID";
        } else if ("test".equals(type)) {
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.TEST_VALUE,b.CONFIG_DESC,b.IS_SHOW "
                    + "FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID AND "
                    + "b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID in (:moduleIds)"
                    + " ORDER BY a.MODULE_ID, b.CONFIG_ID";
        } else if ("build".equals(type)) {
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.BUILD_VALUE,b.CONFIG_DESC,b.IS_SHOW "
                    + "FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID "
                    + "AND b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID "
                    + "AND a.MODULE_ID in (:moduleIds) ORDER BY a.MODULE_ID, b.CONFIG_ID";
        } else {
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.PRODUCTION_VALUE,b.CONFIG_DESC,b.IS_SHOW"
                    + " FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID AND "
                    + "b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID in (:moduleIds) "
                    + "ORDER BY a.MODULE_ID, b.CONFIG_ID";
        }
        List<Map<String, Object>> moduleConfigListData = new ArrayList<Map<String, Object>>();
        moduleConfigListData = namedParameterJdbcTemplate.queryForList(sql, paramMap);
        return moduleConfigListData;
    }

    public boolean isExistModuleName(int projectId, String name){
        String sql = "SELECT count(*) FROM CONF_PROJECT_MODULE WHERE PROJ_ID = ? AND MODULE_NAME = ?";
        int num = jdbcTemplate.queryForObject(sql, Integer.class, projectId,name);
        return num > 0;
    }
}
