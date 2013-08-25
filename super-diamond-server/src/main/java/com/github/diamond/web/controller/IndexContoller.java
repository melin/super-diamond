/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ProjectService;

/**
 * Create on @2013-7-19 @下午1:52:20 
 * @author bsli@ustcinfo.com
 */
@Controller
public class IndexContoller {
	
	@Autowired
	private ProjectService projectService;
	
	@RequestMapping("/index")
	public void index(ModelMap modelMap) {
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		List<Project> projects = projectService.queryProjectForUser(user);
		for(Project project : projects) {
			List<String> roles = projectService.queryRoles(project.getId(), user.getId());
			project.setRoles(roles);
		}
		modelMap.addAttribute("projects", projects);
	}
}
