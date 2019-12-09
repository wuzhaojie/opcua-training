/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_opcfoundation_ua_stacktest_random_library_RandomGenerator */

#ifndef _Included_org_opcfoundation_ua_stacktest_random_library_RandomGenerator
#define _Included_org_opcfoundation_ua_stacktest_random_library_RandomGenerator
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    randomCreate
 * Signature: (Ljava/lang/String;JJ)I
 */
JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomCreate
  (JNIEnv *, jobject, jstring, jlong, jlong);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    randomDestroy
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomDestroy
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    randomGetValue
 * Signature: ([BJ)I
 */
JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomGetValue
  (JNIEnv *, jobject, jbyteArray, jlong);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueInt8
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt8
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueInt16
 * Signature: ()S
 */
JNIEXPORT jshort JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt16
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueInt32
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt32
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueInt64
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt64
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueUInt8
 * Signature: ()Lorg/opcfoundation/ua/builtintypes/UnsignedByte;
 */
JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt8
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueUInt16
 * Signature: ()Lorg/opcfoundation/ua/builtintypes/UnsignedShort;
 */
JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt16
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueUInt32
 * Signature: ()Lorg/opcfoundation/ua/builtintypes/UnsignedInteger;
 */
JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt32
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueUInt64
 * Signature: ()Lorg/opcfoundation/ua/builtintypes/UnsignedLong;
 */
JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt64
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueFloat
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueFloat
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueDouble
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueDouble
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueDateTime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueDateTime
  (JNIEnv *, jobject);

/*
 * Class:     org_opcfoundation_ua_stacktest_random_library_RandomGenerator
 * Method:    getValueString
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueString
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
