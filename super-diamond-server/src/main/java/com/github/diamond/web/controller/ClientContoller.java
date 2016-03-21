/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */

package com.github.diamond.web.controller;

import com.github.diamond.netty.DiamondServerHandler;
import com.github.diamond.netty.DiamondServerHandler.ClientInfo;
import com.github.diamond.netty.DiamondServerHandler.ClientKey;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Create on @2013-12-18 @上午11:44:10 
 * @author bsli@ustcinfo.com
 */
@Controller
public class ClientContoller extends BaseController {

    private static final String DATEFORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    @RequestMapping("/queryClients")
    public void queryClients(ModelMap modelMap) {
        List<Map<String, String>> clients = new ArrayList<Map<String, String>>();
        for (Entry<ClientKey, List<ClientInfo>> entry : DiamondServerHandler.clients.entrySet()) {
            ClientKey key = entry.getKey();
            String projcode = key.getProjCode();
            String modules = StringUtils.join(key.getModuleArr());
            String profile = key.getProfile();

            for (ClientInfo info : entry.getValue()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("projcode", projcode);
                map.put("modules", modules);
                map.put("profile", profile);
                map.put("address", info.getAddress().substring(1));
                map.put("connectTime", new SimpleDateFormat(DATEFORMAT_STRING).format(info.getConnectTime()));
                clients.add(map);
            }
        }

        modelMap.addAttribute("clients", clients);
    }
}
