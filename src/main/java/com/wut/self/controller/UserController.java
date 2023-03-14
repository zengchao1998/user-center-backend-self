package com.wut.self.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wut.self.model.domain.User;
import com.wut.self.model.request.UserLoginRequestParams;
import com.wut.self.model.request.UserRegisterRequestParams;
import com.wut.self.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wut.self.constant.UserConstant.ADMIN_ROLE;
import static com.wut.self.constant.UserConstant.USER_LOGIN_STATE;

/**
 * Author: zeng
 * Description: 用户接口(用户控制器)
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequestParams requestParams) {
        if(requestParams == null) {
            return null;
        }
        String userAccount = requestParams.getUserAccount();
        String userPassword = requestParams.getUserPassword();
        String checkPassword = requestParams.getCheckPassword();
        String validateCode = requestParams.getValidateCode();
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, validateCode)) {
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword, validateCode);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequestParams requestParams, HttpServletRequest req) {
        if(requestParams == null) {
            return null;
        }
        String userAccount = requestParams.getUserAccount();
        String userPassword = requestParams.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        return userService.userLogin(userAccount, userPassword, req);
    }

    /**
     * 用户注销接口
     */
    @PostMapping("/logout")
    public Integer logoutUser(HttpServletRequest req) {
        if(req == null) {
            return null;
        }
        return userService.userLogout(req);
    }

    /**
     * 获取当前用户登录态接口
     */
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest req) {
        Object userObj = req.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(userObj == null) {
            return null;
        }
        // 对于频繁变换的信息，可以考虑不直接从缓存中去，而是去查询数据库
        Long userId = currentUser.getId();
        // todo：如果用户状态异常，需要进行逻辑判断
        User newCurrentUser = userService.getById(userId);
        return userService.getSafetyUser(newCurrentUser);
    }

    /**
     * 查询用户接口
     */
    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest req) {
        // 1. 鉴权，仅管理员可调用
        if(!isAdmin(req)) {
            return new ArrayList<>();
        }
        // 2. 执行查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);

        }
        List<User> userList = userService.list(userQueryWrapper);
        // 3. 用户信息过滤
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    }

    /**
     * 删除用户接口
     */
    @PostMapping ("/delete")
    public boolean deleteUser(Long id, HttpServletRequest req) {
        // 鉴权，仅管理员可调用
        if(!isAdmin(req)) {
            return false;
        }
        if(id == null || id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }

    /**
     * 是否为管理员
     * @param req 请求对象
     * @return 判断结果 true：admin
     */
    private boolean isAdmin(HttpServletRequest req) {
        // 鉴权，仅管理员可调用
        Object userObj = req.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
