package com.hss01248.media.mymediastore.scanner;

/**
 * text/plain（纯文本）
 * text/html（HTML文档）
 * text/javascript（js代码）
 * application/xhtml+xml（XHTML文档）
 * image/gif（GIF图像）
 * image/jpeg（JPEG图像）
 * image/png（PNG图像）
 * video/mpeg（MPEG动画）
 * application/octet-stream（二进制数据）
 * application/pdf（PDF文档）
 * application/(编程语言) 该种语言的代码
 * application/msword（Microsoft Word文件）
 * message/rfc822（RFC 822形式）
 * multipart/alternative（HTML邮件的HTML形式和纯文本形式，相同内容使用不同形式表示）
 * application/x-www-form-urlencoded（POST方法提交的表单）multipart/form-data（POST提交时伴随文件上传的表单）
 * ————————————————
 * 版权声明：本文为CSDN博主「L_lemo004」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/qq_32108547/article/details/95653220
 *
 *
 * public static final int TYPE_IMAGE = 1;
 *     public static final int TYPE_VIDEO = 2;
 *     public static final int TYPE_AUDIO = 3;
 *     public static final int TYPE_DOC_PDF = 4;//pdf
 *     public static final int TYPE_DOC_DOC = 5;//msword
 *     public static final int TYPE_DOC_EXCEL = 6;//excel
 *     public static final int TYPE_DOC_PPT = 7;//powerpoint
 *     public static final int TYPE_DOC_TXT = 8;  //文件名.txt
 */
public interface IFileTypeScanner {


}
