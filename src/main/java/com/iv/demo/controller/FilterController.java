package com.iv.demo.controller;

import javax.annotation.Resource;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.iv.demo.model.ApiResource;
import org.apache.commons.codec.binary.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;



/*请求IdentityServer4 自省端点的拦截过滤器
 * @author DanniKe
 */
//过滤器是Filter，拦截器是Interceptor
//urlPattern 定义拦截的路径,不需要此过滤器的话，注释掉即可
@WebFilter(urlPatterns = "/*", filterName="tokenFilter")
public class FilterController implements Filter {
    @Resource
    private ApiResource api;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("Filter 初始化中，认证服务器自省端点版本");
    }
    //认证部分
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("开始进行过滤请求，由认证服务器自省端点验证token");
        boolean authenticated = false;
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse rep = (HttpServletResponse) servletResponse;

        //--------------给自省端点发送请求-------------------------------
        //--------------准备请求信息----------------------------------------
        //其实一个url请求就是一组组键对值，getHeader（）方法获取的是头部的你想要的
        //键名后面的值,由于请求里面token的keyname是这个，倒是要是要改这里也要改
        //这里面header要是没有token这个就不行，会异常
        boolean authorizationHeaderExist = req.getHeader("Authorization") != null;
        if (!authorizationHeaderExist) {
            rep.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String token = cutToken(req.getHeader("Authorization"));
        //获取编码后的ApiSecret和ApiName，在application.propertiesz中
        String apiNameSecret = "Basic " + ApiNameSecretbase64();
        //倒是可以放到配置里面去，那里统一改
        String introspectEndpoint = "http://localhost:5000/connect/introspect";


        //-------------创造请求----------------------------------------------
        //protected HttpClient client = new DefaultHttpClient();已过时
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(introspectEndpoint);
        //添加请求头
        post.setHeader("Authorization", apiNameSecret);
        //添加请求主体（body）
        List<NameValuePair> urlBodys = new ArrayList<NameValuePair>();
        urlBodys.add(new BasicNameValuePair("token", token));
        post.setEntity(new UrlEncodedFormEntity((urlBodys)));
        HttpResponse response = client.execute(post);

        System.out.println("\nSending 'POST' request to URL : " + introspectEndpoint);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());
        //读取返回reponse的content的信息，含有决定结果
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        //注意StringBuffer不是String
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        //调试用，打印得到的请求的content
        System.out.println(result.toString());
        //-------------------------------决定authenticated结果---------------------------
        JSONObject jo = new JSONObject(result.toString());
        Boolean active = jo.getBoolean("active");

        if (response.getStatusLine().getStatusCode() == 200&& active==true)
        {
            String role = jo.getString("role");
            authenticated = true;
        }
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


    //返回Api名字和secret的编码，请求头的一部分
    public String ApiNameSecretbase64()
    {
        String result = api.getapiName()+":"+api.getapiSecret();
        byte[] data=Base64.encodeBase64(result.getBytes());
        return new String(data);
    }
    //处理token字符串，去掉Bearer
    public String cutToken(String originToken)
    {
         String[] temp = originToken.split(" ");
         return temp[1];
    }
    @Override
    public void destroy() {
        System.out.println("Filter销毁中，认证服务器自省端点版本");
    }
}