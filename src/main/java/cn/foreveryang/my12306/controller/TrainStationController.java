package cn.foreveryang.my12306.controller;

import cn.foreveryang.my12306.common.Result;
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
}
