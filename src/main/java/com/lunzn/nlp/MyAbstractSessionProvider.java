package com.lunzn.nlp;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.mvc.impl.session.AbstractSessionProvider;

public class MyAbstractSessionProvider extends AbstractSessionProvider
{
    
    @Override
    public HttpSession createSession(HttpServletRequest req, HttpServletResponse resp, ServletContext servletContext)
    {
        return req.getSession();
    }
    
    @Override
    public HttpSession getExistSession(HttpServletRequest req, HttpServletResponse resp, ServletContext servletContext)
    {
        return null;
    }
    
}
