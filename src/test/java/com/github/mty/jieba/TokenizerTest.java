package com.github.mty.jieba;

import junit.framework.TestCase;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lihongfu on 17/6/2.
 */
public class TokenizerTest extends TestCase {

    public void testHanPattern() {
        Pattern hanP = Pattern.compile("([\\u4E00-\\u9FD5]+)");
        String sentence = "abc我是中国人bc你好 workd";
        Matcher matcher = hanP.matcher(sentence);
        System.out.println(matcher.matches());
        for (String s : hanP.split(sentence)) {
            System.out.print(s + " / ");
        }
    }

    public void testSplit() {
        for (String x : "hek fd 133 4.def".split("((?!^))")) {
            System.out.println(x + "====");
        }
    }

    private void printResult(List<String> tokens) {
        for (String t : tokens) {
            System.out.print(t + "|");
        }
        System.out.println();

    }

    private void printTokens(List<Token> tokens){
        for (Token token : tokens){
            System.out.println(token);
        }
    }

    public void testCutAll() {
        Tokenizer t = new Tokenizer();
        printResult(t.cut("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", true, false));
        printResult(t.cut("我不喜欢日本和服。", true, false));
        printResult(t.cut("雷猴回归人间。", true, false));
        printResult(t.cut("工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", true, false));
        printResult(t.cut("“Microsoft”一词由“MICROcomputer（微型计算机）”和“SOFTware（软件）”两部分组成", true, false));
    }

    public void testCutNoHMM() {
        Tokenizer t = new Tokenizer();
        printResult(t.cut("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", false, false));
        printResult(t.cut("我不喜欢日本和服。", false, false));
        printResult(t.cut("雷猴回归人间。", false, false));
        printResult(t.cut("工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", false, false));
        printResult(t.cut("“Microsoft”一词由“MICROcomputer（微型计算机）”和“SOFTware（软件）”两部分组成", false, false));
    }

    public void testCutHMM() {
        Tokenizer t = new Tokenizer();
        printResult(t.cut("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", false, true));
        printResult(t.cut("我不喜欢日本和服。", false, true));
        printResult(t.cut("雷猴回归人间。", false, true));
        printResult(t.cut("工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", false, true));
        printResult(t.cut("“Microsoft”一词由“MICROcomputer（微型计算机）”和“SOFTware（软件）”两部分组成", false, true));
        printResult(t.cut("这个洒金皮的和田玉你喜欢吗", false, true));
    }

    public void testCutForSearch() {
        Tokenizer t = new Tokenizer();
        printResult(t.cutForSearch("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。"));
        printResult(t.cutForSearch("我不喜欢日本和服。"));
        printResult(t.cutForSearch("雷猴回归人间。"));
        printResult(t.cutForSearch("工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作"));
    }

    public void testToknizer() {
        Tokenizer t = new Tokenizer();
        printTokens(t.tokenize("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", false, true));
        System.out.println("====================");
        printTokens(t.tokenize("hello world, this is my first program", false, true));
    }

}

