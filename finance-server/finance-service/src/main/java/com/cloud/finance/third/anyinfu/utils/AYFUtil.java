package com.cloud.finance.third.anyinfu.utils;

import com.cloud.finance.common.utils.GetUtils;
import com.cloud.finance.third.hangzhou.utils.XmlUtil;
import com.cloud.sysconf.common.utils.finance.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AYFUtil {

    private static Logger logger = LoggerFactory.getLogger(AYFUtil.class);

    public static String getToken(Map<String, String> data, String key, String loginURL) {

        // 登录签名
        String loginSign = "";
        try {
            loginSign = MD5.MD5Encode(data.get("appid") + key + data.get("random"));
            logger.info("[anyinfu login sign]: " + loginSign);
            data.put("sign", loginSign);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[anyinfu login sign exception]");
        }

        try {
            String loginResult = GetUtils.sendGetMethod(loginURL, data);
            logger.info("[anyinfu login result]: " + loginResult);

            Map<String, String> respMap = XmlUtil.xmlToMap(loginResult);
            return respMap != null ? respMap.get("token") : null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[anyinfu send POST request exception]");
            return "";
        }
    }
}
