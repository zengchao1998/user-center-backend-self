package com.wut.self.service;

import com.wut.self.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import javax.servlet.http.HttpServletRequest;


/**
* @author zeng
* description 针对表【user】的数据库操作Service
* createDate 2023-03-13 09:56:46
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   账户名
     * @param userPassword  账户密码
     * @param checkPassword 校验密码
     * @param validateCode 用户校验码
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

    /**
     * 更新用户信息
     * @param user 修改的用户信息
     * @param loginUser 当前登录用户
     * @return 是否成功 0,1
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @param req 请求对象
     * @return 用户对象
     */
    User getLoginUser(HttpServletRequest req);

    /**
     * 是否为管理员
     * @param req 请求对象
     * @return 判断结果 true：admin
     */
    boolean isAdmin(HttpServletRequest req);

    /**
     * 是否为管理员
     * @param loginUser 登录用户
     * @return 判断结果 true：admin
     */
    boolean isAdmin(User loginUser);
}

