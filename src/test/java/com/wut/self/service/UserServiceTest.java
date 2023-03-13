package com.wut.self.service;

import com.wut.self.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: zeng
 * Description:
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void testAddUser() {
        User user = new User();
        user.setUsername("jerry");
        user.setUserAccount("jerry_test");
        user.setAvatarUrl("https://www.baidu.com");
        user.setGender(0);
        user.setUserPassword("jerry1234");
        user.setPhone("1344545");
        user.setEmail("1315@gmail.com");
        user.setUserStatus(0);
        boolean save = userService.save(user);
        System.out.println(save);
        Assertions.assertTrue(save);
        System.out.println(user.getId());
    }

    @Test
    void userRegister() {
        String userAccount = "zengchao";
        String userPassword = "";
        String checkPassword = "zeng_12345";
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        userAccount = "tom";
        userPassword = "zeng_12345";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        userAccount = "zengchao";
        userPassword = "zeng_12";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        userAccount = "zeng-chao";
        userPassword = "zeng_12345";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        checkPassword = "chao_12345";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        userAccount = "jerry_test";
        checkPassword = "zeng_12345";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        userAccount = "zengchao";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(result);
        Assertions.assertTrue(result > 0);
    }

    @Test
    void testSpecialCharacter() {
        String userAccount = "z111_";
        String validateStr = "^[a-zA-Z][\\w_]{3,}$";
        Matcher matcher = Pattern.compile(validateStr).matcher(userAccount);
        if(matcher.find()) {
            System.out.println(matcher.group(0));
        }
    }
}