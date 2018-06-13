//
// Created by Jiang on 6/8/2018.
//
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <vector>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT jdouble JNICALL Java_edu_ucsb_cs_cs184_hjiang00_musicperformer_GestureRecognition_findInscribedCircleJNI(JNIEnv* env, jobject obj, jlong imgAddr,
                                                                                                                   jdouble rectTLX, jdouble rectTLY, jdouble rectBRX, jdouble rectBRY,
                                                                                                                   jdoubleArray incircleX, jdoubleArray incircleY, jlong contourAddr);

JNIEXPORT jdouble JNICALL Java_edu_ucsb_cs_cs184_hjiang00_musicperformer_GestureRecognition_findInscribedCircleJNI(JNIEnv* env, jobject obj, jlong imgAddr,
                                                                                                                   jdouble rectTLX, jdouble rectTLY, jdouble rectBRX, jdouble rectBRY,
                                                                                                                   jdoubleArray incircleX, jdoubleArray incircleY, jlong contourAddr)
{
    Mat& img_cpp  = *(Mat*)imgAddr;

    Mat& contourMat = *(Mat*)contourAddr;
    vector<Point2f> contourVec;

    contourMat.copyTo(contourVec);

    double r = 0;
    double targetX = 0;
    double targetY = 0;

    for (int y = (int)rectTLY; y < (int)rectBRY; y++)
    {
        for (int x = (int)rectTLX; x < (int)rectBRX; x++)
        {
            double curDist = pointPolygonTest(contourVec, Point2f(x, y), true);

            if (curDist > r) {
                r = curDist;
                targetX = x;
                targetY = y;
            }
        }
    }

    jdouble outArrayX[] = {0};
    jdouble outArrayY[] = {0};

    outArrayX[0] = targetX;
    outArrayY[0] = targetY;

    env->SetDoubleArrayRegion(incircleX, 0 , 1, (const jdouble*)outArrayX);
    env->SetDoubleArrayRegion(incircleY, 0 , 1, (const jdouble*)outArrayY);


    return r;
}
}

