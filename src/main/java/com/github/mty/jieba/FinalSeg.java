package com.github.mty.jieba;

import com.github.mty.utils.MtyStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lihongfu on 17/6/8.
 */
public class FinalSeg {
    private static final Double MIN_FLOAT = -3.14e100;
    private Map<Character, Map<Character, Double>> emitP;

    private static final String DEFAULT_EMIT_FILE = "/finalseg_prob_emit.txt";

    private static final Set<Character> STATES = new HashSet();
    private static final Map<Character, Double> PROB_START = new HashMap();
    private static final Map<Character, Map<Character, Double>> PROB_TRANS = new HashMap();
    private static final Map<Character, Character[]> PREV_STATES = new HashMap();

    private static final Pattern RE_HAN = Pattern.compile("([\\u4E00-\\u9FD5]+)");
    private static final Pattern RE_SKIP = Pattern.compile("(\\d+\\.\\d+|[a-zA-Z0-9]+)");

    static {
        STATES.add('B');
        STATES.add('E');
        STATES.add('M');
        STATES.add('S');

        PROB_START.put('B', -0.26268660809250016);
        PROB_START.put('E', -3.14e+100);
        PROB_START.put('M', -3.14e+100);
        PROB_START.put('S', -1.4652633398537678);

        PROB_TRANS.put('B', new HashMap<Character, Double>() {{
            put('E', -0.510825623765990);
            put('M', -0.916290731874155);
        }});
        PROB_TRANS.put('E', new HashMap<Character, Double>() {{
            put('B', -0.5897149736854513);
            put('S', -0.8085250474669937);
        }});
        PROB_TRANS.put('M', new HashMap<Character, Double>() {{
            put('E', -0.33344856811948514);
            put('M', -1.2603623820268226);
        }});
        PROB_TRANS.put('S', new HashMap<Character, Double>() {{
            put('B', -0.7211965654669841);
            put('S', -0.6658631448798212);
        }});

        PREV_STATES.put('B', new Character[]{'E', 'S'});
        PREV_STATES.put('M', new Character[]{'M', 'B'});
        PREV_STATES.put('S', new Character[]{'E', 'S'});
        PREV_STATES.put('E', new Character[]{'B', 'M'});
    }

    public FinalSeg() throws IOException {
        this(DEFAULT_EMIT_FILE);
    }

    public FinalSeg(String emitFileName) throws IOException {
        this.loadEmitP(emitFileName);
    }


    protected void loadEmitP(String emitPFileName) throws IOException {
        emitP = new HashMap();
        for (Character s : STATES) {
            emitP.put(s, new HashMap(10000));
        }

        Pattern wordPattern = Pattern.compile("'\\\\u(.*?)': (.*?),");

        BufferedReader reader = new BufferedReader(new InputStreamReader(Tokenizer.class.getResourceAsStream(emitPFileName)));
        String line;
        Character currentType = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            if (line.length() == 1 && STATES.contains(line.charAt(0))) {
                currentType = line.charAt(0);
                continue;
            } else {
                if (currentType == null) {
                    throw new IllegalStateException("emit probability data must be followed the BEMS character");
                }
            }

            Map<Character, Double> stateP = emitP.get(currentType);
            Matcher matcher = wordPattern.matcher(line);
            if (matcher.find()) {
                String word = matcher.group(1);
                Double p = Double.valueOf(matcher.group(2));
                stateP.put((char) Integer.parseInt(word, 16), p);
            }
        }
    }

    private String viterbi(String obs, Set<Character> states, Map<Character, Double> startP,
                           Map<Character, Map<Character, Double>> transP,
                           Map<Character, Map<Character, Double>> emitP) {
        List<Map<Character, Double>> V = new ArrayList(obs.length());
        Map<Character, Double> first = new HashMap();
        V.add(first);
        Map<Character, String> path = new HashMap();
        for (Character y : states) {
            first.put(y, startP.get(y) + emitP.get(y).getOrDefault(obs.charAt(0), MIN_FLOAT));
            path.put(y, String.valueOf(y));
        }

        for (int i = 1; i < obs.length(); i++) {
            Map<Character, Double> v = new HashMap();
            V.add(v);

            Map<Character, String> newPath = new HashMap();
            for (Character y : states) {
                double emP = emitP.get(y).getOrDefault(obs.charAt(i), MIN_FLOAT);
                double maxProb = Double.NEGATIVE_INFINITY;
                Character bestY = null;
                for (Character y0 : PREV_STATES.get(y)) {
                    double emP0 = V.get(i - 1).get(y0) + transP.get(y0).getOrDefault(y, MIN_FLOAT) + emP;
                    if (emP0 > maxProb) {
                        maxProb = emP0;
                        bestY = y0;
                    }
                }
                V.get(i).put(y, maxProb);
                newPath.put(y, path.get(bestY) + String.valueOf(y));
            }
            path = newPath;
        }

        Double maxD = null;
        Character finalState = null;
        for (Character y : new Character[]{'E', 'S'}) {
            Double d = V.get(obs.length() - 1).get(y);
            if (maxD == null || d > maxD) {
                maxD = d;
                finalState = y;
            }
        }

        return path.get(finalState);
    }

    private List<String> innerCut(String sentence) {
        List<String> result = new ArrayList();
        String posList = viterbi(sentence, STATES, PROB_START, PROB_TRANS, emitP);
        int begin = 0, nextI = 0;
        for (int i = 0; i < sentence.length(); i++) {
            char ch = sentence.charAt(i);
            char pos = posList.charAt(i);
            if (pos == 'B') {
                begin = i;
            } else if (pos == 'E') {
                result.add(sentence.substring(begin, i + 1));
                nextI = i + 1;
            } else if (pos == 'S') {
                result.add(String.valueOf(ch));
                nextI = i + 1;
            }
        }

        if (nextI < sentence.length()) {
            result.add(sentence.substring(nextI));
        }
        return result;
    }


    public List<String> cut(String sentence) {
        List<String> blocks = MtyStringUtils.splitAndReturnDelimiters(RE_HAN, sentence);
        List<String> result = new ArrayList<String>();
        for (String blk : blocks) {
            if (RE_HAN.matcher(blk).matches()) {
                result.addAll(innerCut(blk));
            } else {
                List<String> tmp = MtyStringUtils.splitAndReturnDelimiters(RE_SKIP, blk);
                for (String x : tmp) {
                    if (!x.isEmpty()) {
                        result.add(x);
                    }
                }
            }
        }
        return result;
    }


    public Map<Character, Map<Character, Double>> getEmitP() {
        return emitP;
    }


}
