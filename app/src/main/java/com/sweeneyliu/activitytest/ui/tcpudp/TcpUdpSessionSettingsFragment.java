package com.sweeneyliu.activitytest.ui.tcpudp;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.sweeneyliu.activitytest.R;

public class TcpUdpSessionSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}