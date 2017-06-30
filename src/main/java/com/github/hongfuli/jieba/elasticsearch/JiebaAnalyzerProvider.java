package com.github.hongfuli.jieba.elasticsearch;

import com.github.hongfuli.jieba.lucene.JiebaAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Created by lihongfu on 17/6/23.
 */
public class JiebaAnalyzerProvider extends AbstractIndexAnalyzerProvider<JiebaAnalyzer> {
    private final JiebaAnalyzer analyzer;
    private static final Logger logger = LogManager.getLogger(JiebaAnalyzerProvider.class);

    public JiebaAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        analyzer = new JiebaAnalyzer();

        logger.info("load jieba_config.properties");
        Path configPath = PathUtils.get(new File(JiebaAnalyzer.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent()).toAbsolutePath().resolve("jieba_config.properties");
        Properties props = new Properties();
        try {
            props.load(Files.newInputStream(configPath));
        } catch (IOException e) {
            throw new RuntimeException("load jieba_config.properties error");
        }

        String userDictPath = props.getProperty("user_dict");
        if (userDictPath != null && !userDictPath.trim().isEmpty()) {
            try {
                logger.info("load user dict from file: " + userDictPath);
                analyzer.setUserDictIn(new FileInputStream(userDictPath));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("user_dict file path cannot load: " + userDictPath);
            }
        }

    }

    @Override
    public JiebaAnalyzer get() {
        return this.analyzer;
    }
}
