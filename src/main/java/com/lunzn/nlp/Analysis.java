package com.lunzn.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.annotation.Execute;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;

public class Analysis {
    private final static String ALLOW_REGEX = "^(?:ns|nr|channel|channelgroup|control|mvname|topname|typename)?";

    private final static TimeNormalizer normalizer;

    static {
        normalizer = new TimeNormalizer();
        normalizer.setPreferFuture(true);
    }

    /**
     * tv场景语音解析（加工后的语义解析）
     * 
     * @param content
     *            待解析语音
     * @return
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object parse(String content) {
        TimeUnit[] times = getTime(content);
        Result re = DicAnalysis.parse(content);
        List<Term> terms = re.getTerms();

        List<TimeUnit> tmp = new ArrayList<TimeUnit>();
        for (TimeUnit time : times) {
            int start = time.getStart(), end = time.getEnd();
            boolean flag = false;
            for (Term term : terms) {
                int from = term.getOffe(), to = from + term.getName().length();
                if (start <= from && end >= to) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                tmp.add(time);
            }
        }
        String word = "";
        int start = 0;
        for (TimeUnit bean : tmp) {
            word += content.substring(start, bean.getStart());
            start = bean.getEnd();
        }
        word += content.substring(start, content.length());
        Map<String, Object> map = getWeights(DicAnalysis.parse(word));
        if (!tmp.isEmpty()) {
            map.put("date", tmp);
        }
        return map;
    }

    /**
     * 内部处理
     * 
     * @param re
     *            结果转义
     * @return String
     */
    private String getNatures(Result re) {
        String natures = "";
        for (Term term : re.getTerms()) {
            String nature = term.getNatureStr();
            natures += "|" + nature;
        }
        return natures.length() > 0 ? natures.substring(1) : "";
    }

    /**
     * 语音处理
     * 
     * @param map
     *            结果
     * @param key
     *            key
     * @param value
     *            value
     */
    @SuppressWarnings("unchecked")
    private void putMap(Map<String, Object> map, String key, String value) {
        Object obj = map.get(key);
        if (null != obj) {
            List<String> re = null;
            if (obj instanceof String) {
                re = new ArrayList<String>();
                re.add((String) obj);
                re.add(value);
                map.put(key, re);
            } else if (obj instanceof List) {
                re = (List<String>) obj;
                re.add(value);
            }
        } else {
            map.put(key, value);
        }
    }

    /**
     * 权重获取
     * 
     * @param re
     *            结果
     * @return map
     */
    private Map<String, Object> getWeights(Result re) {
        String natures = getNatures(re);
        if (StringUtil.isBlank(natures)) {
            return null;
        }
        List<Term> result = DicAnalysis.parse(natures).getTerms();
        int step = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        for (Term term : result) {
            String name = term.getRealName();
            String nature = term.getNatureStr();
            if (name.indexOf("|") > -1 && !"|".equals(name)) {
                String[] nsp = name.split("\\|");
                if (nature.matches(ALLOW_REGEX)) {
                    String value = "";
                    for (int i = step, j = i + nsp.length; i < j; i++) {
                        Term tm = re.get(i);
                        value += tm.getName();
                    }
                    putMap(map, nature, value);
                } else {
                    String[] nat = nature.split(",");
                    for (String string : nat) {
                        String[] nt = string.split(":");
                        if (nt[0].matches("^\\d+$")) {
                            Integer idx = Integer.parseInt(nt[0]);
                            Term tm = re.get(step + idx);
                            String key = nt.length == 2 ? nt[1] : tm.getNatureStr();
                            putMap(map, key, tm.getName());
                        }
                    }
                }
                step += nsp.length;
            } else if (name.indexOf("|") == -1) {
                if (name.matches(ALLOW_REGEX)) {
                    Term tm = re.get(step);
                    putMap(map, tm.getNatureStr(), tm.getName());
                }
                step++;
            }
        }
        return map;
    }

    /**
     * 时间转化器
     * 
     * @param word
     *            待识别的词
     * @return TimeUnit[]
     */
    private TimeUnit[] getTime(String word) {
        normalizer.parse(word);
        return normalizer.getTimeUnit();
    }
}
