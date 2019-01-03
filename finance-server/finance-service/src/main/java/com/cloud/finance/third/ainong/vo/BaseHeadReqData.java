/**
 * @author cmt
 * @E-mail 29572320@qq.com
 * @version Created on: 2017年5月2日 上午7:20:10
 * Class description
 */
package com.cloud.finance.third.ainong.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author cmt
 * @E-mail:29572320@qq.com
 * @version Create on:  2017年5月2日 上午7:20:10
 * Class description
 */
@ToString(callSuper = true)
public class BaseHeadReqData implements Serializable {


    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private HeadReqData head;

}
