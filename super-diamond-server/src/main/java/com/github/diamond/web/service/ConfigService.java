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
	
	public String queryConfigs(String projectCode, String type) {
		String sql = "SELECT * FROM conf_project_config a, conf_project_module b, conf_project c " +
				"WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
		List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projectCode);
		
		return viewConfig(configs, type);
	}
	
	@Transactional
	public void insertConfig(String configKey, String configValue, String configDesc, Long projectId, Long moduleId, String user) {
        String sql = "SELECT MAX(CONFIG_ID)+1 FROM conf_project_config";
        long id = 1;
		try {
			id = jdbcTemplate.queryForObject(sql, Long.class);
		} catch(NullPointerException e) {
			;
		}

        sql = "INSERT INTO conf_project_config(CONFIG_ID,CONFIG_KEY,CONFIG_VALUE,CONFIG_DESC,PROJECT_ID,MODULE_ID,DELETE_FLAG,OPT_USER,OPT_TIME," +
				"PRODUCTION_VALUE,PRODUCTION_USER,PRODUCTION_TIME,TEST_VALUE,TEST_USER,TEST_TIME) VALUES (?,?,?,?,?,?,0,?,?,?,?,?,?,?,?)";
		Date time = new Date();
		jdbcTemplate.update(sql, id, configKey, configValue, configDesc, projectId, moduleId, user, time,
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
	
	private String viewConfig(List<Map<String, Object>> configs, String type) {
		String message = "";
		
		boolean versionFlag = true;
		for(Map<String, Object> map : configs) {
			if(versionFlag) {
				if("development".equals(type)) {
					message += "#version = " + map.get("DEVELOPMENT_VERSION") + "\r\n";
				} else if("production".equals(type)) {
					message += "#version = " + map.get("PRODUCTION_VERSION") + "\r\n";
				} else if("test".equals(type)) {
					message += "#version = " + map.get("TEST_VERSION") + "\r\n";
				}
				
				versionFlag = false;
			}
			
			String desc = (String)map.get("CONFIG_DESC");
			message += "#" + desc + "\r\n";
			
			if("development".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("CONFIG_VALUE") + "\r\n";
			} else if("production".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("PRODUCTION_VALUE") + "\r\n";
			} else if("test".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("TEST_VALUE") + "\r\n";
			}
		}
		
		return message;
	}
}
