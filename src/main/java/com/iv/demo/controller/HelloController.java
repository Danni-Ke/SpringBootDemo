package com.iv.demo.controller;
import com.iv.demo.model.ApiResource;
import com.iv.demo.model.StudentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Resource;

@RestController
public class HelloController {

    // @Autowired
    //private StudentProperties student;
    @Resource
    private ApiResource api;
    @RequestMapping(value="/hello",method = RequestMethod.GET)
    //@GetMapping("/hello")
    public String hello()
    {
        return "Test api json";
    }

    @PostMapping("/base64")
    public String base64()
    {
        String result = api.getapiName()+":"+api.getapiSecret();
        byte[] data=Base64.encodeBase64(result.getBytes());
        return new String(data);
    }

}
