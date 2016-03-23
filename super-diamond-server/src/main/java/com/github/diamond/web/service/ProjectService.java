package com.github.diamond.web.service;

import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;

import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ProjectService {
    List<Project> queryProjects(User user, int offset, int limit);

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

    List<Project> queryProjectForUser(User user, int offset, int limit);

    int queryProjectCountForUser(User user);

    void updateVersion(int projectId);

    void updateVersion(int projectId, String type);

    Map<String, Object> queryProject(int projectId);

    void copyProjConfig(int projId, String projCode, String userCode);

    ConfigExportData getConfigExportData(int projectId, String userName);

    int queryCommonProjectId();

    int getProjectIdByProjectCode(String code);

    boolean findProjCode(String projCode);
}
