
package com.cloud.finance.third.ainong.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author cmt
 * @E-mail:29572320@qq.com
 * @version Create on: 2017年5月2日 上午7:20:31 Class description
 */
@ToString(callSuper = true)
public class BaseHeadRespData implements Serializable {

	private static final long serialVersionUID = 1L;

	//@Valid
	@Getter
	@Setter
	private HeadRespData head;

}
