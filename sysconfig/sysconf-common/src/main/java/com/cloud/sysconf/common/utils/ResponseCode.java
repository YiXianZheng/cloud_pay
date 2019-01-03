package com.cloud.sysconf.common.utils;


import org.apache.commons.lang.StringUtils;

/**
 * 返回码<br>
 * <ul>
 * <li>可使用{@link #getCode()}获取Integer类型的状态码，或</li>
 * <li>可使用{@link #getExplain(LanguageEnum language)}获取错误原因说明</li>
 * <li>使用{@link #toString()}字符串类型的状态码</li>
 * </ul>
 * 本接口含有以下enum类：
 * <ul>
 * <li>{@link Base}基础返回码，码段(0~99)</li>
 * <li>{@link Parameter}参数类，码段(110-199)</li>
 * <li>{@link LoginRegister}登录权限类，码段(310-399)</li>
 * <p>
 * </ul>
 *
 * @author Anty
 */
public interface ResponseCode {

    /**
     * @return 状态码
     */
    int getCode();

    /**
     * @return 状态码对应的解释。
     */
    String getExplain(LanguageEnum language);

    /**
     * @return 将状态码转换为字符串返回。
     */
    String toString();

    /**
     * 基础返回码(0-99)
     */
    public enum Base implements ResponseCode {
        /**
         * 基础返回码--请求成功
         */
        SUCCESS(0, "请求成功", "Success"),
        /**
         * 基础返回码--错误
         */
        ERROR(1, "错误", "Error"),
        /**
         * 基础返回码--请求超时
         */
        REQUEST_TIMEOUT(2, "请求超时", "Request timeout"),
        /**
         * 基础返回码--系统通讯异常
         */
        NET_ERR(3, "系统通讯异常", "Net error"),
        /**
         * 基础返回码--接口不存在
         */
        API_NO_EXISTS(4, "接口不存在", "API NO EXISTS"),
        /**
         * 基础返回码--接口异常
         */
        API_ERR(5, "接口异常", "API error"),
        /**
         * 基础返回码--MybatisTooManyException
         */
        TOO_MANY_EXCEP(6, "查询结果不唯一", "TOO MANY Exception"),
        /**
         * 基础返回码--MybatisTooManyException
         */
        AUTH_ERR(7, "没有操作权限", "No operation permissions"),
        /**
         * 基础返回码--系统错误
         */
        SYSTEM_ERR(10, "系统繁忙，请稍候再试", "System is busy, please try again later");


        private final Integer code;
        private String zhExplain;
        private String enExplain;

        private Base(int code) {
            this.code = code;
        }

        private Base(Integer code, String zhExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
        }

        private Base(Integer code, String zhExplain, String enExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
            this.enExplain = enExplain;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code.toString();
        }

        @Override
        public String getExplain(LanguageEnum language) {
            if (language == LanguageEnum.zh_CN) {
                return zhExplain;
            } else {
                return enExplain;
            }
        }

    }

