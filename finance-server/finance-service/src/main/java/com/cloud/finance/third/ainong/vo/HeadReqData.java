package com.cloud.finance.third.ainong.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author cmt
 */
@ToString(callSuper = true)
public class HeadReqData {


    private static final long serialVersionUID = 1L;
    @Getter
    @Setter

    private String reqDate = "";
    @Getter
    @Setter

    private String reqTime = "";
    @Getter
    @Setter

    private String version = "";
    @Getter
    @Setter

    private String charset = "";
    @Getter
    @Setter

    private String partnerNo = "";
    @Getter
    @Setter

    private String partnerType = "";

    @Getter
    @Setter

    private String txnCode = "";
    @Getter
    @Setter

    private String traceId = "";

}
