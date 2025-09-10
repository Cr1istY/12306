package cn.foreveryang.my12306.service;

import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.dto.req.RegionStationQueryReqDTO;
import cn.foreveryang.my12306.dto.resp.RegionStationQueryRespDTO;
import cn.foreveryang.my12306.dto.resp.StationQueryRespDTO;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;

import java.util.List;

public interface TrainStationService {
    
    Result<List<TrainStationQueryRespDTO>> listTrainStationQuery(String trainId);

    List<StationQueryRespDTO> listAllTrainStationQuery();

    List<RegionStationQueryRespDTO> listRegionStationQuery(RegionStationQueryReqDTO request);
}