    /**
     * 请求参数错误提示返回码(1xx)
     */
    public enum Parameter implements ResponseCode {
        /**
         * 返回码--传入参数类--参数为空
         */
        NULL(101, "传入参数不能为空", "Incoming parameters cannot be empty"),
        /**
         * 返回码--传入参数类--无法解析
         */
        CANNOT_PARSE(102, "传入参数无法解析", "Unable to parse the incoming parameters"),
        /**
         * 返回码--传入参数类--格式错误
         */
        FORMAT_ERR(103, "传入参数格式错误", "Incoming parameters format error"),
        /**
         * 返回码--传入参数类--缺少必要参数
         */
        LACK(104, "缺少必要参数", "Missing Parameters "),
        /**
         * 返回码--传入参数类--参数值不合法
         */
        ILLEGAL(105, "传入参数值不合法", "Incoming parameters values are not legal"),
        /**
         * 返回码--传入参数类--参数值不合法
         */
        NOTMATCHPASSWORD(106, "新密码输入不一致", "New passwords not match"),
        /**
         * 返回码--传入参数类--交易密码不能为空
         */
        MISSINGCOINPASSWORD(107, "交易密码不能为空", "Missing coin password "),
        /**
         * 返回码--传入参数类--密码不能为空
         */
        MISSINGPASSWORD(108, "密码不能为空", "Missing password "),
        /**
         * 返回码--传入参数类--用户名不能为空
         */
        MISSINGUSERNAME(109, "用户名不能为空", "Missing user name "),
        /**
         * 返回码--传入参数类--验证码不能为空
         */
        MISSINGVSCODE(110, "验证码不能为空", "Missing vscode "),
        /**
         * 返回码--传入参数类--验证码不能为空
         */
        MISSINGNEWPASSWORD(111, "新密码不能为空", "Missing vscode "),
        /**
         * 返回码--传入参数类--所属银行不能为空
         */
        MISSINGBANKID(112, "所属银行不能为空", "Missing vscode "),
        /**
         * 返回码--传入参数类--其他错误
         */
        OTHERS(199, "参数错误", "Parameters error");

        private final Integer code;
        private String zhExplain;
        private String enExplain;

        private Parameter(int code) {
            this.code = code;
        }

        private Parameter(Integer code, String zhExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
        }

        private Parameter(Integer code, String zhExplain, String enExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
            this.enExplain = enExplain;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code.toString();
        }

        @Override
        public String getExplain(LanguageEnum language) {
            if (language == LanguageEnum.zh_CN) {
                return zhExplain;
            } else {
                return enExplain;
            }
        }
    }

    /**
     * 用户登录权限相关(310~399)
     */
    public enum LoginRegister implements ResponseCode {
        /**
         * 返回码--用户登录注册--手机号码不能为空
         */
        PHONE_EMPTY(310, "手机号码不能为空", "Phone number cannot be empty"),
        /**
         * 返回码--用户登录注册--密码不能为空
         */
        PASSWORD_EMPTY(311, "密码不能为空", "Password cannot be empty"),
        /**
         * 返回码--用户登录注册--用户不存在
         */
        USER_NO_EXISTS(312, "用户不存在", "The user does not exist"),
        /**
         * 返回码--用户登录注册--用户名已锁定
         */
        USER_LOCKED(313, "用户已锁定", "The user has been locked"),
        /**
         * 返回码--用户登录注册--用户名或密码错误，请重新输入
         */
        PWD_INPUT_ERROR(314, "用户名或密码错误，请重新输入", "User name or password error, please re-enter"),
        /**
         * 返回码--用户登录注册--密码输入错误次数已超过上限
         */
        PWD_INPUT_ERROR_LIMIT(315, "密码输入错误超过上限，请${time}分钟后重新输入", "Password error more than limit, please re-enter ${time} minutes"),
        /**
         * 返回码--用户登录注册--第三方账号未注册(补充注册时使用，不要提示文字)
         */
        THIRD_NO_REGISTER(316, "第三方账号未注册", "The third party account registered"),
        /**
         * 返回码--用户登录注册--手机号码已注册
         */
        PHONE_EXISTS(317, "手机号码已注册", "Registered mobile phone number"),
        /**
         * 返回码--用户登录注册--未登录
         */
        NOLOGIN(318, "未登录或登陆超时", "Not login or login timeout"),
        /**
         * 返回码--用户登录注册--昵称不能为空
         */
        NICKNAME_EMPTY(319, "昵称不能为空", "nick cannot be empty"),
        /**
         * 返回码--用户登录注册--国际码不能为空
         */
        SMSCODE_EMPTY(320, "国际码不能为空", "The international code cannot be empty"),
        /**
         * 返回码--用户登录注册--手机号码不合法
         */
        PHONE_NO_VALID(321, "手机号码不合法", "Phone number is no valid"),

