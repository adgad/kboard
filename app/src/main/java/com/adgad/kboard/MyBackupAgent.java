package com.adgad.kboard;


import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class MyBackupAgent extends BackupAgentHelper {

    // The name of the SharedPreferences file
    private static final String PREFS = "com.adgad.kboard_preferences";

    // A key to uniquely identify the set of backup data
    private static final String PREFS_BACKUP_KEY = "mybackup";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
    }

}
