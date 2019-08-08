//
// Created by Administrator on 2019/8/7 0007.
//
#include "libyuv.h"
//#include <string>
#include <opencv2/opencv.hpp>
#include "com_zkteco_autk_utils_ImageUtil.h"

using namespace cv;
using namespace libyuv;

extern "C"

JNIEXPORT jboolean JNICALL Java_com_zkteco_autk_utils_ImageUtil_absdiff
        (JNIEnv *, jclass, jbyteArray , jbyteArray) {
    //I420Scale();
    //https://codeday.me/bug/20171113/95862.html
    //https://github.com/eterrao/AndroidLibyuvImageUtils/blob/master/app/src/main/jni/tech_shutu_jni_YuvUtils.c
    return true;
}