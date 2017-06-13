package com.github.mty.jieba;

/**
 * Created by lihongfu on 17/6/13.
 */
public class Token {
    public String value;
    public int startPos;
    public int endPos;

    public Token(String value, int startPos, int endPos) {
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    @Override
    public String toString(){
        return "token: value = " + value + "; startPos = " + startPos + "; endPos = " + endPos;
    }
}
