package com.example.demo.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@MapperScan(basePackages = "com.example.demo.mapper")
public class MybatisPlusConfig {

    //逻辑删除组件
    @Bean
    public ISqlInjector sqlInjector(){
        return new LogicSqlInjector();
    }
}
