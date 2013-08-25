/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.web.service.ModuleService;

/**
 * Create on @2013-8-22 @下午5:25:00 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ModuleController {
	
	@Autowired
	private ModuleService moduleService;
	
	@RequestMapping("/module/save")
	public String save(String type, Long projectId, String name) {
		moduleService.save(projectId, name);
		return "redirect:/profile/" + type + "/" + projectId;
	}
}
