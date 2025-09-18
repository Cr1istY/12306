package cn.foreveryang.my12306.service;

import cn.foreveryang.my12306.dto.req.PassengerReqDTO;
import cn.foreveryang.my12306.dto.resp.PassengerRespDTO;

import java.util.List;

public interface PassengerService {
    List<PassengerRespDTO> listPassengerQueryByUsername(String username);

    void savePassenger(PassengerReqDTO request);

    List<PassengerRespDTO> listPassengerQueryByIds(String username, List<Long> ids);
}
