package com.iv.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
//程序入口点
//该配置注解组合了 @Configuration, @EnableAutoConfiguration, @ComponentScan
//@EnableAutoConfiguration 让Spring Boot根据类路径中的jar包依赖为当前项目进行自动配置
//列如添加了spring-boot-starter-web依赖，会自动添加Tomcat和SpringMVC的依赖
//Spring Boot 还会自动扫描 @SpringBootApplication 所在类的同级包以及下级包里的Bean，入口类
//简易配置在group+artifact组合包名下。
@SpringBootApplication
//该注册扫描相应的包，比如我写的Filter
@ServletComponentScan
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class,args);
    }

}