        /**
         * 返回码--用户登录注册--密码不合法
         */
        PASSWORD_NO_VALID(322, "密码不合法", "Password is no valid"),

        /**
         * 返回码--用户登录注册--昵称不合法
         */
        NICK_NAME_NO_VALID(323, "昵称不合法", "Nick name is no valid"),
        /**
         * 返回码--用户登录注册--头像不能为空
         */
        HEADICON_EMPTY(324, "头像不能为空", "head icon cannot be empty"),
        /**
         * 返回码--忘记密码--验证码不能为空
         */
        VERIFY_CODE(325, "图形验证码不能为空", "verify code cannot be empty"),
        /**
         * 返回码--忘记密码--验证码错误
         */
        VERIFY_CODE_ERROR(326, "验证码错误", "verify code error"),
        /**
         * 返回码--用户登录注册--用户不存在
         */
        USER_EXIST(328, "用户名已存在", "The user name not verify"),
        /**
         * 返回码--旧取款密码错吾
         */
        COINPWD_INPUT_ERROR(314, "旧取款密码错误，请重新输入", "old coin password error, please re-enter"),
        /**
         * 返回码--获取desKey--获取失败
         */
        GET_KEY_ERROR(327, "获取Key失败", "get key fail"),
        /**
         * 返回码--设置取款密码--设置取款密码不一致
         */
        CIPHERS_INCONSISTENCY_ERROR(329,"设置取款密码不一致","Setting the inconsistency of the withdrawal ciphers"),
        /**
         * 返回码--用户登陆--登陆panId与注册panId不一致
         */
        USER_NO_EXISTS_THIS_PAN(330,"用户注册盘与登入盘不一致","he user does not exist this pan"),
        /**
         * 返回码--用户登陆--新密码与确认密码不一致
         */
        PASSWORD_NO_CONSISTENT(331,"新密码与确认密码不一致","The new password is not consistent with the confirmed password"),
        /**
         * 返回码--用户登陆--新密码与旧密码一致
         */
        PASSWORD_EQUALLY(332,"新密码与旧密码一致","The new password is in line with the old one"),
        /**
         * 返回码--用户登陆--密码错误
         */
        PASSWORD_NO_ERROR(333,"密码错误","Password error"),
        /**
         * 返回码--用户不属于此平台
         */
        USER_NO_EXISTS_THIS_PLATFORM(334,"用户不属于此平台","the user does not exists this platform");



        private final Integer code;
        private String zhExplain;
        private String enExplain;

        private LoginRegister(int code) {
            this.code = code;
        }

        private LoginRegister(Integer code, String zhExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
        }

        private LoginRegister(Integer code, String zhExplain, String enExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
            this.enExplain = enExplain;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code.toString();
        }

        @Override
        public String getExplain(LanguageEnum language) {
            if (language == LanguageEnum.zh_CN) {
                return zhExplain;
            } else {
                return enExplain;
            }
        }
    }
    /**
     * 银行卡绑定相关(900~999)
     */
    public enum bankcard implements ResponseCode {
        /**
         * 返回码--银行卡绑定--银行卡已存在
         */
        BANKCARD_EXIST(900, "银行卡已存在", "The bank card already exist"),
        /**
         * 返回码--银行卡绑定--银行卡不存在
         */
        BANKCARD_NOT_EXIST(901, "银行卡不存在", "The card do not exist"),
        /**
         * 返回码--银行卡绑定--银行卡不存在
         */
        NO_MORE_BANKCARD(902, "不能再添加更多的银行卡", "No more cards"),
        /**
         * 返回码--银行卡默认绑定--银行卡默认绑定修改成功
         */
        BANKCARD_MODIFICATION_SUCCESS(902, "银行卡默认绑定修改成功", "The bank card default binding modification success"),
        /**
         * 返回码--银行卡默认绑定--银行卡默认绑定修改成功
         */
        BANKCARD_NO_BINDING(903, "暂无绑定银行卡", "No binding bank card");

