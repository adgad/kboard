package com.adgad.kboard;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class CustomKeysActivity extends ListActivity implements AddWordDialogFragment.AddWordDialogListener {


    private ArrayList<String> keys;
    private SharedPreferences sharedPref;
    private final Gson gson = new Gson();
    private ArrayAdapter<String> listAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CustomKeysActivity() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_view);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultJson = gson.toJson(KboardIME.Keys.getDefault());
        String keysAsString = sharedPref.getString(KboardIME.Keys.STORAGE_KEY, defaultJson);
        keys = gson.fromJson(keysAsString, ArrayList.class);

        listAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, keys);
        // TODO: Change Adapter to display your content
        setListAdapter(listAdapter);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.myFab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAddDialog(-1, null);
            }
        });

    }


    private void showAddDialog(int index, String word) {
        DialogFragment newFragment = new AddWordDialogFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putString("word", word);
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "new_word");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        showAddDialog(position, keys.get(position));
    }

    private void updateWords() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KboardIME.Keys.STORAGE_KEY, gson.toJson(keys));
        editor.commit();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int index) {
        TextView t = (TextView) dialog.getDialog().findViewById(R.id.word);
        if(index > 0) {
            keys.set(index, t.getText().toString());
        } else {
            keys.add(t.getText().toString());

        }
        listAdapter.notifyDataSetChanged();
        updateWords();
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, int index) {
        if(index > 0) {
            keys.remove(index);
            listAdapter.notifyDataSetChanged();
            updateWords();
        }
        dialog.dismiss();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reset:
                keys.clear();
                for(String key: KboardIME.Keys.getDefault()) {
                    keys.add(key);
                }
                listAdapter.notifyDataSetChanged();
                updateWords();
                Toast toast = Toast.makeText(this.getBaseContext(), "Reset keys to defaults!", Toast.LENGTH_SHORT);
                toast.show();
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

}
