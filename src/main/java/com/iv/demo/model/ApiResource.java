package com.iv.demo.model;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
@Component
@PropertySource(value={"classpath:application.properties"})
public class ApiResource {

    @Value("${api.name}")
    private String apiname;
    @Value("${api.secret}")
    private String apisecret ;
    public String getapiName(){return apiname;}
    public String getapiSecret(){return apisecret;}
    public void setapiName(String name){this.apiname =apiname;}
    public void setapiSecret(String age){this.apisecret=apisecret;}
}
