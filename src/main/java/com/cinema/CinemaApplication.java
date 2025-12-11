package com.cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CinemaApplication {
    public static void main(String[] args) {
        try {
            // 正常启动
            SpringApplication.run(CinemaApplication.class, args);
        } catch (Throwable e) {
            // ⚠️ 强行捕获所有错误并打印到控制台
            System.err.println("❌❌❌ 发生严重错误 ❌❌❌");
            e.printStackTrace(); // 这行代码会把被吞掉的错误吐出来
        }
    }
}