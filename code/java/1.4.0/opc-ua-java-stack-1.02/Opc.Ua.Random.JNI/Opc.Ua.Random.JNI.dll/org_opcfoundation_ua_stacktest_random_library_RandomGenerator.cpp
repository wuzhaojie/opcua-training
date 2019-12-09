#include "stdafx.h"
#include "org_opcfoundation_ua_stacktest_random_library_RandomGenerator.h"
#include "RandomGenerator.h"
#include <string>
#include <sstream>


// 2007-05-24
// TODO:
// * Find resources that need to be freed
//		- JNI-document tells the  

// 2007-05-23
// RandomGenerator.h and .c files have wrong parameter of type RANDOM * at 
// GetValueXXX functions. In C++ we have to cast RANDOM type to RANDOM * type
// if we want to compile this module. Change the define if the RandomGenerator.h 
// get fixed.
//
// #define CAST_RANDOM 



#define CAST_RANDOM (RANDOM *) 

void ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
	jclass cls = env->FindClass(name);
	/* if cls is NULL, an exception has already been thrown */
	if (cls != NULL) {
		env->ThrowNew(cls, msg);
	}
	/* free the local ref */
	env->DeleteLocalRef(cls);
}


char *GetStringNativeChars(JNIEnv *env, jstring jstr)
{
	jbyteArray bytes = 0;
	jthrowable exc;
	jclass stringClass;
	jmethodID MID_String_getBytes;

	char *result = 0;
	if (env->EnsureLocalCapacity(2) < 0) {
		return NULL; /* out of memory error */
	}
	stringClass = env->FindClass("java/lang/String");
	if (stringClass != NULL) {

		MID_String_getBytes = env->GetMethodID(stringClass, "getBytes", "()[B");
		if (MID_String_getBytes != NULL) {
			bytes = (jbyteArray)(env->CallObjectMethod(jstr, MID_String_getBytes));
			exc = env->ExceptionOccurred();
			if (!exc) {
				jint len = env->GetArrayLength(bytes);
				result =  new char[len + 1];
				if (result == 0) {
					ThrowByName(env, "java/lang/OutOfMemoryError",
						0);
					env->DeleteLocalRef(bytes);
					return NULL;
				}
				env->GetByteArrayRegion(bytes, 0, len,(jbyte *)result);
				result[len] = 0; /* NULL-terminate */
			} else {
				env->DeleteLocalRef(exc);
			}
			env->DeleteLocalRef(bytes);
			return result;
		}
		else {
			return NULL;
		}
		env->DeleteLocalRef(stringClass);
	}
	else {
		return NULL;
	}
}

jfieldID AccessRandomField(JNIEnv * env, jobject obj){
	jclass cls = NULL;
	jfieldID fid = NULL;
	cls = env->GetObjectClass(obj);
	if (cls != NULL){
		fid = env->GetFieldID(cls, "random", "J");	
		env->DeleteLocalRef(cls);
	}
	return fid;
}

jlong GetRandom(JNIEnv * env, jobject obj) {
	jlong result=0;
	jfieldID randomID = AccessRandomField(env, obj);
	if (randomID != NULL) {
		result = env->GetLongField(obj, randomID);
	}
	return result;
}

void SetRandom(JNIEnv * env, jobject obj, jlong value) {
	jfieldID randomID = AccessRandomField(env, obj);
	if (randomID != NULL) {
		env->SetLongField(obj, randomID, value);
	}
}

jobject UnsignedByteFactory(JNIEnv *env, int value) {
	jclass uNumberClass=NULL;
	jmethodID cid=NULL;
	jobject result =NULL;
	if (value >= 0) {			 
		uNumberClass = env->FindClass("org/opcfoundation/ua/builtintypes/UnsignedByte");
		if (uNumberClass != NULL) {
			/* Get the method ID for the constructor */
			cid = env->GetMethodID(uNumberClass, "<init>", "(I)V");
			if (cid != NULL) {
				result = env->NewObject(uNumberClass, cid, value);
			}
			env->DeleteLocalRef(uNumberClass);
		}
	}
	return result;
}

jobject UnsignedShortFactory(JNIEnv *env, int value) {
	jclass uNumberClass=NULL;
	jmethodID cid=NULL;
	jobject result =NULL;
	if (value >= 0) {			 
		uNumberClass = env->FindClass("org/opcfoundation/ua/builtintypes/UnsignedShort");
		if (uNumberClass != NULL) {
			/* Get the method ID for the constructor */
			cid = env->GetMethodID(uNumberClass, "<init>", "(I)V");
			if (cid != NULL) {
				result = env->NewObject(uNumberClass, cid, value);
			}
			env->DeleteLocalRef(uNumberClass);
		}
	}
	return result;
}

jobject UnsignedIntegerFactory(JNIEnv *env, jlong value) {
	jclass uNumberClass=NULL;
	jmethodID cid=NULL;
	jobject result =NULL;
	if (value >= 0) {			 
		uNumberClass = env->FindClass("org/opcfoundation/ua/builtintypes/UnsignedInteger");
		if (uNumberClass != NULL) {
			/* Get the method ID for the constructor */
			cid = env->GetMethodID(uNumberClass, "<init>", "(J)V");
			if (cid != NULL) {
				result = env->NewObject(uNumberClass, cid, value);
			}
			env->DeleteLocalRef(uNumberClass);
		}
	}
	return result;
}

