package com.lunzn.nlp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;

public class SinaUpload {

    private Map<String, String> cookieMap = new HashMap<String, String>();
    private String cookies;

    private SinaUpload(String cookies) {
        super();
        this.cookies = cookies;
        String[] coks = cookies.split(";\\s*");
        for (String cok : coks) {
            String[] ck = cok.split("=");
            cookieMap.put(ck[0], ck[1]);
        }
    }

    private String mimeBoundary() {
        int boundaryLength = 32;
        char[] mimeBoundaryChars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        final StringBuilder mime = new StringBuilder(boundaryLength);
        final Random rand = new Random();
        for (int i = 0; i < boundaryLength; i++) {
            mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.length)]);
        }
        return mime.toString();
    }

    /**
     * 根据专辑名字获取专辑id
     * 
     * @param name
     *            专辑名字
     * @return
     */
    private String getList(String name) {
        try {
            String body = Jsoup.connect("http://photo.weibo.com/albums/get_list?__rnd=" + System.currentTimeMillis())
                    .ignoreContentType(true)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                    .cookies(cookieMap).execute().body();
            return (String) JSONPath.eval(JSON.parseObject(body), "$.data[caption = '" + name + "'][0].album_id");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 上传文件归类到专辑
     * 
     * @param album_id
     *            专辑id
     * @param pid
     *            图片id
     * @return
     */
    private String uploadAlbum(String album_id, String pid) {
        try {
            String body = Jsoup.connect("http://photo.weibo.com/upload/photo").ignoreContentType(true)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "http://photo.weibo.com/upload/index")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                    .cookies(cookieMap).method(Method.POST).data("album_id", album_id).data("pid", pid)
                    .data("upload_type", "1").data("isOrig", "0").execute().body();
            return String.valueOf(JSONPath.eval(JSON.parseObject(body), "$.data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 归类结果显示
     * 
     * @param album_id
     *            专辑id
     * @param feed_id
     *            归类id
     * @return
     */
    private String uploadFeed(String album_id, String feed_id) {
        try {
            return Jsoup.connect("http://photo.weibo.com/upload/up_feed").ignoreContentType(true)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "http://photo.weibo.com/upload/index")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                    .cookies(cookieMap).data("album_id", album_id).data("photo_ids", feed_id).method(Method.POST)
                    .execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片上传
     * 
     * @param path
     *            文件地址
     * @return
     */
    private String uploadFile(String path) {
        if (null == cookies) {
            return "{'error':'the cookies is null'}";
        }
        String uploadUrl = "http://picupload.service.weibo.com/interface/pic_upload.php?s=rdxt&app=miniblog&cb=http://photo.weibo.com/upload/simple_upload/pic/1";
        String end = "\r\n";
        String twoHyphens = "--";
        String name = "pic1";
        String boundary = mimeBoundary();
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
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
            while ((bytes = inputStream.read(bufferOut)) != -1) {
                // 上传文件(一份)
                dos.write(bufferOut, 0, bytes);
                // 计算当前已上传的大小
                count += bytes;
                // 打印上传文件进度(已上传除以总大*100就是进度)
            }

            // 关闭文件流
            inputStream.close();

            // dos.write(bbyte);
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            dos.close();

            // 读取响应数据
            int code = httpURLConnection.getResponseCode();
            // 存放响应结果
            if (code == 302) {
                String sTotalString = httpURLConnection.getHeaderField("Location");
                return URLDecoder
                        .decode(sTotalString.replaceAll("^.*?url=([^&]+).*?$", "$1").replaceAll("^.*?([^=]+)$", "$1"))
                        .replaceAll("^.*?([^=]+)$", "$1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片上传完整流程
     * 
     * @param albumName
     *            专辑名
     * @param filePath
     *            上传图片本地路径
     * @return
     */
    public String upload(String albumName, String filePath) {
        String album_id = getList(albumName);
        if (null == album_id) {
            return null;
        }
        String pid = uploadFile(filePath);
        if (null == pid) {
            return null;
        }
        String feed_id = uploadAlbum(album_id, pid);
        if (null == feed_id) {
            return null;
        }
        String result = uploadFeed(album_id, feed_id);
        return null == result ? null : ("http://wx1.sinaimg.cn/mw690/" + pid + ".jpg");
    }

    public static void main(String[] args) {
        if (null != args && args.length == 3) {
            SinaUpload upload = new SinaUpload(args[0]);
            String img = upload.upload(args[1], args[2]);
            System.out.println(args[2] + "\t" + img);
        } else {
            throw new IllegalArgumentException("Input argument error!");
        }
    }
}
