package com.cloud.merchant.common.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户类型
 * @Auther Toney
 * @Date 2018/8/7 16:52
 * @Description:
 */
@Getter
public enum MerchantTypeEnum {

    T0_TRADING_PAID("t0_trading_paid", "D0交易代付"),
    T1_TOP_UP_PAID("t1_top-up_paid", "T1充值代付"),
    BRANCH_SETTLEMENT_MERCHANTS("branch_settlement_merchants", "分润结算商户"),
    RECONCILIATION_RECHARGE_MERCHANTS("reconciliation_recharge_merchants", "调账充值商户"),
    COMMISSIONING_TEST_MERCHANTS("commissioning_test_merchants", "调试测试商户"),
    ;

    private String code;

    private String name;

    MerchantTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    public static List<Map<String,Object>> loadAgentType(){
        List<Map<String,Object>> list =new ArrayList<>();
        for (MerchantTypeEnum type : MerchantTypeEnum.values()){
            Map<String,Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("value", type.getName());
            list.add(map);
        }
        return list;
    }

    /**
     * 通过code获取name
     * @param code
     * @return
     */
    public static String getMerchantNameByCode(String code){
        if(StringUtils.isBlank(code)) return "";
        for (MerchantTypeEnum type : MerchantTypeEnum.values()){
            if(code.equals(type.code)){
                return type.name;
            }
        }
        return "";
    }
}
