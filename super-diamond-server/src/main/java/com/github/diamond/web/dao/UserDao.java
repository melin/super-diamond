package com.github.diamond.web.dao;

import com.github.diamond.web.model.User;

import java.util.List;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface UserDao {
    User getUser(String userCode);

    List<User> queryUsers(int offset, int limit);

    int queryUserCount();

    void saveUser(User user);

    void deleteUser(int id);

    void updatePassword(int id, String password);
}
