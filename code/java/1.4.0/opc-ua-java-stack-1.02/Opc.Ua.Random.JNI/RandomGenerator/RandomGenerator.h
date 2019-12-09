#ifndef H_RANDOMGENERATOR_H
#define H_RANDOMGENERATOR_H

#include <wchar.h>
#include <wctype.h>

// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the RANDOMGENERATOR_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// RANDOMGENERATOR_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef RANDOMGENERATOR_EXPORTS
#define RANDOMGENERATOR_API __declspec(dllexport)
#else
#define RANDOMGENERATOR_API __declspec(dllimport)
#endif

#ifdef __cplusplus
extern "C"
{
#endif

typedef void * RANDOM;

/**
 * Creates a random data source.
 * A random data source is defined by following ordered set (random file, seed, step).
 *
 * @param pRandom [out] Pointer to RANDOM variable. RANDOM variables are like windows handles. They abstract to the user of the API internals of the random generator mechanism.
 * @param pPathToFile [in] Path to disk file containing random content to be used in assocation with random argument.
 * @param nSeed [in] Seed to be used in assocation with random argument.
 * @param nStep [in] Step to be used in association with random argument.
 *
 * @return 0 on success, value > 1 on error as follows 1 (invalid arguments), 2 (could not open file), 3 (empty file), 4 (out of memory (malloc))
 */
RANDOMGENERATOR_API int RandomCreate (RANDOM * pRandom, const char * pPathToFile, long nSeed, long nStep);

/**
 * Retrieves random data based on current state of random object described with random argument.
 *
 * @param random [in] Random handler (pointer to random struct abstracted for clients of the API). random argument will not change, instead random referred structure will.
 * @param pData [in] Pointer to storage where to write random data. pRandom will not change, instead memory referred with pData will be written.
 * @param nCount [in] Number of TCHAR successive memory locations to fill with random content.
 *
 * @return 0 on success, value > 0 on error as follows:
 */
RANDOMGENERATOR_API int RandomGetValue (RANDOM random, unsigned char * pData, long nCount);

/**
 * Destroys random structure variable abstracted to client code with the use of RANDOM handle (descriptor).
 *
 * @param random [out] Random internals are deallocated (memory is released, file handle is closed) and random is assigned NULL on success.
 *
 * @return 0 on success , value > 0 on error as follows:
 */
RANDOMGENERATOR_API int RandomDestroy (RANDOM * random);

// currently WIN32 primitive data - types supported
// define primitive data - types in order to support GetValueZZZ functionality

// Assumptions:
// wchat_t is  16 - bit [maps C# Char   primitive type]
// char    is   8 - bit [maps C# Byte   primitive type]
// short   is  16 - bit [maps C# Int16  primitive type]
// int     is  32 - bit [maps C# Int32  primitive type]
// __int64 is  64 - bit [maps C# Int64  primitive type]
// single  is  32 - bit [maps C# Int32  primitive type]
// double  is  64 - bit [maps C# Double primitive type]

/**
 * Interprets bytes as pData memory location as 8 - bit signed value independently of machine architecture (big or little endian). 
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 8-bit signed integer.
 */
RANDOMGENERATOR_API char  GetValueInt8(RANDOM random);

/**
 * Interprets bytes as pData memory location as 16 - bit signed value independently of machine architecture (big or little endian). 
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 16-bit signed integer.
 */
RANDOMGENERATOR_API short GetValueInt16(RANDOM random);

/**
 * Interprets bytes as pData memory location as 32 - bit signed value independently of machine architecture (big or little endian). 
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 32-bit signed integer.
 */
RANDOMGENERATOR_API int GetValueInt32(RANDOM random);

/**
 * Interprets bytes as pData memory location as 64 - bit signed value independently of machine architecture (big or little endian).  
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 64-bit signed integer.
 */
RANDOMGENERATOR_API __int64 GetValueInt64(RANDOM random);

/**
 * Interprets bytes as pData memory location as 8 - bit unsigned value independently of machine architecture (big or little endian).
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 8-bit unsigned integer.
 */
RANDOMGENERATOR_API unsigned char GetValueUInt8(RANDOM random);

/**
 * Interprets bytes as pData memory location as 16 - bit unsigned value independently of machine architecture (big or little endian).
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 16-bit unsigned integer.
 */
RANDOMGENERATOR_API unsigned short GetValueUInt16(RANDOM random);

/**
 * Interprets bytes as pData memory location as 32 - bit unsigned value independently of machine architecture (big or little endian).
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 32-bit unsigned integer.
 */
RANDOMGENERATOR_API unsigned int GetValueUInt32(RANDOM random);

/**
 * Interprets bytes as pData memory location as 64 - bit signed value independently of machine architecture (big or little endian).
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 64-bit unsigned integer.
 */
RANDOMGENERATOR_API unsigned __int64 GetValueUInt64(RANDOM random);

/**
 * Raw memory interprets a got 32 - bit signed value independent of platform to float.
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 32-bit floating point number.
 */
RANDOMGENERATOR_API float GetValueFloat(RANDOM random);

/**
 * Raw memory interprets a got 64 - bit signed value independent of platform to double.
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random 64-bit floating point number.
 */
RANDOMGENERATOR_API double GetValueDouble(RANDOM random);

/**
 * Returns DateTime between 1900/1/1 and 2099/12/31 (as number of 100ns ticks since 1601/1/1).
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @return A random DateTime value.
 */
RANDOMGENERATOR_API __int64 GetValueDateTime(RANDOM random);

/**
 * Fills a string with random characters. 
 * Range for characters of this string are hardcoded into range 0x0001 .. 0x33ff.
 * The string generated is null terminated an will have a minimum length of 1 and a maximum length of nSize-1
 * The length of the string calculated by generating an random UInt32 and the modulus of the min and max size.
 * @param pRandom [in] Pointer to a RANDOM variable.
 * @param [in/out] pString A pointer to a buffer allocated by the caller.
 * @param [in] nSize The number of characters in the string buffer.
 * @return pString address
 */
RANDOMGENERATOR_API wchar_t * GetValueString(RANDOM random, wchar_t* pString, int nSize);

#ifdef __cplusplus
}
#endif

#endif // #ifndef H_RANDOMGENERATOR_H

