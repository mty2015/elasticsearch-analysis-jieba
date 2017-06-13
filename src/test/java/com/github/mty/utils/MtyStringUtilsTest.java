package com.github.mty.utils;

import junit.framework.TestCase;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by lihongfu on 17/6/5.
 */
public class MtyStringUtilsTest extends TestCase {

    public void testSplitAndReturnDelimiters(){
        Pattern hanP = Pattern.compile("([\\u4E00-\\u9FD5]+)");
        String sentence = "abc我是中国人bc你好 workd";
        List<String> strings = MtyStringUtils.splitAndReturnDelimiters(hanP, sentence);
        System.out.println(strings);

        sentence = "这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。";
        strings = MtyStringUtils.splitAndReturnDelimiters(hanP, sentence);
        System.out.println(strings);


        hanP = Pattern.compile("[^a-zA-Z0-9+#\\n]");
        sentence = "C++。";
        strings = MtyStringUtils.splitAndReturnDelimiters(hanP, sentence);
        System.out.println(strings);
    }

    public void testSplitAndReturnDelimiters4continue(){
        Pattern hanP = Pattern.compile("(ab)");
        String sentence = "ababab";
        List<String> strings = MtyStringUtils.splitAndReturnDelimiters(hanP, sentence);
        System.out.println(strings);
    }

}