/*
 * ******************************************************************************
 *  *
 *  *  Copyright (c) 2014 , Hookflash Inc.
 *  *  All rights reserved.
 *  *
 *  *  Redistribution and use in source and binary forms, with or without
 *  *  modification, are permitted provided that the following conditions are met:
 *  *
 *  *  1. Redistributions of source code must retain the above copyright notice, this
 *  *  list of conditions and the following disclaimer.
 *  *  2. Redistributions in binary form must reproduce the above copyright notice,
 *  *  this list of conditions and the following disclaimer in the documentation
 *  *  and/or other materials provided with the distribution.
 *  *
 *  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  *
 *  *  The views and conclusions contained in the software and documentation are those
 *  *  of the authors and should not be interpreted as representing official policies,
 *  *  either expressed or implied, of the FreeBSD Project.
 *  ******************************************************************************
 */

package com.openpeer.sdk.model;

import android.content.Intent;
import android.text.format.Time;

import com.openpeer.javaapi.CallClosedReasons;
import com.openpeer.javaapi.CallStates;
import com.openpeer.javaapi.OPCall;
import com.openpeer.javaapi.OPCallDelegate;
import com.openpeer.javaapi.OPConversationThread;
import com.openpeer.javaapi.OPMessage;
import com.openpeer.javaapi.OPSystemMessage;
import com.openpeer.sdk.app.IntentData;
import com.openpeer.sdk.app.OPDataManager;
import com.openpeer.sdk.app.OPHelper;
import com.openpeer.sdk.utils.OPModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.UUID;

public class CallManager implements OPCallDelegate {

    Hashtable<String, OPCall> mIdToCalls;//peerId to call map
    Hashtable<Long, OPCall> mUserIdToCalls;//peerId to call map

    private Hashtable<Long, CallStatus> mCallStates;
    private CallDelegate delegate;

    private static CallManager instance;

    public static CallManager getInstance() {
        if (instance == null) {
            instance = new CallManager();
        }
        return instance;
    }

    private CallManager() {
    }

    public void registerDelegate(CallDelegate delegate){
        this.delegate = delegate;
    }
    public void unregisterDelegate(CallDelegate delegate){
        this.delegate=null;
    }
    @Override
    public void onCallStateChanged(OPCall call, CallStates state) {
        OPConversationThread thread = call.getConversationThread();
        call.setCbcId(OPModelUtils.getWindowIdForThread(thread));
        OPConversation conversation = ConversationManager.getInstance().getConversation(thread,  true);

        switch (state){
        case CallState_Preparing:{
            //Handle racing condition. SImply hangup the existing call for now.
            OPCall oldCall = findCallForPeer(call.getPeerUser().getUserId());
            if (oldCall != null) {
                call.hangup(CallClosedReasons.CallClosedReason_NotAcceptableHere);
            } else {
                int direction = call.getCaller().isSelf() ? 0 : 1;
                OPDataManager.getInstance().saveCall(
                    call.getCallID(),
                    conversation.getConversationId(),
                    call.getPeerUser().getUserId(),
                    direction,
                    call.hasVideo() ? CallSystemMessage.MEDIATYPE_VIDEO : CallSystemMessage
                        .MEDIATYPE_AUDIO);
                cacheCall(call);
                if (call.getCaller().isSelf()) {
                    OPMessage message = callSystemMessage(
                        CallSystemMessage.STATUS_PLACED,
                        call);
                    conversation.sendMessage(message, false);
                    CallEvent event = new CallEvent(call.getCallID(),
                                                    CallSystemMessage.STATUS_PLACED,
                                                    message.getTime().toMillis(false));
                    OPDataManager.getInstance().saveCallEvent(call.getCallID(),
                                                              conversation.getConversationId(),
                                                              event);
                }
            }
        }
        break;
        case CallState_Placed:{
        }
        break;
        case CallState_Open:{
            if (call.getCaller().isSelf()) {
                OPMessage message = callSystemMessage(
                    CallSystemMessage.STATUS_ANSWERED,
                    call);
                conversation.sendMessage(message, false);
                CallEvent event = new CallEvent(call.getCallID(),
                                                CallSystemMessage.STATUS_ANSWERED,
                                                message.getTime().toMillis(false));
                OPDataManager.getInstance().saveCallEvent(call.getCallID(),
                                                          conversation.getConversationId(),
                                                          event);
            }
        }
        break;

        case CallState_Closed:{
            if (call.getCaller().isSelf()) {
                OPMessage message = callSystemMessage(
                    CallSystemMessage.STATUS_HUNGUP,
                    call);
                conversation.sendMessage(message, false);
                CallEvent event = new CallEvent(call.getCallID(),
                                                CallSystemMessage.STATUS_HUNGUP,
                                                message.getTime().toMillis(false));
                OPDataManager.getInstance().saveCallEvent(call.getCallID(),
                                                          conversation.getConversationId(),
                                                          event);
            }
            removeCallCache(call);
        }
        break;
        default:
            break;

        }

        delegate.onCallStateChanged(call,state);
    }

