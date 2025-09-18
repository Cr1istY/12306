package cn.foreveryang.my12306.service.impl;

import cn.foreveryang.my12306.common.cache.DistributedCache;
import cn.foreveryang.my12306.common.enums.VerifyStatusEnum;
import cn.foreveryang.my12306.common.exception.ClientException;
import cn.foreveryang.my12306.common.exception.ServiceException;
import cn.foreveryang.my12306.common.toolkit.BeanUtil;
import cn.foreveryang.my12306.dao.entity.PassengerDO;
import cn.foreveryang.my12306.dto.req.PassengerReqDTO;
import cn.foreveryang.my12306.dto.resp.PassengerRespDTO;
import cn.foreveryang.my12306.mapper.PassengerMapper;
import cn.foreveryang.my12306.service.PassengerService;
import cn.foreveryang.my12306.service.user.core.UserContext;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.foreveryang.my12306.common.constant.UserRedisConstant.USER_PASSENGER_LIST;


@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerMapper passengerMapper;
    private final DistributedCache distributedCache;

    private String getActualUserPassengerListStr(String username) {
        return distributedCache.safeGet(
                USER_PASSENGER_LIST + username,
                String.class,
                () -> {
                    LambdaQueryWrapper<PassengerDO> queryWrapper = Wrappers.lambdaQuery(PassengerDO.class)
                            .eq(PassengerDO::getUsername, username);
                    List<PassengerDO> passengerDOList = passengerMapper.selectList(queryWrapper);
                    return CollUtil.isNotEmpty(passengerDOList) ? JSON.toJSONString(passengerDOList) : null;
                },
                1,
                TimeUnit.DAYS
        );
    }

    private void verifyPassenger(PassengerReqDTO requestParam) {
        int length = requestParam.getRealName().length();
        if (!(length >= 2 && length <= 16)) {
            throw new ClientException("乘车人名称请设置2-16位的长度");
        }
        if (!IdcardUtil.isValidCard(requestParam.getIdCard())) {
            throw new ClientException("乘车人证件号错误");
        }
        if (!PhoneUtil.isMobile(requestParam.getPhone())) {
            throw new ClientException("乘车人手机号错误");
        }
    }

    private void delUserPassengerCache(String username) {
        distributedCache.delete(USER_PASSENGER_LIST + username);
    }
    
    
    @Override
    public List<PassengerRespDTO> listPassengerQueryByUsername(String username) {
        String actualUserPassengerListStr = getActualUserPassengerListStr(username);
        return Optional.ofNullable(actualUserPassengerListStr)
                .map(each -> JSON.parseArray(each, PassengerRespDTO.class))
                .map(each -> BeanUtil.convert(each, PassengerRespDTO.class))
                .orElse(null);
    }

    @Override
    public void savePassenger(PassengerReqDTO request) {
        verifyPassenger(request);
        String username = UserContext.getUsername();
        try {
            PassengerDO passengerDO = BeanUtil.convert(request, PassengerDO.class);
            passengerDO.setUsername(username);
            passengerDO.setCreateDate(new Date());
            passengerDO.setVerifyStatus(VerifyStatusEnum.REVIEWED.getCode());
            int inserted = passengerMapper.insert(passengerDO);
            if (!SqlHelper.retBool(inserted)) {
                throw new ServiceException(String.format("[%s] 新增乘车人失败", username));
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                log.error("{}，请求参数：{}", ex.getMessage(), JSON.toJSONString(request));
            } else {
                log.error("[{}] 新增乘车人失败，请求参数：{}", username, JSON.toJSONString(request), ex);
            }
            throw ex;
        }
        delUserPassengerCache(username);
    }

    @Override
    public List<PassengerRespDTO> listPassengerQueryByIds(String username, List<Long> ids) {
        String actualUserPassengerListStr = getActualUserPassengerListStr(username);
        if (StrUtil.isEmpty(actualUserPassengerListStr)) {
            return List.of();
        }
        return JSON.parseArray(actualUserPassengerListStr, PassengerDO.class)
                .stream()
                .filter(each -> ids.contains(each.getId()))
                .map(each -> BeanUtil.convert(each, PassengerRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updatePassenger(PassengerReqDTO request) {
        verifyPassenger(request);
        String username = UserContext.getUsername();
        try {
            PassengerDO passengerDO = BeanUtil.convert(request, PassengerDO.class);
            passengerDO.setUsername(username);
            LambdaUpdateWrapper<PassengerDO> updateWrapper = Wrappers.lambdaUpdate(PassengerDO.class)
                    .eq(PassengerDO::getId, request.getId())
                    .eq(PassengerDO::getUsername, username);
            int updated = passengerMapper.update(passengerDO, updateWrapper);
            if (!SqlHelper.retBool(updated)) {
                throw new ServiceException(String.format("[%s] 修改乘车人失败", username));
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                log.error("{}，请求参数：{}", ex.getMessage(), JSON.toJSONString(request));
            } else {
                log.error("[{}] 修改乘车人失败，请求参数：{}", username, JSON.toJSONString(request), ex);
            }
            throw ex;
            }
        delUserPassengerCache(username);
    }
}
