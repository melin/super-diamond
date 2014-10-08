/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ProjectService;

/**
 * Create on @2013-8-23 @上午11:46:19 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ConfigController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigController.class);
	
	@Autowired
	private ConfigService configService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DiamondServerHandler diamondServerHandler;
	
	@RequestMapping("/config/save")
	public String saveConfig(String type, Long configId, String configKey, String configValue, 
			String configDesc, Long projectId, Long moduleId, Long selModuleId, int page, 
			@RequestParam(defaultValue="")String flag) {
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		if(configId == null) {
			configService.insertConfig(configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
		} else {
			configService.updateConfig(type, configId, configKey, configValue, configDesc, projectId, moduleId, user.getUserCode());
		}

		String projCode = (String)projectService.queryProject(projectId).get("PROJ_CODE");
		String config = configService.queryConfigs(projCode, type, "");
		diamondServerHandler.pushConfig(projCode, type, config);
		if(selModuleId != null)
			return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + selModuleId + "&flag=" + flag;
		else
			return "redirect:/profile/" + type + "/" + projectId + "?page=" + page + "&flag=" + flag;
	}
	
	@RequestMapping("/config/delete/{id}")
	public String deleteConfig(String type, Long projectId, @PathVariable Long id) {
		configService.deleteConfig(id, projectId);
		
		String projCode = (String)projectService.queryProject(projectId).get("PROJ_CODE");
		String config = configService.queryConfigs(projCode, type, "");
		diamondServerHandler.pushConfig(projCode, type, config);
		return "redirect:/profile/" + type + "/" + projectId;
	}
	
	@RequestMapping("/preview/{projectCode}/{type}")
	public void preview(@PathVariable("type") String type, @PathVariable("projectCode") String projectCode, 
			HttpServletRequest request, HttpServletResponse resp) {
		try {
			String format = request.getParameter("format");
			if(StringUtils.isBlank(format)) {
				format = "properties";
			}
			String config = configService.queryConfigs(projectCode, type, format);
			
			if(format.equals("json"))
				resp.setContentType("application/json;charset=UTF-8");
			else
				resp.setContentType("text/plain;charset=UTF-8");
			PrintWriter out = resp.getWriter();
			out.println(config);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				PrintWriter out = resp.getWriter();
				out.println("error = " + e.getMessage());
			} catch (IOException e1) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	@RequestMapping("/preview/{projectCode}/{module}/{type}")
	public void previewModule(@PathVariable("type") String type, @PathVariable("module") String module, 
			@PathVariable("projectCode") String projectCode, 
			HttpServletRequest request, HttpServletResponse resp) {
		try {
			String format = request.getParameter("format");
			if(StringUtils.isBlank(format)) {
				format = "properties";
			}
			String config = configService.queryConfigs(projectCode, module, type, format);
			
			if(format.equals("json"))
				resp.setContentType("application/json;charset=UTF-8");
			else
				resp.setContentType("text/plain;charset=UTF-8");
			PrintWriter out = resp.getWriter();
			out.println(config);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				PrintWriter out = resp.getWriter();
				out.println("error = " + e.getMessage());
			} catch (IOException e1) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	@RequestMapping("/preview/{projectCode}/{module}/{key}/{type}")
	public void previewKey(@PathVariable("type") String type, @PathVariable("key") String key,  
			@PathVariable("module") String module, 
			@PathVariable("projectCode") String projectCode, 
			HttpServletRequest request, HttpServletResponse resp) {
		try {
			String config = configService.queryValue(projectCode, module, key, type);
			resp.setContentType("text/plain");
			PrintWriter out = resp.getWriter();
			out.println(config);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				PrintWriter out = resp.getWriter();
				out.println("error = " + e.getMessage());
			} catch (IOException e1) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
