package com.github.mty.jieba.elasticsearch;

import com.github.mty.jieba.lucene.JiebaAnalyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

/**
 * Created by lihongfu on 17/6/23.
 */
public class JiebaAnalyzerProvider extends AbstractIndexAnalyzerProvider<JiebaAnalyzer> {
    /**
     * Constructs a new analyzer component, with the index name and its settings and the analyzer name.
     *
     * @param indexSettings the settings and the name of the index
     * @param name          The analyzer name
     * @param settings
     */
    private final JiebaAnalyzer analyzer;

    public JiebaAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        analyzer = new JiebaAnalyzer();

    }

    @Override
    public JiebaAnalyzer get() {
        return this.analyzer;
    }
}
