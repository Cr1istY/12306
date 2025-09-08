package cn.foreveryang.my12306.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_station")
public class StationDO {

    /**
     * id
     */
    private Long id;

    /**
     * 站点编码
     */
    private String code;

    /**
     * 车站名称
     */
    private String name;

    /**
     * 名称拼音
     */
    private String spell;
    
    /**
     * 所属区域编号
     */
    private String region;

    /**
     * 区域名称
     */
    private String regionName;
    
}
