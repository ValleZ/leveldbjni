#include <jni.h>
#include <iostream>
#include "com_github_vallez_leveldbjni_LevelDb.h"
#include "leveldb/db.h"
#include "leveldb/cache.h"
#include "leveldb/filter_policy.h"
#include "leveldb/write_batch.h"

JNIEXPORT jlong JNICALL Java_com_github_vallez_leveldbjni_LevelDb_open
  (JNIEnv* env, jobject thisObject, jstring fileName,
  jboolean createIfMissing, jboolean errorIfExists,
  jboolean compression, jboolean paranoidChecks,
  jlong cacheSizeBytes, jbyte bloomFilterBitsPerKey  ) {

   leveldb::Options options;
   options.create_if_missing = createIfMissing;
   options.error_if_exists = errorIfExists;
   if (!compression) {
        options.compression = leveldb::kNoCompression;
   }
   if (cacheSizeBytes>0) {
        options.block_cache = leveldb::NewLRUCache((size_t)cacheSizeBytes);
   }
   if (bloomFilterBitsPerKey>0) {
        options.filter_policy = leveldb::NewBloomFilterPolicy(bloomFilterBitsPerKey);
   }
   options.paranoid_checks = paranoidChecks;
   const char* fileNameCpp = env->GetStringUTFChars(fileName, NULL);
   leveldb::DB* db;
   leveldb::Status status = leveldb::DB::Open(options, fileNameCpp, &db);
   if (status.ok()) {
        return (jlong) db;
   }
   return (jlong) 0;
}

JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_put
  (JNIEnv* env, jobject thisObject, jlong dbRef, jbyteArray key, jbyteArray value) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   jbyte *ptrV = env->GetByteArrayElements(value, 0);
   std::string valueStr((char *)ptrV, env->GetArrayLength(value));
   env->ReleaseByteArrayElements(value, ptrV, 0);

   leveldb::Status status = ((leveldb::DB*)dbRef)->Put(leveldb::WriteOptions(), keyStr, valueStr);
   return status.ok();
}

JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_delete
  (JNIEnv* env, jobject thisObject, jlong dbRef, jbyteArray key) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   std::string value;
   leveldb::Status status = ((leveldb::DB*)dbRef)->Delete(leveldb::WriteOptions(), keyStr);
   return status.ok();
}

JNIEXPORT jbyteArray JNICALL Java_com_github_vallez_leveldbjni_LevelDb_get
  (JNIEnv* env, jobject thisObject, jlong dbRef, jbyteArray key) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   std::string value;
   leveldb::Status status = ((leveldb::DB*)dbRef)->Get(leveldb::ReadOptions(), keyStr, &value);
   if(!status.ok()) {
    return (jbyteArray) 0;
   }

   jbyteArray result = env->NewByteArray(value.length());
   env->SetByteArrayRegion(result,0,value.length(),(jbyte*)value.c_str());
   return result;
}

JNIEXPORT jlong JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchNew
  (JNIEnv *, jobject thisObject, jlong dbRef) {
    if (!dbRef) {
        return (jlong) 0;
    }
    leveldb::WriteBatch* batch = new leveldb::WriteBatch();
    return (jlong) batch;
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchPut
  (JNIEnv* env, jobject thisObject, jlong ref, jbyteArray key, jbyteArray value) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   jbyte *ptrV = env->GetByteArrayElements(value, 0);
   std::string valueStr((char *)ptrV, env->GetArrayLength(value));
   env->ReleaseByteArrayElements(value, ptrV, 0);

   ((leveldb::WriteBatch*)ref)->Put(keyStr, valueStr);
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchDelete
  (JNIEnv* env, jobject thisObject, jlong ref, jbyteArray key) {
   jbyte *ptr = env->GetByteArrayElements(key, 0);
   std::string keyStr((char *)ptr, env->GetArrayLength(key));
   env->ReleaseByteArrayElements(key, ptr, 0);

   ((leveldb::WriteBatch*)ref)->Delete(keyStr);
}

JNIEXPORT jlong JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorNew
  (JNIEnv *, jobject thisObject, jlong dbRef) {
    if (!dbRef) {
        return (jlong) 0;
    }
    leveldb::Iterator* it = ((leveldb::DB*)dbRef)->NewIterator(leveldb::ReadOptions());
    return (jlong) it;
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorSeekToFirst
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->SeekToFirst();
    }
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorSeekToLast
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->SeekToLast();
    }
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorSeek
  (JNIEnv * env, jobject, jlong ref, jbyteArray key) {
    if (ref) {
        jbyte *ptr = env->GetByteArrayElements(key, 0);
        std::string keyStr((char *)ptr, env->GetArrayLength(key));
        env->ReleaseByteArrayElements(key, ptr, 0);
        ((leveldb::Iterator*)ref)->Seek(keyStr);
    }
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorNext
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->Next();
    }
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorPrev
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         ((leveldb::Iterator*)ref)->Prev();
    }
}

JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorValid
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
         return ((leveldb::Iterator*)ref)->Valid();
    }
    return false;
}

JNIEXPORT jbyteArray JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorKey
  (JNIEnv* env, jobject thisObject, jlong ref) {

   std::string value;
   value = ((leveldb::Iterator*)ref)->key().ToString();

   jbyteArray result = env->NewByteArray(value.length());
   env->SetByteArrayRegion(result,0,value.length(),(jbyte*)value.c_str());
   return result;
}

JNIEXPORT jbyteArray JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorValue
  (JNIEnv* env, jobject thisObject, jlong ref) {

   std::string value;
   value = ((leveldb::Iterator*)ref)->value().ToString();

   jbyteArray result = env->NewByteArray(value.length());
   env->SetByteArrayRegion(result,0,value.length(),(jbyte*)value.c_str());
   return result;
}

JNIEXPORT jboolean JNICALL Java_com_github_vallez_leveldbjni_LevelDb_writeBatchWriteAndClose
  (JNIEnv *, jobject, jlong dbRef, jlong ref) {
    jboolean result = false;
    if (ref) {
        if (dbRef) {
            leveldb::Status status = ((leveldb::DB*)dbRef)->Write(leveldb::WriteOptions(),
                (leveldb::WriteBatch*) ref);
            result = status.ok();
        }
        delete (leveldb::WriteBatch*) ref;
    }
    return result;
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_iteratorClose
  (JNIEnv *, jobject, jlong ref) {
    if (ref) {
        delete (leveldb::Iterator*) ref;
    }
}

JNIEXPORT void JNICALL Java_com_github_vallez_leveldbjni_LevelDb_close
  (JNIEnv*, jobject, jlong dbRef) {
    if (dbRef) {
        delete (leveldb::DB*) dbRef;
    }
}