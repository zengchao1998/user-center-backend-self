package com.wut.self.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wut.self.model.domain.User;
import com.wut.self.model.request.UserLoginRequestParams;
import com.wut.self.model.request.UserRegisterRequestParams;
import com.wut.self.service.UserService;
import com.wut.self.service.impl.UserServiceImpl;
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

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequestParams requestParams) {
        if(requestParams == null) {
            return null;
        }
        String userAccount = requestParams.getUserAccount();
        String userPassword = requestParams.getUserPassword();
        String checkPassword = requestParams.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

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

    @GetMapping("/search")
    public List<User> searchUser(String username, HttpServletRequest req) {
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
