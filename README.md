基于 [jieba](https://github.com/fxsjy/jieba) 的 [elasticsearch](https://www.elastic.co/products/elasticsearch) 中文分词插件。

集成到ElasticSearch
=======

```bash
git clone git@github.com:hongfuli/elasticsearch-analysis-jieba.git
cd elasticsearch-analysis-jieba
mvn package
```
把release/elasticsearch-analysis-jieba-{version}.zip文件解压到 elasticsearch 的 plugins 目录下，重启elasticsearch即可。

创建字段：
```bash

curl -XPOST http://localhost:9200/index/type/_mapping -d'
{
        "properties": {
            "content": {
                "type": "text",
                "analyzer": "jieba",
                "search_analyzer": "jieba"
            }
        }
    }
}'
```


直接使用Tokenizer分词
=======
可直接使用 `com.github.hongfuli.jieba.Tokenizer` 对文本字符进行分词，方法参数完全和 [jieba python](https://github.com/fxsjy/jieba) 一致。

```java
imort com.github.hongfuli.jieba.Tokenizer

Tokenizer t = new Tokenizer();
t.cut("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", false, true);
```

集成到Lucene
=======

```java
import com.github.hongfuli.jieba.lucene.JiebaAnalyzer;

Analyzer analyzer = new JiebaAnalyzer();
try(TokenStream ts = analyzer.tokenStream("field", "这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。")) {
      StringBuilder b = new StringBuilder();
      CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posIncAtt = ts.getAttribute(PositionIncrementAttribute.class);
      PositionLengthAttribute posLengthAtt = ts.getAttribute(PositionLengthAttribute.class);
      OffsetAttribute offsetAtt = ts.getAttribute(OffsetAttribute.class);
      assertNotNull(offsetAtt);
      ts.reset();
      int pos = -1;
      while (ts.incrementToken()) {
        pos += posIncAtt.getPositionIncrement();
        b.append(termAtt);
        b.append(" at pos=");
        b.append(pos);
        if (posLengthAtt != null) {
          b.append(" to pos=");
          b.append(pos + posLengthAtt.getPositionLength());
        }
        b.append(" offsets=");
        b.append(offsetAtt.startOffset());
        b.append('-');
        b.append(offsetAtt.endOffset());
        b.append('\n');
      }
      ts.end();
      return b.toString();
    }
```