    private void cacheCall(OPCall call) {
        if (mIdToCalls == null) {
            mIdToCalls = new Hashtable<>();
        }
        mIdToCalls.put(call.getCallID(), call);
        if (mUserIdToCalls == null) {
            mUserIdToCalls = new Hashtable<>();

        }
        mUserIdToCalls.put(call.getPeerUser().getUserId(), call);
    }

    public CallStatus getMediaStateForCall(long userId) {
        CallStatus state = null;
        if (mCallStates == null) {
            mCallStates = new Hashtable<>();

        } else {
            state = mCallStates.get(userId);
        }
        if (state == null) {
            state = new CallStatus();
            mCallStates.put(userId, state);
        }
        return state;

    }

    public void handleCallSystemMessage(JSONObject message, OPUser user, String conversationId,
                                        long timestamp) {
        try {
            String callId = message.getString(CallSystemMessage.KEY_ID);
            OPCall call = findCallById(callId);

            if (call == null) {
                //couldn't find call in memory. try to save call
                OPDataManager.getInstance().saveCall(message.getString(CallSystemMessage.KEY_ID),
                                                     conversationId,
                                                     user.getUserId(),
                                                     OPCall.DIRECTION_INCOMING,
                                                     message.getString(CallSystemMessage
                                                                           .KEY_CALL_STATUS_MEDIA_TYPE));
            }
            CallEvent event = new CallEvent(callId,
                                            message.getString(CallSystemMessage.KEY_CALL_STATUS_STATUS),
                                            timestamp);
            OPDataManager.getInstance().saveCallEvent(callId, conversationId, event);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCalls() {
        return mIdToCalls != null && mIdToCalls.size() > 0;
    }

    private void removeCallCache(OPCall call) {
        long userId = call.getPeerUser().getUserId();
        if (mIdToCalls != null) {
            mIdToCalls.remove(call.getCallID());
            mUserIdToCalls.remove(call.getPeerUser().getUserId());
            if (mCallStates != null) {
                mCallStates.remove(userId);
            }
            if (mIdToCalls.isEmpty()) {
                mIdToCalls = null;
                mUserIdToCalls = null;
                mCallStates = null;
            }
        }
    }

    public OPCall findCallById(String callId) {
        if (mIdToCalls != null) {
            return mIdToCalls.get(callId);
        }
        return null;
    }

    public OPCall findCallForPeer(long userId) {
        if (mIdToCalls == null) {
            return null;
        }

        return mUserIdToCalls.get(userId);
    }

    public OPCall findCallByCbcId(long cbcId) {
        if (mIdToCalls != null) {
            for (OPCall call : mIdToCalls.values()) {
                if (call.getCbcId() == cbcId) {
                    return call;
                }
            }
        }
        return null;
    }

    public static void clearOnSignout() {
        if (instance != null) {
            instance.mIdToCalls = null;
            instance.mUserIdToCalls = null;
            instance.mCallStates = null;
        }
    }

    OPMessage callSystemMessage(String status, OPCall call) {
        int callClosedReason = -1;
        switch (status){
        case CallSystemMessage.STATUS_HUNGUP:
            if (Time.isEpoch(call.getAnswerTime())) {
                callClosedReason = 404;
            } else {
                callClosedReason = call.getClosedReason();
            }
            break;
        }

        String mediaType = call.hasVideo() ? CallSystemMessage.MEDIATYPE_VIDEO :
            CallSystemMessage.MEDIATYPE_AUDIO;
        JSONObject callSystemMessage = SystemMessage.CallSystemMessage(
            call.getCallID(),
            status,
            mediaType,
            call.getCallee().getPeerURI(),
            callClosedReason);

        OPMessage message = new OPMessage(
            OPDataManager.getInstance().getCurrentUserId(),
            OPSystemMessage.getMessageType(),
            callSystemMessage.toString(),
            System.currentTimeMillis(),
            UUID.randomUUID().toString());
        return message;
    }
}
