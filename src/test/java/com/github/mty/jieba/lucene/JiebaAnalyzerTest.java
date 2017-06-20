package com.github.mty.jieba.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;

import java.io.IOException;
import java.util.Random;

/**
 * Created by lihongfu on 17/6/19.
 */
public class JiebaAnalyzerTest extends BaseTokenStreamTestCase {

    public void testStandardAnalyzer() throws IOException {
        Analyzer analyzer = new JiebaAnalyzer();

        checkRandomData(new Random(0), analyzer, 1);

        System.out.println(BaseTokenStreamTestCase.toString(analyzer, "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作"));

    }


}