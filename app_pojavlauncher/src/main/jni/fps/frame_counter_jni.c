#include <jni.h>
#include "frame_counter.h"
JNIEXPORT jlong JNICALL
Java_net_kdt_pojavlaunch_game_NativeFrameCounter_readFrames(JNIEnv *env, jclass clazz) {
    return cleave_frames_available() ? (jlong) cleave_frame_count() : -1;
}
JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_game_NativeFrameCounter_setTiming(JNIEnv *env, jclass clazz, jboolean enabled) {
    cleave_set_frame_timing(enabled ? 1 : 0);
}
JNIEXPORT jlongArray JNICALL
Java_net_kdt_pojavlaunch_game_NativeFrameCounter_readTiming(JNIEnv *env, jclass clazz) {
    jlong values[CLEAVE_FRAME_TIME_BUCKETS];
    for (unsigned i = 0; i < CLEAVE_FRAME_TIME_BUCKETS; i++) values[i] = (jlong) cleave_frame_time_bucket(i);
    jlongArray result = (*env)->NewLongArray(env, CLEAVE_FRAME_TIME_BUCKETS);
    if (result) (*env)->SetLongArrayRegion(env, result, 0, CLEAVE_FRAME_TIME_BUCKETS, values);
    return result;
}
