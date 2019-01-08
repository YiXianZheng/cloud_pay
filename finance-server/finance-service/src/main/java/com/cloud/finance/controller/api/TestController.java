package com.cloud.finance.controller.api;

import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.pay.req.RepPayCreateData;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.PassWordUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/hctest")
public class TestController {
    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private RedisClient redisClient;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

    @RequestMapping("/test")
    public ApiResponse test(HttpServletRequest request){
        String merchantCode = this.getStringParameter("assCode",request);                //商户编号
        String merchantPayOrderNo = this.getStringParameter("assPayOrderNo",request);    //商户订单号
        String merchantNotifyUrl = this.getStringParameter("assNotifyUrl",request);    //商户回调地址
        String merchantReturnUrl = this.getStringParameter("assReturnUrl",request);    //商户返回地址
        String merchantCancelUrl = this.getStringParameter("assCancelUrl",request);    //商户取消支付跳转地址
        String paymentType = this.getStringParameter("paymentType",request);        //支付类型
        String subPayCode = this.getStringParameter("subPayCode",request);        //子支付类型
        String merchantPayMoney = this.getStringParameter("assPayMoney",request);        //以分为单位
        Double merchantPayMoneyYuan = SafeComputeUtils.div(this.getDoubleParameter("assPayMoney", request), 100D);    //以元为单位
        //不参与签名
        String merchantPayMessage = this.getStringParameter("assPayMessage",request);  //商户保留字段
        String merchantGoodsTitle = this.getStringParameter("assGoodsTitle",request);    //支付产品标题
        String merchantGoodsDesc = this.getStringParameter("assGoodsDesc",request);    //支付产品描述
        String sign = request.getParameter("sign");                        //商户签名结果

        ApiResponse apiResponse = new ApiResponse();
        try {
            Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
            String merchantMd5Key = merchantInfoDto.get("md5Key");

            RepPayCreateData req = new RepPayCreateData(merchantCode, merchantPayOrderNo, merchantNotifyUrl, merchantReturnUrl,
                    merchantCancelUrl, paymentType, subPayCode, merchantPayMoney, merchantPayMessage, merchantGoodsTitle,
                    merchantGoodsDesc, merchantMd5Key);

            Map<String, String> data = new HashMap<>();
            data.put("sysSign", req.getSign()); //系统延签加密结果

            //按ASCII排序后，转"&key=value&key2=value2..."格式（请注意最开始的"&"）组成StringA，然后加上商户的秘钥Key，最后做MD5加密
            //          sign = MD5(StringA + Key).toUpperCase()
            //参与签名的字段 "assCode","assPayOrderNo","assNotifyUrl","assReturnUrl","assCancelUrl","paymentType","subPayCode","assPayMoney"
            Map<String, String> params = new HashMap<>();
            if(StringUtils.isNotBlank(merchantCode)) {
                params.put("assCode", merchantCode);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户号不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(merchantPayOrderNo)) {
                params.put("assPayOrderNo", merchantPayOrderNo);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户订单号不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(merchantNotifyUrl)) {
                params.put("assNotifyUrl", merchantNotifyUrl);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户异步通知地址不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(merchantReturnUrl)) {
                params.put("assReturnUrl", merchantReturnUrl);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户同步跳转地址不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(merchantCancelUrl)) {
                params.put("assCancelUrl", merchantCancelUrl);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户取消跳转地址不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(paymentType)) {
                params.put("paymentType", paymentType);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[支付类型不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(subPayCode)) {
                params.put("subPayCode", subPayCode);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户支付子类型不能为空]");
                return apiResponse;
            }
            if(StringUtils.isNotBlank(merchantPayMoney)) {
                params.put("assPayMoney", merchantPayMoney);
            }else{
                apiResponse.setCode("1");
                apiResponse.setMsg("请求失败[商户支付金额不能为空]");
                return apiResponse;
            }

            String signMsg = ASCIISortUtil.buildSign(params, "=", merchantMd5Key);
            data.put("beforeSignStr", "&" + signMsg); //加密前字符串
            logger.info("-------before sing data :"+"&" + signMsg);

            String signStr = MD5Util.MD5Encode("&" + signMsg);
            data.put("afterSignStr", signStr);  //加密结果
            logger.info("-------sign data :" +signStr);

            logger.info("-------sys sign :" + req.getSign());

            data.put("assSign", sign);
            if(req.getSign().equals(sign)){
                data.put("signResult", "签名正确");
            }else{
                data.put("signResult", "签名不正确");
            }

            apiResponse.setData(data);
            apiResponse.setCode("0");
            apiResponse.setMsg("请求成功");
            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        apiResponse.setCode("1");
        apiResponse.setMsg("请求失败");
        return apiResponse;

    }

    @RequestMapping("/postRecharge.html")
    public void postRecharge(HttpServletRequest request, HttpServletResponse response,Model model) throws Exception {

        logger.info("...[postRecharge] create recharge action...");


        String html = createHtmlStr(getBasePayUrl() + "/recharge/api/create");
        logger.info("...[postRecharge] html:" + html);
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.print(html);
            out.flush();
        } finally {
            out.close();
        }

    }

