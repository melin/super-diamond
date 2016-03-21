package com.github.diamond.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author libinsong1204@gmail.com
 */
public class MD5 {
    private static final Log log = LogFactory.getLog(MD5.class);
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static Map<Character, Integer> rDigits = new HashMap<Character, Integer>(16);

    static {
        for (int i = 0; i < digits.length; ++i) {
            rDigits.put(digits[i], i);
        }
    }

    private static MD5 me = new MD5();
    private MessageDigest messageDigest;
    private ReentrantLock opLock = new ReentrantLock();

    public static final String ENCODE = "UTF-8";

    private MD5() {
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static MD5 getInstance() {
        return me;
    }


    public String getMD5String(String content) {
        return bytes2string(hash(content));
    }


    public String getMD5String(byte[] content) {
        return bytes2string(hash(content));
    }


    public byte[] getMD5Bytes(byte[] content) {
        return hash(content);
    }


    /**
     * 对字符串进行md5
     *
     * @param str
     * @return md5 byte[16]
     */
    public byte[] hash(String str) {
        opLock.lock();
        try {
            byte[] bt = messageDigest.digest(str.getBytes(ENCODE));
            if (null == bt || bt.length != 16) {
                throw new IllegalArgumentException("md5 need");
            }
            return bt;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("unsupported utf-8 encoding", e);
        } finally {
            opLock.unlock();
        }
    }


    /**
     * 对二进制数据进行md5
     *
     * @param data
     * @return md5 byte[16]
     */
    public byte[] hash(byte[] data) {
        opLock.lock();
        try {
            byte[] bt = messageDigest.digest(data);
            if (null == bt || bt.length != 16) {
                throw new IllegalArgumentException("md5 need");
            }
            return bt;
        } finally {
            opLock.unlock();
        }
    }


    /**
     * 将一个字节数组转化为可见的字符串
     *
     * @param bt
     * @return
     */
    public String bytes2string(byte[] bt) {
        int lengthTemp = bt.length;

        char[] out = new char[lengthTemp << 1];

        for (int i = 0, j = 0; i < lengthTemp; i++) {
            out[j++] = digits[(0xF0 & bt[i]) >>> 4];
            out[j++] = digits[0x0F & bt[i]];
        }

        if (log.isDebugEnabled()) {
            log.debug("[hash]" + (new String(out)));
        }

        return new String(out);
    }


    /**
     * 将字符串转换为bytes
     *
     * @param str
     * @return byte[]
     */
    public byte[] string2bytes(String str) {
        if (null == str) {
            throw new NullPointerException("参数不能为空");
        }
        if (str.length() != 32) {
            throw new IllegalArgumentException("字符串长度必须是32");
        }
        byte[] data = new byte[16];
        char[] chs = str.toCharArray();
        for (int i = 0; i < 16; ++i) {
            int num1 = rDigits.get(chs[i * 2]).intValue();
            int num2 = rDigits.get(chs[i * 2 + 1]).intValue();
            data[i] = (byte) ((num1 & 0x0F) << 4 | (num2 & 0x0F));
        }
        return data;
    }

    public static void main(String[] args) {
        System.out.println(MD5.getInstance().getMD5String("000000"));
    }
}
