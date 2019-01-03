package com.cloud.finance.common.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class SafeComputeUtils {
	
	//ROUND_HALF_UP:如果被舍弃部分 >= 0.5，则舍入行为同 ROUND_UP；否则舍入行为同ROUND_DOWN。注意，此舍入模式就是通常学校里讲的四舍五入
	
	public static Double add(Double d1, Double d2) {
		d1 = (d1==null)?new Double(0):d1;
		d2 = (d2==null)?new Double(0):d2;
		BigDecimal db1 = new BigDecimal(Double.toString(d1));
		BigDecimal db2 = new BigDecimal(Double.toString(d2));
		
		return Double.valueOf(db1.add(db2).toString());
	}
	public static Double div(Double d1, Double d2){
		return div(d1, d2, BigDecimal.ROUND_HALF_UP);
	}
	public static Double div(Double d1, Double d2,int roundingMode){
		d1 = (d1==null)?new Double(0):d1;
		d2 = (d2==null)?new Double(0):d2;
		if(d2.compareTo(new Double(0))==0){
			return new Double(0);
		}
		BigDecimal db1 = new BigDecimal(Double.toString(d1));
		BigDecimal db2 = new BigDecimal(Double.toString(d2));
		return Double.valueOf(db1.divide(db2,8,roundingMode).toString());
	}
	public static Double multiply(Double d1, Double d2) {
		return multiply(d1, d2, BigDecimal.ROUND_HALF_UP);
	}
	public static Double multiply(Double d1, Double d2,int roundingMode) {
		d1 = (d1==null)?new Double(0):d1;
		d2 = (d2==null)?new Double(0):d2;
		BigDecimal db1 = new BigDecimal(Double.toString(d1));
		BigDecimal db2 = new BigDecimal(Double.toString(d2));
		
		return Double.valueOf(db1.multiply(db2).setScale(8,roundingMode).toString());
	}
	public static Double sub(Double d1, Double d2){
		d1 = (d1==null)?new Double(0):d1;
		d2 = (d2==null)?new Double(0):d2;
		BigDecimal db1 = new BigDecimal(Double.toString(d1));
		BigDecimal db2 = new BigDecimal(Double.toString(d2));
		
		return Double.valueOf(db1.subtract(db2).toString());
	}
	
	/**
	 * 分以下舍位
	 * @param d1
	 * @return
	 */
	public static Double round2Down(Double d1){
		d1 = (d1==null)?new Double(0):d1;
		BigDecimal db1 = new BigDecimal(Double.toString(d1));
		
		return Double.valueOf(db1.setScale(2,BigDecimal.ROUND_DOWN).toString());
	}

	/**
	 * 价格格式化  返回两位小数的字符串
	 * @param number
	 * @return
	 */
	public static String numberFormate(Double number){
		DecimalFormat df = new DecimalFormat("#.00");
		return df.format(number);
	}

	/**
	 * 数字格式化 返回整数
	 * @param number
	 * @return
	 */
	public static String numberFormate2(Double number){
		DecimalFormat df = new DecimalFormat("#");
		return df.format(number);
	}
}
