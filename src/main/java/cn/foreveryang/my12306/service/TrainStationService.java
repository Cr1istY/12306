package cn.foreveryang.my12306.service;

import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;

import java.util.List;

public interface TrainStationService {
    Result<List<TrainStationQueryRespDTO>> listTrainStationQuery(String trainId);
}
