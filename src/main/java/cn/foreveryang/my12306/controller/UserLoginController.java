package cn.foreveryang.my12306.controller;


import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.common.Results;
import cn.foreveryang.my12306.dto.req.UserLoginReqDTO;
import cn.foreveryang.my12306.dto.resp.UserLoginRespDTO;
import cn.foreveryang.my12306.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserLoginController {
    
    private final UserLoginService userLoginService;
    
    @PostMapping("/api/user-service/v1/login")
    public Result<UserLoginRespDTO> userLogin(@RequestBody UserLoginReqDTO request) {
        log.info("user login:{}", request.getUsernameOrMailOrPhone());
        return Results.success(userLoginService.userLogin(request));
    }
    
    
}
