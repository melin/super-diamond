package com.github.diamond.web.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;

/**
 * Create on @2013-7-18 @下午10:51:27 
 * @author melin
 */
@Service
public class ProjectService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public List<Project> queryProjects(User user) {
		String sql = "SELECT b.ID, b.PROJ_CODE, b.PROJ_NAME, a.USER_NAME, b.OWNER_ID FROM CONF_USER a, CONF_PROJECT b " +
				"WHERE a.ID=b.OWNER_ID AND b.DELETE_FLAG = 0";
		
		if(!"admin".equals(user.getUserCode())) {
			sql = sql + " AND b.OWNER_ID = ?";
		    return jdbcTemplate.query(sql, new ProjectRowMapper(), user.getId());
		} else
			return jdbcTemplate.query(sql, new ProjectRowMapper());
	}
	
	public long findUserId(String userCode) {
		try {
			String sql = "SELECT ID FROM CONF_USER WHERE USER_CODE = ?";
			long userid = jdbcTemplate.queryForObject(sql, new Object[]{userCode}, Long.class);
			return userid;
		} catch(DataAccessException e) {
			return 0;
		}
	}
	
	@Transactional
	public void saveProject(Project project) {
		String sql = "SELECT MAX(id)+1 FROM conf_project";
		long id = jdbcTemplate.queryForObject(sql, Long.class);
		sql = "insert into CONF_PROJECT (ID, PROJ_CODE, PROJ_NAME, OWNER_ID, CREATE_TIME) values (?, ?, ?, ?)";
		
		jdbcTemplate.update(sql, id, project.getCode(), project.getName(), project.getOwnerId(), new Date());
		this.saveUser(id, project.getOwnerId(), "development", "test", "production", "admin");
	}
	
	@Transactional
	public void deleteProject(long id) {
		String sql = "update CONF_PROJECT set DELETE_FLAG = 1 where id = ?";
		jdbcTemplate.update(sql, id);
	}
	
	public List<User> queryUsers(long projectId) {
		String sql = "SELECT a.ID, a.USER_CODE, a.USER_NAME FROM conf_user a WHERE a.ID NOT IN " +
				"(SELECT b.USER_ID FROM conf_project_user b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0";
		
		return jdbcTemplate.query(sql, new UserRowMapper(), projectId);
	}
	
	public List<User> queryProjUsers(long projectId) {
		String sql = "SELECT a.ID, a.USER_CODE, a.USER_NAME FROM conf_user a WHERE a.ID IN " +
				"(SELECT b.USER_ID FROM conf_project_user b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0";
		
		List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), projectId);
		
		for(User user : users) {
			sql = "SELECT c.ROLE_CODE FROM conf_project_user_role c WHERE c.PROJ_ID = ?  AND c.USER_ID = ?";
			List<String> roles = jdbcTemplate.queryForList(sql, String.class, projectId, user.getId());
			user.setRoles(roles);
		}
		
		return users;
	}
	
	public Project queryProject(long projectId) {
		String sql = "SELECT b.ID, b.PROJ_CODE, b.PROJ_NAME, b.OWNER_ID FROM CONF_PROJECT b " +
				"WHERE b.ID=? AND b.DELETE_FLAG = 0";
		List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {

			public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
					DataAccessException {
				Project project = new Project();
				project.setId(rs.getLong(1));
				project.setCode(rs.getString(2));
				project.setName(rs.getString(3));
				project.setOwnerId(rs.getLong(4));
				return project;
			}
		}, projectId);
		
		return projects.get(0);
	}
	
	public List<String> queryRoles(long projectId, long userId) {
		String sql = "SELECT a.ROLE_CODE FROM conf_project_user_role a WHERE a.PROJ_ID=? AND a.USER_ID=? ORDER BY a.ROLE_CODE";
		return jdbcTemplate.queryForList(sql, String.class, projectId, userId);
	}
	
	@Transactional
	public void saveUser(long projectId, long userId, String development, String test, String production, String admin) {
		String sql = "insert into CONF_PROJECT_USER (PROJ_ID, USER_ID) values (?, ?)";
		jdbcTemplate.update(sql, projectId, userId);
		
		sql = "insert into CONF_PROJECT_USER_ROLE (PROJ_ID, USER_ID, ROLE_CODE) values (?, ?, ?)";
		if(StringUtils.isNotBlank(admin)) {
			jdbcTemplate.update(sql, projectId, userId, "admin");
			//如果拥有admin权限，自动添加development、test、production
			development = "development";
			test = "test";
			production = "production";
		}
		if(StringUtils.isNotBlank(development)) {
			jdbcTemplate.update(sql, projectId, userId, "development");
		}
		if(StringUtils.isNotBlank(test)) {
			jdbcTemplate.update(sql, projectId, userId, "test");
		}
		if(StringUtils.isNotBlank(production)) {
			jdbcTemplate.update(sql, projectId, userId, "production");
		}
	}
	
	@Transactional
	public void deleteUser(long projectId, long userId) {
		String sql = "delete from CONF_PROJECT_USER_ROLE where PROJ_ID = ? and USER_ID = ?";
		jdbcTemplate.update(sql, projectId, userId);
		sql = "delete from CONF_PROJECT_USER where PROJ_ID = ? and USER_ID = ?";
		jdbcTemplate.update(sql, projectId, userId);
	}
	
	/**
	 * 查询用户所拥有的项目
	 * 
	 * @param userId
	 */
	public List<Project> queryProjectForUser(User user) {
		if("admin".equals(user.getUserCode())) {
			String sql = "SELECT distinct b.ID, b.PROJ_NAME FROM CONF_PROJECT_USER a, CONF_PROJECT b " +
					"WHERE a.PROJ_ID = b.ID AND b.DELETE_FLAG = 0";
			List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {
	
				public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
						DataAccessException {
					Project project = new Project();
					project.setId(rs.getLong(1));
					project.setName(rs.getString(2));
					return project;
				}
			});
			return projects;
		} else {
			String sql = "SELECT distinct b.ID, b.PROJ_NAME FROM CONF_PROJECT_USER a, CONF_PROJECT b " +
					"WHERE a.PROJ_ID = b.ID and a.USER_ID=? AND b.DELETE_FLAG = 0";
			List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {
	
				public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
						DataAccessException {
					Project project = new Project();
					project.setId(rs.getLong(1));
					project.setName(rs.getString(2));
					return project;
				}
			}, user.getId());
			return projects;
		}
	}
	
	/**
	 * 增加配置项时，增加版本号
	 * @param projectId
	 */
	@Transactional
	public void updateVersion(Long projectId) {
		String sql = "update CONF_PROJECT set DEVELOPMENT_VERSION=DEVELOPMENT_VERSION+1,PRODUCTION_VERSION=PRODUCTION_VERSION+1," +
				"TEST_VERSION=TEST_VERSION+1 where ID=?";
		jdbcTemplate.update(sql, projectId);
	}
	
	/**
	 * 增加配置项时，增加版本号
	 * @param projectId
	 */
	@Transactional
	public void updateVersion(Long projectId, String type) {
		if("development".equals(type)) {
			String sql = "update CONF_PROJECT set DEVELOPMENT_VERSION=DEVELOPMENT_VERSION+1 where ID=?";
			jdbcTemplate.update(sql, projectId);
		} else if("production".equals(type)) {
			String sql = "update CONF_PROJECT set PRODUCTION_VERSION=PRODUCTION_VERSION+1 where ID=?";
			jdbcTemplate.update(sql, projectId);
		} else if("test".equals(type)) {
			String sql = "update CONF_PROJECT set TEST_VERSION=TEST_VERSION+1 where ID=?";
			jdbcTemplate.update(sql, projectId);
		}
	}
	
	public 	Map<String, Object> queryProject(Long projectId) {
		String sql = "select * from CONF_PROJECT where ID=?";
		return jdbcTemplate.queryForMap(sql, projectId);
	}
	
	private class ProjectRowMapper implements RowMapper<Project> {

		public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
				DataAccessException {
			Project project = new Project();
			project.setId(rs.getLong(1));
			project.setCode(rs.getString(2));
			project.setName(rs.getString(3));
			project.setUserName(rs.getString(4));
			project.setOwnerId(rs.getLong(5));
			return project;
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
