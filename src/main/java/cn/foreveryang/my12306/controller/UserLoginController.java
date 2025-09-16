package cn.foreveryang.my12306.controller;


import cn.foreveryang.my12306.common.Result;
import cn.foreveryang.my12306.common.Results;
import cn.foreveryang.my12306.dto.req.UserLoginReqDTO;
import cn.foreveryang.my12306.dto.req.UserRegisterReqDTO;
import cn.foreveryang.my12306.dto.resp.UserLoginRespDTO;
import cn.foreveryang.my12306.dto.resp.UserRegisterRespDTO;
import cn.foreveryang.my12306.service.UserLoginService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    
    
    @GetMapping("/api/user-service/check-login")
    public Result<UserLoginRespDTO> checkLogin(@RequestParam("accessToken") String accessToken) {
        log.info("check login:{}", accessToken);
        return Results.success(userLoginService.checkLogin(accessToken));
    }
    
    @GetMapping("/api/user-service/logout")
    public Result<Void> userLogout(@RequestParam(value = "accessToken", required = false) String accessToken) {
        log.info("用户登出：{}", accessToken);
        userLoginService.userLogout(accessToken);
        return Results.success();
    }
    
    @GetMapping("/api/user-service/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userLoginService.hasUsername(username));
    }
    
    @PostMapping("/api/user-service/register")
    public Result<UserRegisterRespDTO> registerUser(@RequestBody @Valid UserRegisterReqDTO request) {
        log.info("用户注册: {}", request.getUsername());
        return Results.success(userLoginService.registerUser(request));
    }
    
}
