/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.client.util;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Create on @2013-8-26 @下午4:59:50 
 * @author bsli@ustcinfo.com
 */
public class FileUtils {

    /**
     * 备份数据到本地.
     */
    public static void saveData(String projCode, String profile, String data, String localFilePath) {
        File file = null;
        if (StringUtils.isBlank(localFilePath)) {
            String userHome = System.getProperty("user.home");
            String dirStr = userHome + File.separator + ".superdiamond" + File.separator + projCode + File.separator + profile;
            File dir = new File(dirStr);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(dirStr + File.separator + "data.properties");
        } else if (StringUtils.contains(localFilePath, "classpath") && localFilePath.indexOf("classpath") == 0) {
            String str = FileUtils.class.getResource("/").getPath() + localFilePath.substring(localFilePath.lastIndexOf(":") + 1);
            file = new File(str);
        } else {
            int start = localFilePath.lastIndexOf(".properties");
            String str = null;
            if (start != -1) {
                str = localFilePath.substring(start);
            } else {
                //todo 抛异常
            }
            if (str != null && ".properties".equals(str)) {
                file = new File(localFilePath);
                if (!file.getAbsoluteFile().getParentFile().exists()) {
                    file.getAbsoluteFile().getParentFile().mkdirs();
                }
            } else {
                //todo 抛异常
            }
        }
        try {
            FileUtils.writeStringToFile(file, data, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeStringToFile(File file, String data, Charset encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    public static void writeStringToFile(File file, String data, Charset encoding, boolean append) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file, append);
            if (data != null) {
                out.write(data.getBytes(encoding));
            }
            out.close(); // don't swallow close Exception if copy completes normally
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    public static String readConfigFromLocal(String projCode, String profile, String localFilePath) throws IOException {
        BufferedReader br = null;
        File file = null;
        if (StringUtils.isBlank(localFilePath)) {
            String userHome = System.getProperty("user.home");
            String fileStr = userHome + File.separator + ".superdiamond" + File.separator + projCode
                    + File.separator + profile + File.separator + "data.properties";
            file = new File(fileStr);
        } else if (StringUtils.contains(localFilePath, "classpath") && localFilePath.indexOf("classpath") == 0) {
            String str = FileUtils.class.getResource("/").getPath() + localFilePath.substring(localFilePath.lastIndexOf(":") + 1);
            file = new File(str);
        } else {
            file = new File(localFilePath);
        }
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String data = null;
            while ((data = br.readLine()) != null) {
                sb.append(data).append("\r\n");
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        return sb.toString();
    }
}
