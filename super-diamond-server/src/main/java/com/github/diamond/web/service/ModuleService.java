package com.github.diamond.web.service;

import com.github.diamond.web.model.ConfigCheckResult;
import com.github.diamond.web.model.ConfigExportData;
import com.github.diamond.web.model.ModuleConfigId;
import com.github.diamond.web.model.ModuleIdExist;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sjpan on 2016/3/16.
 */
public interface ModuleService {
    List<Map<String, Object>> queryModules(int projectId);

    int save(int projectId, String name);

    boolean delete(int moduleId, int projectId);

    ModuleConfigId moduleConfigIdIsExist(String configName, String moduleName, int projectId);

    ModuleIdExist moduleIdIsExist(String moduleName, int projectId);

    void fillConfigExportJsonData(int projectId, int[] moduleIds, String type, ConfigExportData configExportData);

    String getConfigExportPropertiesInfo(int projectId, int[] moduleIds, String type);

    ConfigExportData getExportData(MultipartFile file) throws IOException;

    void getConfigCheckResult(MultipartFile file, int projectId, ConfigCheckResult checkResult) throws IOException;

    String getHandlerResult(String checkId,int operation,int projectId,String type,
                            HttpSession session,ConfigExportData exportData,
                            HashMap<String, ConfigExportData> IMPORT_CONFIG_MAP);

    boolean isExistModuleName(int projectId, String name);
}
