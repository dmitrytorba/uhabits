/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.activities.settings;

import android.app.backup.*;
import android.content.*;
import android.os.*;
import android.provider.*;
import android.support.v7.preference.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.notifications.*;
import org.isoron.uhabits.utils.*;

import static android.os.Build.VERSION.*;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static int RINGTONE_REQUEST_CODE = 1;

    private SharedPreferences prefs;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RINGTONE_REQUEST_CODE)
        {
            RingtoneUtils.parseRingtoneData(getContext(), data);
            updateRingtoneDescription();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        setResultOnPreferenceClick("importData", ListHabitsScreen.RESULT_IMPORT_DATA);
        setResultOnPreferenceClick("exportCSV", ListHabitsScreen.RESULT_EXPORT_CSV);
        setResultOnPreferenceClick("exportDB", ListHabitsScreen.RESULT_EXPORT_DB);
        setResultOnPreferenceClick("repairDB", ListHabitsScreen.RESULT_REPAIR_DB);
        setResultOnPreferenceClick("bugReport", ListHabitsScreen.RESULT_BUG_REPORT);
        setResultOnPreferenceClick("syncDB", ListHabitsScreen.RESULT_SYNC_DB);

        updateRingtoneDescription();

        if (SDK_INT < Build.VERSION_CODES.O)
            findPreference("reminderCustomize").setVisible(false);
        else
        {
            findPreference("reminderSound").setVisible(false);
            findPreference("pref_snooze_interval").setVisible(false);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s)
    {
        // NOP
    }

    @Override
    public void onPause()
    {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference)
    {
        String key = preference.getKey();
        if (key == null) return false;

        if (key.equals("reminderSound"))
        {
            RingtoneUtils.startRingtonePickerActivity(this,
                RINGTONE_REQUEST_CODE);
            return true;
        }
        else if (key.equals("reminderCustomize"))
        {
            if (SDK_INT < Build.VERSION_CODES.O) return true;

            NotificationTray.createAndroidNotificationChannel(getContext());
            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationTray.REMINDERS_CHANNEL_ID);
            startActivity(intent);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        BackupManager.dataChanged("org.isoron.uhabits");
    }

    private void setResultOnPreferenceClick(String key, final int result)
    {
        Preference pref = findPreference(key);
        pref.setOnPreferenceClickListener(preference -> {
            getActivity().setResult(    result);
            getActivity().finish();
            return true;
        });
    }

    private void updateRingtoneDescription()
    {
        String ringtoneName = RingtoneUtils.getRingtoneName(getContext());
        if (ringtoneName == null) return;
        Preference ringtonePreference = findPreference("reminderSound");
        ringtonePreference.setSummary(ringtoneName);
    }
}