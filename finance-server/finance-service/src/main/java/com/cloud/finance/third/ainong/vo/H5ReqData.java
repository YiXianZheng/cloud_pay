package com.cloud.finance.third.ainong.vo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author cmt
 * @E-mail:29572320@qq.com
 * @Date: Create in  2017/11/13 14:00
 * @Version: 1.0.0
 * @Modify by:
 * Class description
 */
@ToString(callSuper = true)
@Getter
@Setter
public class H5ReqData extends BaseHeadReqData {

    private static final long serialVersionUID = 1L;

    private String notifyUrl = "";
    private String returnUrl = "";
    private String body = "";
    private String amount = "";

    public String text(String key) {
        StringBuffer sb = new StringBuffer();
        sb.append("partnerNo=").append(this.getHead().getPartnerNo())
                .append("&txnCode=").append(this.getHead().getTxnCode())
                .append("&traceId=").append(this.getHead().getTraceId())
                .append("&reqTime=").append(this.getHead().getReqTime())
                .append("&amount=").append(this.getAmount())
                .append("&notifyUrl=").append(this.getNotifyUrl())
                .append("&returnUrl=").append(this.getReturnUrl())
                .append(key);
        return sb.toString();
    }
}
