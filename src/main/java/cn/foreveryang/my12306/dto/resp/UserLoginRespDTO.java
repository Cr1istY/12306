package cn.foreveryang.my12306.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRespDTO {
    
    private String userId;
    
    private String username;
    
    private String realName;
    
    private String accessToken;
    
}
