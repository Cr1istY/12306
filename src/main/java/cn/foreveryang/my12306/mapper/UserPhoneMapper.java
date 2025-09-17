package cn.foreveryang.my12306.mapper;

import cn.foreveryang.my12306.dao.entity.UserPhoneDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface UserPhoneMapper extends BaseMapper<UserPhoneDO> {

    void deletionUser(UserPhoneDO userDO);
}
