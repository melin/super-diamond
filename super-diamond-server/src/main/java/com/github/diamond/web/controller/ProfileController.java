/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import com.github.diamond.utils.PageUtil;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ModuleService;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.impl.ConfigServiceImpl;
import com.github.diamond.web.service.impl.ModuleServiceImpl;
import com.github.diamond.web.service.impl.ProjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Create on @2013-8-21 @下午6:55:09 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ProfileController extends BaseController {
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private ProjectService projectService;

    @RequestMapping("/profile/{type}/{projectId}")
    public String profile(@PathVariable("type") String type,
                          @PathVariable("projectId") int projectId,
                          @RequestParam(defaultValue = "-1") int moduleId,
                          ModelMap modelMap,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "false") boolean isShow,
                          @RequestParam(defaultValue = "10") int recordLimit) {
        modelMap.addAttribute("modules", moduleService.queryModules(projectId));
        modelMap.addAttribute("configs", configService.queryConfigs(projectId, type, moduleId,
                PageUtil.getOffset(page, recordLimit), recordLimit, isShow));
        modelMap.addAttribute("moduleId", moduleId);
        modelMap.addAttribute("project", projectService.queryProject(projectId));

        long recordCount = configService.queryConfigCount(projectId, moduleId);
        modelMap.addAttribute("totalPages", PageUtil.pageCount(recordCount, recordLimit));
        modelMap.addAttribute("currentPage", page);
        modelMap.addAttribute("isShow", isShow);
        modelMap.addAttribute("recordLimit", recordLimit);

        return "profile/" + type;
    }

    @RequestMapping("/profile/preview/{projectCode}/{type}")
    public String preview(@PathVariable("type") String type, @PathVariable("projectCode") String projectCode,
                          int projectId, ModelMap modelMap) {
        String config = configService.queryConfigs(projectCode, type, "");

        modelMap.addAttribute("project", projectService.queryProject(projectId));
        modelMap.addAttribute("message", config);
        return "profile/preview";
    }
}
