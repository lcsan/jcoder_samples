package com.lunzn.nlp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Execute;

public class SinaImgUpload
{
    private String cookies;
    
    private String mimeBoundary()
    {
        int boundaryLength = 32;
        char[] mimeBoundaryChars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        final StringBuilder mime = new StringBuilder(boundaryLength);
        final Random rand = new Random();
        for (int i = 0; i < boundaryLength; i++)
        {
            mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.length)]);
        }
        return mime.toString();
    }
    
    /**
     * 新浪微博登录后的cookies
     * 
     * @param cookies
     * @return
     * @see [类、类#方法、类#成员]
     */
    @Execute
    public String cookies(String cookies)
    {
        this.cookies = cookies;
        return cookies;
    }
    
    /**
     * 图片上传
     * 
     * @param path  图片路径
     * @return
     * @see [类、类#方法、类#成员]
     */
    @Execute
    @DefaultExecute
    public String uploadFile(String path)
    {
        if (null == cookies)
        {
            return "{'error':'the cookies is null'}";
        }
        String uploadUrl =
            "http://picupload.service.weibo.com/interface/pic_upload.php?s=rdxt&app=miniblog&cb=http://photo.weibo.com/upload/simple_upload/pic/1";
        String end = "\r\n";
        String twoHyphens = "--";
        String name = "pic1";
        String boundary = mimeBoundary();
        try
        {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpURLConnection.setRequestProperty("Referer", "http://photo.weibo.com/upload/index");
            httpURLConnection.setRequestProperty("Origin", "http://photo.weibo.com");
            httpURLConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            httpURLConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            httpURLConnection.setRequestProperty("Cookie", cookies);
            httpURLConnection.setRequestProperty("Cache-Control", "max-age=0");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            httpURLConnection.setFollowRedirects(true);
            
            File fe = new File(path);
            
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes(
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fe.getName() + "\"" + end);
            dos.writeBytes("Content-Type: " + URLConnection.getFileNameMap().getContentTypeFor(path) + end + end);
            
            // 获取文件流
            FileInputStream fileInputStream = new FileInputStream(fe);
            DataInputStream inputStream = new DataInputStream(fileInputStream);
            
            // 每次上传文件的大小(文件会被拆成几份上传)
            int bytes = 0;
            // 计算上传进度
            float count = 0;
            // 获取文件总大小
            int fileSize = fileInputStream.available();
            // 每次上传的大小
            byte[] bufferOut = new byte[1024];
            // 上传文件
            while ((bytes = inputStream.read(bufferOut)) != -1)
            {
                // 上传文件(一份)
                dos.write(bufferOut, 0, bytes);
                // 计算当前已上传的大小
                count += bytes;
                // 打印上传文件进度(已上传除以总大*100就是进度)
            }
            
            // 关闭文件流
            inputStream.close();
            
            //            dos.write(bbyte);
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            dos.close();
            
            //读取响应数据
            int code = httpURLConnection.getResponseCode();
            String sCurrentLine = "";
            //存放响应结果
            String sTotalString = "";
            if (code == 200)
            {
                java.io.InputStream is = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((sCurrentLine = reader.readLine()) != null)
                    if (sCurrentLine.length() > 0)
                        sTotalString = sTotalString + sCurrentLine.trim();
            }
            else
            {
                sTotalString = httpURLConnection.getHeaderField("Location");
                sTotalString =
                    "http://wx1.sinaimg.cn/mw690/" + sTotalString.replaceAll("^.*?pid%3D([^&]+).*?$", "$1") + ".jpg";
            }
            return sTotalString;
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
    //    
    //    public static void main(String[] args)
    //        throws IOException
    //    {
    //        String str = uploadFile("C:/Users/longchensan/Desktop/pic-util/2.jpg",
    //            "SINAGLOBAL=2324540635491.3745.1502244529397; UOR=bbs.anjian.com,widget.weibo.com,bbs.anjian.com; _s_tentry=-; Apache=716856762342.4727.1508210376986; ULV=1508210377046:5:3:1:716856762342.4727.1508210376986:1507889790343; login_sid_t=a1a0cbc2a0b7d60cd79084334f777143; SSOLoginState=1508216883; SCF=Ao6w0zOvQs8YzmIgl09bZ79pEFcG5e8jlRKa-fwW_NoDIuSR8pSrloQ11D2YZLvQaLiWnA0F9XJCb47sM-qmRi0.; SUB=_2A2504eBjDeRhGedH6lAW8i3FyD6IHXVXl1arrDV8PUNbmtANLUrakW992ErTAwsdDiNWcOTNYoFd1uFW2Q..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9Whrx5gp1MYMYULaWeXN2fy55JpX5K2hUgL.Fo24eKzNeoe4e0z2dJLoI7yhMJ8AwHxE95tt; SUHB=0h14gkazmx5rpZ; ALF=1539752883; un=qq444139212@sina.cn");
    //        System.out.println(str);
    //        //        Response res = Jsoup.connect("http://weibo.com/login.php?url=http://photo.weibo.com/upload/index")
    //        //            .userAgent(
    //        //                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
    //        //            .execute();
    //        //        System.out.println(res.cookies());
    //        //        System.out.println(res.url());
    //    }
    
}
