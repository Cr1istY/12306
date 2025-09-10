package cn.foreveryang.my12306.controller;

import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.common.Results;
import cn.foreveryang.my12306.dto.req.RegionStationQueryReqDTO;
import cn.foreveryang.my12306.dto.resp.RegionStationQueryRespDTO;
import cn.foreveryang.my12306.dto.resp.StationQueryRespDTO;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;
import cn.foreveryang.my12306.service.TrainStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrainStationController {
    
    private final TrainStationService trainStationService;

    /**
     * 获取列车经停站信息
     * @param trainId
     * @return
     */
    @GetMapping("/api/ticket-service/train-station/query")
    public Result<List<TrainStationQueryRespDTO>> listTrainStationQuery(String trainId){
        log.info("listTrainStationQuery:{}", trainId);
        return trainStationService.listTrainStationQuery(trainId);
    }
    
    /**
     * 获取所有站点信息
     * @return 所有站点信息
     */
    @GetMapping("/api/ticket-service/station/all")
    public Result<List<StationQueryRespDTO>> listAllTrainStationQuery() {
        log.info("list all train station");
        // 高复用、并发场景考虑使用redis缓存实现
        return Results.success(trainStationService.listAllTrainStationQuery());
    }
    
    @GetMapping("/api/ticket-service/region-station/query")
    public Result<List<RegionStationQueryRespDTO>> listRegionStationQuery(RegionStationQueryReqDTO request) {
        log.info("listRegionStationQuery:{},{}", request.getQueryType(), request.getName());
        return Results.success(trainStationService.listRegionStationQuery(request));
    }
    
}
