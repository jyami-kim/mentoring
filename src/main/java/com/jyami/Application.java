package com.jyami;

import com.jyami.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        Map<String, UserRepository> beansOfType = context.getBeansOfType(UserRepository.class);
    }
}
