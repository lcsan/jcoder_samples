package com.lunzn.nlp;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.ansj.library.DicLibrary;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.run.annotation.Execute;

public class TestLoadSource
{
    @Execute
    public Object load()
        throws UnsupportedEncodingException, FileNotFoundException
    {
        DicLibrary.put(DicLibrary.DEFAULT, "./jcoder_home/resource/dic/all");
        return IOUtil.readFile2List("./jcoder_home/resource/dic/all", IOUtil.UTF8);
        
    }
}
