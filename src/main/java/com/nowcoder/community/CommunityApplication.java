package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init(){
		// 解决netty启动(es和redis都进行配置)冲突问题
		// see Netty4Utils.setAvailableProcessors
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		//启动Tomcat
		SpringApplication.run(CommunityApplication.class, args);
	}
	
}
