package com.study.matzip.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;


@MapperScan(basePackages = "com.study.matzip.model.dao")
@Configuration
public class DBConfig {

}
