package com.github.mtxn.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan({"com.gitee.sunchenbin.mybatis.actable.dao.*","com.github.mtxn.mapper","com.github.mtxn.web.mapper"})
@ComponentScan(basePackages = {"com.github.mtxn","com.gitee.sunchenbin.mybatis.actable.manager.*"})
public class Bootstrap {

	public static void main(String[] args) {
		SpringApplication.run(Bootstrap.class, args);
	}
	

}
