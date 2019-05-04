package com.cloud.finance.third.moshang.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * @description: 判断是否json字符串
 * @author: zyx
 * @create: 2019-04-16 14:26
 **/
public class MSUtils {
    public static boolean isJson(String content) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
