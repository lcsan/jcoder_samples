package com.lunzn.nlp;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.annotation.Execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;

public class SegApi {

    /**
     * 取词识别
     */
    private final static String ALLOW_REGEX = "^(?:ns|nr|channel|channelgroup|control|mvname|topname|typename)?";
    /**
     * 取词识别
     */
    private final static String DEF_PATH = "./j_home/resource/dic/all";
    /**
     * 时间处理器
     */
    private final static TimeNormalizer normalizer;

    static {
        normalizer = new TimeNormalizer();
        normalizer.setPreferFuture(true);
    }

    /**
     * 基础分词，最小粒度分词
     * 
     * @param content
     *            待分词语句
     * @return Object
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object baseSeg(String content) {
        return BaseAnalysis.parse(content).getTerms();
    }

    /**
     * 自定义分词优先
     * 
     * @param content
     *            待分词语句
     * @return Object
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object dicSeg(String content) {
        return DicAnalysis.parse(content).getTerms();
    }

    /**
     * 自然语言分词
     * 
     * @param content
     *            待分词语句
     * @return Object
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object nlpSeg(String content) {
        return NlpAnalysis.parse(content).getTerms();
    }

    /**
     * 索引分词
     * 
     * @param content
     *            待分词语句
     * @return Object
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object indexSeg(String content) {
        return IndexAnalysis.parse(content).getTerms();
    }

    /**
     * 标准分词
     * 
     * @param content
     *            待分词语句
     * @return Object
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object toSeg(String content) {
        return ToAnalysis.parse(content).getTerms();
    }

    /**
     * 添加词典（词典增量累加）
     * 
     * @param key
     *            词典标识(不填为默认词典)
     * @param keyword
     *            词
     * @param nature
     *            词性
     * @param freq
     *            词频
     * @return Object
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object addDic(String key, String keyword, String nature, int freq) {
        if (null == key) {
            key = DicLibrary.DEFAULT;
        }
        if (0 == freq) {
            freq = DicLibrary.DEFAULT_FREQ;
        }
        if (StringUtil.isNotBlank(keyword) && StringUtil.isNotBlank(nature)) {
            DicLibrary.insert(key, keyword, nature, freq);
            return "{'msg':'success'}";
        } else {
            return "{'msg':'fail,The argument `keyword` and `nature` must not null!'}";
        }

    }

    /**
     * json数组格式批量添加词典（词典增量累加）
     * 
     * @param json
     *            词典json列表
     * @return Object
     * @see addDic
     */
    @Execute
    public Object addDic4Json(JSONArray json) {
        if (null == json) {
            return "{'msg':'fail,The argument `json` must not null!'}";
        } else {
            for (int i = 0, j = json.size(); i < j; i++) {
                Object obj = json.get(i);
                if (obj instanceof JSONObject) {
                    JSONObject js = (JSONObject) obj;
                    DicLibrary.insert(DicLibrary.DEFAULT, js.getString("keyword"), js.getString("nature"),
                            js.getIntValue("freq"));
                } else if (obj instanceof String) {
                    String line = (String) obj;
                    String[] ag = line.split("\t");
                    DicLibrary.insert(DicLibrary.DEFAULT, ag[0], ag[1], Integer.parseInt(ag[2]));
                }
            }
            return "{'msg':'success'}";
        }
    }

    /**
     * 批量添加词典（词典增量累加） Demo example: 小 a 57969<br/>
     * 高 a 57483 长 a 40281 重要 a 37557 老 a 33423
     * 
     * @param dic
     *            DicLibrary : 用户自定义词典,格式:词语\t词性\t词频
     * @return Object
     * @see https://github.com/NLPchina/ansj_seg/wiki/%E7%94%A8%E6%88%B7%E8%87%
     *      AA%E5%AE%9A%E4%B9%89%E8%AF%8D%E5%85%B8
     */
    @Execute
    public Object addDic4Str(String dic) {
        if (StringUtil.isBlank(dic)) {
            return "{'msg':'fail,The argument `dic` must not null!'}";
        } else {
            String[] ary = dic.split("\r\n|\r");
            for (String line : ary) {
                String[] ag = line.split("\t");
                DicLibrary.insert(DicLibrary.DEFAULT, ag[0], ag[1], Integer.parseInt(ag[2]));
            }
            return "{'msg':'success'}";
        }
    }

    /**
     * json数组格式批量添加词典（替换词典）
     * 
     * @param json
     *            词典json列表
     * @return Object
     * @see addDic
     */
    @Execute
    public Object putDic4Json(JSONArray json) {
        if (null == json) {
            return "{'msg':'fail,The argument `json` must not null!'}";
        } else {
            Forest forest = new Forest();
            for (int i = 0, j = json.size(); i < j; i++) {
                Object obj = json.get(i);
                if (obj instanceof JSONObject) {
                    JSONObject js = (JSONObject) obj;
                    Library.insertWord(forest,
                            js.getString("keyword") + "\t" + js.getString("nature") + "\t" + js.getIntValue("freq"));
                } else if (obj instanceof String) {
                    String line = (String) obj;
                    Library.insertWord(forest, line);
                }
            }
            DicLibrary.put(DicLibrary.DEFAULT, DicLibrary.DEFAULT, forest);
            return "{'msg':'success'}";
        }
    }

    /**
     * 批量添加词典（替换词典） Demo example: 小 a 57969<br/>
     * 高 a 57483 长 a 40281 重要 a 37557 老 a 33423
     * 
     * @param dic
     *            DicLibrary : 用户自定义词典,格式:词语\t词性\t词频
     * @return Object
     * @see https://github.com/NLPchina/ansj_seg/wiki/%E7%94%A8%E6%88%B7%E8%87%
     *      AA%E5%AE%9A%E4%B9%89%E8%AF%8D%E5%85%B8
     */
    @Execute
    public Object putDic4Str(String dic) {
        if (StringUtil.isBlank(dic)) {
            return "{'msg':'fail,The argument dic must not null!'}";
        } else {
            String[] ary = dic.split("\r\n|\r");
            Forest forest = new Forest();
            for (String line : ary) {
                Library.insertWord(forest, line);
            }
            DicLibrary.put(DicLibrary.DEFAULT, DicLibrary.DEFAULT, forest);
            return "{'msg':'success'}";
        }
    }

    /**
     * 从source目录加载（替换词典）
     * 
     * @param path
     *            resources路径（default：./jcoder_home/resource/dic/all）
     * @return Object
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object loadDic4UploadFile(String path) throws UnsupportedEncodingException, FileNotFoundException {
        path = null == path ? DEF_PATH : path;
        DicLibrary.put(DicLibrary.DEFAULT, path);
        return IOUtil.readFile2List(path, IOUtil.UTF8);
    }

    /**
     * tv场景语音解析（加工后的语义解析）
     * 
     * @param content
     *            待解析语音
     * @return Object
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

    public static void main(String[] args) {
        SegApi api = new SegApi();
        System.out.println(api.parse("刘德华和梁朝伟的电影"));

    }
}
