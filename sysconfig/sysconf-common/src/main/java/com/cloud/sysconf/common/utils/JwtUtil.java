package com.cloud.sysconf.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Created by zengsizhang on 2017/7/5.
 */
public  class JwtUtil
{
    @Value("${spring.jwt.express}")
    private  String  jwtexpress;
    @Value("${spring.jwt.id}")
    private String jwtid;

    /*
    * 生成TOKEN
    * */
    public static String createToken(String t,String jwtexpress,String jwtid)  {
        try{
            try {
                Algorithm algorithm = Algorithm.HMAC256(jwtid);
                long nowMillis = System.currentTimeMillis();
                Date now = new Date(nowMillis);
                long expMillis = nowMillis +  Long.parseLong(jwtexpress);
                Date exp = new Date(expMillis);
                String token = JWT.create()
                        .withIssuer("champion2017")
                        .withSubject(generalSubject(t))
                        .withExpiresAt(exp)
                        .sign(algorithm);
                return token;
            } catch (JWTCreationException exception){
                exception.printStackTrace();
            }
        }catch (ExceptionInInitializerError er){
            return "";
        }
        return "";


    }
    /*
   * 解密token
   * */
    public static String decodeToken(String token,String jwtid){

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtid))
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            String subJson = jwt.getSubject();
            JSONObject json=JSONObject.fromObject(subJson);

            System.out.println(json.get("t"));
            return (String) json.get("t");
        } catch (Exception exception){
            return "";
        }

    }
    /**
     * 生成subject信息
     * @param token
     * @return
     */
    public static String generalSubject(String token){
        JSONObject jo = new JSONObject();
        jo.put("t", token);
        return jo.toString();
    }


}