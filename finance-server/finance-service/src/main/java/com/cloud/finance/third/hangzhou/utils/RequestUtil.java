package com.cloud.finance.third.hangzhou.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class RequestUtil {

    /**
     * 发一次POST请求，只请求一次，不做重试
     * @param urlSuffix
     * @param reqData
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @return
     * @throws Exception
     */
    public  String requestOnce(final String urlSuffix, Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        BasicHttpClientConnectionManager connManager;

        Set<String> keySet = reqData.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);//集合中的key排序


        String data = XmlUtil.mapToXml(reqData);

        //解决主机名与对等方提供的证书主题不匹配
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(100);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(cm)
                .build();

        HttpPost httpPost = new HttpPost(urlSuffix);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeoutMs).setConnectTimeout(connectTimeoutMs).build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.setEntity(postEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");

    }

    /**
     * 发送一次get请求
     * @param urlSuffix
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @throws Exception
     */
    public  void requestGetOnce(final String urlSuffix, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        BasicHttpClientConnectionManager connManager;


        //data = key.publicKeyEncrypt(key.publicKey, data);

        connManager = new BasicHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                null,
                null,
                null
        );

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        HttpGet httpPost = new HttpGet(urlSuffix);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeoutMs).setConnectTimeout(connectTimeoutMs).build();
        httpPost.setConfig(requestConfig);

        httpPost.addHeader("Content-Type", "text/xml");
        //httpPost.setEntity(postEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        // HttpEntity httpEntity = httpResponse.getEntity();
        //System.out.println(EntityUtils.toString(httpEntity, "UTF-8"));
        // return EntityUtils.toString(httpEntity, "UTF-8");
    }
}
