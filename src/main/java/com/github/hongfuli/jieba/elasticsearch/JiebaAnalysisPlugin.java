package com.github.hongfuli.jieba.elasticsearch;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lihongfu on 17/6/23.
 */
public class JiebaAnalysisPlugin extends Plugin implements AnalysisPlugin {


    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> map = new HashMap<>();
        map.put("jieba", JiebaAnalyzerProvider::new);
        return map;
    }
}
