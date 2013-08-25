/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ProjectService;

/**
 * Create on @2013-8-23 @上午11:46:19 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ConfigController {
	
	@Autowired
	private ConfigService configService;
	@Autowired
	private ProjectService projectService;
	
	@RequestMapping("/config/save")
	public String saveConfig(String type, Long configId, String configKey, String configValue, String configDesc, Long projectId, Long moduleId) {
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		if(configId == null) {
			configService.insertConfig(configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
		} else {
			configService.updateConfig(type, configId, configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
		}
		return "redirect:/profile/" + type + "/" + projectId;
	}
	
	@RequestMapping("/config/delete/{id}")
	public String deleteConfig(String type, Long projectId, @PathVariable Long id) {
		configService.deleteConfig(id, projectId);
		return "redirect:/profile/" + type + "/" + projectId;
	}
}
