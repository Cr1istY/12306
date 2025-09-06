package cn.foreveryang.my12306.common;

import cn.foreveryang.my12306.common.errorcode.BaseErrorCode;
import cn.foreveryang.my12306.common.exception.AbstractException;

import java.util.Optional;

public final class Results {

    /**
     * 创建一个成功结果
     *
     * @return 结果
     */
    public static Result<Void> success() {
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE);
    }
    /**
     * 创建一个带参数成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(Result.SUCCESS_CODE)
                .setData(data);
    }
    /**
     * 创建一个带信息失败结果
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 错误结果
     */
    public static Result<Void> failure(String code, String message) {
        return new Result<Void>()
                .setCode(code)
                .setMessage(message);
    }
    /**
     * 创建一个服务端失败响应
     *
     * @return 错误结果
     */
    public static Result<Void> failure() {
        return new Result<Void>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message());
    }

    /**
     * 通过 {@link AbstractException} 构建失败响应
     */
    protected static Result<Void> failure(AbstractException abstractException) {
        String errorCode = Optional.ofNullable(abstractException.getErrorCode())
                .orElse(BaseErrorCode.SERVICE_ERROR.code());
        String errorMessage = Optional.ofNullable(abstractException.getErrorMessage())
                .orElse(BaseErrorCode.SERVICE_ERROR.message());
        return new Result<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }
    
}
