package com.lunzn.nlp;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.ansj.library.DicLibrary;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.annotation.Execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class EditDic
{
    /**
     * 添加词典（词典增量累加）
     * 
     * @param key   词典标识(不填为默认词典)
     * @param keyword   词
     * @param nature    词性
     * @param freq  词频
     * @return
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object addDic(String key, String keyword, String nature, int freq)
    {
        if (null == key)
        {
            key = DicLibrary.DEFAULT;
        }
        if (0 == freq)
        {
            freq = DicLibrary.DEFAULT_FREQ;
        }
        if (StringUtil.isNotBlank(keyword) && StringUtil.isNotBlank(nature))
        {
            DicLibrary.insert(key, keyword, nature, freq);
            return "{'msg':'success'}";
        }
        else
        {
            return "{'msg':'fail,The argument `keyword` and `nature` must not null!'}";
        }
        
    }
    
    /**
     * json数组格式批量添加词典（词典增量累加）
     * 
     * @param json  词典json列表
     * @return
     * @see addDic
     */
    @Execute
    public Object addDic4Json(JSONArray json)
    {
        if (null == json)
        {
            return "{'msg':'fail,The argument `json` must not null!'}";
        }
        else
        {
            for (int i = 0, j = json.size(); i < j; i++)
            {
                Object obj = json.get(i);
                if (obj instanceof JSONObject)
                {
                    JSONObject js = (JSONObject)obj;
                    DicLibrary.insert(DicLibrary.DEFAULT,
                        js.getString("keyword"),
                        js.getString("nature"),
                        js.getIntValue("freq"));
                }
                else if (obj instanceof String)
                {
                    String line = (String)obj;
                    String[] ag = line.split("\t");
                    DicLibrary.insert(DicLibrary.DEFAULT, ag[0], ag[1], Integer.parseInt(ag[2]));
                }
            }
            return "{'msg':'success'}";
        }
    }
    
    /**
     * 批量添加词典（词典增量累加）
     * Demo example:
     * 小   a   57969<br/>
     * 高   a   57483
     * 长   a   40281
     * 重要  a   37557
     * 老   a   33423
     * 
     * @param dic   DicLibrary : 用户自定义词典,格式:词语\t词性\t词频
     * @return
     * @see https://github.com/NLPchina/ansj_seg/wiki/%E7%94%A8%E6%88%B7%E8%87%AA%E5%AE%9A%E4%B9%89%E8%AF%8D%E5%85%B8
     */
    @Execute
    public Object addDic4Str(String dic)
    {
        if (StringUtil.isBlank(dic))
        {
            return "{'msg':'fail,The argument `dic` must not null!'}";
        }
        else
        {
            String[] ary = dic.split("\r\n|\r");
            for (String line : ary)
            {
                String[] ag = line.split("\t");
                DicLibrary.insert(DicLibrary.DEFAULT, ag[0], ag[1], Integer.parseInt(ag[2]));
            }
            return "{'msg':'success'}";
        }
    }
    
    /**
     * json数组格式批量添加词典（替换词典）
     * 
     * @param json  词典json列表
     * @return
     * @see addDic
     */
    @Execute
    public Object putDic4Json(JSONArray json)
    {
        if (null == json)
        {
            return "{'msg':'fail,The argument `json` must not null!'}";
        }
        else
        {
            Forest forest = new Forest();
            for (int i = 0, j = json.size(); i < j; i++)
            {
                Object obj = json.get(i);
                if (obj instanceof JSONObject)
                {
                    JSONObject js = (JSONObject)obj;
                    Library.insertWord(forest,
                        js.getString("keyword") + "\t" + js.getString("nature") + "\t" + js.getIntValue("freq"));
                }
                else if (obj instanceof String)
                {
                    String line = (String)obj;
                    Library.insertWord(forest, line);
                }
            }
            DicLibrary.put(DicLibrary.DEFAULT, DicLibrary.DEFAULT, forest);
            return "{'msg':'success'}";
        }
    }
    
    /**
     * 批量添加词典（替换词典）
     * Demo example:
     * 小   a   57969<br/>
     * 高   a   57483
     * 长   a   40281
     * 重要  a   37557
     * 老   a   33423
     * 
     * @param dic   DicLibrary : 用户自定义词典,格式:词语\t词性\t词频
     * @return
     * @see https://github.com/NLPchina/ansj_seg/wiki/%E7%94%A8%E6%88%B7%E8%87%AA%E5%AE%9A%E4%B9%89%E8%AF%8D%E5%85%B8
     */
    @Execute
    public Object putDic4Str(String dic)
    {
        if (StringUtil.isBlank(dic))
        {
            return "{'msg':'fail,The argument dic must not null!'}";
        }
        else
        {
            String[] ary = dic.split("\r\n|\r");
            Forest forest = new Forest();
            for (String line : ary)
            {
                Library.insertWord(forest, line);
            }
            DicLibrary.put(DicLibrary.DEFAULT, DicLibrary.DEFAULT, forest);
            return "{'msg':'success'}";
        }
    }
    
    /**
     * 从source目录加载（替换词典）
     * 
     * @param path  resources路径（default：./jcoder_home/resource/dic/all）
     * @return
     * @throws FileNotFoundException 
     * @throws UnsupportedEncodingException 
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public Object loadDic4UploadFile(String path)
        throws UnsupportedEncodingException, FileNotFoundException
    {
        path = null == path ? "./jcoder_home/resource/dic/all" : path;
        DicLibrary.put(DicLibrary.DEFAULT, path);
        return IOUtil.readFile2List(path, IOUtil.UTF8);
    }
}
