package com.github.diamond.web.dao;

import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.ProjectQueryMode;
import com.github.diamond.web.model.User;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ProjectDao {
    /**
     *  查询所有公共项目
     *
     * @return
     */
    List queryMultiCommonProjectId();

    int getProjectIdByProjectCode(String code);

    void updateVersion(int projectId);

    void updateVersion(int projectId, String type);

    int queryProjectCountForUser(User user);

    Map<String, Object> queryProject(int projectId);

    List<Integer> queryProjectAdmins(int projectId);

    void copyProjConfig(int projId, String projCode, String userCode);

    List<Map<String, Object>> getProject(int projectId);

    List<String> queryRoles(int projectId, int userId);

    boolean checkProjectExist(String code);

    void saveUser(int projectId, int userId, String development, String test, String build, String production, String admin);

    void deleteUser(int projectId, int userId);

    void saveProject(Project project, String copyCode, User user, boolean isCommon);

    void deleteProject(int id);

    List<User> queryUsers(int projectId, int offset, int limit);

    int queryUserCount(int projectId);

    List<User> queryProjUsers(int projectId);

    List<Project> queryProjects(User user, ProjectQueryMode mode, int offset, int limit);

    int queryProjectCount(User user);

    int findUserId(String userCode);

    boolean findProjCode(String projCode);

    Project queryProjectToObject(int projectId);

    void updateProject(Project project,Project oldProject);

    String getProjectCodeByProjectId(int projectId);

}
