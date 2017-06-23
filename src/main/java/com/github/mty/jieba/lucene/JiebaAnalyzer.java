package com.github.mty.jieba.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;

import java.io.Reader;
import java.util.regex.Pattern;

/**
 * Created by lihongfu on 17/6/19.
 */
public final class JiebaAnalyzer extends Analyzer {
    private static final Pattern RE_SKIP_DEFAULT = Pattern.compile("(\\r\\n|\\s)");

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new JiebaTokenizer();
        return new TokenStreamComponents(tokenizer);
    }


    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        return new PatternReplaceCharFilter(RE_SKIP_DEFAULT, ",", reader);
    }
}
