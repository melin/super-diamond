package com.github.diamond.web.service.impl;

import com.github.diamond.utils.MD5;
import com.github.diamond.web.dao.UserDao;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    public Object login(String userCode, String password) {
        String md5Passwd = MD5.getInstance().getMD5String(password);

        try {
            User user = userDao.getUser(userCode);

            if (md5Passwd.equals(user.getPassword())) {
                user.setUserCode(userCode);
                return user;
            } else if (user.getDeleteFlag() == 1) {
                return "用户已经被注销";
            } else {
                return "登录失败，用户密码不正确";
            }
        } catch (TransientDataAccessResourceException e) {
            return "登录失败，用户不存在";
        }
    }

    public List<User> queryUsers(int offset, int limit) {
        return userDao.queryUsers(offset, limit);
    }

    public int queryUserCount() {
        return userDao.queryUserCount();
    }

    public void saveUser(User user) {
        userDao.saveUser(user);
    }

    @Transactional
    public void deleteUser(int id) {
        userDao.deleteUser(id);
    }

    @Transactional
    public void updatePassword(int id, String password) {
        userDao.updatePassword(id, password);
    }

    private class UserResultSetExtractor implements ResultSetExtractor<User> {

        @Override
        public User extractData(ResultSet rs) throws SQLException,
                DataAccessException {
            User user = new User();
            rs.next();
            user.setId(rs.getInt(1));
            user.setUserName(rs.getString(2));
            user.setPassword(rs.getString(3));
            user.setDeleteFlag(rs.getInt(4));
            return user;
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

    public boolean checkUserCodeExist(String userCode){
        return userDao.checkUserCodeExist(userCode);
    }
}
