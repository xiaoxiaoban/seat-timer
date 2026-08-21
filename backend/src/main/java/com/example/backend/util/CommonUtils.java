package com.example.backend.util;

import java.util.UUID;

/**
 * 工具类
 */
public class CommonUtils {

    /**
     * 生成UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 字符串脱敏
     */
    public static String maskString(String str, int start, int end) {
        if (isEmpty(str) || str.length() <= start + end) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str, 0, start);
        for (int i = 0; i < str.length() - start - end; i++) {
            sb.append("*");
        }
        sb.append(str.substring(str.length() - end));
        return sb.toString();
    }

    /**
     * 手机号脱敏
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 邮箱脱敏
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        if (localPart.length() <= 2) {
            return "*" + domainPart;
        }
        return localPart.charAt(0) + "****" + localPart.charAt(localPart.length() - 1) + domainPart;
    }
}
