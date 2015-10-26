/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */    
package com.github.diamond.web.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.github.diamond.web.model.Config;
import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.ExportDoc;
import com.github.diamond.web.model.Module;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.diamond.web.service.ModuleService;

import java.io.*;
import java.util.List;

/**
 * Create on @2013-8-22 @下午5:25:00 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ModuleController extends BaseController {


	@Autowired
	private ConfigService configService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ModuleService moduleService;


	@RequestMapping("/module/save")
	public String save(String type, Long projectId, String name) {
		moduleService.save(projectId, name);
		return "redirect:/profile/" + type + "/" + projectId;
	}

	@RequestMapping("/module/delete/{type}/{projectId}/{moduleId}")
	public String delete(@PathVariable String type, @PathVariable Long projectId,
						 @PathVariable Long moduleId, HttpSession session) {
		boolean result = moduleService.delete(moduleId, projectId);
		if (!result) {
			session.setAttribute("message", "模块已经被配置项关联，不能删除！");
			return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + moduleId;
		} else {
			session.setAttribute("message", "删除成功！");
			return "redirect:/profile/" + type + "/" + projectId;
		}
	}

	@RequestMapping("/module/export/{type}/{projectId}/{userName}/{moduleIds}")
	public void export(HttpServletResponse response,@PathVariable String type, @PathVariable long projectId, @PathVariable String userName, @PathVariable long[] moduleIds) {

		ConfigExportData configExportData;
		configExportData = projectService.getConfigExportData(projectId, userName);

		ExportDoc exportDoc = new ExportDoc();
		exportDoc.getConfigExportDatas().add(configExportData);

		for (int i = 0; i < moduleIds.length; i++) {
			Module module = moduleService.getExportModule(projectId, moduleIds[i]);
			configExportData.getModules().add(module);

			List<Long> configIds = moduleService.getConfigCount(projectId, moduleIds[i]);
			for (int j = 0; j < configIds.size(); j++) {
				Config config = new Config();
				config = configService.getExportConfig(projectId, moduleIds[i], configIds.get(j));
				module.getConfigs().add(config);
			}
		}
		String json = JSON.toJSONString(exportDoc, true);
		response.setContentType("application/json;charset=utf-8");
		PrintWriter outjson=null;
		System.out.println(json);
		try {
			outjson = response.getWriter();
			outjson.write(json);
			outjson.flush();
			outjson.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveDocJson(ExportDoc doc) {
		HttpServletResponse response=null;
		String jsonPath = "exportDoc.json";
		String json = JSON.toJSONString(doc, true);
		response.setContentType("application/json");

		System.out.println(json);
		try {
			PrintWriter outjson= response.getWriter();
			File file = new File(jsonPath);
			file.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			outjson.print(json);
			out.write(json);
			outjson.flush();
			outjson.close();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
