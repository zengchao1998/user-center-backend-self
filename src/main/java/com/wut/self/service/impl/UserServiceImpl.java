package com.wut.self.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.self.common.ErrorCode;
import com.wut.self.exception.BusinessException;
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

import static com.wut.self.constant.UserConstant.*;

/**
* @author zeng
* description 针对表【user】的数据库操作Service实现
* createDate 2023-03-13 09:56:46
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
    public long userRegister(String userAccount, String userPassword, String checkPassword, String validateCode) {

        // 1. 用户注册信息校验
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, validateCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度过短");
        }
        if(userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度过长");
        }
        if(validateCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户验证码长度不符合要求");
        }
        // 账户不能包含特殊字符
        String validateStr = "^[a-zA-Z][\\w_]{3,}$";
        Matcher matcher = Pattern.compile(validateStr).matcher(userAccount);
        if(!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码和校验密码相同
        if(!StringUtils.equals(userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }

        // 账户不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(userQueryWrapper);
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名重复");
        }

        // 用户验证码不能重复
        userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("validate_code", validateCode);
        count = userMapper.selectCount(userQueryWrapper);
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户验证码重复");
        }

        // 2. 对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 将用户保存到数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setValidateCode(validateCode);
        user.setAvatarUrl(USER_AVATAR_DEFAULT_URL);
        int saveResult = userMapper.insert(user);
        if(saveResult != 1) {
            throw new BusinessException(ErrorCode.EXECUTE_FAIL, "用户注册失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest req) {

        // 1. 用户登录信息校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if(userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 账户不能包含特殊字符
        String validateStr = "^[a-zA-Z][\\w_]{3,}$";
        Matcher matcher = Pattern.compile(validateStr).matcher(userAccount);
        if(!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号内包含特殊字符");
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
            throw new BusinessException(ErrorCode.EXECUTE_FAIL, "登录失败,用户不存在");
        }
        // 可以补充内容: 登录限流 ....

        // 3. 用户信息脱敏
        User safetyUser = getSafetyUser(currentUser);

        // 4. 登录成功，将脱敏后的用户信息保存到Session域中
        HttpSession session = req.getSession();
        session.setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public Integer userLogout(HttpServletRequest req) {
        req.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 用户信息脱敏
     * @param currentUser 当前用户
     * @return 脱敏后的用户
     */
    @Override
    public User getSafetyUser(User currentUser) {
        if(currentUser == null) {
            return null;
        }
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
        safetyUser.setValidateCode(currentUser.getValidateCode());
        return safetyUser;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        // 查询是否传入用户id
        Long userId = user.getId();
        if(userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否为管理员,或者登录用户修改的是自己的信息
        if(!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 如果在需要更新的用户信息中，仅包含 id，不包含任何需要更新的内容，直接抛出参数异常
        if(user.getUsername() == null && user.getAvatarUrl() == null && user.getGender() == null
            && user.getPhone() == null && user.getEmail() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User oldUser = userMapper.selectById(userId);
        if(oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest req) {
        if(req == null) {
            return null;
        }
        User user = (User) req.getSession().getAttribute(USER_LOGIN_STATE);
        if(user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "当前用户未登录");
        }
        return user;
    }

    @Override
    public boolean isAdmin(HttpServletRequest req) {
        if(req == null) {
            return false;
        }
        // 鉴权，仅管理员可调用
        Object userObj = req.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }
}




