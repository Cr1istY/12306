package cn.foreveryang.my12306.common.cache.core;

@FunctionalInterface
public interface CacheLoader<T> {
    T load();
}
