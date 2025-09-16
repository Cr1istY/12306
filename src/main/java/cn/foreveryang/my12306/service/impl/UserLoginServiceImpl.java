package cn.foreveryang.my12306.service.impl;

import cn.foreveryang.my12306.common.cache.DistributedCache;
import cn.foreveryang.my12306.common.chain.AbstractChainContext;
import cn.foreveryang.my12306.common.enums.UserChainMarkEnum;
import cn.foreveryang.my12306.common.exception.ClientException;
import cn.foreveryang.my12306.common.exception.ServiceException;
import cn.foreveryang.my12306.common.toolkit.BeanUtil;
import cn.foreveryang.my12306.common.toolkit.JWTUtil;
import cn.foreveryang.my12306.dao.entity.*;
import cn.foreveryang.my12306.dto.req.UserLoginReqDTO;
import cn.foreveryang.my12306.dto.req.UserRegisterReqDTO;
import cn.foreveryang.my12306.dto.resp.*;
import cn.foreveryang.my12306.mapper.*;
import cn.foreveryang.my12306.service.UserLoginService;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cn.foreveryang.my12306.common.constant.UserRedisConstant.LOCK_USER_REGISTER;
import static cn.foreveryang.my12306.common.constant.UserRedisConstant.USER_REGISTER_REUSE_SHARDING;
import static cn.foreveryang.my12306.common.enums.UserRegisterErrorCodeEnum.*;
import static cn.foreveryang.my12306.common.toolkit.UserReuseUtil.hashShardingIdx;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {
    
    private final UserMailMapper userMailMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMapper userMapper;
    private final DistributedCache distributedCache;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;
    private final RedissonClient redissonClient;
    private final UserReuseMapper userReuseMapper;
    private final UserDeletionMapper userDeletionMapper;

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

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void userLogout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Override
    public Boolean hasUsername(String username) {
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        }
        return true;
    }

    
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO registerUser(UserRegisterReqDTO request) {
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), request);
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + request.getUsername());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new ServiceException(USER_NAME_NOTNULL);
        }
        try {
            try {
                int insert = userMapper.insert(BeanUtil.convert(request, UserDO.class));
                if (insert < 1) {
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException dke) {
                log.error("用户名 [{}] 已存在", request.getUsername());
                throw new ServiceException(USER_NAME_NOTNULL);
            }
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .username(request.getUsername())
                    .phone(request.getPhone())
                    .build();
            try {
                userPhoneMapper.insert(userPhoneDO);
            } catch (DuplicateKeyException dke) {
                log.error("用户 [{}] 手机号 [{}] 已存在", request.getUsername(), request.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
            String username = request.getUsername();
            userReuseMapper.delete(Wrappers.update(new UserReuseDO(username)));
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().remove(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(request, UserRegisterRespDTO.class);
    }

    @Override
    public Integer queryUserDeletionNum(Integer idType, String idCard) {
        LambdaQueryWrapper<UserDeletionDO> queryWrapper = Wrappers.lambdaQuery(UserDeletionDO.class)
                .eq(UserDeletionDO::getIdType, idType)
                .eq(UserDeletionDO::getIdCard, idCard);
        Long deletionCount = userDeletionMapper.selectCount(queryWrapper);
        return Optional.ofNullable(deletionCount).map(Long::intValue).orElse(0);
    }

    @Override
    public UserQueryRespDTO queryUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在，请检查用户名是否正确");
        }
        return BeanUtil.convert(userDO, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.convert(queryUserByUsername(username), UserQueryActualRespDTO.class);
    }


    @Transactional
    @Override
    public void update(UserRegisterReqDTO registerReqDTO) {
        UserQueryRespDTO userQueryRespDTO = queryUserByUsername(registerReqDTO.getUsername());
        UserDO userDO = BeanUtil.convert(registerReqDTO, UserDO.class);
        LambdaUpdateWrapper<UserDO> userUpdateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, registerReqDTO.getUsername());
        userMapper.update(userDO, userUpdateWrapper);
        if (StrUtil.isNotBlank(registerReqDTO.getMail()) && !Objects.equals(userQueryRespDTO.getMail(), registerReqDTO.getMail())) {
            LambdaUpdateWrapper<UserMailDO> updateWrapper = Wrappers.lambdaUpdate(UserMailDO.class)
                    .eq(UserMailDO::getMail, userQueryRespDTO.getMail());
            userMailMapper.delete(updateWrapper);
            UserMailDO userMailDO = UserMailDO.builder()
                    .mail(registerReqDTO.getMail())
                    .username(registerReqDTO.getUsername())
                    .build();
            userMailMapper.insert(userMailDO);
        }

    }
}
