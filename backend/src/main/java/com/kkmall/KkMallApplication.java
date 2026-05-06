package com.kkmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kkmall.**.infrastructure")
public class KkMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(KkMallApplication.class, args);
    }
}
