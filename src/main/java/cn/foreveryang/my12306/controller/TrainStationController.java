package cn.foreveryang.my12306.controller;

import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.dto.resp.TrainStationQueryRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainStationController {

    @GetMapping("/api/ticket-service/region-station/query")
    public Result<List<TrainStationQueryRespDTO>>listTrainStationQuery(){
        return new Result<List<TrainStationQueryRespDTO>>().setData(null);
    }
}
