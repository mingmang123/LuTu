package com.liu.lutu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.liu.lutu.mapper")
public class LuTuApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuTuApplication.class, args);
    }

}
