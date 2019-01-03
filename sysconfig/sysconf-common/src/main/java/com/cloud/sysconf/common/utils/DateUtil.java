package com.cloud.sysconf.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.StringUtils;


/**
 * 类说明 时间工具类
 * 
 */
public class DateUtil {

	public static String DATE_PATTERN_01 = "yyyy-MM-dd HH:mm:ss";
	public static String DATE_PATTERN_02 = "yyyy-MM-dd";
	public static String DATE_PATTERN_03 = "yyyy年MM月dd日";
	public static String DATE_PATTERN_04 = "MM月dd日";
	public static String DATE_PATTERN_05 = "yyyy-MM-dd-HH-mm";
	public static String DATE_PATTERN_06 = "yyyy-MM";
	public static String DATE_PATTERN_07 = "HH:mm";
	public static String DATE_PATTERN_08 = "yyyy-MM";
	public static String DATE_PATTERN_09 = "mm";
	public static String DATE_PATTERN_10 = "yyyy";
	public static String DATE_PATTERN_11 = "yyyyMMdd";
	public static String DATE_PATTERN_12 = "dd";
	public static String DATE_PATTERN_13 = "yyyyMMddHHmmssSS"; // 精确到毫秒
	public static String DATE_PATTERN_14 = "yyyy-MM-dd HH:mm";
	public static String DATE_PATTERN_15 = "yyMMddHH";
	public static String DATE_PATTERN_16 = "yyMMdd";
	public static String DATE_PATTERN_17 = "yyMMddHHmmss";
	public static String DATE_PATTERN_18 = "yyyyMMddHHmmss";
	public static String DATE_PATTERN_19 = "yyMMddHHmmssSS";

	/**
	 * 两个时间点的间隔小时数
	 * 
	 * @param beginTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @return
	 */
	public static Long countHour(Date beginTime, Date endTime) {
		// 除以1000是为了转换成秒
		long between = (endTime.getTime() - beginTime.getTime()) / 1000;
		long hour = between / 3600;
		// 另外的算法
		// long day1 = between / (24 * 3600);天
		// long hour1 = between % (24 * 3600) / 3600; 小时
		// long minute1 = between % 3600 / 60; 分
		// long second1 = between % 60; 秒
		// System.out.println("" + day1 + "天" + hour1 + "小时" + minute1 + "分"
		// + second1 + "秒");
		return hour;
	}

