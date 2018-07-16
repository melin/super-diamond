package com.github.diamond.support;

import com.github.diamond.utils.EnvUtil;

import java.io.IOException;
import javax.servlet.*;


/**
 * 打印工程版本信息
 *
 * @author libinsong1204@gmail.com
 * @date 2012-3-1 下午1:20:50
 */
@SuppressWarnings("serial")
public class PrintProjectVersionServlet extends GenericServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("项目名称：").append(EnvUtil.getProjectName()).append(", ");
        stringBuilder.append("项目版本：").append(EnvUtil.getBuildVersion()).append(", ");
        stringBuilder.append("构建时间：").append(EnvUtil.getBuildTime()).append(".\n");

        System.out.println("====================================================="
                + "======================================================================");
        String info = stringBuilder.toString();
        System.out.println(info);
        System.out.println("===================================================================="
                + "=======================================================");
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
    }
}