        public String getZhExplain() {
            return zhExplain;
        }

        public String getEnExplain() {
            return enExplain;
        }

        private final Integer code;
        private String zhExplain;

        private String enExplain;

        private bankcard(int code) {
            this.code = code;
        }

        private bankcard(Integer code, String zhExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
        }

        private bankcard(Integer code, String zhExplain, String enExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
            this.enExplain = enExplain;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code.toString();
        }

        @Override
        public String getExplain(LanguageEnum language) {
            if (language == LanguageEnum.zh_CN) {
                return zhExplain;
            } else {
                return enExplain;
            }
        }
    }

    /**
     * 非预期结果(800~899)
     */
    public enum UNINTENDED_RESULT implements ResponseCode {
        /**
         * 非预期返回结果-查询结果为空
         */
        RESULT_NULL(800, "查询结果为空", "The query result is empty"),
        /**
         * 非预期返回结果-查询结果为空
         */
        UPDATE_OBJECT_UNEXPECT(801, "数据不存在或不可更新", "Data does not exist or cannot be updated"),
        /**
         * 代理商不存在或已删除
         */
        AGENT_NOT_EXIST(802, "代理商不存在或已删除", "The agent does not exist or has been deleted"),
        /**
         * 没有数据更新
         */
        NO_DATA_UPDATE(803, "没有数据更新","No data updates");
        ;

        public String getZhExplain() {
            return zhExplain;
        }

        public String getEnExplain() {
            return enExplain;
        }

        private final Integer code;
        private String zhExplain;

        private String enExplain;

        private UNINTENDED_RESULT(int code) {
            this.code = code;
        }

        private UNINTENDED_RESULT(Integer code, String zhExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
        }

        private UNINTENDED_RESULT(Integer code, String zhExplain, String enExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
            this.enExplain = enExplain;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code.toString();
        }

        @Override
        public String getExplain(LanguageEnum language) {
            if (language == LanguageEnum.zh_CN) {
                return zhExplain;
            } else {
                return enExplain;
            }
        }
    }

    /**
     *  自定义业务CODE，要使用的地方直接new responseCode
     */
    public class COMMON implements ResponseCode {

        public String getZhExplain() {
            return zhExplain;
        }

        public String getEnExplain() {
            return enExplain;
        }

        private final Integer code;
        private String zhExplain;

        private String enExplain;

        public COMMON(int code) {
            this.code = code;
        }

        public COMMON(Integer code, String zhExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
        }

        public COMMON(Integer code, String zhExplain, String enExplain) {
            this.code = code;
            this.zhExplain = zhExplain;
            this.enExplain = enExplain;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code.toString();
        }

        @Override
        public String getExplain(LanguageEnum language) {
            if (language == LanguageEnum.zh_CN) {
                return zhExplain;
            } else {
                return enExplain;
            }
        }
    }

    /**
     * 通过code获取Response
     * @param code
     * @return
     */
    public static ResponseCode getByCode(String code){
        if(StringUtils.isBlank(code)) return Base.ERROR;
        for (ResponseCode type : ResponseCode.Base.values()){
            if(code.equals(type.getCode()+"")){
                return type;
            }
        }
        for (ResponseCode type : ResponseCode.UNINTENDED_RESULT.values()){
            if(code.equals(type.getCode()+"")){
                return type;
            }
        }
        for (ResponseCode type : ResponseCode.Parameter.values()){
            if(code.equals(type.getCode()+"")){
                return type;
            }
        }
        for (ResponseCode type : ResponseCode.LoginRegister.values()){
            if(code.equals(type.getCode()+"")){
                return type;
            }
        }
        for (ResponseCode type : ResponseCode.bankcard.values()){
            if(code.equals(type.getCode()+"")){
                return type;
            }
        }
        return Base.ERROR;
    }

}
