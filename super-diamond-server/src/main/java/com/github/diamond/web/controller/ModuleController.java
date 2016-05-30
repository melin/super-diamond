/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import com.alibaba.fastjson.JSON;
import com.github.diamond.web.model.ConfigCheckResult;
import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.service.ModuleService;
import com.github.diamond.web.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;


/**
 * Create on @2013-8-22 @下午5:25:00
 *
 * @author bsli@ustcinfo.com
 */

@Controller
public class ModuleController extends BaseController {

    private static final HashMap<String, ConfigExportData> IMPORT_CONFIG_MAP = new HashMap<>();
    private static Date DATE = new Date();

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;

    /**
     * 配置导入检查.
     *
     * @param file      配置文件
     * @param projectId 项目编号
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/module/import/{type}/{projectId}/{currentPage}")
    @ResponseBody
    public String importModuleCheck(@RequestParam("file") MultipartFile file,
                                    @PathVariable int projectId,
                                    @PathVariable String type) throws UnsupportedEncodingException {


        ConfigExportData exportData = new ConfigExportData();
        final ConfigCheckResult checkResult = new ConfigCheckResult() {
            {
                setCheckId(UUID.randomUUID().toString());
            }
        };
        try {
            if (file.isEmpty()) {
                checkResult.setCheckSuccess(0);
                checkResult.setMessage("导入的文件为空");
                return URLEncoder.encode(JSON.toJSONString(checkResult), "utf-8").replace("+", "%20");
            }
            exportData = moduleService.getExportData(file);
            moduleService.getConfigCheckResult(exportData, file, type, projectId, checkResult);
        } catch (Exception ex) {
            checkResult.setCheckSuccess(0);
            checkResult.setMessage(ex.getMessage());
        }

        if (checkResult.getCheckSuccess() == 1) {
            IMPORT_CONFIG_MAP.put(checkResult.getCheckId(), exportData);
        }
        return URLEncoder.encode(JSON.toJSONString(checkResult), "utf-8").replace("+", "%20");
    }

    /**
     * 导入配置时遇到重复配置信息的情况处理.
     *
     * @param checkId   配置检查id
     * @param operation 操作 1-保存当前 2-覆盖 3-取消
     * @param projectId 项目Id
     * @param type      项目profile
     * @param session   session会话
     * @return 导入结果
     */
    @RequestMapping("/module/import/perform/{checkId}/{operation}/{projectId}/{type}")
    @ResponseBody
    public String importModulePerform(@PathVariable String checkId,
                                      @PathVariable int operation,
                                      @PathVariable int projectId,
                                      @PathVariable String type,
                                      HttpSession session) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        Date dateNow = new Date();
        if ((dateNow.getTime() - DATE.getTime()) % nd % nh / nm >= 10) {
            IMPORT_CONFIG_MAP.remove(checkId);
        }
        ConfigExportData exportData = IMPORT_CONFIG_MAP.get(checkId);
        return moduleService.getHandlerResult(checkId, operation, projectId, type, session, exportData, IMPORT_CONFIG_MAP);

    }

    @RequestMapping("/module/save")
    public String save(String type, int projectId, String name, HttpSession httpSession) {
        boolean ret = moduleService.isExistModuleName(projectId,name);
        if(!ret) {
            moduleService.save(projectId, name);
        }else {
            httpSession.setAttribute("moduleName",name);
            return "redirect:/profile/" + type + "/" + projectId + "?moduleNameExistFlag=" + ret;
        }
        return "redirect:/profile/" + type + "/" + projectId;
    }

    @RequestMapping("/module/delete/{type}/{projectId}/{moduleId}")
    public String delete(@PathVariable String type, @PathVariable int projectId,
                         @PathVariable int moduleId, HttpSession session) {
        boolean result = moduleService.delete(moduleId, projectId);
        if (!result) {
            session.setAttribute("message", "模块已经被配置项关联，不能删除！");
            return "redirect:/profile/" + type + "/" + projectId + "?moduleId=" + moduleId;
        } else {
            session.setAttribute("message", "删除成功！");
            return "redirect:/profile/" + type + "/" + projectId;
        }
    }

    @RequestMapping("/module/exportJson/{type}/{projectId}/{userName}/{moduleIds}")
    @ResponseBody
    public String exportJson(@PathVariable String type, @PathVariable int projectId, @PathVariable String userName, @PathVariable int[] moduleIds) throws UnsupportedEncodingException {
        ConfigExportData configExportData = projectService.getConfigExportData(projectId, userName);
        moduleService.fillConfigExportJsonData(projectId, moduleIds, type, configExportData);
        String json = JSON.toJSONString(configExportData, true);
        return URLEncoder.encode(json,"utf-8").replace("+", "%20");
    }

    @RequestMapping("/module/exportProperties/{type}/{projectId}/{moduleIds}")
    @ResponseBody
    public String exportProperties(@PathVariable String type,
                                   @PathVariable int projectId,
                                   @PathVariable int[] moduleIds) throws UnsupportedEncodingException {
        String str = URLEncoder.encode(moduleService.getConfigExportPropertiesInfo(projectId, moduleIds, type), "utf-8").replace("+", "%20");
        return  str;
    }
}
