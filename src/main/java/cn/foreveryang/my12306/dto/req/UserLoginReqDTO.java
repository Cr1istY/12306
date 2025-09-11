package cn.foreveryang.my12306.dto.req;


import lombok.Data;

@Data
public class UserLoginReqDTO {
    private String usernameOrMailOrPhone;
    private String password;
}
