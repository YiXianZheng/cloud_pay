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
public class QueryRespData extends BaseHeadRespData {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String oriTraceId = "";
    @Getter
    @Setter
    private String oriPlatformId = "";
    @Getter
    @Setter
    private String amount = "";
    @Getter
    @Setter
    private String status = "";
    @Getter
    @Setter
    private String cutDate = "";
    @Getter
    @Setter
    private String extend1 = "";
    @Getter
    @Setter
    private String extend2 = "";
    @Getter
    @Setter
    private String extend3 = "";

    public String doSign(String key) {
        StringBuffer sb = new StringBuffer();
        sb.append("partnerNo=").append(this.getHead().getPartnerNo())
                .append("&txnCode=").append(this.getHead().getTxnCode())
                .append("&traceId=").append(this.getHead().getTraceId())
                .append("&respTime=").append(this.getHead().getRespTime())
                .append("&respCode=").append(this.getHead().getRespCode())
                .append("&respMsg=").append(this.getHead().getRespMsg())
                .append("&amount=").append(this.getAmount())
                .append("&oriTraceId=").append(this.getOriTraceId())
                .append("&status=").append(this.getStatus())
                .append("&cutDate=").append(this.getCutDate())
                .append(key);
        return sb.toString();
    }
}
