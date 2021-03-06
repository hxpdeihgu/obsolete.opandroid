/*******************************************************************************
 *
 *  Copyright (c) 2014 , Hookflash Inc.
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those
 *  of the authors and should not be interpreted as representing official policies,
 *  either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
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

public:
	static IStackMessageQueuePtr queuePtr;
	static ISettingsPtr settingsPtr;
	static ICachePtr cachePtr;

};

#endif //_ANDROID_OPENPEER_CORE_MANAGER_H_
