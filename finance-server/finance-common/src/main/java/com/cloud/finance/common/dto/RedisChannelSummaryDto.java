package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 商户区分通道的财务统计  redis
 */
@Data
public class RedisChannelSummaryDto extends BaseDto {

	/**
	 * 历史统计数据
	 */
	private Double historyTotalMoney;				//历史总交易额（成功）

	private Integer historyTotalOrder;				//历史订单总数

	private Integer historyTotalSuccessOrder;		//历史成功订单总数

	private Double historyTotalCharge;				//历史总利润

    private Double historyTotalPaidMoney;			//历史总代付金额

	private Integer historyTotalPaid;				//历史总代付笔数

	private Integer historyTotalSuccessPaid;		//历史总成功代付笔数

	private Integer historyTotalRiskControlOrder;	//历史总风控订单数


	/**
	 * 近一个月除去今天的统计数据
	 */
	private Double totalMoney;				//总交易额

	private Integer totalOrder;				//订单总数

	private Integer totalSuccessOrder;		//成功订单总数

	private Double totalCharge;				//总利润

    private Double totalPaidMoney;			//历史总代付金额

	private Integer totalPaid;				//总代付笔数

	private Integer totalSuccessPaid;		//总成功代付笔数

	private Integer totalRiskControlOrder;	//总风控订单数


	/**
	 * 今天的统计数据
	 */
	private Double dailyTotalMoney;				//总交易额

	private Integer dailyTotalOrder;			//订单总数

	private Integer dailyTotalSuccessOrder;		//成功订单总数

	private Double dailyTotalCharge;			//总利润

    private Double dailyTotalPaidMoney;			//历史总代付金额

	private Integer dailyTotalPaid;				//总代付笔数

	private Integer dailyTotalSuccessPaid;		//总成功代付笔数

	private Integer dailyTotalRiskControlOrder;	//总风控订单数


	public static RedisChannelSummaryDto map2Object(Map<String, String> map){
		RedisChannelSummaryDto summary = new RedisChannelSummaryDto();

		if(map == null || map.size() == 0){
			return summary;
		}

		if(StringUtils.isNotBlank(map.get("historyTotalRiskControlOrder"))) {
			summary.setHistoryTotalRiskControlOrder(Integer.parseInt(map.get("historyTotalRiskControlOrder")));
		}else{
			summary.setHistoryTotalRiskControlOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalSuccessOrder"))) {
			summary.setHistoryTotalSuccessOrder(Integer.parseInt(map.get("historyTotalSuccessOrder")));
		}else{
			summary.setHistoryTotalSuccessOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalSuccessPaid"))) {
			summary.setHistoryTotalSuccessPaid(Integer.parseInt(map.get("historyTotalSuccessPaid")));
		}else{
			summary.setHistoryTotalSuccessPaid(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalCharge"))) {
			summary.setHistoryTotalCharge(Double.parseDouble(map.get("historyTotalCharge")));
		}else{
			summary.setHistoryTotalCharge(0D);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalMoney"))) {
			summary.setHistoryTotalMoney(Double.parseDouble(map.get("historyTotalMoney")));
		}else{
			summary.setHistoryTotalMoney(0D);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalOrder"))) {
			summary.setHistoryTotalOrder(Integer.parseInt(map.get("historyTotalOrder")));
		}else{
			summary.setHistoryTotalOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalPaid"))) {
			summary.setHistoryTotalPaid(Integer.parseInt(map.get("historyTotalPaid")));
		}else{
			summary.setHistoryTotalPaid(0);
		}

        if(StringUtils.isNotBlank(map.get("historyTotalPaidMoney"))) {
            summary.setHistoryTotalPaidMoney(Double.parseDouble(map.get("historyTotalPaidMoney")));
        }else{
            summary.setHistoryTotalPaidMoney(0D);
        }



		if(StringUtils.isNotBlank(map.get("dailyTotalRiskControlOrder"))) {
			summary.setDailyTotalRiskControlOrder(Integer.parseInt(map.get("dailyTotalRiskControlOrder")));
		}else{
			summary.setDailyTotalRiskControlOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalSuccessOrder"))) {
			summary.setDailyTotalSuccessOrder(Integer.parseInt(map.get("dailyTotalSuccessOrder")));
		}else{
			summary.setDailyTotalSuccessOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalSuccessPaid"))) {
			summary.setDailyTotalSuccessPaid(Integer.parseInt(map.get("dailyTotalSuccessPaid")));
		}else{
			summary.setDailyTotalSuccessPaid(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalCharge"))) {
			summary.setDailyTotalCharge(Double.parseDouble(map.get("dailyTotalCharge")));
		}else{
			summary.setDailyTotalCharge(0D);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalMoney"))) {
			summary.setDailyTotalMoney(Double.parseDouble(map.get("dailyTotalMoney")));
		}else{
			summary.setDailyTotalMoney(0D);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalOrder"))) {
			summary.setDailyTotalOrder(Integer.parseInt(map.get("dailyTotalOrder")));
		}else{
			summary.setDailyTotalOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalPaid"))) {
			summary.setDailyTotalPaid(Integer.parseInt(map.get("dailyTotalPaid")));
		}else{
			summary.setDailyTotalPaid(0);
		}

        if(StringUtils.isNotBlank(map.get("dailyTotalPaidMoney"))) {
            summary.setDailyTotalPaidMoney(Double.parseDouble(map.get("dailyTotalPaidMoney")));
        }else{
            summary.setDailyTotalPaidMoney(0D);
        }



		if(StringUtils.isNotBlank(map.get("totalRiskControlOrder"))) {
			summary.setTotalRiskControlOrder(Integer.parseInt(map.get("totalRiskControlOrder")));
		}else{
			summary.setTotalRiskControlOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("totalSuccessOrder"))) {
			summary.setTotalSuccessOrder(Integer.parseInt(map.get("totalSuccessOrder")));
		}else{
			summary.setTotalSuccessOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("totalSuccessPaid"))) {
			summary.setTotalSuccessPaid(Integer.parseInt(map.get("totalSuccessPaid")));
		}else{
			summary.setTotalSuccessPaid(0);
		}

		if(StringUtils.isNotBlank(map.get("totalCharge"))) {
			summary.setTotalCharge(Double.parseDouble(map.get("totalCharge")));
		}else{
			summary.setTotalCharge(0D);
		}

		if(StringUtils.isNotBlank(map.get("totalMoney"))) {
			summary.setTotalMoney(Double.parseDouble(map.get("totalMoney")));
		}else{
			summary.setTotalMoney(0D);
		}

		if(StringUtils.isNotBlank(map.get("totalOrder"))) {
			summary.setTotalOrder(Integer.parseInt(map.get("totalOrder")));
		}else{
			summary.setTotalOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("totalPaid"))) {
			summary.setTotalPaid(Integer.parseInt(map.get("totalPaid")));
		}else{
			summary.setTotalPaid(0);
		}

        if(StringUtils.isNotBlank(map.get("totalPaidMoney"))) {
            summary.setTotalPaidMoney(Double.parseDouble(map.get("totalPaidMoney")));
        }else{
            summary.setTotalPaidMoney(0D);
        }

		return summary;
	}

}