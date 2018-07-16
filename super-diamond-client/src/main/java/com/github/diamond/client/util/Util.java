package com.github.diamond.client.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/2/29.
 */
public class Util {
    public static Map<String, String> findPlaceHolderVar(String key, String value) {
        Map<String, String> variableMap = new HashMap<>();
        List<String> list = new ArrayList<>();
        while (value.length() > 3) {
            int start = org.apache.commons.lang.StringUtils.indexOf(value, "${");
            if (start != -1 && start < value.length() - 1) {
                int end = org.apache.commons.lang.StringUtils.indexOf(value, '}',start);
                String var = value.substring(start + 2, end);
                list.add(var);
                if (end != -1 && end < value.length() - 2) {
                    value = value.substring(end + 1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (list.size() > 0) {
            String variables = StringUtils.join(list, ",");
            variableMap.put(key, variables);
        }
        return variableMap;
    }

    public static String replaceVarWithValue(Map<String, String> store, String variables, String str) {
        String[] varArray = StringUtils.split(variables, ",");
        for (int i = 0; i < varArray.length; i++) {
            String varValue = store.get(varArray[i]);
            int start = org.apache.commons.lang.StringUtils.indexOf(str, "${");
            int end = org.apache.commons.lang.StringUtils.indexOf(str, '}',start);
            String var = str.substring(start, end + 1);
            str = org.apache.commons.lang.StringUtils.replace(str, var, varValue);
        }
        return str;
    }

    public static String replaceVarWithValue(Map<String, String> store, String variables, String str, String key, String realValue) {
        String[] varArray = StringUtils.split(variables, ",");
        for (int i = 0; i < varArray.length; i++) {
            String varValue = null;
            if (key.equals(varArray[i])) {
                varValue = realValue;
            } else {
                varValue = store.get(varArray[i]);
            }
            int start = org.apache.commons.lang.StringUtils.indexOf(str, "${");
            int end = org.apache.commons.lang.StringUtils.indexOf(str, '}',start);
            String var = str.substring(start, end + 1);
            str = org.apache.commons.lang.StringUtils.replace(str, var, varValue);
        }
        return str;
    }
}
