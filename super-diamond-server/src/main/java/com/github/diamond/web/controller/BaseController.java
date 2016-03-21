/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Create on @2013-8-28 @下午8:37:39 
 * @author bsli@ustcinfo.com
 */
public abstract class BaseController {
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @ExceptionHandler()
    public void handleException(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        logger.error(exception.getMessage(), exception);
        HttpSession httpSession = request.getSession();

        if (exception instanceof CannotGetJdbcConnectionException) {
            httpSession.setAttribute("message", "不能获取数据库连接，请联系管理员！");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        RequestDispatcher rd = request.getSession().getServletContext().getRequestDispatcher("/error");
        try {
            rd.forward(request, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
