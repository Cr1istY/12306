package cn.foreveryang.my12306.service.impl;


import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.common.Results;
import cn.foreveryang.my12306.common.cache.DistributedCache;
import cn.foreveryang.my12306.common.cache.core.CacheLoader;
import cn.foreveryang.my12306.common.cache.toolkit.CacheUtil;
import cn.foreveryang.my12306.common.enums.FlagEnum;
import cn.foreveryang.my12306.common.enums.RegionStationQueryTypeEnum;
import cn.foreveryang.my12306.common.exception.ClientException;
import cn.foreveryang.my12306.common.toolkit.BeanUtil;
import cn.foreveryang.my12306.dao.entity.RegionDO;
import cn.foreveryang.my12306.dao.entity.StationDO;
import cn.foreveryang.my12306.dao.entity.TrainStationDO;
import cn.foreveryang.my12306.dto.req.RegionStationQueryReqDTO;
import cn.foreveryang.my12306.dto.resp.RegionStationQueryRespDTO;
import cn.foreveryang.my12306.mapper.RegionMapper;
import cn.foreveryang.my12306.mapper.TrainStationMapper;
import cn.foreveryang.my12306.dto.resp.StationQueryRespDTO;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;
import cn.foreveryang.my12306.mapper.StationMapper;
import cn.foreveryang.my12306.service.TrainStationService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.foreveryang.my12306.common.constant.Index12306Constant.ADVANCE_TICKET_DAY;
import static cn.foreveryang.my12306.common.constant.RedisKeyConstant.*;

@Service
@RequiredArgsConstructor
public class TrainStationServiceImpl implements TrainStationService {
    
    private final TrainStationMapper trainStationMapper;
    private final DistributedCache distributedCache;
    private final StationMapper stationMapper;
    private final RedissonClient redissonClient;
    private final RegionMapper regionMapper;

    @Override
    public Result<List<TrainStationQueryRespDTO>> listTrainStationQuery(String trainId) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId);
        List<TrainStationDO> trainStationDOS = trainStationMapper.selectList(queryWrapper);
        return Results.success(BeanUtil.convert(trainStationDOS, TrainStationQueryRespDTO.class));
    }

    @Override
    public List<StationQueryRespDTO> listAllTrainStationQuery() {
        return distributedCache.safeGet(
                STATION_ALL,
                List.class,
                () -> BeanUtil.convert(stationMapper.selectList(Wrappers.emptyWrapper()), StationQueryRespDTO.class),
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
    }

    @Override
    public List<RegionStationQueryRespDTO> listRegionStationQuery(RegionStationQueryReqDTO request) {
        String key;
        if (StrUtil.isNotBlank(request.getName())) {
            key = REGION_STATION + request.getName();
            return safeGetRegionStation(
                    key,
                    () -> {
                        LambdaQueryWrapper<StationDO> queryWrapper = Wrappers.lambdaQuery(StationDO.class)
                                .like(StationDO::getName, request.getName())
                                .or()
                                .likeRight(StationDO::getSpell, request.getName());
                        List<StationDO> stationDOS = stationMapper.selectList(queryWrapper);
                        return JSON.toJSONString(BeanUtil.convert(stationDOS, StationQueryRespDTO.class));
                    },
                    request.getName()
            );
        }
        key = REGION_STATION + request.getQueryType();
        LambdaQueryWrapper<RegionDO> queryWrapper = switch (request.getQueryType()) {
            case 0 -> Wrappers.lambdaQuery(RegionDO.class)
                    .eq(RegionDO::getPopularFlag, FlagEnum.TRUE.code());
            case 1 -> Wrappers.lambdaQuery(RegionDO.class)
                    .in(RegionDO::getInitial, RegionStationQueryTypeEnum.A_E.getSpells());
            case 2 -> Wrappers.lambdaQuery(RegionDO.class)
                    .in(RegionDO::getInitial, RegionStationQueryTypeEnum.F_J.getSpells());
            case 3 -> Wrappers.lambdaQuery(RegionDO.class)
                    .in(RegionDO::getInitial, RegionStationQueryTypeEnum.K_O.getSpells());
            case 4 -> Wrappers.lambdaQuery(RegionDO.class)
                    .in(RegionDO::getInitial, RegionStationQueryTypeEnum.P_T.getSpells());
            case 5 -> Wrappers.lambdaQuery(RegionDO.class)
                    .in(RegionDO::getInitial, RegionStationQueryTypeEnum.U_Z.getSpells());
            default -> throw new ClientException("查询失败，请检查查询参数是否正确");
        };
        
        return safeGetRegionStation(
                key,
                () -> {
                    List<RegionDO> regionDOS = regionMapper.selectList(queryWrapper);
                    return JSON.toJSONString(BeanUtil.convert(regionDOS, RegionStationQueryRespDTO.class));
                },
                String.valueOf(request.getQueryType())
        );
    }
    
    private List<RegionStationQueryRespDTO> safeGetRegionStation(final String key, CacheLoader<String> loader, String param) {
        List<RegionStationQueryRespDTO> result;
        if (CollUtil.isNotEmpty(result = JSON.parseArray(distributedCache.get(key, String.class),
                RegionStationQueryRespDTO.class))) {
            return result;
        }

        String lockKey = String.format(LOCK_QUERY_REGION_STATION_LIST, param);
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            if (CollUtil.isEmpty(result = JSON.parseArray(distributedCache.get(key, String.class),
                    RegionStationQueryRespDTO.class))) {
                if (CollUtil.isEmpty(result = loadAndSet(key, loader))) {
                    return Collections.emptyList();
                }
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    private List<RegionStationQueryRespDTO> loadAndSet(String key, CacheLoader<String> loader) {
        String result = loader.load();
        if (CacheUtil.isNullOrBlank(result)) {
            return Collections.emptyList();
        }
        List<RegionStationQueryRespDTO> respDTOList = JSON.parseArray(result, RegionStationQueryRespDTO.class);
        distributedCache.put(
                key,
                result,
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
        return respDTOList;
    }
    

}
