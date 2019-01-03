package com.cloud.finance.third.ainong.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author cmt
 * @E-mail:29572320@qq.com
 * @Date: Create in  2017/11/15 11:33
 * @Version: 1.0.0
 * @Modify by:
 * Class description
 */
@ToString(callSuper=true)
public class H5NotifyData extends BaseHeadRespData {

    private static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private  String amount = "";
    @Getter
    @Setter
    private String extend1;
    @Getter
    @Setter
    private String extend2;
    @Getter
    @Setter
    private String extend3;

    public String sign(String key){
        StringBuffer sb = new StringBuffer();
        sb.append("partnerNo=").append(this.getHead().getPartnerNo())
                .append("&txnCode=").append(this.getHead().getTxnCode())
                .append("&traceId=").append(this.getHead().getTraceId())
                .append("&respTime=").append(this.getHead().getRespTime())
                .append("&respCode=").append(this.getHead().getRespCode())
                .append("&respMsg=").append(this.getHead().getRespMsg())
                .append("&amount=").append(this.getAmount())
                .append(key);
        return sb.toString();
    }
}
