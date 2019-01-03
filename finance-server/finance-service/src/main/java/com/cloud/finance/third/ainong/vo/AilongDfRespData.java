package com.cloud.finance.third.ainong.vo;

import lombok.Data;
import lombok.ToString;

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
        StringBuffer sb = new StringBuffer();
        sb.append("partnerNo=").append(this.getHead().getPartnerNo())
                .append("&txnCode=").append(this.getHead().getTxnCode())
                .append("&traceId=").append(this.getHead().getTraceId())
                .append("&respTime=").append(this.getHead().getRespTime())
                .append("&respCode=").append(this.getHead().getRespCode())
                .append("&respMsg=").append(this.getHead().getRespMsg())
                .append("&cutDate=").append(this.getCutDate())
                .append("&txnAmt=").append(this.getTxnAmt())
                .append("&fee=").append(this.getFee())
                .append(key);
        return sb.toString();
    }
}
