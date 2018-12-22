#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <malloc.h>
#include "include/jpeglib.h"

#define LOG_TAG "Luban"

#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define true 1
#define false 0

typedef uint8_t BYTE;

int generateJpg(BYTE *data, int width, int height, int quality, const char *outfile) {
    int component = 3;

    struct jpeg_compress_struct jcs;
    struct jpeg_error_mgr jem;

    jcs.err = jpeg_std_error(&jem);

    jpeg_create_compress(&jcs);

    FILE *file = fopen(outfile, "wb");
    if (file == NULL) {
        return 0;
    }

    jpeg_stdio_dest(&jcs, file);

    jcs.image_width = (JDIMENSION) width;
    jcs.image_height = (JDIMENSION) height;
    jcs.arith_code = FALSE;
    jcs.input_components = component;
    jcs.in_color_space = JCS_RGB;
    jcs.optimize_coding = TRUE;
    //boolean optimize_coding：

    //参数为TRUE时，图片压缩算法使用最优的哈夫曼编码表，它需要额外传递数据，因此会耗费CPU运算时间，以及开辟很多临时内存空间。
    //参数为FALSE时，使用默认的哈夫曼编码表。在大多数情况，使用最优哈夫曼编码表相比默认哈夫曼编码表，能节省图像文件很大比例的大小。

    jpeg_set_defaults(&jcs);
    jpeg_set_quality(&jcs, quality, TRUE);
    jpeg_start_compress(&jcs, TRUE);

    JSAMPROW rowPointer[1];
    int rowStride = jcs.image_width * component;

    while (jcs.next_scanline < jcs.image_height) {
        rowPointer[0] = &data[jcs.next_scanline * rowStride];
        jpeg_write_scanlines(&jcs, rowPointer, 1);
    }

    jpeg_finish_compress(&jcs);
    jpeg_destroy_compress(&jcs);
    fclose(file);

    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_hss01248_lubanturbo_TurboCompressor_nativeCompress(JNIEnv *env, jclass type,
                                                          jobject bitmap, jint quality,
                                                          jstring outfile) {
    AndroidBitmapInfo bitmapInfo;
    BYTE *pixelColor;
    BYTE *data;
    BYTE *tempData;

    const char *filepath = (*env)->GetStringUTFChars(env, outfile, 0);
    LOG_I("outfile path is %s", filepath);

    if ((AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOG_E("compress error");
        (*env)->ReleaseStringUTFChars(env, outfile, filepath);
        return false;
    }

    int width = bitmapInfo.width;
    int height = bitmapInfo.height;

    if ((AndroidBitmap_lockPixels(env, bitmap, (void **) &pixelColor)) < 0) {
        LOG_E("lock pixels error");
        (*env)->ReleaseStringUTFChars(env, outfile, filepath);
        return false;
    }

    BYTE a,r, g, b;
    int color;
    data = malloc((size_t) (width * height * 3));
    tempData = data;

    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            color = *((int *) pixelColor);
            //a = (BYTE) ((color & 0xFF000000) >> 24);
            r = (BYTE) ((color & 0x00FF0000) >> 16);
            g = (BYTE) ((color & 0x0000FF00) >> 8);
            b = (BYTE) (color & 0X000000FF);
            //解决完全透明的像素点压缩后变黑的bug:还是会有部分变黑
            /*if(a == 0 && r ==0 && g ==0 && b ==0){
                r = 0X000000FF;
                g = 0X000000FF;
                b = 0X000000FF;
            }*/

            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data += 3;
            pixelColor += 4;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    int result = generateJpg(tempData, width, height, quality, filepath);
    LOG_I("compress complete, result code is %d", result);

    free(tempData);
    (*env)->ReleaseStringUTFChars(env, outfile, filepath);

    if (result == 0) {
        return false;
    } else {
        return true;
    }
}