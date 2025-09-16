package cn.foreveryang.my12306.dto.resp;


import lombok.Data;

@Data
public class UserRegisterRespDTO {
    private String username;
    private String phone;
    private String realName;
}
