package com.lunzn.nlp;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.annotation.Cache;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.view.HttpServerResponse;

/**
 * this is a api example
 * 
 * @author ansj
 *
 */
@Single(false) // default is true
public class ApiExampleAction
{
    
    @Inject
    private Logger log;
    
    @Inject
    private Dao dao;
    
    @Execute // publish this function to api !
             // use url is
             // http://host:port/[className]/methodName?field=value
             // http//localhost:8080/ApiExampleAction/method?name=heloo&hello_word=hi
    public Object function(HttpServletRequest req, HttpServerResponse rep, String name,
        @Param("hello_word") Integer helloWord)
    {
        return null;
    }
    
    @DefaultExecute // publish this function to api !
    //http://localhost:8080/ApiExampleAction?user.name=aaa&user.passowrd=bbb
    //http://localhost:8080/ApiExampleAction/function2?user.name=aaa&user.passowrd=bbb
    //more about http://www.nutzam.com/core/mvc/http_adaptor.html
    @Execute(methods = {"get", "post"}, restful = true, rpc = true) //methods default all , restufl,rpc default true 
    @Cache(time = 10, size = 1000, block = false) // time SECONDS , block if false use asynchronous
    public Object function2(@Param("..") User user)
    {
        
        return null;
    }
    
}