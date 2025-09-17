package cn.foreveryang.my12306.mapper;

import cn.foreveryang.my12306.dao.entity.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface UserMapper extends BaseMapper<UserDO> {
    
    void deletionUser(UserDO userDO);
}
