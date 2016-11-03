package com.github.diamond.utils;

import java.util.List;
import java.util.Map;

/**
 * Created by weiwang15 on 2016/6/20.
 */
public class ProjectIdUtil {
    public static boolean isIdExistsInCommonId(int projectId, List<Map<String, Object>> commonId) {
        boolean isExists = false;

        if(commonId != null) {
            for (int i = 0; i < commonId.size(); i++) {
                int value = Integer.valueOf(String.valueOf(commonId.get(i).get("ID")));
                if (projectId == value) {
                    isExists = true;
                    break;
                }
            }
        }

        return isExists;
    }
}
