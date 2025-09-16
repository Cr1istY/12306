package cn.foreveryang.my12306.service;

import cn.foreveryang.my12306.dto.req.UserLoginReqDTO;
import cn.foreveryang.my12306.dto.req.UserRegisterReqDTO;
import cn.foreveryang.my12306.dto.resp.UserLoginRespDTO;
import cn.foreveryang.my12306.dto.resp.UserRegisterRespDTO;
import jakarta.validation.constraints.NotEmpty;

public interface UserLoginService {
    UserLoginRespDTO userLogin(UserLoginReqDTO request);

    UserLoginRespDTO checkLogin(String accessToken);

    void userLogout(String accessToken);

    Boolean hasUsername(@NotEmpty String username);

    UserRegisterRespDTO registerUser(UserRegisterReqDTO request);

    Integer queryUserDeletionNum(Integer idType, String idCard);
    
}
