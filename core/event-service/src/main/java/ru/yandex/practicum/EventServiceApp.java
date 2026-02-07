package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.yandex.practicum.client")
@ConfigurationPropertiesScan
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}
