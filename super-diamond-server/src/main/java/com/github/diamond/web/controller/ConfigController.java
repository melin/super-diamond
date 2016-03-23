/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.Config;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.ConfigService;
import com.github.diamond.web.service.ModuleService;
import com.github.diamond.web.service.ProjectService;
import com.github.diamond.web.service.impl.ConfigServiceImpl;
import com.github.diamond.web.service.impl.ModuleServiceImpl;
import com.github.diamond.web.service.impl.ProjectServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Create on @2013-8-23 @上午11:46:19
 *
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

    /**
     * @param type        profile的值.
     * @param configId
     * @param configKey
     * @param configValue
     * @param configDesc
     * @param projectId
     * @param moduleId
     * @param selModuleId
     * @param page
     * @param flag
     * @return
     */
    @RequestMapping("/config/save")
    public String saveConfig(String type, @RequestParam(defaultValue = "-1") int configId,
                             String configKey, String configValue,
                             String configDesc, boolean isConceal, int projectId, int moduleId,
                             @RequestParam(defaultValue = "-1") int selModuleId, int page,
                             @RequestParam(defaultValue = "") String flag,
                             HttpServletRequest request) throws IOException {
        User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
        if (configId == -1) {
            boolean ret = configService.checkConfigKeyExist(configKey, projectId);
            if(!ret) {
                configService.insertConfig(configKey, configValue, configDesc, !isConceal, projectId, moduleId, user.getUserCode());
            }else {
                request.getSession().setAttribute("moduleId",moduleId);
                request.getSession().setAttribute("configObj", new Config(configKey, configValue, configDesc,isConceal));
                if (selModuleId != -1) {
                    return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + selModuleId + "&flag=" + flag + "&keyExistFlag=" + ret;
                } else {
                    return "redirect:/profile/" + type + "/" + projectId + "?page=" + page + "&flag=" + flag + "&keyExistFlag=" + ret;
                }
            }
        } else {
            configService.updateConfig(type, configId, configKey, configValue, configDesc, !isConceal, projectId, moduleId, user.getUserCode());
        }

        String projCode = (String) projectService.queryProject(projectId).get("PROJ_CODE");
        int isCommon = (int) projectService.queryProject(projectId).get("IS_COMMON");
        if (isCommon == 0) {
            diamondServerHandler.pushConfig(projCode, type);
        } else {
            diamondServerHandler.pushConfig("", type);
        }
        if (selModuleId != -1) {
            return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + selModuleId + "&flag=" + flag;
        } else {
            return "redirect:/profile/" + type + "/" + projectId + "?page=" + page + "&flag=" + flag;
        }
    }

    @RequestMapping("/config/delete/{id}")
    public String deleteConfig(String type, int projectId, @PathVariable int id) {
        configService.deleteConfig(id, projectId);

        String projCode = (String) projectService.queryProject(projectId).get("PROJ_CODE");
        diamondServerHandler.pushConfig(projCode, type);
        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/config/move")
    public String moveConfig(String type, int projectId, int configId, int newModuleId) {
        configService.moveConfig(configId, newModuleId, projectId);

        String projCode = (String) projectService.queryProject(projectId).get("PROJ_CODE");
        diamondServerHandler.pushConfig(projCode, type);
        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/preview/{projectCode}/{type}")
    public void preview(@PathVariable("type") String type, @PathVariable("projectCode") String projectCode,
                        HttpServletRequest request, HttpServletResponse resp) {
        try {
            String format = request.getParameter("format");
            if (StringUtils.isBlank(format)) {
                format = "properties";
            }
            String config = configService.queryConfigs(projectCode, type, format);

            if (format.equals("json")) {
                resp.setContentType("application/json;charset=UTF-8");
            } else {
                resp.setContentType("text/plain;charset=UTF-8");
            }
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
    public void previewModule(@PathVariable("type") String type, @PathVariable("module") String modules,
                              @PathVariable("projectCode") String projectCode,
                              HttpServletRequest request, HttpServletResponse resp) {
        try {
            String format = request.getParameter("format");
            if (StringUtils.isBlank(format)) {
                format = "properties";
            }
            String[] moduleArr = StringUtils.split(modules, ",");
            String config = configService.queryConfigs(projectCode, moduleArr, type, format);

            if (format.equals("json")) {
                resp.setContentType("application/json;charset=UTF-8");
            } else {
                resp.setContentType("text/plain;charset=UTF-8");
            }
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
