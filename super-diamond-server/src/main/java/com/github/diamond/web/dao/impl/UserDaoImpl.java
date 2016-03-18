package com.github.diamond.web.dao.impl;

import com.github.diamond.web.dao.UserDao;
import com.github.diamond.web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by sjpan on 2016/3/16.
 */
@Repository
public class UserDaoImpl implements UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User getUser(String userCode) {
        String sql = "SELECT ID, USER_NAME, PASSWORD, DELETE_FLAG "
                + "FROM CONF_USER WHERE USER_CODE = ?";
        User user = jdbcTemplate.query(sql, new UserResultSetExtractor(), userCode);
        return user;
    }

    public List<User> queryUsers(int offset, int limit) {
        String sql = "SELECT ID, USER_CODE, USER_NAME "
                + "FROM CONF_USER WHERE DELETE_FLAG = 0 order by ID limit ?,?";
        return jdbcTemplate.query(sql, new UserRowMapper(), offset, limit);
    }

    public int queryUserCount() {
        String sql = "SELECT count(*) FROM CONF_USER WHERE DELETE_FLAG = 0";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Transactional
    public void saveUser(User user) {
        String sql = "SELECT MAX(id)+1 FROM CONF_USER";
        int id = jdbcTemplate.queryForObject(sql, Integer.class);
        sql = "insert into CONF_USER (ID, USER_CODE, USER_NAME, PASSWORD, CREATE_TIME) "
                + "values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, id, user.getUserCode(), user.getUserName(), user.getPassword(), new Date());
    }

    @Transactional
    public void deleteUser(int id) {
        String sql = "update CONF_USER set DELETE_FLAG = 1 where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Transactional
    public void updatePassword(int id, String password) {
        String sql = "update CONF_USER set password = ? where id = ?";
        jdbcTemplate.update(sql, password, id);
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
}
