package com.cloud.finance.third.ainong.vo;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.sysconf.common.utils.StringUtil;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString(callSuper = true)
public class AilongDfRespData extends BaseHeadRespData {
    private String cutDate = "";
    private String txnAmt = "";
    private String fee = "";
    private String remark = "";
    private String extend1 = "";
    private String extend2 = "";
    private String extend3 = "";

    public String doSign(String key) {
        StringBuilder sb = new StringBuilder();
        sb.append("partnerNo=").append(this.getHead().getPartnerNo());
        if (StringUtil.isNotEmpty(this.getHead().getTxnCode())) {
            sb.append("&txnCode=").append(this.getHead().getTxnCode());
        }
        if (StringUtil.isNotEmpty(this.getHead().getTraceId())) {
            sb.append("&traceId=").append(this.getHead().getTraceId());
        }
        if (StringUtil.isNotEmpty(this.getHead().getRespTime())) {
            sb.append("&respTime=").append(this.getHead().getRespTime());
        }
        if (StringUtil.isNotEmpty(this.getHead().getRespCode())) {
            sb.append("&respCode=").append(this.getHead().getRespCode());
        }
        if (StringUtil.isNotEmpty(this.getHead().getRespMsg())) {
            sb.append("&respMsg=").append(this.getHead().getRespMsg());
        }
        if (StringUtil.isNotEmpty(this.getCutDate())) {
            sb.append("&cutDate=").append(this.getCutDate());
        }
        if (StringUtil.isNotEmpty(this.getTxnAmt())) {
            sb.append("&txnAmt=").append(this.getTxnAmt());
        }
        if (StringUtil.isNotEmpty(this.getFee())) {
            sb.append("&fee=").append(this.getFee());
        }
        sb.append(key);
        return sb.toString();
    }
}
