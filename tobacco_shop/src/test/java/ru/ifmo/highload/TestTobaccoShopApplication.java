package ru.ifmo.highload;

import org.springframework.boot.SpringApplication;

public class TestTobaccoShopApplication {

    public static void main(String[] args) {
        SpringApplication.from(TobaccoShopApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
