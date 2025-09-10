package cn.foreveryang.my12306.service.impl;


import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.common.Results;
import cn.foreveryang.my12306.common.cache.DistributedCache;
import cn.foreveryang.my12306.common.toolkit.BeanUtil;
import cn.foreveryang.my12306.dao.entity.TrainStationDO;
import cn.foreveryang.my12306.mapper.TrainStationMapper;
import cn.foreveryang.my12306.dto.resp.StationQueryRespDTO;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;
import cn.foreveryang.my12306.mapper.StationMapper;
import cn.foreveryang.my12306.service.TrainStationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.foreveryang.my12306.common.constant.Index12306Constant.ADVANCE_TICKET_DAY;
import static cn.foreveryang.my12306.common.constant.RedisKeyConstant.STATION_ALL;

@Service
@RequiredArgsConstructor
public class TrainStationServiceImpl implements TrainStationService {
    
    private final TrainStationMapper trainStationMapper;
    private final DistributedCache distributedCache;
    private final StationMapper stationMapper;
    
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


}
