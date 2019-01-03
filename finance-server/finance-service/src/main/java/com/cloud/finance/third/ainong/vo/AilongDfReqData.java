package com.cloud.finance.third.ainong.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
@Setter
public class AilongDfReqData extends BaseHeadReqData {
    private String accountNo = "";
    private String accountName = "";
    private String bankName = "";
    private String callBackUrl = "";
    private String bankBranchNo = "";
    private String txnAmt = "";
    private String extend1;
    private String extend2;
    private String extend3;

    public String doSign(String key) {
        StringBuffer sb = new StringBuffer();
        sb.append("partnerNo=").append(this.getHead().getPartnerNo())
                .append("&txnCode=").append(this.getHead().getTxnCode())
                .append("&traceId=").append(this.getHead().getTraceId())
                .append("&reqTime=").append(this.getHead().getReqTime())
                .append("&accountNo=").append(this.getAccountNo())
                .append("&accountName=").append(this.getAccountName())
                .append("&txnAmt=").append(this.getTxnAmt())
                .append("&bankName=").append(this.getBankName())
                .append(key);
        return sb.toString();
    }
}