	/**
	 * 两个时间点的间隔天数
	 * 
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public static Long countDay(Date beginTime, Date endTime) {
//		System.out.println("beginTime==" + beginTime + ",endTime==" + endTime);
		Long day = countHour(beginTime, endTime) / 24;
		return day;
	}

	/**
	 * 把时间转换成长整型
	 * 
	 * @param parameter
	 * @param pattern
	 * @return
	 */
	public static Long getLong(String parameter, String pattern) {
		if (parameter != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				Date date = sdf.parse(parameter);
				return date.getTime();
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static Long getLong(Date date, String pattern) {
		if (date != null) {
			try {
				return date.getTime();
			} catch (Exception e) {
//				System.out.println("转为长整型的时间异常");
				return null;
			}
		}
		return null;
	}

	/**
	 * 根据传入日期返回日期字符串
	 * 
	 * @param date
	 *            日期参数
	 * @param pattern
	 *            格式化类型
	 * @return 格式化的日期字符串
	 */
	public static String DateToString(Date date, String pattern) {
		if(date!=null){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			return simpleDateFormat.format(date);
		}else{
			return "";
		}
	}

	public static Date stringToDate(String str, String pattern) {
		try {
			if (str != null && !"".equals(str)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				return simpleDateFormat.parse(str);
			}
		} catch (ParseException e) {
			return new Date();
			// SimpleDateFormat simpleDateFormat = new
			// SimpleDateFormat(DateUtil.DATE_PATTERN_01);
			// Date date = new Date();
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获得系统时间
	 * 
	 * @param pattern
	 *            时间格式
	 * @return
	 */
	public static String getSystemTime(String pattern) {
		// 获得系统时间
		Date date = new Date();
		String systemTime = new SimpleDateFormat(pattern).format(date);
		return systemTime;
	}

	/**
	 * 得到前几天的时间
	 * 
	 * @param d
	 *            当前时间
	 * @param day
	 *            具体的天数
	 * @return
	 */
	public static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	/**
	 * 得到几天后的时间
	 * 
	 * @param d
	 *            当前时间
	 * @param day
	 *            几天后的时间
	 * @return
	 */
	public static Date getDateAfter(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime();
	}

	/**
	 * 根据指定时间,获取当前时间周的开始日期
	 * 
	 * @param date
	 * @return
	 */
	public static Date getWeekStartDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int weekday = c.get(7) - 1;
		c.add(5, -weekday);
		return c.getTime();
	}

	/**
	 * 根据指定的日期,获取当前时间周的结束日期
	 * 
	 * @param date
	 * @return
	 */
	public static Date getWeekEndDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int weekday = c.get(7) - 1;
		c.add(5, -weekday);
		c.add(5, 6);
		return c.getTime();
	}

	/**
	 * 根据指定的日期，获取当前时间的下周一的日期
	 * 
	 * @author hong
	 * @param date
	 * @return
	 */
	public static Date getNextMonday(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int weekday = c.get(7) - 1;
		c.add(5, -weekday);
		c.add(5, 8);
		return c.getTime();
	}

	/**
	 * 根据指定的日期，获取当前时间的上周周一的日期
	 * 
	 * @author hong
	 * @param date
	 * @return
	 */
	public static Date getPreviousWeekday(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int weekday = c.get(7) - 1;
		c.add(5, -weekday);
		c.add(5, -6);
		return c.getTime();
	}

	/**
	 * 根据指定的日期，获取当前时间的上周周日的日期
	 * 
	 * @author hong
	 * @param date
	 * @return
	 */
	public static Date getPreviousWeekSunday(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int weekday = c.get(7) - 1;
		c.add(5, -weekday);
		c.add(5, 0);
		return c.getTime();
	}

	/**
	 * 获取本月第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getMonthStartDate(Date date) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		return lastDate.getTime();
	}

	/**
	 * 获取本月的最后一天
	 * 
	 * @return
	 */
	public static Date getMonthEndDate(Date date) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
		lastDate.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天
		return lastDate.getTime();
	}

	/**
	 * 获取当月的天数
	 * @param date
	 * @return
	 */
	public static int getDayOfMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取上月第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getPreviousMonthFirst(Date date) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, -1);// 减一个月，变为上月的1号
		return lastDate.getTime();
	}

	/**
	 * 获取上月最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getPreviousMonthEnd(Date date) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.DATE, -1);// 减去一天，变为上月最后一天
		return lastDate.getTime();
	}

	/**
	 * 添加分数
	 * 
	 * @param date
	 *            日期
	 * @param minute
	 *            需要添加的分数值
	 * @return
	 */
	public static Date addMinute(Date date, int minute) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.add(Calendar.MINUTE, minute);// 添加分钟
		return lastDate.getTime();
	}

	/**
	 * 减少时间分数
	 * 
	 * @param date
	 *            日期
	 * @param minute
	 *            需要减少的分数值
	 * @return
	 */
	public static Date cutMinute(Date date, int minute) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.add(Calendar.MINUTE, -minute);// 添加分钟
		return lastDate.getTime();
	}

	// 获取上月当前系统时间
	public static Date getLastDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}

	/**
	 * 获得上月当前系统时间,并转换成日期的格式
	 * 
	 * @param date
	 *            需要转换的日期
	 * @param pattern
	 * @return
	 */
	public static Date getLastDate(Date date, String pattern) {
		String lastMonth = DateUtil.DateToString(DateUtil.getLastDate(date), pattern);
		return DateUtil.stringToDate(lastMonth, pattern);
	}

	public static String getChineseWeek(Calendar date) {
		final String dayNames[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
//		System.out.println("dayOfWeek:" + dayOfWeek);
		return dayNames[dayOfWeek - 1];
	}

	public static Date StringToDate(Object obj, String format) {
		if (obj == null)
			return null;
		String date = obj.toString().trim();
		if (StringUtil.isEmpty(date))
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(date.trim());
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date StringToDate2_NewDate(Object obj, String format) {
		if (obj == null)
			return new Date();
		String date = obj.toString().trim();
		if (StringUtil.isEmpty(date))
			return new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(date.trim());
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	/**
	 	中介模块中：通过开始时间跟周期， 算出订单结束时间
	 */
	public static Date countEndDate(Date date,String time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(StringUtils.isEmpty(time))//参数错误， 默认15天
			cal.add(Calendar.DAY_OF_MONTH, 15);
		else if(time.indexOf("天")!=-1)//选择天
			cal.add(Calendar.DAY_OF_MONTH, NumberUtils.createInteger(time.substring(0,time.length()-1)));
		else if(time.indexOf("个月")!=-1)//选择天
			cal.add(Calendar.MONTH, NumberUtils.createInteger(time.substring(0,time.length()-2)));
		else 
			cal.add(Calendar.DAY_OF_MONTH, 15);
		return cal.getTime();
	}
	
	/**
	 * 计算工作日，不包含节假日
	 * @param date
	 * @param adddays
	 * @return
	 */
	public static Date addDateByWorkDay(Date date,int adddays){
		boolean holidayFlag = false;
		Calendar srcDate = Calendar.getInstance();
		srcDate.setTime(date);
		if(adddays > 0){
			for (int i = 0; i < adddays; i++){
				srcDate.add(Calendar.DAY_OF_MONTH, 1);
				holidayFlag = checkHoliday(srcDate);
				if(holidayFlag){
					i--;
				}
			}
		}else{
			for (int i = 0; i > adddays; i--){
				srcDate.add(Calendar.DAY_OF_MONTH, -1);
				holidayFlag = checkHoliday(srcDate);
				if(holidayFlag){
					i++;
				}
			}
		}
		return srcDate.getTime();
	}
	
	private static boolean checkHoliday(Calendar srcDate) {
		int currentDay = srcDate.get(Calendar.DAY_OF_WEEK);
//		System.out.println("当前日期=="+currentDay);
		//判断是否星期六
		if(currentDay == Calendar.SATURDAY || 
									currentDay == Calendar.SUNDAY) {
			 return true;
		}
		return false;
	}

	/**
	 * 获取指定日期所在年第一天日期
	 * @param date 指定日期
	 * @return Date
	 */
	public static Date getYearFirst(Date date){
		Calendar currCal = Calendar.getInstance();
		currCal.setTime(date);
		int currentYear = currCal.get(Calendar.YEAR);

		currCal.clear();
		currCal.set(Calendar.YEAR, currentYear);
		Date currYearFirst = currCal.getTime();
		return currYearFirst;
	}

	/**
	 * 获取指定日期所在年最后一天日期
	 * @param date 指定日期
	 * @return Date
	 */
	public static Date getYearLast(Date date){
		Calendar currCal = Calendar.getInstance();
		currCal.setTime(date);
		int currentYear = currCal.get(Calendar.YEAR);

		currCal.clear();
		currCal.set(Calendar.YEAR, currentYear);
		currCal.roll(Calendar.DAY_OF_YEAR, -1);
		Date currYearLast = currCal.getTime();

		return currYearLast;
	}

}
