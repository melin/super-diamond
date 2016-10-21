package com.github.diamond;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Author: yuwang@iflytek.com
 * Date: 2016/10/21 11:26
 */
public class DiamondPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    @Override
    protected String convertPropertyValue(String originalValue) {
        originalValue = originalValue == null ? null : originalValue.trim();
        return super.convertPropertyValue(originalValue);
    }
}
