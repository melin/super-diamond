/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
	
	public List<Map<String, Object>> queryConfigs(Long projectId, Long moduleId, int offset, int limit) {
		String sql = "SELECT * FROM conf_project_config a, conf_project_module b "
				+ "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? ";
		
		if(moduleId != null) {
			sql = sql + " AND a.MODULE_ID = ? order by a.MODULE_ID limit ?,?";
			return jdbcTemplate.queryForList(sql, projectId, moduleId, offset, limit);
		} else {
			sql = sql + " order by a.MODULE_ID limit ?,?";
			return jdbcTemplate.queryForList(sql, projectId, offset, limit);
		}
		
	}
	
	public long queryConfigCount(Long projectId, Long moduleId) {
		String sql = "SELECT count(*) FROM conf_project_config a, conf_project_module b "
				+ "WHERE a.MODULE_ID = b.MODULE_ID AND a.DELETE_FLAG =0 AND a.PROJECT_ID=? ";
		
		if(moduleId != null) {
			sql = sql + " AND a.MODULE_ID = ? order by a.MODULE_ID";
			return jdbcTemplate.queryForObject(sql, Long.class, projectId, moduleId);
		} else {
			sql = sql + " order by a.MODULE_ID";
			return jdbcTemplate.queryForObject(sql, Long.class, projectId);
		}
		
	}
	
	public String queryConfigs(String projectCode, String type, String format) {
		String sql = "SELECT * FROM conf_project_config a, conf_project_module b, conf_project c " +
				"WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=?";
		List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projectCode);
		if("php".equals(format))
			return viewConfigPhp(configs, type);
		else
			return viewConfig(configs, type);
	}
	
	public String queryConfigs(String projectCode, String module, String type, String format) {
		String sql = "SELECT * FROM conf_project_config a, conf_project_module b, conf_project c " +
				"WHERE a.MODULE_ID = b.MODULE_ID AND a.PROJECT_ID=c.id AND a.DELETE_FLAG =0 AND c.PROJ_CODE=? AND b.MODULE_NAME=?";
		List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql, projectCode, module);
		if("php".equals(format))
			return viewConfigPhp(configs, type);
		else
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
				"PRODUCTION_VALUE,PRODUCTION_USER,PRODUCTION_TIME,TEST_VALUE,TEST_USER,TEST_TIME,BUILD_VALUE,BUILD_USER,BUILD_TIME) "
				+ "VALUES (?,?,?,?,?,?,0,?,?,?,?,?,?,?,?,?,?,?)";
		Date time = new Date();
		jdbcTemplate.update(sql, id, configKey, configValue, configDesc, projectId, moduleId, user, time,
				configValue, user, time, configValue, user, time, configValue, user, time);
		
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
		} else if("build".equals(type)) {
			String sql = "update conf_project_config set CONFIG_KEY=?,BUILD_VALUE=?,CONFIG_DESC=?,PROJECT_ID=?,MODULE_ID=?,BUILD_USER=?,BUILD_TIME=? where CONFIG_ID=?";
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
				} else if("build".equals(type)) {
					message += "#version = " + map.get("BUILD_VERSION") + "\r\n";
				}
				
				versionFlag = false;
			}
			
			String desc = (String)map.get("CONFIG_DESC");
			if(StringUtils.isNotBlank(desc))
				message += "#" + desc + "\r\n";
			
			if("development".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("CONFIG_VALUE") + "\r\n";
			} else if("production".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("PRODUCTION_VALUE") + "\r\n";
			} else if("test".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("TEST_VALUE") + "\r\n";
			} else if("build".equals(type)) {
				message += map.get("CONFIG_KEY") + " = " + map.get("BUILD_VALUE") + "\r\n";
			}
		}
		
		return message;
	}
	
	private String viewConfigPhp(List<Map<String, Object>> configs, String type) {
		String message = "<?php\r\n"
						+ "return array(\r\n"
						+ "\t//profile = " + type + "\r\n";
		
		boolean versionFlag = true;
		for(Map<String, Object> map : configs) {
			if(versionFlag) {
				if("development".equals(type)) {
					message += "\t//version = " + map.get("DEVELOPMENT_VERSION") + "\r\n";
				} else if("production".equals(type)) {
					message += "\t//version = " + map.get("PRODUCTION_VERSION") + "\r\n";
				} else if("test".equals(type)) {
					message += "\t//version = " + map.get("TEST_VERSION") + "\r\n";
				} else if("build".equals(type)) {
					message += "\t//version = " + map.get("BUILD_VALUE") + "\r\n";
				}
				
				versionFlag = false;
			}
			
			String desc = (String)map.get("CONFIG_DESC");
			if(StringUtils.isNotBlank(desc))
				message += "\t//" + desc + "\r\n";
			
			if("development".equals(type)) {
				message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("CONFIG_VALUE"));
			} else if("production".equals(type)) {
				message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("PRODUCTION_VALUE"));
			} else if("test".equals(type)) {
				message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("TEST_VALUE"));
			} else if("build".equals(type)) {
				message += "\t'" + map.get("CONFIG_KEY") + "' => " + convertType(map.get("BUILD_VALUE"));
			}
		}

		message += ");\r\n";
		
		return message;
	}
	
	private String convertType(Object value) {
		String conf = String.valueOf(value).trim();
		if("true".equals(conf) || "false".equals(conf)) {
			return  conf + ",\r\n";
		} else if(isNumeric(conf)) {
			return  conf + ",\r\n";
		}else  {
			return  "'" + conf + "',\r\n";
		}
	}
	
	public final static boolean isNumeric(String s) {
		if (s != null && !"".equals(s.trim()))
			return s.matches("^[0-9]*$");
		else
			return false;
	}
}
