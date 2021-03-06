/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_github_vallez_leveldbjni_LevelDb */

#ifndef _Included_com_github_vallez_leveldbjni_LevelDb
#define _Included_com_github_vallez_leveldbjni_LevelDb
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    open
 * Signature: (Ljava/lang/String;ZZZZJB)J
 */
JNIEXPORT jlong JNICALL Java_com_github_vallez_leveldbjni_LevelDb_open
  (JNIEnv *, jobject, jstring, jboolean, jboolean, jboolean, jboolean, jlong, jbyte);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    put
 * Signature: (J[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_put
  (JNIEnv *, jobject, jlong, jbyteArray, jbyteArray);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    get
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_github_vallez_leveldbjni_LevelDb_get
  (JNIEnv *, jobject, jlong, jbyteArray);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    delete
 * Signature: (J[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_delete
  (JNIEnv *, jobject, jlong, jbyteArray);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    writeBatchNew
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchNew
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    writeBatchPut
 * Signature: (J[B[B)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchPut
  (JNIEnv *, jobject, jlong, jbyteArray, jbyteArray);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    writeBatchDelete
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchDelete
  (JNIEnv *, jobject, jlong, jbyteArray);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    writeBatchWriteAndClose
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchWriteAndClose
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorNew
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorNew
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorSeekToFirst
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorSeekToFirst
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorSeekToLast
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorSeekToLast
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorSeek
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorSeek
  (JNIEnv *, jobject, jlong, jbyteArray);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorValid
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorValid
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorNext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorNext
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorPrev
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorPrev
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorKey
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorKey
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorValue
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorValue
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    iteratorClose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorClose
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_github_vallez_leveldbjni_LevelDb
 * Method:    close
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_close
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
