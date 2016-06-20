package com.github.diamond.web.service.impl;

import com.github.diamond.web.dao.ProjectDao;
import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ProjectService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Create on @2013-7-18 @下午10:51:27.
 *
 * @author melin
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectDao projectDao;

    public List<Project> queryProjects(User user, int offset, int limit) {
        return projectDao.queryProjects(user, offset, limit);
    }

    public int queryProjectCount(User user) {
        return projectDao.queryProjectCount(user);
    }

    public int findUserId(String userCode) {
        return projectDao.findUserId(userCode);
    }

    /**
     * 检查项目是否存在.
     *
     * @param code
     * @return
     */
    public boolean checkProjectExist(String code) {
        return projectDao.checkProjectExist(code);
    }

    public void saveProject(Project project, String copyCode, User user, boolean isCommon) {
        projectDao.saveProject(project, copyCode, user, isCommon);
    }

    public void deleteProject(int id) {
        projectDao.deleteProject(id);
    }

    public List<User> queryUsers(int projectId, int offset, int limit) {
        return projectDao.queryUsers(projectId, offset, limit);
    }

    public int queryUserCount(int projectId) {
        return projectDao.queryUserCount(projectId);
    }

    public List<User> queryProjUsers(int projectId) {
        return projectDao.queryProjUsers(projectId);
    }

    public List<String> queryRoles(int projectId, int userId) {
        return projectDao.queryRoles(projectId, userId);
    }

    public void saveUser(int projectId, int userId, String development, String test, String build, String production, String admin) {
        projectDao.saveUser(projectId, userId, development, test, build, production, admin);
    }

    public void deleteUser(int projectId, int userId) {
        projectDao.deleteUser(projectId, userId);
    }

    /**
     * 查询用户所拥有的项目.
     *
     * @param user
     * @param offset
     * @param limit
     * @return
     */
    public List<Project> queryProjectForUser(User user, int offset, int limit) {
        List<Project> projects = projectDao.queryProjectForUser(user, offset, limit);
        return projects;
    }

    /**
     * 查询用户所拥有的项目数量.
     *
     * @param user
     */
    public int queryProjectCountForUser(User user) {
        return projectDao.queryProjectCountForUser(user);
    }

    /**
     * 增加配置项时，增加版本号.
     *
     * @param projectId
     */
    public void updateVersion(int projectId) {
        projectDao.updateVersion(projectId);
    }

    /**
     * 增加配置项时，增加版本号.
     *
     * @param projectId
     */
    public void updateVersion(int projectId, String type) {
        projectDao.updateVersion(projectId, type);
    }

    public Map<String, Object> queryProject(int projectId) {
        return projectDao.queryProject(projectId);
    }

    public void copyProjConfig(int projId, String projCode, String userCode) {
        projectDao.copyProjConfig(projId, projCode, userCode);
    }

    public ConfigExportData getConfigExportData(int projectId, String userName) {
        String exportUser = userName;
        String projectCode = null;
        String projectDesc = null;
        String configver = null;
        List<Map<String, Object>> projects = projectDao.getProject(projectId);
        for (Map<String, Object> project : projects) {
            projectCode = project.get("PROJ_CODE").toString();
            projectDesc = project.get("PROJ_NAME").toString();
            configver = project.get("DEVELOPMENT_VERSION").toString();
        }
        InetAddress ia = null;
        String serverIp = null;
        try {
            ia = ia.getLocalHost();
            serverIp = ia.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ConfigExportData(exportUser, new Date(), projectCode, projectDesc, configver, serverIp);
    }

    public List queryMultiCommonProjectId() {
        return projectDao.queryMultiCommonProjectId();
    }

    public int getProjectIdByProjectCode(String code) {
        return projectDao.getProjectIdByProjectCode(code);
    }

    public boolean findProjCode(String projCode){
        return projectDao.findProjCode(projCode);
    }

    public Project queryProjectToObject(int projectId){return projectDao.queryProjectToObject(projectId);}

   public void updateProject(Project project,Project oldProject){projectDao.updateProject(project,oldProject);}
}
