package com.openpeer.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.view.MenuItem;

import com.openpeer.javaapi.OPLogLevel;
import com.openpeer.javaapi.OPLogger;
import com.openpeer.sample.util.SettingsHelper;
import com.openpeer.sdk.app.OPHelper;

public class SettingsActivity extends Activity {
	static final String KEY_OUT_TELNET_LOGGER = "out_telnet_logger";
	static final String KEY_LOCAL_TELNET_LOGGER = "local_telnet_logger";
	static final String KEY_FILE_LOGGER = "file_logger";
	static final String KEY_LOG_LEVEL = "log_level";
	static final String KEY_OUT_LOG_SERVER = "log_server_url";
	static final String KEY_FILE_LOGGER_PATH = "log_file";
	static final String KEY_RINGTONE = "ringtone";
    static final String KEY_NOTIFICATION_SOUND_SWITCH = "notification_sound_switch";
    static final String KEY_NOTIFICATION_SOUND_SELECT = "notification_sound_select";


    public static void launch(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {

		ListPreference logLevelPref;
		EditTextPreference logServerPref;
		EditTextPreference logFilePref;
		RingtonePreference ringtonePref;
        RingtonePreference notificationSoundPref;


        @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preference);
			SwitchPreference outTelnetLogging = (SwitchPreference) findPreference(KEY_OUT_TELNET_LOGGER);
			SwitchPreference localTelnetLogging = (SwitchPreference) findPreference(KEY_LOCAL_TELNET_LOGGER);
			SwitchPreference fileLogging = (SwitchPreference) findPreference(KEY_FILE_LOGGER);
            SwitchPreference notificationSoundSwitchPref = (SwitchPreference) findPreference(KEY_NOTIFICATION_SOUND_SWITCH);

			logLevelPref = (ListPreference) findPreference(KEY_LOG_LEVEL);
			logServerPref = (EditTextPreference) findPreference(KEY_OUT_LOG_SERVER);
			logFilePref = (EditTextPreference) findPreference(KEY_FILE_LOGGER_PATH);

			outTelnetLogging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String url = logServerPref.getText();
					OPHelper.getInstance().toggleOutgoingTelnetLogging(((SwitchPreference) preference).isChecked(), url);
					return true;
				}
			});
			localTelnetLogging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					OPHelper.getInstance().toggleTelnetLogging(((SwitchPreference) preference).isChecked(), 59999);
					return true;
				}
			});
			fileLogging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String fileName = logFilePref.getText();
					OPHelper.getInstance().toggleOutgoingTelnetLogging(((SwitchPreference) preference).isChecked(), fileName);
					return true;
				}
			});
			logLevelPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int intValue = Integer.parseInt(((ListPreference) preference).getValue());
					OPLogLevel level = OPLogLevel.values()[intValue];
					OPLogger.setLogLevel(level);
					return true;
				}
			});
            notificationSoundPref=(RingtonePreference) findPreference(KEY_NOTIFICATION_SOUND_SELECT);
			ringtonePref = (RingtonePreference) findPreference(KEY_RINGTONE);

			setupAboutInfo();
		}

		private static final String KEY_VERSION = "version";
		private static final String KEY_BUILD = "build";

		void setupAboutInfo() {
			Preference versionPref = findPreference(KEY_VERSION);
			Preference buildPref = findPreference(KEY_BUILD);
			PackageInfo pInfo;
			try {
				pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
				String version = pInfo.versionName;
				String versionCode = "" + pInfo.versionCode;
				versionPref.setSummary(version);
				buildPref.setSummary(versionCode);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		void setupSettingDisplays() {
			logLevelPref.setTitle("Log level       " + logLevelPref.getEntry());
			logServerPref.setTitle("Log server:  " + logServerPref.getText());
			logFilePref.setTitle("Log file:  " + logFilePref.getText());
            Ringtone ringtone = SettingsHelper.getRingtone();
            ringtonePref.setSummary(ringtone.getTitle(getActivity()));
            Uri notificationSound = SettingsHelper.getNotificationSound();
            if(notificationSound!=null) {
                notificationSoundPref.setSummary(RingtoneManager.getRingtone(getActivity(),notificationSound).getTitle(getActivity()));
            } else {
                notificationSoundPref.setSummary("None");
            }
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			setupSettingDisplays();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
