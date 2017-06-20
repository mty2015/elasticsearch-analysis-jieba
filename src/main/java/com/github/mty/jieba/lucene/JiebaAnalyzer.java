package com.github.mty.jieba.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Created by lihongfu on 17/6/19.
 */
public final class JiebaAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new JiebaTokenizer();
        return new TokenStreamComponents(tokenizer);
    }
}
