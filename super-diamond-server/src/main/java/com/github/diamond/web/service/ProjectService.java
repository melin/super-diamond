package com.github.diamond.web.service;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.diamond.web.model.ConfigExportData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import  com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;

/**
 * Create on @2013-7-18 @下午10:51:27 
 * @author melin
 */
@Service
public class ProjectService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ModuleService moduleService;
	
	@Autowired
	private ConfigService configService;
	
	public List<Project> queryProjects(User user, int offset, int limit) {
		String sql = "SELECT b.ID, b.PROJ_CODE, b.PROJ_NAME, a.USER_NAME, b.OWNER_ID FROM CONF_USER a, CONF_PROJECT b " +
				"WHERE a.ID=b.OWNER_ID AND b.DELETE_FLAG = 0 ";
		
		if(!"admin".equals(user.getUserCode())) {
			sql = sql + " AND b.OWNER_ID = ? ORDER BY b.id asc limit ?,?";
		    return jdbcTemplate.query(sql, new ProjectRowMapper(), user.getId(), offset, limit);
		} else
			sql = sql + " ORDER BY b.id asc limit ?,?";
			return jdbcTemplate.query(sql, new ProjectRowMapper(), offset, limit);
	}
	
	public Long queryProjectCount(User user) {
		String sql = "SELECT count(*) FROM CONF_USER a, CONF_PROJECT b " +
				"WHERE a.ID=b.OWNER_ID AND b.DELETE_FLAG = 0 ";
		
		if(!"admin".equals(user.getUserCode())) {
			sql = sql + " AND b.OWNER_ID = ?";
		    return jdbcTemplate.queryForObject(sql, Long.class, user.getId());
		} else
			return jdbcTemplate.queryForObject(sql, Long.class);
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
	
	/**
	 * 检查项目是否存在
	 * 
	 * @param code
	 * @return
	 */
	public boolean checkProjectExist(String code) {
		String sql = "SELECT COUNT(*) FROM CONF_PROJECT WHERE PROJ_CODE=?";
		int count = jdbcTemplate.queryForObject(sql, Integer.class, code);
		if(count == 1)
			return true;
					
		return false;
	}
	
	@Transactional
	public void saveProject(Project project, String copyCode, User user) {
		String sql = "SELECT MAX(id)+1 FROM CONF_PROJECT";
		long projId = 1;
		try {
			projId = jdbcTemplate.queryForObject(sql, Long.class);
		} catch(NullPointerException e) {
			;
		}
		sql = "insert into CONF_PROJECT (ID, PROJ_CODE, PROJ_NAME, OWNER_ID, CREATE_TIME) values (?, ?, ?, ?, ?)";
		
		jdbcTemplate.update(sql, projId, project.getCode(), project.getName(), project.getOwnerId(), new Date());
		this.saveUser(projId, project.getOwnerId(), "development", "test", "build", "production", "admin");
		
		if(StringUtils.isNotBlank(copyCode)) {
			copyProjConfig(projId, copyCode, user.getUserCode());
		}
	}
	
	@Transactional
	public void deleteProject(long id) {
		String sql = "update CONF_PROJECT set DELETE_FLAG = 1 where id = ?";
		jdbcTemplate.update(sql, id);
	}
	
	public List<User> queryUsers(long projectId, int offset, int limit) {
		String sql = "SELECT a.ID, a.USER_CODE, a.USER_NAME FROM CONF_USER a WHERE a.ID NOT IN " +
				"(SELECT b.USER_ID FROM CONF_PROJECT_USER b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0 order by a.ID limit ?,?";
		
		return jdbcTemplate.query(sql, new UserRowMapper(), projectId, offset, limit);
	}
	
	public long queryUserCount(long projectId) {
		String sql = "SELECT count(*) FROM CONF_USER a WHERE a.ID NOT IN " +
				"(SELECT b.USER_ID FROM CONF_PROJECT_USER b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0";
		
		return jdbcTemplate.queryForObject(sql, Long.class, projectId);
	}
	
	public List<User> queryProjUsers(long projectId) {
		String sql = "SELECT a.ID, a.USER_CODE, a.USER_NAME FROM CONF_USER a WHERE a.ID IN " +
				"(SELECT b.USER_ID FROM CONF_PROJECT_USER b WHERE b.PROJ_ID=?) AND a.DELETE_FLAG=0";
		
		List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), projectId);
		
		for(User user : users) {
			sql = "SELECT c.ROLE_CODE FROM CONF_PROJECT_USER_ROLE c WHERE c.PROJ_ID = ?  AND c.USER_ID = ?";
			List<String> roles = jdbcTemplate.queryForList(sql, String.class, projectId, user.getId());
			user.setRoles(roles);
		}
		
		return users;
	}
	
	public List<String> queryRoles(long projectId, long userId) {
		String sql = "SELECT a.ROLE_CODE FROM CONF_PROJECT_USER_ROLE a WHERE a.PROJ_ID=? AND a.USER_ID=? ORDER BY a.ROLE_CODE";
		return jdbcTemplate.queryForList(sql, String.class, projectId, userId);
	}
	
	@Transactional
	public void saveUser(long projectId, long userId, String development, String test, String build, String production, String admin) {
		String sql = "insert into CONF_PROJECT_USER (PROJ_ID, USER_ID) values (?, ?)";
		jdbcTemplate.update(sql, projectId, userId);
		
		sql = "insert into CONF_PROJECT_USER_ROLE (PROJ_ID, USER_ID, ROLE_CODE) values (?, ?, ?)";
		if(StringUtils.isNotBlank(admin)) {
			jdbcTemplate.update(sql, projectId, userId, "admin");
			//如果拥有admin权限，自动添加development、test、build、production
			development = "development";
			test = "test";
			build = "build";
			production = "production";
		}
		if(StringUtils.isNotBlank(development)) {
			jdbcTemplate.update(sql, projectId, userId, "development");
		}
		if(StringUtils.isNotBlank(test)) {
			jdbcTemplate.update(sql, projectId, userId, "test");
		}
		if(StringUtils.isNotBlank(build)) {
			jdbcTemplate.update(sql, projectId, userId, "build");
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
	 * @param user
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<Project> queryProjectForUser(User user, int offset, int limit) {
		if("admin".equals(user.getUserCode())) {
			String sql = "SELECT distinct b.ID, b.PROJ_CODE, b.PROJ_NAME FROM CONF_PROJECT_USER a, CONF_PROJECT b " +
					"WHERE a.PROJ_ID = b.ID AND b.DELETE_FLAG = 0 order by b.ID desc limit ?, ?";
			List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {
	
				public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
						DataAccessException {
					Project project = new Project();
					project.setId(rs.getLong(1));
					project.setCode(rs.getString(2));
					project.setName(rs.getString(3));
					return project;
				}
			}, offset, limit);
			return projects;
		} else {
			String sql = "SELECT distinct b.ID, b.PROJ_CODE, b.PROJ_NAME FROM CONF_PROJECT_USER a, CONF_PROJECT b " +
					"WHERE a.PROJ_ID = b.ID and a.USER_ID=? AND b.DELETE_FLAG = 0 order by b.ID desc limit ?, ?";
			List<Project> projects = jdbcTemplate.query(sql, new RowMapper<Project>() {
	
				public Project mapRow(ResultSet rs, int rowNum) throws SQLException,
						DataAccessException {
					Project project = new Project();
					project.setId(rs.getLong(1));
					project.setCode(rs.getString(2));
					project.setName(rs.getString(3));
					return project;
				}
			}, user.getId(), offset, limit);
			return projects;
		}
	}
	
	/**
	 * 查询用户所拥有的项目数量
	 * 
	 * @param user
	 */
	public long queryProjectCountForUser(User user) {
		if("admin".equals(user.getUserCode())) {
			String sql = "select count(*) from (SELECT distinct b.ID FROM CONF_PROJECT_USER a, CONF_PROJECT b " +
					"WHERE a.PROJ_ID = b.ID AND b.DELETE_FLAG = 0) as proj";
			return jdbcTemplate.queryForObject(sql, Long.class);
		} else {
			String sql = "select count(*) from (SELECT distinct b.ID FROM CONF_PROJECT_USER a, CONF_PROJECT b " +
					"WHERE a.PROJ_ID = b.ID and a.USER_ID=? AND b.DELETE_FLAG = 0) as proj";
			return jdbcTemplate.queryForObject(sql, Long.class, user.getId());
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
	
	private void copyProjConfig(long projId, String projCode, String userCode) {
		String sql = "SELECT b.MODULE_ID, b.MODULE_NAME FROM CONF_PROJECT a, CONF_PROJECT_MODULE b "
				+ "WHERE a.ID = b.PROJ_ID AND a.PROJ_CODE = ?";
		List<Map<String, Object>> modules = jdbcTemplate.queryForList(sql, projCode);
		
		for(Map<String, Object> module : modules) {
			long moduleId = moduleService.save(projId, (String)module.get("MODULE_NAME"));
			sql = "SELECT b.CONFIG_KEY, b.CONFIG_VALUE, b.CONFIG_DESC FROM CONF_PROJECT a, CONF_PROJECT_CONFIG b "
					+ "WHERE a.ID = b.PROJECT_ID AND a.PROJ_CODE=? AND b.MODULE_ID = ?";
			List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projCode, module.get("MODULE_ID"));
			
			for(Map<String, Object> conf : configs) {
				configService.insertConfig((String)conf.get("CONFIG_KEY"), (String)conf.get("CONFIG_VALUE"), (String)conf.get("CONFIG_DESC"), 
						projId, moduleId, userCode);
			}
		}
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

	@Transactional
	public ConfigExportData getConfigExportData(long projectId,String userName)
	{
		String exportUser=userName;
		String projectCode=null;
		String projectDesc=null;
		String configver=null;
		String sql="select PROJ_CODE,PROJ_NAME,DEVELOPMENT_VERSION from CONF_PROJECT where ID=?";
		List<Map<String, Object>> projects=null;
		try {
			projects = jdbcTemplate.queryForList(sql,projectId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		for(Map<String,Object> project :projects)
		{
			projectCode=project.get("PROJ_CODE").toString();
			projectDesc=project.get("PROJ_NAME").toString();
			configver=project.get("DEVELOPMENT_VERSION").toString();
		}
		InetAddress ia=null;
		String serverIp=null;
		try
		{
			ia=ia.getLocalHost();
			serverIp=ia.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ConfigExportData(exportUser,new Date(),projectCode,projectDesc,configver,serverIp);
	}
}
