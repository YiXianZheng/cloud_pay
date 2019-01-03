package com.cloud.finance.common.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.TrustStrategy;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

public class GetUtils {

    private static Logger logger = LoggerFactory.getLogger(PostUtils.class);
    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 7000;

    static {
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        // Validate connections after 1 sec of inactivity
        connMgr.setValidateAfterInactivity(1000);
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        requestConfig = configBuilder.build();
    }


    /**
     * 通过get方式发送请求，并返回响应结果
     *
     * @param url
     *            请求地址
     * @param params
     *            参数列表，例如name=小明&age=8里面的中文要经过Uri.encode编码
     * @return 服务器响应结果
     * @throws Exception
     */
    public static String sendGetMethod(String url, Map<String, String> params) throws Exception {
        Date getBeginTime = new Date();
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0) param.append("?");
            else param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        HttpClient httpClient = null;
        if (apiUrl.startsWith("https")) {
            httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        } else {
            httpClient = HttpClients.createDefault();
        }
        try {
            HttpGet httpGet = new HttpGet(apiUrl);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                result = IOUtils.toString(instream, "UTF-8");
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
            Date getEndTime = new Date();
            logger.info("get ["+ url +"] cost time == >  ["+ (getEndTime.getTime()-getBeginTime.getTime()) +"ms]");

        return result;

    }

    /**
     * 通过get方式发送请求，并返回响应结果
     *
     * @param url
     *            请求地址
     * @param params
     *            参数列表，例如name=小明&age=8里面的中文要经过Uri.encode编码
     * @return 服务器响应结果
     * @throws Exception
     */
    public static String sendGetMethodForCharset(String url, Map<String, String> params, String charset) throws Exception {
        Date getBeginTime = new Date();
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        if(params != null) {
            for (String key : params.keySet()) {
                if (i == 0) param.append("?");
                else param.append("&");
                param.append(key).append("=").append(params.get(key));
                i++;
            }
        }
        apiUrl += param;
        String result = null;
        HttpClient httpClient = null;
        if (apiUrl.startsWith("https")) {
            httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        } else {
            httpClient = HttpClients.createDefault();
        }
        try {
            HttpGet httpGet = new HttpGet(apiUrl);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                result = IOUtils.toString(instream, charset);
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        Date getEndTime = new Date();
        logger.info("get ["+ url +"] cost time == >  ["+ (getEndTime.getTime()-getBeginTime.getTime()) +"ms]");

        return result;

    }

    /**	 * 创建SSL安全连接	 * 	 * @return	 */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (GeneralSecurityException e) {
            throw new ServiceException(e.getMessage());
        }
        return sslsf;
    }

}
