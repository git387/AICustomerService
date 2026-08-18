package com.king.aicustomerservice.common;

import lombok.Data;

/**
 * 统一接口返回结果
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 状态码，200 表示成功 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /**
     * 构造成功结果（无数据）
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 构造成功结果
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    /**
     * 构造成功结果并自定义提示
     */
    public static <T> Result<T> ok(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 构造失败结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 构造失败结果并指定状态码
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
