package com.king.aicustomerservice.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 * 本学习项目按要求使用 MD5 存储用户密码（生产环境建议使用 BCrypt）
 */
public final class Md5Util {

    private Md5Util() {
    }

    /**
     * 对明文进行 MD5 加密，返回 32 位小写十六进制字符串
     *
     * @param raw 明文
     * @return MD5 摘要
     */
    public static String encrypt(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }
}
