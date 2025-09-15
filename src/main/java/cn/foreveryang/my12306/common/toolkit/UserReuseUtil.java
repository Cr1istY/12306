package cn.foreveryang.my12306.common.toolkit;

public final class UserReuseUtil {

    public static final int USER_REGISTER_REUSE_SHARDING_COUNT = 1024;
    
    public static int hashShardingIdx(String username) {
        return Math.abs(username.hashCode() % USER_REGISTER_REUSE_SHARDING_COUNT);
    }
}
