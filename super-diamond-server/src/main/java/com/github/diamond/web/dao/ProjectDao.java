package com.github.diamond.web.dao;

import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ProjectDao {
    List<Project> queryProjectForUser(User user, int offset, int limit);

    int queryCommonProjectId();

    int getProjectIdByProjectCode(String code);

    void updateVersion(int projectId);

    void updateVersion(int projectId, String type);

    int queryProjectCountForUser(User user);

    Map<String, Object> queryProject(int projectId);

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

    List<Project> queryProjects(User user, int offset, int limit);

    int queryProjectCount(User user);

    int findUserId(String userCode);

    boolean findProjCode(String projCode);

}
