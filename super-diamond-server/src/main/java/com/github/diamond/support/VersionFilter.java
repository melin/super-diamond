package com.github.diamond.support;

import com.github.diamond.utils.EnvUtil;

import java.io.IOException;
import javax.servlet.*;

/**
 * Author: yuwang@iflytek.com
 * Date: 2016/7/1 12:08
 */
public class VersionFilter  implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setAttribute("version", EnvUtil.getBuildVersion());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
