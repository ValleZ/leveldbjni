#include <jni.h>
#include <iostream>
#include "LevelDb.h"
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

JNIEXPORT jlong JNICALL Java_LevelDb_iteratorNew
  (JNIEnv *, jobject thisObject) {
    if (!db) {
        return 0;
    }
    leveldb::Iterator* it = db->NewIterator(leveldb::ReadOptions());
//    std::cout << "created" << (jlong) it << std::endl;
    return (jlong) it;
}

JNIEXPORT void JNICALL Java_LevelDb_iteratorSeekToFirst
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->SeekToFirst();
    }
}

JNIEXPORT void JNICALL Java_LevelDb_iteratorSeekToLast
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->SeekToLast();
    }
}

JNIEXPORT void JNICALL Java_LevelDb_iteratorSeek
  (JNIEnv * env, jobject, jlong ref, jbyteArray key) {
    if (ref) {
        jbyte *ptr = env->GetByteArrayElements(key, 0);
        std::string keyStr((char *)ptr, env->GetArrayLength(key));
        env->ReleaseByteArrayElements(key, ptr, 0);
        ((leveldb::Iterator*)ref)->Seek(keyStr);
    }
}

JNIEXPORT void JNICALL Java_LevelDb_iteratorNext
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->Next();
    }
}

JNIEXPORT void JNICALL Java_LevelDb_iteratorPrev
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->Prev();
    }
}

JNIEXPORT jboolean JNICALL Java_LevelDb_iteratorValid
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         return ((leveldb::Iterator*)ref)->Valid();
    }
    return false;
}

JNIEXPORT jbyteArray JNICALL Java_LevelDb_iteratorKey
  (JNIEnv* env, jobject thisObject, jlong ref) {

   std::string value;
   value = ((leveldb::Iterator*)ref)->key().ToString();

   jbyteArray result = env->NewByteArray(value.length());
   env->SetByteArrayRegion(result,0,value.length(),(jbyte*)value.c_str());
   return result;
}

JNIEXPORT jbyteArray JNICALL Java_LevelDb_iteratorValue
  (JNIEnv* env, jobject thisObject, jlong ref) {

   std::string value;
   value = ((leveldb::Iterator*)ref)->value().ToString();

   jbyteArray result = env->NewByteArray(value.length());
   env->SetByteArrayRegion(result,0,value.length(),(jbyte*)value.c_str());
   return result;
}

JNIEXPORT void JNICALL Java_LevelDb_iteratorClose
  (JNIEnv *, jobject thisObject, jlong ref) {
    if (ref) {
        delete (leveldb::Iterator*)ref;
    }
}

JNIEXPORT void JNICALL Java_LevelDb_close
  (JNIEnv* env, jobject thisObject) {
    if(db) {
        delete db;
    }
}