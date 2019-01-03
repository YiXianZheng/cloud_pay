package com.cloud.finance.third.guanjun.service.utils;

import com.cloud.finance.third.guanjun.service.GuanjunPayService;
import com.cloud.finance.third.moshang.utils.MD5;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GJSignUtil {

    private static Logger logger = LoggerFactory.getLogger(GuanjunPayService.class);

    public static String signData(Map<String, String> item, String secretKey) {
        if (item == null || "".equals(secretKey)) return "";
        SortedMap<String, String> map = new TreeMap<>(item);
        List<String> queryList = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() == null) continue;
            queryList.add(key + "=" + entry.getValue().toString());
        }
        String uri = StringUtils.join(queryList.iterator(), "&");
        uri = "&" + uri + secretKey;
        logger.info("guanjun channel pay sign before: " + uri);
        return MD5.MD5Encode(uri);
    }
}
