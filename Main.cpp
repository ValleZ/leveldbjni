#include <jni.h>
#include <iostream>
#include "Main.h"
#include "leveldb/db.h"

leveldb::DB* db;

JNIEXPORT jboolean JNICALL Java_LevelDb_open
  (JNIEnv* env, jobject thisObject, jstring fileName,
  jboolean createIfMissing, jboolean errorIfExists) {

   leveldb::Options options;
   options.create_if_missing = createIfMissing;
   options.error_if_exists = errorIfExists;
   const char* fileNameCpp = env->GetStringUTFChars(fileName, NULL);
   leveldb::Status status = leveldb::DB::Open(options, fileNameCpp, &db);
   return status.ok();
}

JNIEXPORT jboolean JNICALL Java_LevelDb_put
  (JNIEnv* env, jobject thisObject, jbyteArray key, jbyteArray value) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   jbyte *ptrV = env->GetByteArrayElements(value, 0);
   std::string valueStr((char *)ptrV, env->GetArrayLength(value));
   env->ReleaseByteArrayElements(value, ptrV, 0);

   leveldb::Status status = db->Put(leveldb::WriteOptions(), keyStr, valueStr);
   return status.ok();
}

JNIEXPORT jboolean JNICALL Java_LevelDb_delete
  (JNIEnv* env, jobject thisObject, jbyteArray key) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   std::string value;
   leveldb::Status status = db->Delete(leveldb::WriteOptions(), keyStr);
   return status.ok();
}

JNIEXPORT jbyteArray JNICALL Java_LevelDb_get
  (JNIEnv* env, jobject thisObject, jbyteArray key) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   std::string value;
   leveldb::Status status = db->Get(leveldb::ReadOptions(), keyStr, &value);
   if(!status.ok()) {
    return NULL;
   }

   jbyteArray result = env->NewByteArray(value.length());
   env->SetByteArrayRegion(result,0,value.length(),(jbyte*)value.c_str());
   return result;
}

JNIEXPORT void JNICALL Java_LevelDb_close
  (JNIEnv* env, jobject thisObject) {
   delete db;
}