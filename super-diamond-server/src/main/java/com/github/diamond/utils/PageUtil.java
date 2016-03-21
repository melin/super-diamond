package com.github.diamond.utils;

/**
 * @author libinsong1204@gmail.com
 */
public class PageUtil {
    public static int pageCount(long recordCount, int limit) {
        int pc = (int) Math.ceil(recordCount * 1d / limit);
        return (pc == 0) ? 1 : pc;
    }

    public static int getOffset(int page, int limit) {
        if (page < 1) {
            page = 1;
        }
        return (page - 1) * limit;
    }
}
