package com.iv.demo.controller;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.proc.*;

//@WebFilter(urlPatterns = "/*", filterName="jwkTokenFilter")
public class JwkFilterController implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("Filter 初始化中，认证服务器jwk公钥解析版本");

    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        System.out.println("开始进行过滤请求，由认证服务器jwk公钥解析验证token");
        boolean authenticated = false;
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse rep = (HttpServletResponse) servletResponse;
        boolean authorizationHeaderExist = req.getHeader("Authorization") != null;
        if (!authorizationHeaderExist) {
            rep.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String jwkEndpoint = "http://localhost:5000/.well-known/openid-configuration/jwks";
        //String token = cutToken(((HttpServletRequest) servletRequest).getHeader("Authorization"));
        String token = cutToken(req.getHeader("Authorization"));


        //------------解析------------------------------------------------------
        //com.nimbusds JWT解析包，这个包目前没有找到源代码
        //https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
        //建立解析处理对象
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        //提供公钥地址来获取
        JWKSource keySource = new RemoteJWKSet(new URL(jwkEndpoint));
        //提供解析算法，算法类型要写对，服务器用的是什么就是什么，目前是RSA256算法
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        //填写 RSA 公钥来源从提供公钥地址获取那边得到
        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        if(keySelector==null)
        {
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("无法获取公钥。");
            return;
        }
        //设置第一步建立的解析处理对象
        jwtProcessor.setJWSKeySelector(keySelector);
        //处理收到的token（令牌),错误则返回对象
        SecurityContext ctx = null;
        JWTClaimsSet claimsSet = null;
        try {
            claimsSet = jwtProcessor.process(token, ctx);
            authenticated = true;
        } catch (ParseException e) {
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            e.printStackTrace();
            return;
        } catch (BadJOSEException e) {
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            e.printStackTrace();
            return;
        } catch (JOSEException e) {
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            e.printStackTrace();
            return;
        }
        //调试用，打印出来
        System.out.println(claimsSet.toJSONObject());
        //失败返回无授权
        if(claimsSet==null) {
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //解码里面具体内容，尤其角色，虽然这里不需要,顺利取出
        JSONObject jo = new JSONObject(claimsSet.toJSONObject());
        String role = jo.getString("role");
        //试一下过期的token，删除用户的可以不试试
        //--------------------------------处理authenticated结果，决定是否发出401-----------
        if (authenticated)
        {
            //调用该方法后，表示过滤器经过原来的url请求处理方法
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }


    //帮助类
    public String cutToken(String originToken)
    {
        String[] temp = originToken.split(" ");
        return temp[1];
    }

    @Override
    public void destroy() {
        System.out.println("Filter销毁中，认证服务器jwk公钥解析版本");
    }
}
