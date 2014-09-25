/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create on @2013-8-21 @下午8:18:44 
 * @author bsli@ustcinfo.com
 */
@Service
public class ModuleService {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public List<Map<String, Object>> queryModules(long projectId) {
        String sql = "SELECT * FROM conf_project_module a WHERE a.PROJ_ID = ? order by a.MODULE_ID";
		return jdbcTemplate.queryForList(sql, projectId);
	}
	
	@Transactional
	public long save(Long projectId, String name) {
        String sql = "SELECT MAX(MODULE_ID)+1 FROM conf_project_module";
        long id = 1;
		try {
			id = jdbcTemplate.queryForObject(sql, Long.class);
		} catch(NullPointerException e) {
			;
		}
		sql = "INSERT INTO conf_project_module(MODULE_ID, PROJ_ID, MODULE_NAME) values(?, ?, ?)";
		jdbcTemplate.update(sql, id, projectId, name);
		return id;
	}
	
	@Transactional
	public boolean delete(long moduleId, long projectId) {
		String sql = "select count(*) from conf_project_config where MODULE_ID = ? and PROJECT_ID = ?";
		
		int count = jdbcTemplate.queryForObject(sql, Integer.class, moduleId, projectId);
		if(count == 0) {
			sql = "delete from conf_project_module where MODULE_ID = ? and PROJ_ID = ?";
			jdbcTemplate.update(sql, moduleId, projectId);
			return true;
		} else {
			return false;
		}
	}
}
