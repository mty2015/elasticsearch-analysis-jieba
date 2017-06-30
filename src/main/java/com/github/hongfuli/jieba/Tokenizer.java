package com.github.hongfuli.jieba;

import com.github.hongfuli.utils.MtyStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by lihongfu on 17/5/31.
 */
public class Tokenizer {


    private Map<String, Integer> freq = new HashMap(349050);
    private FinalSeg finalSeg;
    private long total;
    private boolean initialized;

    private static final String DEFAULT_DICT_FILE_NAME = "/dict.txt";

    private static final Pattern RE_HAN_DEFAULT = Pattern.compile("([\\u4E00-\\u9FD5a-zA-Z0-9+#&\\._]+)");
    private static final Pattern RE_SKIP_DEFAULT = Pattern.compile("(\\r\\n|\\s)");
    private static final Pattern RE_HAN_CUT_ALL = Pattern.compile("([\\u4E00-\\u9FD5]+)");
    private static final Pattern RE_SKIP_HAN_CUT_ALL = Pattern.compile("[^a-zA-Z0-9+#\\n]");
    private static final Pattern RE_ENG = Pattern.compile("[a-zA-Z0-9]");

    private static final Pattern RE_USERDICT = Pattern.compile("^(.+?)( [0-9]+)?( [a-z]+)?$");


