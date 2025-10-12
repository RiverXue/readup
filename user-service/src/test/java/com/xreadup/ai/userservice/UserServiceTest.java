package com.xreadup.ai.userservice;

import com.xreadup.ai.userservice.dto.UserLoginRequest;
import com.xreadup.ai.userservice.dto.UserRegisterRequest;
import com.xreadup.ai.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegister() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setPhone("13800138000");
        request.setInterestTag("tech");
        request.setIdentityTag("考研");
        request.setLearningGoalWords(5000);
        request.setTargetReadingTime(30);

        try {
            userService.register(request);
            System.out.println("注册成功");
        } catch (Exception e) {
            System.err.println("注册失败: " + e.getMessage());
        }
    }

    @Test
    void testLogin() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        try {
            var response = userService.login(request);
            System.out.println("登录成功，token: " + response.getToken());
        } catch (Exception e) {
            System.err.println("登录失败: " + e.getMessage());
        }
    }
}