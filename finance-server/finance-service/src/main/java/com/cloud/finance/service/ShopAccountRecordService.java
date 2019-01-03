package com.cloud.finance.service;

import com.cloud.finance.common.dto.ChannelSummaryDto;
import com.cloud.finance.po.ShopAccountRecord;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.vo.ReturnVo;

import java.util.Date;
import java.util.List;

/**
 * 账户账变的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface ShopAccountRecordService extends BaseMybatisService<ShopAccountRecord, String> {

    /**
     * 商户添加账变记录(重置或代付)
     * @param type          账变类型 1：支付入账  2：代付出账  3：风控冻结  4：解除风控冻结
     * @param shopPay       账变类型为3时 传null
     * @param shopRecharge  账变类型为1、2时 传null
     * @param status        状态  1：账变处理中  2：账变完成 3：账变失败
     */
    void addRecord(Integer type, ShopPay shopPay, ShopRecharge shopRecharge, Integer status);

    /**
     * 更新账变记录状态
     * @param unionOrderNo
     * @param status
     */
    ReturnVo updateRecordStatus(String unionOrderNo, Integer status);

    /**
     * 获取商户某个时间段内每个通道的统计数据
     * @param beginTime
     * @param endTime
     * @param sysUserId
     * @return
     */
    List<ChannelSummaryDto> channelSummary(Date beginTime, Date endTime, String sysUserId);

    /**
     * 更新step 1 -> 2
     */
    void updateStep();

    /**
     * 更新交易流水通道ID（代付成功或失败后需要更新）
     * @param unionOrderNo
     * @param channelId
     */
    void updateChannelId(String unionOrderNo, String channelId);
}
