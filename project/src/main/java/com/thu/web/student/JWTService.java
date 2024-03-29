package com.thu.web.student;

import com.thu.domain.TUser;
import com.thu.domain.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by source on 1/8/17.
 */

@Component
public class JWTService {

    @Autowired
    UserRepository userRepository;

    private String secrectKey = "secretKeyForStudent";

    public String creatToken(String idNumber) throws Exception{
        String token = Jwts.builder().claim("idNumber",idNumber)
                .setIssuedAt(new Date()).signWith(SignatureAlgorithm.HS256,secrectKey).compact();
        Claims claims = Jwts.parser().setSigningKey(secrectKey).parseClaimsJws(token).getBody();
        System.out.println(claims);
        return token;
    }

    public Claims parseToken(String token) throws  Exception{
        Claims claims = Jwts.parser().setSigningKey(secrectKey).parseClaimsJws(token).getBody();
        return claims;
    }

    public Long getUserId(String token){
        String idNumber;
        try{
            Claims claims = parseToken(token);
            idNumber = claims.get("idNumber").toString();
        }
        catch (Exception e){
            return null;
        }
        System.out.println("token 解析得到的idNumber：" + idNumber);
        TUser TUser = userRepository.findByIdNumber(idNumber);
        // 验证标准： 能顺利解析 得到idNumber  并且 idNumber在数据库中

        if(TUser != null)
        {
            return TUser.getId();
        }
        else
        {
            return null;
        }
    }
}
