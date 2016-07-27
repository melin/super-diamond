package com.github.diamond.web.service;

import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.ProjectQueryMode;
import com.github.diamond.web.model.User;

import java.util.List;
import java.util.Map;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 查询用户项目
     *
     * @param user 用户实体类（非admin用户仅查找自己拥有的或是参与的项目，admin用户查找所有项目）
     * @param mode 查询模式
     * @param offset
     * @param limit
     * @return
     */
    List<Project> queryProjects(User user, ProjectQueryMode mode,  int offset, int limit);

    int queryProjectCount(User user);

    int findUserId(String userCode);

    boolean checkProjectExist(String code);

    void saveProject(Project project, String copyCode, User user, boolean isCommon);

    void deleteProject(int id);

    List<User> queryUsers(int projectId, int offset, int limit);

    int queryUserCount(int projectId);

    List<User> queryProjUsers(int projectId);

    List<String> queryRoles(int projectId, int userId);

    void saveUser(int projectId, int userId, String development, String test, String build, String production, String admin);

    void deleteUser(int projectId, int userId);

    int queryProjectCountForUser(User user);

    void updateVersion(int projectId);

    void updateVersion(int projectId, String type);

    Map<String, Object> queryProject(int projectId);

    void copyProjConfig(int projId, String projCode, String userCode);

    ConfigExportData getConfigExportData(int projectId, String userName);

    List queryMultiCommonProjectId();

    int getProjectIdByProjectCode(String code);

    boolean findProjCode(String projCode);

    Project queryProjectToObject(int projectId);

    void updateProject(Project project,Project oldProject);
}
