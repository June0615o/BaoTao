package com.junevi.baotao.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类：生成/解析 Token，并从 Token 中提取用户名与角色。
 *
 * <p>说明：课程项目中为简化演示将密钥写在代码中；生产环境应改为从环境变量或配置中心读取，并定期轮换。</p>
 */
public class JwtUtil {

    // 简化起见，密钥写在代码中；实际生产应放到配置中
    private static final String SECRET = "baotao-demo-secret-key-change-me";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 1 天

    public static String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("role", role);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public static String extractRole(String token) {
        Object role = parseToken(token).get("role");
        return role == null ? null : role.toString();
    }
}


