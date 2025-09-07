package cn.foreveryang.my12306.service.impl;


import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.common.Results;
import cn.foreveryang.my12306.common.toolkit.BeanUtil;
import cn.foreveryang.my12306.dao.entity.TrainStationDO;
import cn.foreveryang.my12306.dao.mapper.TrainStationMapper;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;
import cn.foreveryang.my12306.service.TrainStationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainStationServiceImpl implements TrainStationService {
    
    private final TrainStationMapper trainStationMapper;
    
    @Override
    public Result<List<TrainStationQueryRespDTO>> listTrainStationQuery(String trainId) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId);
        List<TrainStationDO> trainStationDOS = trainStationMapper.selectList(queryWrapper);
        return Results.success(BeanUtil.convert(trainStationDOS, TrainStationQueryRespDTO.class));
    }
    
    
}
