package com.hss01248.media.mymediastore;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.hss01248.media.mymediastore.bean.BaseMediaInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileTypeUtil {


    /**
     * 图片: https://zh.wikipedia.org/wiki/%E5%9B%BE%E5%BD%A2%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F%E6%AF%94%E8%BE%83
     * 视频后缀
     * 最常见：.mpg .mpeg .avi .rm .rmvb .mov .wmv .asf .dat
     * 不常见的：.asx .wvx .mpe .mpa
     * 音频后缀
     * 常见的：.mp3 .wma .rm .wav .mid
     * .ape .flac
     * <p>
     * 常见 MIME 类型列表
     * https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     * <p>
     * 作者：耐住寂寞守住繁华_5b9a
     * 链接：https://www.jianshu.com/p/8962f2a5186e
     * 来源：简书
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     *
     * @param name
     * @return
     */
    public static int guessTypeByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return BaseMediaInfo.TYPE_UNKNOWN;
        }
        if(name.endsWith(".txt")){
            return BaseMediaInfo.TYPE_DOC_TXT;
        }
        if(name.endsWith(".pdf")){
            return BaseMediaInfo.TYPE_DOC_PDF;
        }
        String mime = getTypeForName(name);
        if (mime.startsWith("image/")) {
            return BaseMediaInfo.TYPE_IMAGE;
        } else if (mime.startsWith("video/")) {
            return BaseMediaInfo.TYPE_VIDEO;
        } else if (mime.startsWith("audio/")) {
            return BaseMediaInfo.TYPE_AUDIO;
        }else if (mime.contains("msword")) {
            return BaseMediaInfo.TYPE_DOC_WORD;
        }else if (mime.contains("excel")) {
            return BaseMediaInfo.TYPE_AUDIO;
        }else if (mime.contains("powerpoint")) {
            return BaseMediaInfo.TYPE_DOC_PPT;
        }
        /*if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif")
                || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")
                || name.endsWith(".svg")  || name.endsWith(".bmp")) {
            return BaseMediaInfo.TYPE_IMAGE;
        }else if(name.endsWith(".mp4") || name.endsWith(".MP4") || name.endsWith(".mkv") || name.endsWith(".avi")
                || name.endsWith(".mpeg") || name.endsWith(".wmv") || name.endsWith(".mpg") || name.endsWith(".rmvb")
                || name.endsWith(".mov") || name.endsWith(".flv")){
            return BaseMediaInfo.TYPE_VIDEO;
        }else if(name.endsWith(".m4a") ||name.endsWith(".mp3") || name.endsWith(".MP3") || name.endsWith(".aac") || name.endsWith(".wav")
                || name.endsWith(".wma") || name.endsWith(".mid") || name.endsWith(".ape") || name.endsWith(".flac")){
            return BaseMediaInfo.TYPE_AUDIO;
        }*/
        return BaseMediaInfo.TYPE_UNKNOWN;
    }

    static Map<Integer, List<String>> mimeMap = new HashMap<>();
    static {
        mimeMap.put(BaseMediaInfo.TYPE_DOC_PPT,getTypeExtension(BaseMediaInfo.TYPE_DOC_PPT));
        mimeMap.put(BaseMediaInfo.TYPE_AUDIO,getTypeExtension(BaseMediaInfo.TYPE_AUDIO));
        mimeMap.put(BaseMediaInfo.TYPE_DOC_EXCEL,getTypeExtension(BaseMediaInfo.TYPE_DOC_EXCEL));
        mimeMap.put(BaseMediaInfo.TYPE_DOC_PDF,getTypeExtension(BaseMediaInfo.TYPE_DOC_PDF));
        mimeMap.put(BaseMediaInfo.TYPE_DOC_TXT,getTypeExtension(BaseMediaInfo.TYPE_DOC_TXT));
        mimeMap.put(BaseMediaInfo.TYPE_DOC_WORD,getTypeExtension(BaseMediaInfo.TYPE_DOC_WORD));
        mimeMap.put(BaseMediaInfo.TYPE_VIDEO,getTypeExtension(BaseMediaInfo.TYPE_VIDEO));
        mimeMap.put(BaseMediaInfo.TYPE_IMAGE,getTypeExtension(BaseMediaInfo.TYPE_IMAGE));
    }

    public static List<String> getTypeExtension(int type){
        List<String> ext = new ArrayList<>();
        switch (type){
            case BaseMediaInfo.TYPE_DOC_PPT:
                ext.add( "ppt");
                ext.add("pptx");
                break;
            case BaseMediaInfo.TYPE_AUDIO:
                ext.add( "mp3");
                ext.add("aac");
                ext.add( "wav");
                ext.add("m4a");
                ext.add("flac");
                ext.add("wma");
                break;
            case BaseMediaInfo.TYPE_DOC_EXCEL:
                ext.add( "xls");
                ext.add("csv");
                ext.add( "xlsx");
                break;
            case BaseMediaInfo.TYPE_DOC_PDF:
                ext.add( "pdf");
                break;
            case BaseMediaInfo.TYPE_DOC_TXT:
                ext.add( "txt");
                break;
            case BaseMediaInfo.TYPE_VIDEO:
                ext.add( "mp4");
                ext.add("mkv");
                ext.add( "avi");
                ext.add("mpeg");
                ext.add( "wmv");
                ext.add("rmvb");
                ext.add( "mov");
                ext.add("flv");
                break;
            case BaseMediaInfo.TYPE_DOC_WORD:
                ext.add( "doc");
                ext.add("docx");
                break;
            case BaseMediaInfo.TYPE_IMAGE:
                ext.add( "jpg");
                ext.add("jpeg");
                ext.add( "webp");
                ext.add("avif");
                ext.add( "heif");
                break;
        }
        return ext;
    }


    public static int getTypeByFileName(String name){
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            Iterator<Map.Entry<Integer, List<String>>> iterator = mimeMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<Integer, List<String>> next = iterator.next();
                List<String> value = next.getValue();
                for (String s : value) {
                    if(extension.equals(s)){
                        return next.getKey();
                    }
                }
            }

        }
        return BaseMediaInfo.TYPE_UNKNOWN;
    }

    /*public static String getTypeDesc(int type){
        switch (type){
            case BaseMediaInfo.TYPE_DOC_PPT:
                return "ppt";
            case BaseMediaInfo.TYPE_AUDIO:
                return "audio";
            case BaseMediaInfo.TYPE_DOC_EXCEL:
                return "excel";
            case BaseMediaInfo.TYPE_DOC_PDF:
                return "pdf";
            case BaseMediaInfo.TYPE_DOC_TXT:
                return "txt";
            case BaseMediaInfo.TYPE_VIDEO:
                return "video";
            case BaseMediaInfo.TY:
                return "ppt";
            case BaseMediaInfo.TYPE_DOC_PPT:
                return "ppt";
            case BaseMediaInfo.TYPE_DOC_PPT:
                return "ppt";
            case BaseMediaInfo.TYPE_DOC_PPT:
                return "ppt";
            case BaseMediaInfo.TYPE_DOC_PPT:
                return "ppt";
        }
    }*/

    public static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                /*Log.v(SafUtil.TAG,"mimeType:"+mime +" ->>"+name);
                int last = mime.indexOf("/");
                if(last >0){
                    String type = mime.substring(0,last);
                    Log.v(SafUtil.TAG,"raw type:"+type +" ->>"+name);
                }*/

                return mime;
            }
        }

        return "application/octet-stream";
    }


    static Map<Class,Map<Object,String>> descMap = new HashMap<>();


    /**
     *
     * @param clazz
     * @param prefix  "METADATA_KEY_"   TYPE_
     * @return
     */
    static Map<Object,String> getMap(Class clazz,String prefix){
        if(descMap.containsKey(clazz)){
            return descMap.get(clazz);
        }
        Map<Object,String> map = new TreeMap<>();
        descMap.put(clazz,map);
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if(  Modifier.isFinal(field.getModifiers())  && Modifier.isStatic(field.getModifiers())){
                    String name = field.getName();
                    field.setAccessible(true);
                    if(name.startsWith(prefix)){
                        Object val =  field.get(clazz);
                        String desc = name.substring(prefix.length()).toLowerCase();
                       map.put(val,desc);
                    }
                }
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return map;
    }

    public static String  getDesc(int type){
        return getMap(BaseMediaInfo.class,"TYPE_").get(type);
    }


}
