package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 财务概览  redis
 */
@Data
public class RedisFinanceDto extends BaseDto {

	private String userCode;				//商户号或代理号或平台（"000000"）

	/**
	 * 历史统计数据
	 */
	private Double historyTotalMoney;				//历史总交易额（成功）

	private Integer historyTotalOrder;				//历史订单总数

	private Integer historyTotalSuccessOrder;		//历史成功订单总数

	private Double historyTotalCharge;				//历史总利润

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

	private Integer dailyTotalPaid;				//总代付笔数

	private Integer dailyTotalSuccessPaid;		//总成功代付笔数

	private Integer dailyTotalRiskControlOrder;	//总风控订单数


	public static RedisFinanceDto map2Object(Map<String, String> map){
		RedisFinanceDto finance = new RedisFinanceDto();

		if(map == null || map.size() == 0){
			return finance;
		}

		if(StringUtils.isNotBlank(map.get("historyTotalRiskControlOrder"))) {
			finance.setHistoryTotalRiskControlOrder(Integer.parseInt(map.get("historyTotalRiskControlOrder")));
		}else{
			finance.setHistoryTotalRiskControlOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalSuccessOrder"))) {
			finance.setHistoryTotalSuccessOrder(Integer.parseInt(map.get("historyTotalSuccessOrder")));
		}else{
			finance.setHistoryTotalSuccessOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalSuccessPaid"))) {
			finance.setHistoryTotalSuccessPaid(Integer.parseInt(map.get("historyTotalSuccessPaid")));
		}else{
			finance.setHistoryTotalSuccessPaid(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalCharge"))) {
			finance.setHistoryTotalCharge(Double.parseDouble(map.get("historyTotalCharge")));
		}else{
			finance.setHistoryTotalCharge(0D);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalMoney"))) {
			finance.setHistoryTotalMoney(Double.parseDouble(map.get("historyTotalMoney")));
		}else{
			finance.setHistoryTotalMoney(0D);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalOrder"))) {
			finance.setHistoryTotalOrder(Integer.parseInt(map.get("historyTotalOrder")));
		}else{
			finance.setHistoryTotalOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("historyTotalPaid"))) {
			finance.setHistoryTotalPaid(Integer.parseInt(map.get("historyTotalPaid")));
		}else{
			finance.setHistoryTotalPaid(0);
		}



		if(StringUtils.isNotBlank(map.get("dailyTotalRiskControlOrder"))) {
			finance.setDailyTotalRiskControlOrder(Integer.parseInt(map.get("dailyTotalRiskControlOrder")));
		}else{
			finance.setDailyTotalRiskControlOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalSuccessOrder"))) {
			finance.setDailyTotalSuccessOrder(Integer.parseInt(map.get("dailyTotalSuccessOrder")));
		}else{
			finance.setDailyTotalSuccessOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalSuccessPaid"))) {
			finance.setDailyTotalSuccessPaid(Integer.parseInt(map.get("dailyTotalSuccessPaid")));
		}else{
			finance.setDailyTotalSuccessPaid(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalCharge"))) {
			finance.setDailyTotalCharge(Double.parseDouble(map.get("dailyTotalCharge")));
		}else{
			finance.setDailyTotalCharge(0D);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalMoney"))) {
			finance.setDailyTotalMoney(Double.parseDouble(map.get("dailyTotalMoney")));
		}else{
			finance.setDailyTotalMoney(0D);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalOrder"))) {
			finance.setDailyTotalOrder(Integer.parseInt(map.get("dailyTotalOrder")));
		}else{
			finance.setDailyTotalOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("dailyTotalPaid"))) {
			finance.setDailyTotalPaid(Integer.parseInt(map.get("dailyTotalPaid")));
		}else{
			finance.setDailyTotalPaid(0);
		}



		if(StringUtils.isNotBlank(map.get("totalRiskControlOrder"))) {
			finance.setTotalRiskControlOrder(Integer.parseInt(map.get("totalRiskControlOrder")));
		}else{
			finance.setTotalRiskControlOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("totalSuccessOrder"))) {
			finance.setTotalSuccessOrder(Integer.parseInt(map.get("totalSuccessOrder")));
		}else{
			finance.setTotalSuccessOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("totalSuccessPaid"))) {
			finance.setTotalSuccessPaid(Integer.parseInt(map.get("totalSuccessPaid")));
		}else{
			finance.setTotalSuccessPaid(0);
		}

		if(StringUtils.isNotBlank(map.get("totalCharge"))) {
			finance.setTotalCharge(Double.parseDouble(map.get("totalCharge")));
		}else{
			finance.setTotalCharge(0D);
		}

		if(StringUtils.isNotBlank(map.get("totalMoney"))) {
			finance.setTotalMoney(Double.parseDouble(map.get("totalMoney")));
		}else{
			finance.setTotalMoney(0D);
		}

		if(StringUtils.isNotBlank(map.get("totalOrder"))) {
			finance.setTotalOrder(Integer.parseInt(map.get("totalOrder")));
		}else{
			finance.setTotalOrder(0);
		}

		if(StringUtils.isNotBlank(map.get("totalPaid"))) {
			finance.setTotalPaid(Integer.parseInt(map.get("totalPaid")));
		}else{
			finance.setTotalPaid(0);
		}

		finance.setUserCode(map.get("userCode"));

		return finance;
	}

}