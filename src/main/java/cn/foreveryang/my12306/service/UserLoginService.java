package cn.foreveryang.my12306.service;

import cn.foreveryang.my12306.dto.req.UserLoginReqDTO;
import cn.foreveryang.my12306.dto.resp.UserLoginRespDTO;

public interface UserLoginService {
    UserLoginRespDTO userLogin(UserLoginReqDTO request);
}
