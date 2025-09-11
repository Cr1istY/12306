package cn.foreveryang.my12306.service.impl;

import cn.foreveryang.my12306.common.cache.DistributedCache;
import cn.foreveryang.my12306.common.exception.ClientException;
import cn.foreveryang.my12306.common.exception.ServiceException;
import cn.foreveryang.my12306.common.toolkit.JWTUtil;
import cn.foreveryang.my12306.dao.entity.UserDO;
import cn.foreveryang.my12306.dao.entity.UserMailDO;
import cn.foreveryang.my12306.dao.entity.UserPhoneDO;
import cn.foreveryang.my12306.dto.req.UserLoginReqDTO;
import cn.foreveryang.my12306.dto.resp.UserInfoDTO;
import cn.foreveryang.my12306.dto.resp.UserLoginRespDTO;
import cn.foreveryang.my12306.mapper.UserMailMapper;
import cn.foreveryang.my12306.mapper.UserMapper;
import cn.foreveryang.my12306.mapper.UserPhoneMapper;
import cn.foreveryang.my12306.service.UserLoginService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {
    
    private final UserMailMapper userMailMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMapper userMapper;
    private final DistributedCache distributedCache;
    
    @Override
    public UserLoginRespDTO userLogin(UserLoginReqDTO request) {
        String usernameOrMailOrPhone = request.getUsernameOrMailOrPhone();
        boolean mailFlag = false;
        for (char c : usernameOrMailOrPhone.toCharArray()) {
            if (c == '@') {
                mailFlag = true;
                break;
            }
        }
        
        String userName;
        if (mailFlag) {
            LambdaQueryWrapper<UserMailDO> queryWrapper = Wrappers.lambdaQuery(UserMailDO.class)
                    .eq(UserMailDO::getMail, usernameOrMailOrPhone);
            userName = Optional.ofNullable(userMailMapper.selectOne(queryWrapper))
                    .map(UserMailDO::getUsername)
                    .orElseThrow(() -> new ClientException("用户/手机号/邮箱不存在"));
        } else {
            LambdaQueryWrapper<UserPhoneDO> queryWrapper = Wrappers.lambdaQuery(UserPhoneDO.class)
                    .eq(UserPhoneDO::getPhone, usernameOrMailOrPhone);
            userName = Optional.ofNullable(userPhoneMapper.selectOne(queryWrapper))
                    .map(UserPhoneDO::getUsername)
                    .orElse(null);
        }
        userName = Optional.ofNullable(userName).orElse(usernameOrMailOrPhone);
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userName)
                .eq(UserDO::getPassword, request.getPassword())
                .select(UserDO::getId, UserDO::getUsername, UserDO::getRealName);
        UserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(userDO.getId()))
                    .userName(userDO.getUsername())
                    .realName(userDO.getRealName())
                    .build();
            String token = JWTUtil.generateToken(userInfo);
            UserLoginRespDTO userLogin = UserLoginRespDTO.builder()
                    .userId(userInfo.getUserId())
                    .userName(userInfo.getUserName())
                    .realName(userInfo.getRealName())
                    .accessToken(token)
                    .build();
            distributedCache.put(token, JSON.toJSONString(userLogin), 30, TimeUnit.MINUTES);
            return userLogin;
        }
        throw new ServiceException("账号不存在或密码错误");

    }
}
