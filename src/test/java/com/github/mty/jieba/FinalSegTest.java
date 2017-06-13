package com.github.mty.jieba;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by lihongfu on 17/6/9.
 */
public class FinalSegTest extends TestCase {


    public void testLoadEmitP() throws IOException {
        FinalSeg seg = new FinalSeg("/emit_test.txt");
        System.out.println(seg.getEmitP());
    }

    public void testLoadEmitPDefault() throws IOException {
        FinalSeg seg = new FinalSeg();
        System.out.println(seg.getEmitP().get('B').size() + seg.getEmitP().get('E').size()
                + seg.getEmitP().get('M').size() + seg.getEmitP().get('S').size());
    }

    public void testCut() throws IOException {
        FinalSeg seg = new FinalSeg();
        System.out.println(seg.cut("我最喜欢青白玉"));
        System.out.println(seg.cut("你是喜欢Python还是Java呢,我也不知道吧"));
    }

}