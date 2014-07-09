package com.openpeer.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;

import com.openpeer.datastore.OPDatastoreDelegate;
import com.openpeer.datastore.OPDatastoreDelegateImplementation;
import com.openpeer.delegates.CallbackHandler;
import com.openpeer.delegates.ContactsBasedMessageDispatcher;
import com.openpeer.delegates.GroupBasedMessageDispatcher;
import com.openpeer.delegates.MessageDispatcher;
import com.openpeer.delegates.MessageReceiver;
import com.openpeer.delegates.OPCacheDelegateImplementation;
import com.openpeer.delegates.OPCallDelegateImplementation;
import com.openpeer.delegates.OPConversationThreadDelegateImplementation;
import com.openpeer.javaapi.OPCache;
import com.openpeer.javaapi.OPCacheDelegate;
import com.openpeer.javaapi.OPConversationThread;
import com.openpeer.javaapi.OPLogLevel;
import com.openpeer.javaapi.OPLogger;
import com.openpeer.javaapi.OPMediaEngine;
import com.openpeer.javaapi.OPMessage;
import com.openpeer.javaapi.OPSettings;
import com.openpeer.javaapi.OPStack;
import com.openpeer.javaapi.OPStackMessageQueue;
import com.openpeer.javaapi.VideoOrientations;

/**
 * 
 * 
 *
 */
public class OPHelper {
	private static final String TAG = OPHelper.class.getSimpleName();
	private static OPHelper instance;
	Context mContext;

	public Context getApplicationContext() {
		return mContext;
	}

	public static OPHelper getInstance() {
		if (instance == null) {
			instance = new OPHelper();
		}
		return instance;
	}

	public void toggleOutgoingTelnetLogging(boolean enable) {
		if (enable) {
			enableAllLogging();

			String deviceId = Secure.getString(mContext.getContentResolver(),
					Secure.ANDROID_ID);
			String instanceId = OPSdkConfig.getInstanceid();
			String telnetLogString = deviceId + "-" + instanceId + "\n";
			Log.d("output", "Outgoing log string = " + telnetLogString);
			OPLogger.installOutgoingTelnetLogger("logs.opp.me:8115", true,
					telnetLogString);
		} else {
			disableLogging();
			OPLogger.uninstallOutgoingTelnetLogger();
		}
	}

