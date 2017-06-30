package com.github.hongfuli.jieba.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lihongfu on 17/6/19.
 */
public final class JiebaAnalyzer extends Analyzer {
//    private static final Pattern RE_SKIP_DEFAULT = Pattern.compile("(\\r\\n|\\s)");

    private InputStream userDictIn;

    public JiebaAnalyzer() {
    }

    public JiebaAnalyzer(InputStream userDictIn) {
        setUserDictIn(userDictIn);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        JiebaTokenizer tokenizer = new JiebaTokenizer();
        if (userDictIn != null) {
            try {
                tokenizer.loadUserDict(userDictIn);
            } catch (IOException e) {
                throw new RuntimeException("load user dict error");
            }
        }
        TokenFilter stopFilter = new JiebaStopTokenFilter(tokenizer);
        return new TokenStreamComponents(tokenizer, stopFilter);
    }

    public void setUserDictIn(InputStream userDictIn) {
        if (userDictIn == null) {
            throw new IllegalArgumentException("userDictIn is null");
        }
        this.userDictIn = userDictIn;
    }


    //    @Override
//    protected Reader initReader(String fieldName, Reader reader) {
//        return new PatternReplaceCharFilter(RE_SKIP_DEFAULT, ",", reader);
//    }
}
