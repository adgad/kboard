package com.adgad.kboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by arjun on 14/03/15.
 */


public class PrefsActivity extends PreferenceActivity {
    /**
     * Adds intent extras so fragment opens
     */


    @Override
    protected boolean isValidFragment (String fragmentName) {
        return SettingsFragment.class.getName().equals("com.adgad.kboard.PrefsActivity$SettingsFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reset:
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
                pref.edit().clear().commit();
                PreferenceManager.setDefaultValues(PrefsActivity.this, R.xml.prefs, true);
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SettingsFragment())
                        .commit();
                Toast toast = Toast.makeText(this.getBaseContext(), "Reset to defaults!", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            case R.id.macro_help:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adgad/kboard/wiki"));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.prefs_menu, menu);
        return true;
    }


    public static class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final Gson gson = new Gson();
        SharedPreferences prefs = null;
        private final int EXPORT_REQUEST_CODE = 1;
        private final int IMPORT_REQUEST_CODE = 2;



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs);
            initSummary(getPreferenceScreen());

            Preference importKeys = (Preference) findPreference("importKeys");

            importKeys.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    startActivityForResult(intent, IMPORT_REQUEST_CODE);
                    return true;
                }
            });

            Preference exportKeys = (Preference) findPreference("exportKeys");

            exportKeys.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, "kboard-keys.json");
                    startActivityForResult(intent, EXPORT_REQUEST_CODE);
                    return true;
                }

            });




        }

        public String isToString(InputStream inputStream) {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = null;
            try {
                in = new InputStreamReader(inputStream, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            for (; ; ) {
                int rsz = 0;
                try {
                    rsz = in.read(buffer, 0, buffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            OutputStream outputStream = null;
            InputStream inputStream = null;
            if (requestCode == EXPORT_REQUEST_CODE) {

                String currentVals = prefs.getString(KboardIME.Keys.STORAGE_KEY, "");

                // Note: you may use try-with resources if your API is 19+
                try {
                    // InputStream constructor takes File, String (path), or FileDescriptor
                    // data.getData() holds the URI of the path selected by the picker
                    Uri uri = data.getData();
                    outputStream = this.getActivity().getContentResolver().openOutputStream(uri);
                    outputStream.write(currentVals.getBytes());
                    outputStream.close();
                    Toast.makeText(this.getActivity(), "Exported keys to " + uri.getPath(), Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == IMPORT_REQUEST_CODE) {
                try {
                    inputStream =  this.getActivity().getContentResolver().openInputStream(data.getData());
                    String keys = isToString(inputStream);
                    ArrayList<String> keysAsJson = gson.fromJson(keys, ArrayList.class);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KboardIME.Keys.STORAGE_KEY, keys);
                    editor.apply();
                    Toast toast = Toast.makeText(this.getActivity(), "Imported " + keysAsJson.size() + " keys!", Toast.LENGTH_SHORT);
                    toast.show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Unregister the listener whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }


        private void initSummary(Preference p) {
            if (p instanceof PreferenceGroup) {
                PreferenceGroup pGrp = (PreferenceGroup) p;
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                    initSummary(pGrp.getPreference(i));
                }
            } else {
                updatePrefSummary(p);
            }
        }

        private void updatePrefSummary(Preference p) {
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                p.setSummary(editTextPref.getText());
            }
        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            updatePrefSummary(pref);
        }
    }
}
