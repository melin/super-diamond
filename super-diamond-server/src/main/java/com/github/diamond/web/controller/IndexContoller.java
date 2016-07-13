/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import com.github.diamond.utils.PageUtil;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.Project;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.impl.ProjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * Create on @2013-7-19 @下午1:52:20 
 * @author bsli@ustcinfo.com
 */
@Controller
public class IndexContoller extends BaseController {

    @Autowired
    private ProjectService projectService;

    private static final int LIMIT = 10;

    @RequestMapping("/index")
    public void index(ModelMap modelMap, @RequestParam(defaultValue = "1") int page) {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        List<Project> projects = projectService.queryProjects(user, false, PageUtil.getOffset(page, LIMIT), LIMIT);
        for (Project project : projects) {
            List<String> roles = projectService.queryRoles(project.getId(), user.getId());
            project.setRoles(roles);
        }
        modelMap.addAttribute("projects", projects);
        long recordCount = projectService.queryProjectCountForUser(user);
        modelMap.addAttribute("totalPages", PageUtil.pageCount(recordCount, LIMIT));
        modelMap.addAttribute("currentPage", page);
    }
}
