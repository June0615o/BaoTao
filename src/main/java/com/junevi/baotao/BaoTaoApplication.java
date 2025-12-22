package com.junevi.baotao;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.junevi.baotao.service.UserService;

@SpringBootApplication
public class BaoTaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaoTaoApplication.class, args);
    }

    /**
     * 初始化一个管理员账号，方便测试：
     * 用户名：admin  密码：admin123
     */
    @Bean
    public CommandLineRunner initAdmin(UserService userService, PasswordEncoder passwordEncoder) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) {
                String username = "admin";
                String email = "admin@example.com";
                String rawPassword = "admin123";
                String encoded = passwordEncoder.encode(rawPassword);
                userService.createAdminIfNotExists(username, email, encoded);
            }
        };
    }

}