	public void enableTelnetLogging() {
		// OPLogger.setLogLevel(OPLogLevel.LogLevel_Trace);
		// OPLogger.setLogLevel("openpeer_webrtc", OPLogLevel.LogLevel_None);
		// OPLogger.setLogLevel("zsLib_socket", OPLogLevel.LogLevel_Insane);
		OPLogger.setLogLevel(OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel("openpeer_webrtc", OPLogLevel.LogLevel_Basic);
		OPLogger.setLogLevel("zsLib_socket", OPLogLevel.LogLevel_Insane);

		String deviceId = Secure.getString(mContext.getContentResolver(),
				Secure.ANDROID_ID);
		String instanceId = OPSdkConfig.getInstanceid();
		String telnetLogString = deviceId + "-" + instanceId + "\n";
		Log.d("output", "Outgoing log string = " + telnetLogString);
		OPLogger.installOutgoingTelnetLogger("logs.opp.me:8115", true,
				telnetLogString);

		// OPLogger.installTelnetLogger(59999, 60, true);
		OPLogger.installFileLogger("/storage/emulated/0/HFLog1.txt", true);
	}

	private void initMediaEngine() {
		long start = SystemClock.uptimeMillis();
		OPMediaEngine.getInstance().setEcEnabled(true);
		OPMediaEngine.getInstance().setAgcEnabled(true);
		OPMediaEngine.getInstance().setNsEnabled(false);
		OPMediaEngine.getInstance().setMuteEnabled(false);
		OPMediaEngine.getInstance().setLoudspeakerEnabled(false);
		OPMediaEngine.getInstance().setContinuousVideoCapture(true);
		OPMediaEngine.getInstance().setDefaultVideoOrientation(
				VideoOrientations.VideoOrientation_Portrait);
		OPMediaEngine.getInstance().setRecordVideoOrientation(
				VideoOrientations.VideoOrientation_LandscapeRight);
		OPMediaEngine.getInstance().setFaceDetection(false);

		Log.d("performance",
				"initMediaEngine time " + (SystemClock.uptimeMillis() - start));
		// OPMediaEngine.init(mContext);
	}

	public void init(Context context, OPDatastoreDelegate datastoreDelegate) {
		long start = SystemClock.uptimeMillis();

		mContext = context;
		enableTelnetLogging();
		// toggleOutgoingTelnetLogging(true);
		OPMediaEngine.init(mContext);

		// initMediaEngine();
		if (datastoreDelegate != null) {
			OPDataManager.getInstance().init(datastoreDelegate);
		} else {
			OPDataManager.getInstance().init(
					OPDatastoreDelegateImplementation.getInstance().init(
							mContext));
		}
		// TODO: Add delegate when implement mechanism to post events to the
		// android GUI thread
		OPStackMessageQueue stackMessageQueue = OPStackMessageQueue.singleton();
		// stackMessageQueue = new OPStackMessageQueue();
		// stackMessageQueue.interceptProcessing(null);
		OPStack stack = OPStack.singleton();
		OPSdkConfig.getInstance().init(mContext);

		//
		OPCacheDelegate cacheDelegate = OPCacheDelegateImplementation
				.getInstance(mContext);
		CallbackHandler.getInstance().registerCacheDelegate(cacheDelegate);
		OPCache.setup(cacheDelegate);

		OPSettings.applyDefaults();

		String httpSettings = createHttpSettings();
		OPSettings.apply(httpSettings);

		String forceDashSettings = createForceDashSetting();
		OPSettings.apply(forceDashSettings);

		OPSettings.apply(OPSdkConfig.getInstance().getAPPSettingsString());

		CallbackHandler.getInstance().registerConversationThreadDelegate(
				new OPConversationThreadDelegateImplementation());
		CallbackHandler.getInstance().registerCallDelegate(null,
				new OPCallDelegateImplementation());

		stack.setup(null, null);
		// initMediaEngine();
		// this.toggleOutgoingTelnetLogging(true);
		Log.d("performance",
				"OPHelper init time " + (SystemClock.uptimeMillis() - start));

	}

	private String createHttpSettings() {
		try {
			JSONObject parent = new JSONObject();
			JSONObject jsonObject = new JSONObject();

			jsonObject
					.put("openpeer/stack/bootstrapper-force-well-known-over-insecure-http",
							"true");
			jsonObject.put(
					"openpeer/stack/bootstrapper-force-well-known-using-post",
					"true");
			parent.put("root", jsonObject);
			Log.d("output", parent.toString(2));
			return parent.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	private String createForceDashSetting() {
		try {
			JSONObject parent = new JSONObject();
			JSONObject jsonObject = new JSONObject();

			jsonObject.put(
					"openpeer/core/authorized-application-id-split-char", "-");
			parent.put("root", jsonObject);
			Log.d("output", parent.toString(2));
			return parent.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	public void sendBroadcast(Intent intent) {
		mContext.sendBroadcast(intent);
	}

	public static final int MODE_CONTACTS_BASED = 0;
	public static final int MODE_GROUP_BASED = 1;

	public void setChatGroupMode(int mode) {
		if (mode == MODE_CONTACTS_BASED) {
			mDispatcher = new ContactsBasedMessageDispatcher();
		} else {
			mDispatcher = new GroupBasedMessageDispatcher();

		}
	}

	private MessageDispatcher mDispatcher;
	private boolean mAppInBackground;

	public boolean isAppInBackground() {
		return mAppInBackground;
	}

	public void dispatchMessage(OPConversationThread thread, OPMessage message) {
		mDispatcher.dispatch(thread, message);
	}

	public void registerMessageReceiver(MessageReceiver receiver) {

	}

	public void onEnteringForeground() {
		mAppInBackground = false;
		OPCallManager.getInstance().onEnteringForeground();
	}

	public void onEnteringBackground() {
		mAppInBackground = true;
		OPCallManager.getInstance().onEnteringForeground();
	}

	public void enableAllLogging() {
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_APPLICATION.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_WIRE.ordinal(),
				OPLogLevel.LogLevel_Debug);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_ICE.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_TURN.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_RUDP.ordinal(),
				OPLogLevel.LogLevel_Debug);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_HTTP.ordinal(),
				OPLogLevel.LogLevel_Debug);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_MLS.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_TCP.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(
				OPCoreLogModules.MODULE_SERVICES_TRANSPORT.ordinal(),
				OPLogLevel.LogLevel_Debug);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_CORE.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_STACK.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_ZSLIB.ordinal(),
				OPLogLevel.LogLevel_Trace);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_ZSLIB_SOCKET.ordinal(),
				OPLogLevel.LogLevel_Debug);
		OPLogger.setLogLevel(OPCoreLogModules.MODEULE_SDK.ordinal(),
				OPLogLevel.LogLevel_Trace);

		OPLogger.setLogLevel(OPCoreLogModules.MODULE_WEBRTC.ordinal(),
				OPLogLevel.LogLevel_Detail);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_MEDIA.ordinal(),
				OPLogLevel.LogLevel_Detail);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_JAVASCRIPT.ordinal(),
				OPLogLevel.LogLevel_Trace);
	}

	void disableLogging() {
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_APPLICATION.ordinal(),
				OPLogLevel.LogLevel_None);

		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_WIRE.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_ICE.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_TURN.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_RUDP.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_HTTP.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_MLS.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_SERVICES_TCP.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(
				OPCoreLogModules.MODULE_SERVICES_TRANSPORT.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_CORE.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_STACK.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_ZSLIB.ordinal(),
				OPLogLevel.LogLevel_None);
		// OPLogger.setLogLevel(OPCoreLogModules.MODULE_ZSLIB_SOCKET.ordinal(),OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODEULE_SDK.ordinal(),
				OPLogLevel.LogLevel_None);

		OPLogger.setLogLevel(OPCoreLogModules.MODULE_WEBRTC.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_MEDIA.ordinal(),
				OPLogLevel.LogLevel_None);
		OPLogger.setLogLevel(OPCoreLogModules.MODULE_JAVASCRIPT.ordinal(),
				OPLogLevel.LogLevel_None);
	}

}
