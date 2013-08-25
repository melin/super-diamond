/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create on @2013-8-23 @上午10:26:17 
 * @author bsli@ustcinfo.com
 */
@Service
public class ConfigService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ProjectService projectService;
	
	public List<Map<String, Object>> queryConfigs(Long projectId, Long moduleId) {
		String sql = "SELECT * FROM conf_project_config a, conf_project_module b WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=?";
		if(moduleId != null) {
			sql = sql + " AND a.MODULE_ID = ?";
			return jdbcTemplate.queryForList(sql, projectId, moduleId);
		} else {
			return jdbcTemplate.queryForList(sql, projectId);
		}
	}
	
	@Transactional
	public void insertConfig(String configKey, String configValue, String configDesc, Long projectId, Long moduleId, String user) {
		String sql = "INSERT INTO conf_project_config(CONFIG_KEY,CONFIG_VALUE,CONFIG_DESC,PROJECT_ID,MODULE_ID,DELETE_FLAG,OPT_USER,OPT_TIME," +
				"PRODUCTION_VALUE,PRODUCTION_USER,PRODUCTION_TIME,TEST_VALUE,TEST_USER,TEST_TIME) VALUES (?,?,?,?,?,0,?,?,?,?,?,?,?,?)";
		Date time = new Date();
		jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, time,
				configValue, user, time, configValue, user, time);
		
		projectService.updateVersion(projectId);
	}
	
	@Transactional
	public void updateConfig(String type, Long configId, String configKey, String configValue, String configDesc, Long projectId, Long moduleId, String user) {
		if("development".equals(type)) {
			String sql = "update conf_project_config set CONFIG_KEY=?,CONFIG_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,OPT_USER=?,OPT_TIME=? where CONFIG_ID=?";
			jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, new Date(), configId);
			projectService.updateVersion(projectId, type);
		} else if("production".equals(type)) {
			String sql = "update conf_project_config set CONFIG_KEY=?,PRODUCTION_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,PRODUCTION_USER=?,PRODUCTION_TIME=? where CONFIG_ID=?";
			jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, new Date(), configId);
			projectService.updateVersion(projectId, type);
		} else if("test".equals(type)) {
			String sql = "update conf_project_config set CONFIG_KEY=?,TEST_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,TEST_USER=?,TEST_TIME=? where CONFIG_ID=?";
			jdbcTemplate.update(sql, configKey, configValue, configDesc, projectId, moduleId, user, new Date(), configId);
			projectService.updateVersion(projectId, type);
		}
	}
	
	public void deleteConfig(Long id, Long projectId) {
		String sql = "update conf_project_config set DELETE_FLAG=1 where CONFIG_ID=?";
		jdbcTemplate.update(sql, id);
		projectService.updateVersion(projectId);
	}
}
