package com.github.hongfuli.jieba.lucene;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by lihongfu on 17/6/23.
 */
public class JiebaStopTokenFilter extends FilteringTokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private static final Pattern NOT_WORD = Pattern.compile("\\w", Pattern.UNICODE_CHARACTER_CLASS);

    public JiebaStopTokenFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected boolean accept() throws IOException {
        String term = termAtt.toString();
        if (term.length() > 1){
            return true;
        }else{
            return NOT_WORD.matcher(term).matches();
        }
    }
}
