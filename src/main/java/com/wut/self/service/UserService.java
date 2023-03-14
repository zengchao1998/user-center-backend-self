package com.wut.self.service;

import com.wut.self.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import javax.servlet.http.HttpServletRequest;

/**
* @author zeng
* @description 针对表【user】的数据库操作Service
* @createDate 2023-03-13 09:56:46
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   账户名
     * @param userPassword  账户密码
     * @param checkPassword 校验密码
     * @param validateCode
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String validateCode);

    /**
     * 用户登录
     * @param userAccount 账户名
     * @param userPassword 账户密码
     * @param req 请求对象
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest req);

    /**
     * 用户注销
     * @param req 请求对象
     * @return 1：logout success
     */
    Integer userLogout(HttpServletRequest req);

    /**
     * 用户信息脱敏
     * @param currentUser 当前用户
     * @return 脱敏后的用户
     */
    User getSafetyUser(User currentUser);
}

