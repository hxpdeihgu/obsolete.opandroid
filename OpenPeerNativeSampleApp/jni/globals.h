#include <jni.h>
#include "EventManager.h"
#include "CacheDelegateWrapper.h"
#include "SettingsDelegateWrapper.h"

#ifndef _ANDROID_OPENPEER_GLOBALS_H_
#define _ANDROID_OPENPEER_GLOBALS_H_

extern JavaVM *android_jvm;
static JNIEnv* gEnv;
extern jobject jni_object;
static jobject gClassLoader;
static jmethodID gFindClassMethod;

static std::map<jobject, ICallPtr> callMap;
static std::map<jobject, IConversationThreadPtr> conversationThreadMap;
static std::map<jobject, IContactPtr> contactMap;

static jclass gCallbackClass;

extern EventManagerPtr globalEventManager;
extern IAccountPtr accountPtr;
extern IStackPtr stackPtr;
extern IStackMessageQueuePtr queuePtr;
extern IIdentityPtr identityPtr;
extern IMediaEnginePtr mediaEnginePtr;
extern ICachePtr cachePtr;
extern SettingsDelegateWrapperPtr settingsDelegatePtr;
extern CacheDelegateWrapperPtr cacheDelegatePtr;

#endif
