package com.github.diamond.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

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

import com.github.diamond.utils.MD5;
import com.github.diamond.web.model.User;

@Service
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Object login(String userCode, String password) {
		String md5Passwd = MD5.getInstance().getMD5String(password);

		try {
			String sql = "SELECT ID, USER_NAME, PASSWORD, DELETE_FLAG " +
					"FROM CONF_USER WHERE USER_CODE = ?";
			User user = jdbcTemplate.query(sql, new UserResultSetExtractor(), userCode);
			
			if(md5Passwd.equals(user.getPassword())) {
				user.setUserCode(userCode);
				return user;
			} else if(user.getDeleteFlag() == 1)
				return "用户已经被注销";
			else 
				return "登录失败，用户密码不正确";
		} catch(TransientDataAccessResourceException e) {
			return "登录失败，用户不存在";
		}
	}
	
	public List<User> queryUsers() {
		String sql = "SELECT ID, USER_CODE, USER_NAME " +
				"FROM CONF_USER WHERE DELETE_FLAG = 0";
		return jdbcTemplate.query(sql, new UserRowMapper());
	}
	
	@Transactional
	public void saveUser(User user) {
		String sql = "insert into CONF_USER (USER_CODE, USER_NAME, PASSWORD, CREATE_TIME) " +
				"values (?, ?, ?, ?)";
		
		jdbcTemplate.update(sql, user.getUserCode(), user.getUserName(), user.getPassword(), new Date());
	}
	
	@Transactional
	public void deleteUser(long id) {
		String sql = "update CONF_USER set DELETE_FLAG = 1 where id = ?";
		jdbcTemplate.update(sql, id);
	}
	
	private class UserResultSetExtractor implements ResultSetExtractor<User> {

		@Override
		public User extractData(ResultSet rs) throws SQLException,
				DataAccessException {
			User user = new User();
			rs.next();
			user.setId(rs.getLong(1));
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
			user.setId(rs.getLong(1));
			user.setUserCode(rs.getString(2));
			user.setUserName(rs.getString(3));
			return user;
		}
	}
}
