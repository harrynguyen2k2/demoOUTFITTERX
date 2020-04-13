package com.harry.demooutfitterx.Settings;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.harry.demooutfitterx.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    private SwitchPreferenceCompat switchNoti;
    private Preference listNotiSound;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        switchNoti = findPreference("switchNotification");
        listNotiSound = findPreference("listNotificationSound");

        ///////// [BEGIN  INITIALIZE]

        /// Get settings data
        /// To know more: https://developer.android.com/guide/topics/ui/settings/use-saved-values
        SharedPreferences settingsData = PreferenceManager.getDefaultSharedPreferences(getContext());

        /// Set visible for list of notification's sound depend on switchNotification's condition
        if (settingsData.getBoolean(getString(R.string.NOTIFICATION_SWITCH_PREF), true)) {
            listNotiSound.setVisible(true);
            Log.w(TAG, "onSharedPreferenceChanged: TRUE");
        } else {
            listNotiSound.setVisible(false);
            Log.w(TAG, "onSharedPreferenceChanged: FALSE");
        }

        ///////// [END INITIALIZE]

        /// Set visible for list of notification's when change switchNotification's condition
        switchNoti.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    listNotiSound.setVisible(true);
                    Log.w(TAG, "onSharedPreferenceChanged: TRUE");
                } else {
                    listNotiSound.setVisible(false);
                    Log.w(TAG, "onSharedPreferenceChanged: FLASE");
                }

                return true;
            }
        });

        /// Click
        listNotiSound.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                /// Click to know =))
                handleRingtoneSettings();

                return true;
            }
        });
    }

    /// Variables for handleRingtoneSettings()
    private int savedPos, newPos;
    private SharedPreferences sharedPreferences;

    /// Use for show Ringtone's list dialog and handle change ringtone
    private void handleRingtoneSettings() {
        /// Array of sounds' name
        final String[] displayName = getActivity().getResources().getStringArray(R.array.soundsName);
        /// Array of Channels' ID
        final String[] channelID = getActivity().getResources().getStringArray(R.array.channelID);
        /// Array of sounds' raw ID
        final int[] rawID = {R.raw.pikachuuuuuuu, R.raw.mario_power_up, R.raw.message_tone, R.raw.sneeze, R.raw.tuturu};

        /// Declare AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /// SharedPreference time =))
        /// Get saved ID of sound which user has chosen
        sharedPreferences = getContext().getSharedPreferences(
                getString(R.string.PREFERENCE_FILE_KEY),
                Context.MODE_PRIVATE
        );

        savedPos = newPos = sharedPreferences.getInt(getString(R.string.RINGTONE_PREFERENCE_KEY), 0);

        /// Creating dialog's properties
        builder.setTitle("Select your notification's sound")
                .setSingleChoiceItems(displayName, savedPos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.w(TAG, "onClick: " + i);

                        /// Get URI of tapped sound
                        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + '/' + rawID[i]);

                        /// Play sound
                        MediaPlayer mediaPlayer=MediaPlayer.create(getContext(), uri);
                        mediaPlayer.start();

                        /// Update position
                        newPos = i;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /// Check if user actually changed the notification's sound
                        if (newPos != savedPos) {
                            Log.w(TAG, "User selected: " + newPos);

                            /// Check user's android version
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                /// get Notification Manager
                                NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);

                                /// Delete old notification channel
                                notificationManager.deleteNotificationChannel(channelID[savedPos]);

                                /// Create new
                                /// Actual, just a fake notification declare as a chosen ones
                                NotificationChannel notificationChannel = new NotificationChannel(
                                        channelID[newPos],
                                        getString(R.string.CHANNEL_DEFAULT_NAME),
                                        NotificationManager.IMPORTANCE_HIGH
                                );

                                // Register the channel with the system
                                notificationManager.createNotificationChannel(notificationChannel);
                            }

                            /// Change ringtonePreference data
                            sharedPreferences.edit().putInt(getString(R.string.RINGTONE_PREFERENCE_KEY), newPos).apply();

                            /// Toast to inform user
                            Toast.makeText(
                                    getContext(),
                                    "Notification's sound changed to: " + displayName[newPos],
                                    Toast.LENGTH_LONG).show();
                        }

                        /// Dismiss dialog
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /// Just dismiss
                        dialogInterface.dismiss();
                    }
                });

        /// Show dialog
        builder.create().show();
    }
}
