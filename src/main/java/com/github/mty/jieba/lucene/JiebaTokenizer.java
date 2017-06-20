package com.github.mty.jieba.lucene;

import com.github.mty.jieba.Token;
import com.github.mty.jieba.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by lihongfu on 17/6/19.
 */
public class JiebaTokenizer extends org.apache.lucene.analysis.Tokenizer {
    private com.github.mty.jieba.Tokenizer scanner;
    private BufferedReader bufferReader;
    private int tokenIndex;
    private List<Token> tokenBuffer;
    private int finalOffset;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    public JiebaTokenizer() {
        this.scanner = new Tokenizer();
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (bufferReader == null) {
            throw new IllegalStateException("must call reset before call incrementToken()");
        }
        clearAttributes();
        if (tokenBuffer == null || tokenIndex >= tokenBuffer.size()) {
            String line = bufferReader.readLine();
            if (line == null) {
                return false;
            }
            tokenBuffer = scanner.tokenize(line, true, true);
            tokenIndex = 0;
        }
        Token token = tokenBuffer.get(tokenIndex);
        termAtt.append(token.value);
        offsetAtt.setOffset(token.startPos, token.endPos);
        posIncrAtt.setPositionIncrement(1);
        tokenIndex += 1;
        finalOffset = token.endPos;
        return true;
    }

    @Override
    public void end() throws IOException {
        super.end();
        offsetAtt.setOffset(finalOffset+1, finalOffset+1);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        if (BufferedReader.class.isAssignableFrom(input.getClass())) {
            bufferReader = (BufferedReader) input;
        } else {
            bufferReader = new BufferedReader(this.input);
        }
        tokenIndex = 0;
        tokenBuffer = null;
    }

    @Override
    public void close() throws IOException {
        super.close();
        bufferReader.close();
        bufferReader = null;
    }
}
