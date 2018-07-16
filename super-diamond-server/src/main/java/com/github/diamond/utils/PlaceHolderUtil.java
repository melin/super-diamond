package com.github.diamond.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/2/29.
 */
@Component
public class PlaceHolderUtil {
    public static Map<String, String> findPlaceHolderVar(String key, String value) {
        Map<String, String> variableMap = new HashMap<>();
        List<String> list = new ArrayList<>();
        while (value.length() > 3) {
            int start = StringUtils.indexOf(value, "${");
            if (start != -1 && start < value.length() - 1) {
                int end = StringUtils.indexOf(value, '}',start);
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

    public static String findPlaceHolderVar(String value) {
        List<String> list = new ArrayList<>();
        String variables = null;
        while (value.length() > 3) {
            int start = StringUtils.indexOf(value, "${");
            if (start != -1 && start < value.length() - 1) {
                int end = StringUtils.indexOf(value, '}',start);
                String var = value.substring(start + 2, end);
                if(var.indexOf("sys:") == -1 && var.indexOf("env:") == -1) {
                    list.add(var);
                }
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
            variables = StringUtils.join(list, ",");
        }
        return variables;
    }

    public static String replaceVarWithValue(Map<String, String> store, String variables, String str) {
        String[] varArray = StringUtils.split(variables, ",");
        for (int i = 0; i < varArray.length; i++) {
            String varValue = store.get(varArray[i]);
            if (varValue != null) {
                String var = "${" + varArray[i] + "}";
                str = StringUtils.replace(str, var, varValue);
            }
        }
        return str;
    }

    public static String replaceVarWithValue(Map<String, String> store, String variables, String str, String[] encryptPropNameArr) {
        String[] varArray = StringUtils.split(variables, ",");
        String varValue;
        for (int i = 0; i < varArray.length; i++) {
            if (ArrayUtils.contains(encryptPropNameArr, varArray[i])) {
                varValue = "$[" + store.get(varArray[i]) + "]";
            } else {
                varValue = store.get(varArray[i]);
            }
            if (varValue != null) {
                String var = "${" + varArray[i] + "}";
                str = StringUtils.replace(str, var, varValue);
            }
        }
        return str;
    }
}