    public static String createHtmlStr(String postUrl) {
        StringBuffer sf = new StringBuffer();
        sf.append("<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"UTF-8\" />" +
                "    <title>代付</title>" +
                "    <style>" +
                "        h5{margin-left: 10%;}" +
                "        div{text-align: center; }" +
                "        #log{ text-align: left;width: 80%;padding: 10px 30px;border: 1px solid #000;margin-left: auto;margin-right: auto;color: #984444;}" +
                "        div p span{width: 100px; text-align: right; display:inline-block;}" +
                "        div p input{width: 200px;line-height: 24px;padding: 0 10px;}" +
                "        div p button{width: 60px;height: 28px;font-size: 16px; border-radius: 5px;background-color: #03A9F4; border: none;color: #fff;margin-right: 20px;line-height: 28px;}" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div>" +
                "        <h4>手动发起代付</h4>" +
                "        <div id=\"myForm\">" +
                "            <p><span>用户ID：</span><input type=\"text\" class=\"input\" name=\"userId\" alt=\"用户ID\"/></p>" +
                "            <p><span>商户号：</span><input type=\"text\" class=\"input\" name=\"merchantCode\" alt=\"商户号\"/></p>" +
                "            <p><span>银行编码：</span><input type=\"text\" class=\"input\" name=\"bankCode\" alt=\"银行编码\"/></p>" +
                "            <p><span>银行卡号：</span><input type=\"text\" class=\"input\" name=\"cardNo\" alt=\"银行卡号\"/></p> " +
                "            <p><span>持卡人：</span><input type=\"text\" class=\"input\" name=\"account\" alt=\"持卡人\"/></p>" +
                "            <p><span>下发金额：</span><input type=\"text\" class=\"input\" name=\"money\" alt=\"下发金额\"/></p>" +
                "            <p><span>支行名称：</span><input type=\"text\" class=\"input\" name=\"subbranch\" alt=\"支行名称\"/></p>" +
                "            <p><span>所在省份：</span><input type=\"text\" class=\"input\" name=\"province\" alt=\"所在省份\"/></p>" +
                "            <p><span>所在城市：</span><input type=\"text\" class=\"input\" name=\"city\" alt=\"所在城市\"/></p>" +
                "            <p><span>秘钥：</span><input type=\"password\" id=\"key\" name=\"key\"/></p>" +
                "            <p><button onclick=\"apply()\">提交</button><button type=\"reset\">重置</button></p>" +
                "        </div>" +
                "    </div>" +
                "    <h5>代付日志：</h5>" +
                "    <div id=\"log\"></div>" +
                "</body>" +
                "<script>" +
                "    function apply(){" +
                "        var input = document.getElementsByClassName('input');" +
                "        var data = \"\";" +
                "        let msg = \"\";" +
                "        let log = new Date + \"-----<br/>  [代付信息]--\";" +
                "        for (let i = 0; i < input.length; i += 1) {" +
                "            if (\"\"!=data){" +
                "               data += \"&\";" +
                "            }" +
                "            data += input[i].name +\"=\"+ input[i].value;" +
                "            msg += \"\\t\" + input[i].alt +\":\"+ input[i].value +\"\\t\\n\";" +
                "            log += input[i].alt +\":\"+ input[i].value +\"; \";" +
                "        }" +
                "        data += \"&key=\" + document.getElementById(\"key\");" +
                "        var e = document.getElementById(\"log\");" +
                "        var r=confirm(\"\\t确认提交数据？\\t\\n\" + msg);" +
                "        if (r==true){" +
                "            e.innerHTML += log + \"<br/>\";" +
                "            var xhr = new XMLHttpRequest();" +
                "            xhr.open(\"POST\", \""+ postUrl +"\", true);" +
                "            xhr.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");  " +
                "            xhr.onreadystatechange = function() {" +
                "                if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 304)) {" +
                "                    let obj = JSON.parse(xhr.responseText);" +
                "                    if(obj.code==0){" +
                "                       e.innerHTML += \"----下发成功：rechargeNo: \"+ obj.data + \"<br/>\";" +
                "                    }else{" +
                "                       e.innerHTML += \"----下发失败: \"+ obj.msg + \"<br/>\";" +
                "                       alert(obj.msg);" +
                "                    }" +
                "                }" +
                "            };" +
                "            xhr.send(data);" +
                "        }" +
                "    }" +
                "</script>" +
                "</html>");
        return sf.toString();
    }


    @RequestMapping("/md5")
    public ReturnVo getMd5Pwd(@RequestParam("pwd") String pwd){
        String md5pwd = PassWordUtil.entryptPassword(pwd);
        return ReturnVo.returnSuccess(md5pwd);
    }

    private String getStringParameter(String paramName, HttpServletRequest request) {
        String paramValue = request.getParameter(paramName);
        String returnString = "";
        if (StringUtils.isNotBlank(paramValue)) {
            returnString = paramValue;
        } else {
            returnString = "";
        }

        return returnString;
    }


    private Double getDoubleParameter(String paramName, HttpServletRequest request) {
        String paramValue = request.getParameter(paramName);
        Double returnDouble = 0.0D;
        if (StringUtils.isNotBlank(paramValue)) {
            returnDouble = (double)Float.valueOf(paramValue);
        } else {
            returnDouble = 0.0D;
        }

        return returnDouble;
    }

}
