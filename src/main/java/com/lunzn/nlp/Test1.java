package com.lunzn.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class Test1 {

    public static void main(String[] args) throws IOException {
        String cookies = "SINAGLOBAL=5030659264220.904.1512209668514; SCF=AmGJ8yT-ztVfaZ6qt5iQdlcq9mhvfvX4b2pJVsH6uavnxJ70tyCatbSdIPcJ_4qUqDSEL5k1vcf9o3wSAyl4P98.; SUHB=079SlhgIU87mss; ALF=1515986866; SUB=_2A253MOLiDeRhGedH6lAW8i3FyD6IHXVU2o6qrDV8PUJbkNANLRL7kW1NUN4fSU1s9e5GYqa5K2z38oFRLOg_fwQQ; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9Whrx5gp1MYMYULaWeXN2fy55JpX5oz75NHD95Qp1K2ES0z01KeEWs4DqcjzBNiywrL-MJBt; wvr=6; UOR=www.site-digger.com,widget.weibo.com,www.baidu.com; USRANIME=usrmdinst_13; _s_tentry=-; Apache=3798676532538.25.1513408648336; ULV=1513408648429:3:3:2:3798676532538.25.1513408648336:1513394891279; WBStorage=c1cc464166ad44dc|undefined";
        String[] coks = cookies.split(";\\s*");
        Map<String, String> cookieMap = new HashMap<String, String>();
        for (String cok : coks) {
            String[] ck = cok.split("=");
            cookieMap.put(ck[0], ck[1]);
        }
        File fe = new File("C:/Users/longchensan/Desktop/aa.jpg");

        Response resp = Jsoup.connect("http://picupload.service.weibo.com/interface/pic_upload.php")
                .ignoreContentType(true).ignoreHttpErrors(true).header("Content-Type", "multipart/form-data")
                .header("Cache-Control", "max-age=0").header("Origin", "http://photo.weibo.com")
                .header("Upgrade-Insecure-Requests", "1").cookies(cookieMap)
                .data("pic1", fe.getName(), new FileInputStream(fe)).data("s", "rdxt").data("app", "miniblog")
                .data("cb", "http://photo.weibo.com/upload/simple_upload/pic/1")
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                .referrer("http://photo.weibo.com/upload/index").method(Method.POST).execute();
        System.out.println(resp.statusCode());
        System.out.println(resp.body());
        System.out.println(resp.header("Location"));
    }
}
