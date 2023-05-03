package com.adgad.kboard;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class CustomKeysActivity extends Activity implements AddWordDialogFragment.AddWordDialogListener {


    private final Gson gson = new Gson();
    private RecyclerListAdapter adapter;

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultJson = gson.toJson(KboardIME.Keys.getDefault());
        String keysAsString = sharedPref.getString(KboardIME.Keys.STORAGE_KEY, defaultJson);
        ArrayList<String> keys = gson.fromJson(keysAsString, ArrayList.class);

        RecyclerView recyclerView =  findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecyclerListAdapter(keys, sharedPref, new ItemViewHolder.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showAddDialog(position, adapter.get(position));
            }
        });

        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();

                        adapter.swap(fromPos, toPos);
                        return true;// true if moved, false otherwise
                    }
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        adapter.remove(fromPos);
                    }
                });


        recyclerView.setAdapter(adapter);
        mIth.attachToRecyclerView(recyclerView);
        FloatingActionButton myFab = findViewById(R.id.myFab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAddDialog(-1, null);
            }
        });

    }


    private void showAddDialog(int index, String word) {
        final int keyCount = adapter.getItemCount();

        DialogFragment newFragment = new AddWordDialogFragment();
        Bundle args = new Bundle();
        args.putInt("keyCount", keyCount);
        args.putInt("index", index);
        args.putString("word", word);
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "new_word");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int index) {
        TextView t = dialog.getDialog().findViewById(R.id.word);
        String text = t.getText().toString();
        if(text.length() > 0) {
            if (index >= 0) {
                adapter.set(index, t.getText().toString());
            } else {
                adapter.add(t.getText().toString());

            }
        }
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, int index) {
        if(index >= 0) {
            adapter.remove(index);
        }
        dialog.dismiss();
    }

    @Override
    public void onDialogNeutralClick(int index) {
        if(index > 0) {
            adapter.swap(index, index-1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reset:
                adapter.clear();
                adapter.addAll(KboardIME.Keys.getDefault());
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
