package com.cloud.finance.third.yunji.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;


public class YJSignUtil {

    public static String doSign(String context, String merID){
        String filePath = "cert/yunji/" + merID + "_private_key.pem";
        ClassPathResource resource = new ClassPathResource(filePath);
        try {
            InputStream certStream = resource.getInputStream();
            byte[] privateKeyContent = IOUtils.toByteArray(certStream);
            certStream.read(privateKeyContent);
            certStream.close();

            String privateKeyStr = new String(privateKeyContent)
                    .replaceAll("-.*", "");

            privateKeyContent =
                    Base64.getDecoder().decode(privateKeyStr.replace("\n", ""));

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            java.security.Signature signature =
                    java.security.Signature.getInstance("SHA1WithRSA");

            signature.initSign(privateKey);
            signature.update(context.getBytes(Charset.forName("UTF8")));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