    public Tokenizer() {
        try {
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initialize() throws IOException {
        this.genPfDict();
        this.finalSeg = new FinalSeg();
    }


    private void genPfDict() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Tokenizer.class.getResourceAsStream(DEFAULT_DICT_FILE_NAME)));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty())
                continue;
            String[] wordFreqs = line.split(" ");
            String word = wordFreqs[0];
            int freq = Integer.parseInt(wordFreqs[1]);
            this.freq.put(word, freq);
            this.total += freq;
            for (int i = 1; i <= word.length(); i++) {
                String wfrag = word.substring(0, i);
                if (!this.freq.containsKey(wfrag)) {
                    this.freq.put(wfrag, 0);
                }
            }
        }
    }


    private Map<Integer, List<Integer>> getDAG(String sentence) {
        Map<Integer, List<Integer>> DAG = new HashMap<Integer, List<Integer>>();
        int N = sentence.length();
        for (int k = 0; k < N; k++) {
            List<Integer> tmpList = new ArrayList<Integer>();
            int i = k;
            String frag = sentence.substring(k, k + 1);
            while (i < N && this.freq.containsKey(frag)) {
                if (this.freq.get(frag) > 0) {
                    tmpList.add(i);
                }
                i += 1;
                if (i < N) {
                    frag = sentence.substring(k, i + 1);
                }
            }
            if (tmpList.isEmpty()) {
                tmpList.add(k);
            }
            DAG.put(k, tmpList);
        }
        return DAG;
    }

    private static class Pair<E, F> {
        private E first;
        private F second;

        private Pair(E first, F second) {
            this.first = first;
            this.second = second;
        }

        public static <E, F> Pair newPair(E first, F second) {
            return new Pair<E, F>(first, second);
        }

        public E getFirst() {
            return first;
        }

        public F getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return "pair value: " + first + " , " + second;
        }
    }

    private void calc(String sentence, Map<Integer, List<Integer>> DAG, Map<Integer, Pair<Double, Integer>> route) {
        int N = sentence.length();
        route.put(N, Pair.newPair(0.0, 0));
        double logTotal = Math.log(this.total);
        for (int idx = N - 1; idx >= 0; idx--) {
            double maxFreq = -Double.MAX_VALUE;
            int maxIdx = idx;
            for (int x : DAG.get(idx)) {
                Integer freq = this.freq.get(sentence.substring(idx, x + 1));
                double logFreq = Math.log(freq == null || freq == 0 ? 1 : freq) - logTotal + route.get(x + 1).getFirst();
                if (logFreq > maxFreq) {
                    maxIdx = x;
                    maxFreq = logFreq;
                }
            }
            route.put(idx, Pair.newPair(maxFreq, maxIdx));
        }
    }

    public List<String> cut(String sentence, boolean cut_all, boolean HMM) {
        Pattern reHan, reSkip;
        if (cut_all) {
            reHan = RE_HAN_CUT_ALL;
            reSkip = RE_SKIP_HAN_CUT_ALL;
        } else {
            reHan = RE_HAN_DEFAULT;
            reSkip = RE_SKIP_DEFAULT;
        }
        CutStrategy cs;
        if (cut_all) {
            cs = new CutAllStrategy();
        } else if (HMM) {
            cs = new CutDAGStrategy();
        } else {
            cs = new CutDAGNoHMMStrategy();
        }
        List<String> blocks = MtyStringUtils.splitAndReturnDelimiters(reHan, sentence);
        List<String> tokens = new ArrayList();
        for (String blk : blocks) {
            if (blk.isEmpty()) {
                continue;
            }
            if (reHan.matcher(blk).matches()) {
                for (String word : cs.cut(blk)) {
                    tokens.add(word);
                }
            } else {
                for (String x : reSkip.split(blk)) {
                    if (reSkip.matcher(x).matches()) {
                        tokens.add(x);
                    } else if (!cut_all) {
                        for (String c : x.split("(?!^)")) {
                            tokens.add(c);
                        }
                    } else {
                        tokens.add(x);
                    }

                }
            }

        }
        return tokens;
    }

    public List<String> cutForSearch(String sentence, boolean HMM) {
        List<String> frags = this.cut(sentence, false, HMM);
        List<String> result = new ArrayList<String>();
        for (String w : frags) {
            if (w.length() > 2) {
                for (int i = 0; i < w.length() - 1; i++) {
                    String gram2 = w.substring(i, i + 2);
                    if (freq.getOrDefault(gram2, 0) > 0) {
                        result.add(gram2);
                    }
                }
            }
            if (w.length() > 3) {
                for (int i = 0; i < w.length() - 2; i++) {
                    String gram3 = w.substring(i, i + 3);
                    if (freq.getOrDefault(gram3, 0) > 0) {
                        result.add(gram3);
                    }
                }
            }
            result.add(w);
        }
        return result;
    }

    public List<String> cutForSearch(String sentence) {
        return cutForSearch(sentence, true);
    }

    public List<Token> tokenize(String sentence, boolean forSearch, boolean HMM) {
        List<Token> tokens = new ArrayList<Token>();
        int start = 0;
        if (forSearch) {
            for (String w : cut(sentence, false, HMM)) {
                int width = w.length();
                if (w.length() > 2) {
                    for (int i = 0; i < w.length() - 1; i++) {
                        String gram2 = w.substring(i, i + 2);
                        if (freq.getOrDefault(gram2, 0) > 0) {
                            tokens.add(new Token(gram2, start + i, start + i + 2));
                        }
                    }
                }
                if (w.length() > 3) {
                    for (int i = 0; i < w.length() - 2; i++) {
                        String gram3 = w.substring(i, i + 3);
                        if (freq.getOrDefault(gram3, 0) > 0) {
                            tokens.add(new Token(gram3, start + i, start + i + 3));
                        }
                    }
                }
                tokens.add(new Token(w, start, start + width));
                start += width;
            }
        } else {
            for (String w : cut(sentence, false, HMM)) {
                tokens.add(new Token(w, start, start + w.length()));
                start += w.length();
            }
        }

        return tokens;
    }


    private interface CutStrategy {
        List<String> cut(String sentence);
    }

    private class CutAllStrategy implements CutStrategy {

        public List<String> cut(String sentence) {
            List<String> frags = new ArrayList<String>();
            Map<Integer, List<Integer>> dag = Tokenizer.this.getDAG(sentence);
            int old_j = -1;
            for (Integer k : dag.keySet()) {
                List<Integer> L = dag.get(k);
                if (L.size() == 1 && k > old_j) {
                    frags.add(sentence.substring(k, L.get(0) + 1));
                    old_j = L.get(0);
                } else {
                    for (int j : L) {
                        if (j > k) {
                            frags.add(sentence.substring(k, j + 1));
                            old_j = j;
                        }
                    }
                }
            }
            return frags;
        }
    }

    private class CutDAGStrategy implements CutStrategy {

        public List<String> cut(String sentence) {
            List<String> frags = new ArrayList();
            Map<Integer, List<Integer>> dag = Tokenizer.this.getDAG(sentence);
            Map<Integer, Pair<Double, Integer>> route = new HashMap();
            Tokenizer.this.calc(sentence, dag, route);
            int x = 0;
            int N = sentence.length();
            StringBuffer buf = new StringBuffer();
            while (x < N) {
                int y = route.get(x).getSecond() + 1;
                String lWord = sentence.substring(x, y);
                if (y - x == 1) {
                    buf.append(lWord);
                } else {
                    if (buf.length() > 0) {
                        if (buf.length() == 1) {
                            frags.add(buf.toString());
                            buf.setLength(0);
                        } else {
                            if (freq.get(buf.toString()) == null || freq.get(buf.toString()) == 0) {
                                List<String> recognized = finalSeg.cut(buf.toString());
                                frags.addAll(recognized);
                            } else {
                                for (Character elem : buf.toString().toCharArray()) {
                                    frags.add(String.valueOf(elem));
                                }
                            }
                            buf.setLength(0);
                        }
                    }
                    frags.add(lWord);
                }
                x = y;
            }

            if (buf.length() > 0) {
                if (buf.length() == 1) {
                    frags.add(buf.toString());
                } else if (freq.get(buf.toString()) == null || freq.get(buf.toString()) == 0) {
                    List<String> recognized = finalSeg.cut(buf.toString());
                    frags.addAll(recognized);
                } else {
                    for (Character elem : buf.toString().toCharArray()) {
                        frags.add(String.valueOf(elem));
                    }
                }
            }

            return frags;
        }
    }

    private class CutDAGNoHMMStrategy implements CutStrategy {

        public List<String> cut(String sentence) {
            List<String> frags = new ArrayList();
            Map<Integer, List<Integer>> dag = Tokenizer.this.getDAG(sentence);
            Map<Integer, Pair<Double, Integer>> route = new HashMap();
            Tokenizer.this.calc(sentence, dag, route);
            int x = 0;
            int N = sentence.length();
            StringBuffer buf = new StringBuffer();
            while (x < N) {
                int y = route.get(x).getSecond() + 1;
                String lWord = sentence.substring(x, y);
                if (RE_ENG.matcher(lWord).matches() && lWord.length() == 1) {
                    buf.append(lWord);
                    x = y;
                } else {
                    if (buf.length() > 0) {
                        frags.add(buf.toString());
                        buf.setLength(0);
                    }
                    frags.add(lWord);
                    x = y;
                }
            }

            if (buf.length() > 0) {
                frags.add(buf.toString());
            }

            return frags;
        }
    }

    private void loadUserDict(Stream<String> stream) throws IOException {
        try {
            stream.forEach(line -> {
                Matcher matcher = RE_USERDICT.matcher(line.trim());
                if (matcher.find()) {
                    String word = matcher.group(1).trim();
                    String freqStr = matcher.group(2);
                    int freq = 1;
                    if (freqStr != null) {
                        freq = Integer.parseInt(freqStr.trim());
                    } else {
                        double df = 1.;
                        for (String seg : this.cut(word, false, false)) {
                            df *= this.freq.getOrDefault(seg, 1) / total;
                        }
                        freq = Math.max((int) (df * total) + 1, this.freq.getOrDefault(word, 1));
                    }

                    this.freq.put(word, freq);
                    this.total += freq;

                    for (int i = 1; i <= word.length(); i++) {
                        String wfrag = word.substring(0, i);
                        if (!this.freq.containsKey(wfrag)) {
                            this.freq.put(wfrag, 0);
                        }
                    }
                }
            });
        } finally {
            stream.close();
        }
    }


    public void loadUserDict(InputStream in) throws IOException {
        this.loadUserDict(new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8"))).lines());

    }

    private void loadUserDict(Path path) throws IOException {
        this.loadUserDict(Files.lines(path, Charset.forName("UTF-8")));
    }

    public Map<String, Integer> getFreq() {
        return freq;
    }

    public long getTotal() {
        return total;
    }
}
