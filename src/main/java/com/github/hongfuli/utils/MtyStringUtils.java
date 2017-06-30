package com.github.hongfuli.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lihongfu on 17/6/3.
 */
public class MtyStringUtils {
    /**
     * 该方法和 {@link Pattern#split(CharSequence)} 功能一样,利用pattern匹配规则分割字符str, 但是会把匹配的分割符串也返回.
     * 比如字符串 "hello123word" 用 (\d+) 分割, 会返回["hello", "123", "word"]
     *
     * @param pattern
     * @param str
     * @return
     */
    public static List<String> splitAndReturnDelimiters(Pattern pattern, String str) {
        Matcher matcher = pattern.matcher(str);
        List<String> result = new ArrayList<String>();
        int strLen = str.length();
        int lastMatchIdx = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String ds = matcher.group();

            if (lastMatchIdx != start) {
                String leftS = str.substring(lastMatchIdx, start);
                result.add(leftS);
            }

            result.add(ds);

            lastMatchIdx = end;
        }

        if (lastMatchIdx < strLen) {
            result.add(str.substring(lastMatchIdx, strLen));
        }
        return result;

    }


}

