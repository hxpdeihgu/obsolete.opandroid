//#include "openpeer/core/IStack.h"
//#include "openpeer/core/IAccount.h"
//#include "openpeer/core/IIdentity.h"
//#include "openpeer/core/IIdentityLookup.h"
//#include "openpeer/core/IConversationThread.h"
//#include "openpeer/core/ICall.h"
//#include "openpeer/core/IMediaEngine.h"
//#include "openpeer/core/ILogger.h"
#include <vector>
#include "globals.h"
//#define NULL ((void*) 0)

#ifndef _ANDROID_OPENPEER_CORE_MANAGER_H_
#define _ANDROID_OPENPEER_CORE_MANAGER_H_

using namespace openpeer::core;

class OpenPeerCoreManager {
public:

	static jobject getJavaEnumObject(String enumClassName, jint index);
	static jint getIntValueFromEnumObject(jobject enumObject, String enumClassName);

	static String getObjectClassName (jobject delegate);

	static void fillJavaTokenFromCoreObject(jobject javaToken, IIdentity::Token coreToken);

	//push messaging helper methods

	//Push info related helper methods
	static IPushMessaging::PushInfo pushInfoToCore(jobject javaPushInfo);
	static jobject pushInfoToJava(IPushMessaging::PushInfo corePushInfo);
	static IPushMessaging::PushInfoList pushInfoListToCore(jobject javaPushInfoList);
	static jobject pushInfoListToJava(IPushMessaging::PushInfoList corePushInfoList);

	static IPushMessaging::PushStateContactDetail pushStateContactDetailToCore(jobject javaPushStateContactDetail);
	static jobject pushStateContactDetailToJava(IPushMessaging::PushStateContactDetail corePushStateContactDetail);
	static IPushMessaging::PushStateContactDetailList pushStateContactDetailListToCore(jobject javaPushStateContactDetailList);
	static jobject pushStateContactDetailListToJava(IPushMessaging::PushStateContactDetailList);

	static IPushMessaging::PushStateDetailMap pushStateDetailMapToCore(jobject javaPushStateDetailMap);
	static jobject pushStateDetailMapToJava(IPushMessaging::PushStateDetailMap);

	static IPushMessaging::PushMessageList pushMessageListToCore(jobject javaPushMessageList);
	static jobject pushMessageListToJava(IPushMessaging::PushMessageList);



public:
	static IStackMessageQueuePtr queuePtr;
	static ISettingsPtr settingsPtr;
	static ICachePtr cachePtr;

};

#endif //_ANDROID_OPENPEER_CORE_MANAGER_H_
