package com.cloud.finance.third.xunjiefu.utils;

import cfca.sadk.algorithm.common.PKCSObjectIdentifiers;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class XJFSignUtil {

    public static String doSign(String context, String fileName){
        String filePath = "cert/xunjiefu/" + fileName + "_prv.pem";
        ClassPathResource resource = new ClassPathResource(filePath);
        try {
            InputStream certStream = resource.getInputStream();
            byte[]privateKeyContent = IOUtils.toByteArray(certStream);
            certStream.read(privateKeyContent);
            certStream.close();

            String privateKeyStr = new String(privateKeyContent)
                    .replaceAll("-.*", "");

            privateKeyContent =
                    Base64.getDecoder().decode(privateKeyStr.replace("\n", ""));

            /* Add PKCS#8 formatting */
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1Integer(0));
            ASN1EncodableVector v2 = new ASN1EncodableVector();
            v2.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
            v2.add(DERNull.INSTANCE);
            v.add(new DERSequence(v2));
            v.add(new DEROctetString(privateKeyContent));
            ASN1Sequence seq = new DERSequence(v);
            byte[] privKey = seq.getEncoded("DER");

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            java.security.Signature signature =
                    java.security.Signature.getInstance("SHA1WithRSA");

            signature.initSign(privateKey);
            signature.update(context.getBytes(Charset.forName("UTF8")));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public static boolean checkSign(String context, String signatureStr, String fileName){

        String filePath = "cert/xunjiefu/" + fileName + "_pub.pem";
        ClassPathResource resource = new ClassPathResource(filePath);
        try {
            InputStream certStream = resource.getInputStream();
            byte[]publicKeyContent = IOUtils.toByteArray(certStream);
            certStream.read(publicKeyContent);
            certStream.close();

            String publicKeyStr = new String(publicKeyContent)
                    .replaceAll("-.*", "");

            byte[] buffer = Base64.getDecoder().decode(publicKeyStr.replace("\n", ""));

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);


            java.security.Signature signature = java.security.Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey);
            signature.update(context.getBytes(Charset.forName("UTF8")));
            boolean bverify = signature.verify(Base64.getDecoder().decode(signatureStr));
            return bverify;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
