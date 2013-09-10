/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ProjectService;

/**
 * Create on @2013-7-18 @下午10:52:36 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ProjectController extends BaseController {
	@Autowired
	private ProjectService projectService;
	
	@RequestMapping("/project/index")
	public void queryProjects(ModelMap modelMap) {
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		List<Project> projects = projectService.queryProjects(user);
		
		modelMap.addAttribute("projects", projects);
	}
	
	@RequestMapping("/project/new")
	public void newProject() {
	}
	
	@RequestMapping(value="/project/save", method={RequestMethod.POST})
	public String saveProject(Project project, HttpSession session) {
		if(StringUtils.isBlank(project.getCode())) {
			session.setAttribute("project", project);
			session.setAttribute("message", "项目编码不能为空");
		} else if(StringUtils.isBlank(project.getName())) {
			session.setAttribute("project", project);
			session.setAttribute("message", "项目名称不能为空");
		} else if(StringUtils.isBlank(project.getUserCode())) {
			session.setAttribute("project", project);
			session.setAttribute("message", "项目管理者不能为空");
		} else {
			long userid = projectService.findUserId(project.getUserCode());
			if(userid == 0) {
				session.setAttribute("project", project);
				session.setAttribute("message", "项目管理者不存在，请检查拼写是否正确");
			} else {
				project.setOwnerId(userid);
				projectService.saveProject(project);
				session.setAttribute("message", "项目添加成功");
				return "redirect:/project/index";
			}
		}
		return "redirect:/project/new";
	}
	
	@RequestMapping("/project/delete")
	public String deleteProject(long id, HttpSession session, HttpServletResponse response) {
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		
		if("admin".equals(user.getUserCode())) {
			projectService.deleteProject(id);
			session.setAttribute("message", "项目删除成功");
			return "redirect:/project/index";
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			session.setAttribute("message", "无权删除项目");
			return "redirect:/error";
		}
	}
	
	@RequestMapping("/project/addUsers")
	public void addUsers(long id, ModelMap modelMap) {
		modelMap.addAttribute("users", projectService.queryUsers(id));
		modelMap.addAttribute("projUsers", projectService.queryProjUsers(id));
		modelMap.addAttribute("project", projectService.queryProject(id));
	}

	@RequestMapping(value="/project/saveUser", method={RequestMethod.POST})
	public String saveUser(long projectId, long userId, String development, String test, 
			String production, String admin, HttpSession session) {
		if(StringUtils.isBlank(development) && StringUtils.isBlank(test) && 
				StringUtils.isBlank(production) && StringUtils.isBlank(admin)) {
			session.setAttribute("message", "请选择用户角色");
		} else 
			projectService.saveUser(projectId, userId, development, test, production, admin);
		
		return "redirect:/project/addUsers?id=" + projectId;
	}
	
	@RequestMapping(value="/project/deleteUser")
	public String deleteUser(long projectId, long userId, HttpSession session) {
		projectService.deleteUser(projectId, userId);
		session.setAttribute("message", "用户删除成功");
		return "redirect:/project/addUsers?id=" + projectId;
	}
}
