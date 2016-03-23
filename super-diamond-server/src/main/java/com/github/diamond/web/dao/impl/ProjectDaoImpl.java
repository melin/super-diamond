package com.github.diamond.web.dao.impl;

import com.github.diamond.web.dao.ConfigDao;
import com.github.diamond.web.dao.ModuleDao;
import com.github.diamond.web.dao.ProjectDao;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
@Repository
public class ProjectDaoImpl implements ProjectDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ModuleDao moduleDao;

    @Autowired
    private ConfigDao configDao;

    /**
     * 查询用户所拥有的项目.
     *
     * @param user
     * @param offset
     * @param limit
     * @return
     */
    public List<Project> queryProjectForUser(User user, int offset, int limit) {
        if ("admin".equals(user.getUserCode())) {
            String sql = "SELECT distinct b.ID, b.PROJ_CODE, b.PROJ_NAME FROM CONF_PROJECT_USER a, CONF_PROJECT b "
                    + "WHERE a.PROJ_ID = b.ID AND b.DELETE_FLAG = 0 order by b.ID desc limit ?, ?";
            List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {

                public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
                        DataAccessException {
                    Project project = new Project();
                    project.setId(rs.getInt(1));
                    project.setCode(rs.getString(2));
                    project.setName(rs.getString(3));
                    return project;
                }
            }, offset, limit);
            return projects;
        } else {
            String sql = "SELECT distinct b.ID, b.PROJ_CODE, b.PROJ_NAME FROM CONF_PROJECT_USER a, CONF_PROJECT b "
                    + "WHERE a.PROJ_ID = b.ID and a.USER_ID=? AND b.DELETE_FLAG = 0 order by b.ID desc limit ?, ?";
            List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {

                public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
                        DataAccessException {
                    Project project = new Project();
                    project.setId(rs.getInt(1));
                    project.setCode(rs.getString(2));
                    project.setName(rs.getString(3));
                    return project;
                }
            }, user.getId(), offset, limit);
            return projects;
        }
    }

    @Override
    public int queryCommonProjectId() {
        String sql = "select id from CONF_PROJECT where DELETE_FLAG = 0 AND IS_COMMON =1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        int id = -1;
        if (list.size() == 1) {
            id = Integer.valueOf(String.valueOf(list.get(0).get("ID")));
        }
        return id;
    }

    @Override
    public int getProjectIdByProjectCode(String code) {
        String sql = "SELECT id FROM CONF_PROJECT WHERE PROJ_CODE=? AND DELETE_FLAG = 0";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, code);
        int id = -1;
        if (list.size() == 1) {
            id = Integer.valueOf(String.valueOf(list.get(0).get("ID")));
        }
        return id;
    }

    /**
     * 增加配置项时，增加版本号.
     *
     * @param projectId
     */
    @Transactional
    public void updateVersion(int projectId) {
        String sql = "update CONF_PROJECT set DEVELOPMENT_VERSION=DEVELOPMENT_VERSION+1,PRODUCTION_VERSION=PRODUCTION_VERSION+1,"
                + "TEST_VERSION=TEST_VERSION+1 where ID=?";
        jdbcTemplate.update(sql, projectId);
    }

    /**
     * 增加配置项时，增加版本号.
     *
     * @param projectId
     */
    @Transactional
    public void updateVersion(int projectId, String type) {
        if ("development".equals(type)) {
            String sql = "update CONF_PROJECT set DEVELOPMENT_VERSION=DEVELOPMENT_VERSION+1 where ID=?";
            jdbcTemplate.update(sql, projectId);
        } else if ("production".equals(type)) {
            String sql = "update CONF_PROJECT set PRODUCTION_VERSION=PRODUCTION_VERSION+1 where ID=?";
            jdbcTemplate.update(sql, projectId);
        } else if ("test".equals(type)) {
            String sql = "update CONF_PROJECT set TEST_VERSION=TEST_VERSION+1 where ID=?";
            jdbcTemplate.update(sql, projectId);
        }
    }

    /**
     * 查询用户所拥有的项目数量.
     *
     * @param user
     */
    public int queryProjectCountForUser(User user) {
        if ("admin".equals(user.getUserCode())) {
            String sql = "select count(*) from (SELECT distinct b.ID FROM CONF_PROJECT_USER a, CONF_PROJECT b "
                    + "WHERE a.PROJ_ID = b.ID AND b.DELETE_FLAG = 0) as proj";
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } else {
            String sql = "select count(*) from (SELECT distinct b.ID FROM CONF_PROJECT_USER a, CONF_PROJECT b "
                    + "WHERE a.PROJ_ID = b.ID and a.USER_ID=? AND b.DELETE_FLAG = 0) as proj";
            return jdbcTemplate.queryForObject(sql, Integer.class, user.getId());
        }
    }

    public Map<String, Object> queryProject(int projectId) {
        String sql = "select * from CONF_PROJECT where ID=?";
        return jdbcTemplate.queryForMap(sql, projectId);
    }

    public void copyProjConfig(int projId, String projCode, String userCode) {
        String sql = "SELECT b.MODULE_ID, b.MODULE_NAME FROM CONF_PROJECT a, CONF_PROJECT_MODULE b "
                + "WHERE a.ID = b.PROJ_ID AND a.PROJ_CODE = ?";
        List<Map<String, Object>> modules = jdbcTemplate.queryForList(sql, projCode);

        for (Map<String, Object> module : modules) {
            int moduleId = moduleDao.save(projId, (String) module.get("MODULE_NAME"));
            sql = "SELECT b.CONFIG_KEY, b.CONFIG_VALUE, b.CONFIG_DESC, b. FROM CONF_PROJECT a, CONF_PROJECT_CONFIG b "
                    + "WHERE a.ID = b.PROJECT_ID AND a.PROJ_CODE=? AND b.MODULE_ID = ?";
            List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projCode, module.get("MODULE_ID"));

            for (Map<String, Object> conf : configs) {
                configDao.insertConfig((String) conf.get("CONFIG_KEY"), (String) conf.get("CONFIG_VALUE"),
                        (String) conf.get("CONFIG_DESC"), (short) conf.get("IS_SHOW") > 0 ? true : false,
                        projId, moduleId, userCode);
            }
        }
    }

    public List<Map<String, Object>> getProject(int projectId) {
        String sql = "select PROJ_CODE,PROJ_NAME,DEVELOPMENT_VERSION from CONF_PROJECT where ID=?";
        List<Map<String, Object>> projects = null;
        try {
            projects = jdbcTemplate.queryForList(sql, projectId);
        } catch (Exception e) {

        }
        return projects;
    }

    public List<String> queryRoles(int projectId, int userId) {
        String sql = "SELECT a.ROLE_CODE FROM CONF_PROJECT_USER_ROLE a WHERE a.PROJ_ID=? AND a.USER_ID=? ORDER BY a.ROLE_CODE";
        return jdbcTemplate.queryForList(sql, String.class, projectId, userId);
    }

    /**
     * 检查项目是否存在.
     *
     * @param code
     * @return
     */
    public boolean checkProjectExist(String code) {
        String sql = "SELECT COUNT(*) FROM CONF_PROJECT WHERE PROJ_CODE=? AND DELETE_FLAG = 0";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, code);
        if (count == 1) {
            return true;
        }

        return false;
    }

    @Transactional
    public void saveUser(int projectId, int userId, String development, String test, String build, String production, String admin) {
        String sql = "insert into CONF_PROJECT_USER (PROJ_ID, USER_ID) values (?, ?)";
        jdbcTemplate.update(sql, projectId, userId);

        sql = "insert into CONF_PROJECT_USER_ROLE (PROJ_ID, USER_ID, ROLE_CODE) values (?, ?, ?)";
        if (StringUtils.isNotBlank(admin)) {
            jdbcTemplate.update(sql, projectId, userId, "admin");
            //如果拥有admin权限，自动添加development、test、build、production
            development = "development";
            test = "test";
            build = "build";
            production = "production";
        }
        if (StringUtils.isNotBlank(development)) {
            jdbcTemplate.update(sql, projectId, userId, "development");
        }
        if (StringUtils.isNotBlank(test)) {
            jdbcTemplate.update(sql, projectId, userId, "test");
        }
        if (StringUtils.isNotBlank(build)) {
            jdbcTemplate.update(sql, projectId, userId, "build");
        }
        if (StringUtils.isNotBlank(production)) {
            jdbcTemplate.update(sql, projectId, userId, "production");
        }
    }

    @Transactional
    public void deleteUser(int projectId, int userId) {
        String sql = "delete from CONF_PROJECT_USER_ROLE where PROJ_ID = ? and USER_ID = ?";
        jdbcTemplate.update(sql, projectId, userId);
        sql = "delete from CONF_PROJECT_USER where PROJ_ID = ? and USER_ID = ?";
        jdbcTemplate.update(sql, projectId, userId);
    }

    @Transactional
    public void saveProject(Project project, String copyCode, User user, boolean isCommon) {
        String sql = "SELECT MAX(id)+1 FROM CONF_PROJECT";
        Integer projId = jdbcTemplate.queryForObject(sql, Integer.class);
        if(projId == null){
            projId = 1;
        }
        sql = "insert into CONF_PROJECT (ID, PROJ_CODE, PROJ_NAME, OWNER_ID, CREATE_TIME,IS_COMMON) values (?, ?, ?, ?, ?,?)";
        jdbcTemplate.update(sql, projId, project.getCode(), project.getName(), project.getOwnerId(), new Date(), isCommon ? 1 : 0);
        this.saveUser(projId, project.getOwnerId(), "development", "test", "build", "production", "admin");

        if (StringUtils.isNotBlank(copyCode)) {
            copyProjConfig(projId, copyCode, user.getUserCode());
        }
    }

    public void deleteProject(int id) {
        String sql = "update CONF_PROJECT set DELETE_FLAG = 1 where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<User> queryUsers(int projectId, int offset, int limit) {
        String sql = "SELECT a.ID, a.USER_CODE, a.USER_NAME FROM CONF_USER a WHERE a.ID NOT IN "
                + "(SELECT b.USER_ID FROM CONF_PROJECT_USER b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0 order by a.ID limit ?,?";

        return jdbcTemplate.query(sql, new UserRowMapper(), projectId, offset, limit);
    }


    public int queryUserCount(int projectId) {
        String sql = "SELECT count(*) FROM CONF_USER a WHERE a.ID NOT IN "
                + "(SELECT b.USER_ID FROM CONF_PROJECT_USER b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0";

        return jdbcTemplate.queryForObject(sql, Integer.class, projectId);
    }

    public List<User> queryProjUsers(int projectId) {
        String sql = "SELECT a.ID, a.USER_CODE, a.USER_NAME FROM CONF_USER a WHERE a.ID IN "
                + "(SELECT b.USER_ID FROM CONF_PROJECT_USER b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0";

        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), projectId);

        for (User user : users) {
            sql = "SELECT c.ROLE_CODE FROM CONF_PROJECT_USER_ROLE c WHERE c.PROJ_ID = ?  AND c.USER_ID = ?";
            List<String> roles = jdbcTemplate.queryForList(sql, String.class, projectId, user.getId());
            user.setRoles(roles);
        }

        return users;
    }

    public List<Project> queryProjects(User user, int offset, int limit) {
        String sql = "SELECT b.ID, b.PROJ_CODE, b.PROJ_NAME, a.USER_NAME, b.OWNER_ID FROM CONF_USER a, CONF_PROJECT b "
                + "WHERE a.ID=b.OWNER_ID AND b.DELETE_FLAG = 0 ";

        if (!"admin".equals(user.getUserCode())) {
            sql = sql + " AND b.OWNER_ID = ? ORDER BY b.id asc limit ?,?";
            return jdbcTemplate.query(sql, new ProjectRowMapper(), user.getId(), offset, limit);
        } else {
            sql = sql + " ORDER BY b.id asc limit ?,?";
        }
        return jdbcTemplate.query(sql, new ProjectRowMapper(), offset, limit);
    }

    public int queryProjectCount(User user) {
        String sql = "SELECT count(*) FROM CONF_USER a, CONF_PROJECT b "
                + "WHERE a.ID=b.OWNER_ID AND b.DELETE_FLAG = 0 ";

        if (!"admin".equals(user.getUserCode())) {
            sql = sql + " AND b.OWNER_ID = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, user.getId());
        } else {
            return jdbcTemplate.queryForObject(sql, Integer.class);
        }
    }

    public int findUserId(String userCode) {
        try {
            String sql = "SELECT ID FROM CONF_USER WHERE USER_CODE = ?";
            Integer userId = jdbcTemplate.queryForObject(sql, new Object[]{userCode}, Integer.class);
            if(userId != null){
                return userId;
            }else {
                return 0;
            }
        } catch (DataAccessException e) {
            return 0;
        }
    }

    private class ProjectRowMapper implements RowMapper<Project> {

        public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
                DataAccessException {
            Project project = new Project();
            project.setId(rs.getInt(1));
            project.setCode(rs.getString(2));
            project.setName(rs.getString(3));
            project.setUserName(rs.getString(4));
            project.setOwnerId(rs.getInt(5));
            return project;
        }
    }

    private class UserRowMapper implements RowMapper<User> {

        public User mapRow(ResultSet rs, int rowNum) throws SQLException,
                DataAccessException {
            User user = new User();
            user.setId(rs.getInt(1));
            user.setUserCode(rs.getString(2));
            user.setUserName(rs.getString(3));
            return user;
        }
    }

    public boolean findProjCode(String projCode){
        String sql = "SELECT count(*) FROM CONF_PROJECT WHERE PROJ_CODE = ? AND DELETE_FLAG = 0";
        int num = jdbcTemplate.queryForObject(sql,Integer.class,projCode);
        return num > 0;
    }
}

