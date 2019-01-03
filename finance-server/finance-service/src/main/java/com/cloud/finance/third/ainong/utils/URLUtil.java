package com.cloud.finance.third.ainong.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLUtil {
    private static Logger logger = LoggerFactory.getLogger(URLUtil.class);

    public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            logger.info(("toURLEncoded error:"+paramString));
            return "";
        }

        try
        {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        }
        catch (Exception localException)
        {
            logger.info(("toURLEncoded error:"+paramString), localException);
        }

        return "";
    }


    public static String toURLDecoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            logger.info(("toURLDecoded error:"+paramString));
            return "";
        }

        try
        {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLDecoder.decode(str, "UTF-8");
            return str;
        }
        catch (Exception localException)
        {
            logger.info(("toURLDecoded error:"+paramString), localException);
        }

        return "";
    }
}