jobject UnsignedLongFactory(JNIEnv *env, jstring value) {
	jclass uNumberClass=NULL;
	jmethodID cid=NULL;
	jobject result =NULL;
	if (value >= 0) {			 
		uNumberClass = env->FindClass("org/opcfoundation/ua/builtintypes/UnsignedLong");
		if (uNumberClass != NULL) {
			/* Get the method ID for the constructor */
			cid = env->GetMethodID(uNumberClass, "<init>", "(Ljava/lang/String;)V");
			if (cid != NULL) {
				result = env->NewObject(uNumberClass, cid, value);
			}
			env->DeleteLocalRef(uNumberClass);
		}
	}
	return result;
}

JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomCreate
(JNIEnv * env, jobject obj, jstring pathToFile, jlong nSeed, jlong nStep){
	// Release previous file
	Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomDestroy(env, obj);
	RANDOM pRandom=NULL;
	int result;
	char * fileName=NULL;
	fileName = GetStringNativeChars(env, pathToFile);
	if (fileName){
		result = RandomCreate (& pRandom, fileName, (long) nSeed, (long) nStep);
		if (result == 0){
			SetRandom(env, obj, (jlong)pRandom);
		}
		else{
			SetRandom(env, obj, 0);
		}
		delete fileName;
		return result;
	}
	else
		return 1; // (invalid arguments) 
}

/**
* @return 
* <li>Return values from RandomDestroy function
* <li>Added in this wrapper: 1001 = Generator file was not opened	
*/
JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomDestroy
(JNIEnv * env, jobject obj){
	if (GetRandom != NULL){
		RANDOM random = (RANDOM)GetRandom(env, obj);
		return RandomDestroy(&random);
	}
	else {
		return 1001;
	}
}

JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_randomGetValue
(JNIEnv * env, jobject obj, jbyteArray byteArray, jlong count){
	//char * buffer = new char[count];
	RANDOM random = (RANDOM)GetRandom(env, obj);
	int rndResult = 0;
	jboolean isCopy;
	jbyte * buffer = env->GetByteArrayElements(byteArray, &isCopy);
	rndResult = RandomGetValue(random, (unsigned char *) buffer, (long) count);
	env->ReleaseByteArrayElements(byteArray, buffer, JNI_COMMIT);
/*
	if (rndResult == 0) {	
		for (jsize i=0; i<count; i++){
			env->SetObjectArrayElement(byteArray, i, UnsignedByteFactory(env, buffer[i]));
		}
	}
	delete[] buffer;
*/
	return rndResult;
}

JNIEXPORT jbyte JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt8
(JNIEnv * env, jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueInt8(CAST_RANDOM random);
}

JNIEXPORT jshort JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt16
(JNIEnv * env, jobject obj) {
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueInt16(CAST_RANDOM random);
}

JNIEXPORT jint JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt32
(JNIEnv * env, jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueInt32(CAST_RANDOM random);
}

JNIEXPORT jlong JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueInt64
(JNIEnv * env, jobject obj) {
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueInt64(CAST_RANDOM random);
}

JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt8
(JNIEnv * env , jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	int result = GetValueUInt8(CAST_RANDOM random);
	return UnsignedByteFactory(env, result);
}
JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt16
(JNIEnv * env, jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	int result = GetValueUInt16(CAST_RANDOM random);
	return UnsignedShortFactory(env, result);
}

JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt32
(JNIEnv * env, jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	jlong result = GetValueUInt32(CAST_RANDOM random);
	return UnsignedIntegerFactory(env, result);
}

JNIEXPORT jobject JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueUInt64
(JNIEnv * env , jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	std::stringstream resultStrm;
	std::string resultStr;
	jobject result=NULL;

	unsigned __int64 ui64 = GetValueUInt64(CAST_RANDOM random);
	resultStrm << ui64;
	resultStrm >> resultStr;
	jstring jstr = env->NewStringUTF(resultStr.c_str());
	if (jstr != NULL){
		result = UnsignedLongFactory(env, jstr);
	}
	return result;
}

JNIEXPORT jfloat JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueFloat
  (JNIEnv * env, jobject obj) {
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueFloat(CAST_RANDOM random);
}

JNIEXPORT jdouble JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueDouble
  (JNIEnv * env, jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueDouble(CAST_RANDOM random);
}

JNIEXPORT jlong JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueDateTime
  (JNIEnv * env, jobject obj){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	return GetValueDateTime(CAST_RANDOM random);
}

JNIEXPORT jstring JNICALL Java_org_opcfoundation_ua_stacktest_random_library_RandomGenerator_getValueString
  (JNIEnv * env, jobject obj, jint nSize){
	RANDOM random = (RANDOM)GetRandom(env, obj);
	wchar_t *s = new wchar_t[nSize];
	wchar_t *w = GetValueString(CAST_RANDOM random, s, nSize);
	return env->NewString((jchar *) w, (jsize) wcslen(w));
}
