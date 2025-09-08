package cn.foreveryang.my12306.dto.resp;

import lombok.Data;

@Data
public class StationQueryRespDTO {
    
    private String name;
    private String code;
    private String spell;
    private String regionName;
}
