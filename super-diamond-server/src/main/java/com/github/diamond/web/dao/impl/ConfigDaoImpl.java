package com.github.diamond.web.dao.impl;

import com.github.diamond.web.dao.ConfigDao;
import com.github.diamond.web.dao.ProjectDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
@Repository
public class ConfigDaoImpl implements ConfigDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProjectDao projectDao;

    @Override
    public List<Map<String, Object>> queryDevelopmentConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow) {
        List<Map<String, Object>> pageConfigList;
        String pageSql;
        if (isShow) {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,CONFIG_VALUE,CONFIG_DESC,IS_SHOW,OPT_USER,OPT_TIME"
                    + " FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        } else {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,CONFIG_VALUE,CONFIG_DESC,IS_SHOW,OPT_USER,OPT_TIME"
                    + " FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? AND a.IS_SHOW = 1";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        }
        return pageConfigList;
    }

    @Override
    public List<Map<String, Object>> queryTestConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow) {
        List<Map<String, Object>> pageConfigList;
        String pageSql;
        if (isShow) {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,TEST_VALUE,CONFIG_DESC,IS_SHOW,TEST_USER,"
                    + "TEST_TIME FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        } else {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,TEST_VALUE,CONFIG_DESC,IS_SHOW,TEST_USER,TEST_TIME"
                    + " FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? AND a.IS_SHOW = 1";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        }
        return pageConfigList;
    }

    @Override
    public List<Map<String, Object>> queryProductionConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow) {
        List<Map<String, Object>> pageConfigList;
        String pageSql;
        if (isShow) {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,PRODUCTION_VALUE,CONFIG_DESC,IS_SHOW,PRODUCTION_USER,"
                    + "PRODUCTION_TIME FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        } else {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,PRODUCTION_VALUE,CONFIG_DESC,IS_SHOW,PRODUCTION_USER,"
                    + "PRODUCTION_TIME FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? AND a.IS_SHOW = 1";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        }
        return pageConfigList;
    }

    @Override
    public List<Map<String, Object>> queryBuildConfigs(int projectId, String type, int moduleId, int offset, int limit, boolean isShow) {
        List<Map<String, Object>> pageConfigList;
        String pageSql;
        if (isShow) {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,BUILD_VALUE,CONFIG_DESC,IS_SHOW,BUILD_USER,BUILD_TIME"
                    + " FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        } else {
            pageSql = "SELECT CONFIG_ID,a.MODULE_ID,MODULE_NAME,CONFIG_KEY,BUILD_VALUE,CONFIG_DESC,IS_SHOW,BUILD_USER,BUILD_TIME "
                    + "FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? AND a.IS_SHOW = 1";
            if (moduleId != -1) {
                pageSql = pageSql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, moduleId, offset, limit);
            } else {
                pageSql = pageSql + " order by a.MODULE_ID limit ?,?";
                pageConfigList = jdbcTemplate.queryForList(pageSql, projectId, offset, limit);
            }
        }
        return pageConfigList;
    }

    @Override
    public int queryConfigCount(int projectId, int moduleId, boolean isShow) {
        if(isShow) {
            String sql = "SELECT count(*) FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? ";

            if (moduleId != -1) {
                sql = sql + " AND a.MODULE_ID = ? order by a.MODULE_ID";
                return jdbcTemplate.queryForObject(sql, Integer.class, projectId, moduleId);
            } else {
                sql = sql + " order by a.MODULE_ID";
                return jdbcTemplate.queryForObject(sql, Integer.class, projectId);
            }
        }else {
            String sql = "SELECT count(*) FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? AND a.IS_SHOW = 1 ";

            if (moduleId != -1) {
                sql = sql + " AND a.MODULE_ID = ? order by a.MODULE_ID";
                return jdbcTemplate.queryForObject(sql, Integer.class, projectId, moduleId);
            } else {
                sql = sql + " order by a.MODULE_ID";
                return jdbcTemplate.queryForObject(sql, Integer.class, projectId);
            }
        }
    }

    @Override
     public List<Map<String, Object>> queryConfigs(String projectCode, String type, String[] modules) {
        List<Map<String, Object>> configs;
        if ("development".equals(type)) {
            String sql = "SELECT DEVELOPMENT_VERSION,CONFIG_DESC,CONFIG_KEY,CONFIG_VALUE "
                    + "FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            if(modules.length != 0){
                String str ="";
                for(int i=0; i<modules.length; i++){
                    str += "\'" + modules[i]+"\'";
                    if(i+1 < modules.length){
                        str += ",";
                    }
                }
                sql += " AND b.MODULE_NAME in (" + str + ")";
            }
            configs = jdbcTemplate.queryForList(sql, projectCode);
        } else if ("test".equals(type)) {
            String sql = "SELECT TEST_VERSION,CONFIG_DESC,CONFIG_KEY,TEST_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            if(modules.length != 0){
                String str ="";
                for(int i=0; i<modules.length; i++){
                    str += "\'" + modules[i]+"\'";
                    if(i+1 < modules.length){
                        str += ",";
                    }
                }
                sql += " AND b.MODULE_NAME in (" + str + ")";
            }
            configs = jdbcTemplate.queryForList(sql, projectCode);
        } else if ("production".equals(type)) {
            String sql = "SELECT PRODUCTION_VERSION,CONFIG_DESC,CONFIG_KEY,PRODUCTION_VALUE "
                    + "FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            if(modules.length != 0){
                String str ="";
                for(int i=0; i<modules.length; i++){
                    str += "\'" + modules[i]+"\'";
                    if(i+1 < modules.length){
                        str += ",";
                    }
                }
                sql += " AND b.MODULE_NAME in (" + str + ")";
            }
            configs = jdbcTemplate.queryForList(sql, projectCode);
        } else {
            String sql = "SELECT CONFIG_DESC,CONFIG_KEY,BUILD_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            if(modules.length != 0){
                String str ="";
                for(int i=0; i<modules.length; i++){
                    str += "\'" + modules[i]+"\'";
                    if(i+1 < modules.length){
                        str += ",";
                    }
                }
                sql += " AND b.MODULE_NAME in (" + str + ")";
            }
            configs = jdbcTemplate.queryForList(sql, projectCode);
        }
        return configs;
    }

    @Override
    public List<Map<String, Object>> queryConfigs(String projectCode, String type) {
        List<Map<String, Object>> configs;
        if ("development".equals(type)) {
            String sql = "SELECT DEVELOPMENT_VERSION,CONFIG_DESC,CONFIG_KEY,CONFIG_VALUE "
                    + "FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            configs = jdbcTemplate.queryForList(sql, projectCode);
        } else if ("test".equals(type)) {
            String sql = "SELECT TEST_VERSION,CONFIG_DESC,CONFIG_KEY,TEST_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            configs = jdbcTemplate.queryForList(sql, projectCode);
        } else if ("production".equals(type)) {
            String sql = "SELECT PRODUCTION_VERSION,CONFIG_DESC,CONFIG_KEY,PRODUCTION_VALUE "
                    + "FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            configs = jdbcTemplate.queryForList(sql, projectCode);
        } else {
            String sql = "SELECT CONFIG_DESC,CONFIG_KEY,BUILD_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
            configs = jdbcTemplate.queryForList(sql, projectCode);
        }
        return configs;
    }

    @Override
    public Map<String, Object> queryValue(String projectCode, String module, String key) {
        String sql = "SELECT * FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b, CONF_PROJECT c "
                + "WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=? "
                + "AND b.MODULE_NAME=? AND a.CONFIG_KEY=?";
        Map<String, Object> config = jdbcTemplate.queryForMap(sql, projectCode, module, key);
        return config;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void insertConfig(String configKey, String configValue, String configDesc, boolean isShow, int projectId, int moduleId, String user) {
        String sql = "SELECT MAX(CONFIG_ID)+1 FROM CONF_PROJECT_CONFIG";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class);
        if(id == null){
            id = 1;
        }
        sql = "INSERT INTO CONF_PROJECT_CONFIG(CONFIG_ID,CONFIG_KEY,CONFIG_VALUE,"
                + "CONFIG_DESC,IS_SHOW,PROJECT_ID,MODULE_ID,DELETE_FLAG,OPT_USER,OPT_TIME,"
                + "PRODUCTION_VALUE,PRODUCTION_USER,PRODUCTION_TIME,TEST_VALUE,TEST_USER,"
                + "TEST_TIME,BUILD_VALUE,BUILD_USER,BUILD_TIME) "
                + "VALUES (?,?,?,?,?,?,?,0,?,?,?,?,?,?,?,?,?,?,?)";
        Date time = new Date();
        jdbcTemplate.update(sql, id, configKey, configValue, configDesc, isShow ? 1 : 0, projectId, moduleId, user, time,
                configValue, user, time, configValue, user, time, configValue, user, time);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateConfig(String type, int configId, String configKey, String configValue,
                             String configDesc, boolean isShow, int projectId, int moduleId, String user) {
        if ("development".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,CONFIG_VALUE=?,"
                    + "CONFIG_DESC=?,IS_SHOW=?,PROJECT_ID=?,MODULE_ID=?,OPT_USER=?,OPT_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, isShow ? 1 : 0, projectId, moduleId, user, new Date(), configId);
            projectDao.updateVersion(projectId, type);
        } else if ("production".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,PRODUCTION_VALUE=?,"
                    + "CONFIG_DESC=?,IS_SHOW=?,PROJECT_ID=?,MODULE_ID=?,PRODUCTION_USER=?,"
                    + "PRODUCTION_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, isShow ? 1 : 0, projectId, moduleId, user, new Date(), configId);
            projectDao.updateVersion(projectId, type);
        } else if ("test".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,TEST_VALUE=?,CONFIG_DESC=?,IS_SHOW=?,"
                    + "PROJECT_ID=?,MODULE_ID=?,TEST_USER=?,TEST_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, isShow ? 1 : 0, projectId, moduleId, user, new Date(), configId);
            projectDao.updateVersion(projectId, type);
        } else if ("build".equals(type)) {
            String sql = "update CONF_PROJECT_CONFIG set CONFIG_KEY=?,BUILD_VALUE=?,CONFIG_DESC=?,IS_SHOW=?,"
                    + "PROJECT_ID=?,MODULE_ID=?,BUILD_USER=?,BUILD_TIME=? where CONFIG_ID=?";
            jdbcTemplate.update(sql, configKey, configValue, configDesc, isShow ? 1 : 0, projectId, moduleId, user, new Date(), configId);
            projectDao.updateVersion(projectId, type);
        }
    }

    @Override
    public void deleteConfig(int id) {
        String sql = "update CONF_PROJECT_CONFIG set DELETE_FLAG=1 where CONFIG_ID=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void moveConfig(int id, int newModuleId) {
        String sql = "update CONF_PROJECT_CONFIG set MODULE_ID=? where CONFIG_ID=?";
        jdbcTemplate.update(sql, newModuleId, id);
    }

    @Override
    public List<Map<String, Object>> getExportConfig(int projectId, int moduleId, int configId, String type, String valueField) {

        String sql = String.format("SELECT CONFIG_KEY, %s, CONFIG_DESC from CONF_PROJECT_CONFIG where PROJECT_ID=? "
                + "and MODULE_ID=? and CONFIG_ID=?", valueField);

        List<Map<String, Object>> configs = null;
        try {
            configs = jdbcTemplate.queryForList(sql, projectId, moduleId, configId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return configs;
    }

    @Override
    public List<Map<String, Object>> queryConfigs(int projectId, String type) {
        List<Map<String, Object>> allConfigList = null;
        if ("development".equals(type)) {
            String allSql = "SELECT CONFIG_KEY,CONFIG_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            allConfigList = jdbcTemplate.queryForList(allSql, projectId);
        } else if ("production".equals(type)) {
            String allSql = "SELECT CONFIG_KEY,PRODUCTION_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            allConfigList = jdbcTemplate.queryForList(allSql, projectId);
        } else if ("test".equals(type)) {
            String allSql = "SELECT CONFIG_KEY,TEST_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            allConfigList = jdbcTemplate.queryForList(allSql, projectId);
        } else if ("build".equals(type)) {
            String allSql = "SELECT CONFIG_KEY,BUILD_VALUE FROM CONF_PROJECT_CONFIG a, CONF_PROJECT_MODULE b "
                    + "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
            allConfigList = jdbcTemplate.queryForList(allSql, projectId);
        }

        return allConfigList;
    }

    public boolean checkConfigKeyExist(String configKey, int projectId){
        String sql = "SELECT count(*) FROM CONF_PROJECT_CONFIG WHERE PROJECT_ID = ? AND CONFIG_KEY =? AND DELETE_FLAG = 0";
        int num = jdbcTemplate.queryForObject(sql,Integer.class,projectId,configKey);
        return num > 0;
    }

    public Map<String,Object> queryConfigByConfigId(int configId){
        String sql = "select * from CONF_PROJECT_CONFIG where CONFIG_ID = ?";
        return jdbcTemplate.queryForMap(sql, configId);
    }
}
