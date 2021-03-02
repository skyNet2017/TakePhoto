package com.hss01248.media.mymediastore.smb;

import android.util.Log;

import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.SafUtil;

import org.cybergarage.http.HTTPHeader;
import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPRequestListener;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

public class FileServer extends Thread implements HTTPRequestListener {

        public static final String CONTENT_EXPORT_URI = "/smb";
        private HTTPServerList httpServerList = new HTTPServerList();
        // 默认的共享端口
        public static int HTTPPort = 2222;
        // 绑定的ip
        private String bindIP = "127.0.0.1";

        public String getBindIP() {
            return bindIP;
        }

        public void setBindIP(String bindIP) {
            this.bindIP = bindIP;
        }

        public HTTPServerList getHttpServerList() {
            return httpServerList;
        }

        public void setHttpServerList(HTTPServerList httpServerList) {
            this.httpServerList = httpServerList;
        }

        public int getHTTPPort() {
            return HTTPPort;
        }

        public void setHTTPPort(int hTTPPort) {
            HTTPPort = hTTPPort;
        }

        @Override
        public void run() {
            super.run();

            /**************************************************
             *
             * 创建http服务器，接收共享请求
             *
             *************************************************/
            // 重试次数
            int retryCnt = 0;
            // 获取端口 2222
            int bindPort = getHTTPPort();

            HTTPServerList hsl = getHttpServerList();
            while (hsl.open(bindPort) == false) {
                retryCnt++;
                // 重试次数大于服务器重试次数时返回
                if (100 < retryCnt) {
                    return;
                }
                setHTTPPort(bindPort + 1);
                bindPort = getHTTPPort();
            }
            // 给集合中的每个HTTPServer对象添加HTTPRequestListener对象
            hsl.addRequestListener(this);
            // 调用集合中所有HTTPServer的start方法
            hsl.start();

            //SmbToHttp.ip = "127.0.0.1";
            //http:///fe80::480:b0ff:fed5:11d9%dummy0:2222/smb/D/手机图片/20190206早上/IMG_20190206_080032.jpg
            SmbToHttp.ip = hsl.getHTTPServer(0).getBindAddress();
            Log.e("smb","ip:"+SmbToHttp.ip );
            if(SmbToHttp.ip.contains("%dummy0")){
                SmbToHttp.ip =  SmbToHttp.ip.replace("%dummy0","");
            }
            if(SmbToHttp.ip.contains("::")){
                SmbToHttp.ip =  SmbToHttp.ip.replace("::",":");
            }
            if(SmbToHttp.ip.startsWith("/")){
                SmbToHttp.ip =  SmbToHttp.ip.substring(1);
            }
            Log.e("smb","ip2:"+SmbToHttp.ip );

            Log.e("smb","ipv4:"+IPUtils.getIpAddress(SafUtil.context) );
            SmbToHttp.ip = IPUtils.getIpAddress(SafUtil.context);
            //
            SmbToHttp.port = hsl.getHTTPServer(0).getBindPort();

        }

        @Override
        public void httpRequestRecieved(HTTPRequest request) {

            String uri = request.getURI();
            Log.w("smb",Thread.currentThread().getName()+",httpRequestRecieved uri*****->" + uri);

            Log.w("smb","httpRequestRecieved headers*****->" + request.getHeader());
            //User-Agent: Lavf/58.12.100
           // Accept: */*
   // Range: bytes=74130294-
            //Range: bytes=0-1024
            //响应range: https://blog.csdn.net/qq_32099833/article/details/109703883
  //  Connection: close
   // Host: 192.168.3.24:2223
   // Icy-MetaData: 1

            //   /smb/D/%E6%89%8B%E6%9C%BA%E5%9B%BE%E7%89%87/20190206%E6%97%A9%E4%B8%8A/IMG_20190206_080333.jpg
            if (uri.startsWith(CONTENT_EXPORT_URI) == false) {
                request.returnBadRequest();
                return;
            }
            try {
                uri = URLDecoder.decode(uri, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            Log.w("smb","uri=====" + uri);
            if (uri.length() < 6) {
                return;
            }
            // 截取文件的信息
            String filePath = "smb://192.168.3.8" + uri.substring("/smb".length());

            Log.w("smb","smb path =" + filePath);
            // 判断uri中是否包含参数
            int indexOf = filePath.indexOf("&");

            if (indexOf != -1) {
                filePath = filePath.substring(0, indexOf);
            }

            try {
                FileApiForSmb file = SmbToHttp.getFile(filePath);
                // 获取文件的大小
                long contentLen = file.length();
                // 获取文件类型
                String contentType = SafFileFinder22.getTypeForName(file.getName());
                Log.w("smb","contentType=====" + contentType);
                // 获取文文件流


                HTTPResponse response = new HTTPResponse();
                response.setContentType(contentType);
                response.setStatusCode(HTTPStatus.OK);

                //处理range:
                // Range: bytes=74130294-
                //Range: bytes=0-1024
                //响应range: https://blog.csdn.net/qq_32099833/article/details/109703883
                //httpRes.setContentRange();
                if(request.hasHeader("Range")){
                    String valueStr = request.getHeader("Range").getValue();
                   String[] strings = valueStr.split("=");
                   String range = strings[1];
                   long start = Long.parseLong(range.substring(0,range.indexOf("-")));
                   long end = 0;
                   if(!range.endsWith("-")){
                       end = Long.parseLong(range.substring(range.indexOf("-")+1));
                   }

                   int bufferLen = 3*1024 * 1024;
                    byte[] bytes = new byte[ bufferLen];
                    int len = file.getSmbFile().read(bytes,start,0,bufferLen);
                    response.setContentLength(len);
                    //todo 边界值

                    response.setHeader("Content-Range", "bytes "+range+"-"+(range+len));
                    response.setContent(bytes,true);

                    //response.setContentLength(len);
                    //		//设置此次相应返回的数据范围
                    //		response.setHeader("Content-Range", "bytes "+range+"-"+(fileLength-1)+"/"+fileLength);
                    //		// 将这1MB的视频流响应给客户端
                    //		outputStream.write(bytes, 0, len);

                }else {
                    InputStream contentIn = file.getInputStream();

                    if (contentLen <= 0 || contentType.length() <= 0
                            || contentIn == null) {
                        Log.e("smb","contentLen <= 0 || contentType.length() <= 0:"+uri);
                        request.returnBadRequest();
                        return;
                    }
                    response.setContentLength(contentLen);
                    response.setContentInputStream(contentIn);
                    response.setContentRange(0,contentLen,contentLen);
                    try {
                        contentIn.close();
                    }catch (Throwable throwable){
                        throwable.printStackTrace();
                    }

                }
                request.post(response);


           }  catch (Throwable e) {
                 request.returnBadRequest();
                e.printStackTrace();
                return;
            }
        }
}
