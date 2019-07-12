package com.iv.demo.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//表示是一个javabean
@Component
//表示获取前缀为student的配置信息
//@ConfigurationProperties(prefix = "student")
public class StudentProperties {

    private String name;
    private Integer age;
    public String getName(){return name;}
    public Integer getAge(){return age;}
    public void setName(String name){this.name =name;}
    public void setAge(Integer age){this.age=age;}
    
}
