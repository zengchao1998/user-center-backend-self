package com.wut.self.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.self.service.UserService;
import com.wut.self.model.domain.User;
import com.wut.self.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wut.self.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author zeng
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-03-13 09:56:46
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 混淆用户密码
     */
    private static final String SALT = "center";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1. 用户注册信息校验
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            // todo 修改为自定义异常
            return -1;
        }
        if(userAccount.length() < 4) {
            return -1;
        }
        if(userPassword.length() < 8) {
            return -1;
        }
        // 账户不能包含特殊字符
        String validateStr = "^[a-zA-Z][\\w_]{3,}$";
        Matcher matcher = Pattern.compile(validateStr).matcher(userAccount);
        if(!matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if(!StringUtils.equals(userPassword, checkPassword)){
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(userQueryWrapper);
        if(count > 0) {
            return -1;
        }

        // 2. 对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 将用户保存到数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        int saveResult = userMapper.insert(user);
        if(saveResult != 1) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest req) {

        // 1. 用户登录信息校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            // todo 修改为自定义异常
            return null;
        }
        if(userAccount.length() < 4) {
            return null;
        }
        if(userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validateStr = "^[a-zA-Z][\\w_]{3,}$";
        Matcher matcher = Pattern.compile(validateStr).matcher(userAccount);
        if(!matcher.find()) {
            return null;
        }

        // 2. 校验密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount)
                .eq("user_password", encryptPassword);
        User currentUser = userMapper.selectOne(userQueryWrapper);
        // 用户不存在
        if(currentUser == null) {
            log.info("user login failed, the userAccount cannot match userPassword");
            return null;
        }
        // 可以补充内容: 登录限流 ....

        // 3. 用户信息脱敏
        User safetyUser = getSafetyUser(currentUser);

        // 4. 登录成功，将脱敏后的用户信息保存到Session域中
        HttpSession session = req.getSession();
        session.setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户信息脱敏
     * @param currentUser 当前用户
     * @return 脱敏后的用户
     */
    @Override
    public User getSafetyUser(User currentUser) {
        User safetyUser = new User();
        safetyUser.setId(currentUser.getId());
        safetyUser.setUsername(currentUser.getUsername());
        safetyUser.setUserAccount(currentUser.getUserAccount());
        safetyUser.setAvatarUrl(currentUser.getAvatarUrl());
        safetyUser.setGender(currentUser.getGender());
        safetyUser.setPhone(currentUser.getPhone());
        safetyUser.setEmail(currentUser.getEmail());
        safetyUser.setUserStatus(currentUser.getUserStatus());
        safetyUser.setCreateTime(currentUser.getCreateTime());
        safetyUser.setUserRole(currentUser.getUserRole());
        return safetyUser;
    }
}




