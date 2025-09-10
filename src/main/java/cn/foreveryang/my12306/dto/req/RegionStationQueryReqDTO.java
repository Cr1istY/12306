package cn.foreveryang.my12306.dto.req;

import lombok.Data;

@Data
public class RegionStationQueryReqDTO {
    private Integer queryType;
    private String name;
}
