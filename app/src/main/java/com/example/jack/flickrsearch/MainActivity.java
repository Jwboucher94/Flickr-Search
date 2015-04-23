package com.example.jack.flickrsearch;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;


public class MainActivity extends ActionBarActivity {

    private static final String SEARCHES = "searches";

    private EditText queryEditText;
    private EditText tagEditText;
    private SharedPreferences savedSearches;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queryEditText =
                (EditText) findViewById(R.id.queryEditText);
        tagEditText =
                (EditText) findViewById(R.id.tagEditText);

        savedSearches =
                getSharedPreferences(SEARCHES, MODE_PRIVATE);

        tags = new ArrayList<String>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags);
        ListView lv = (ListView)findViewById(R.id.list);
        lv.setAdapter(adapter);

        ImageButton saveButton =
                (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        getListView().setOnItemClickListener(itemClickListener);


        getListView().setOnItemLongClickListener(itemLongClickListener);
    }

    public OnClickListener saveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (queryEditText.getText().length() > 0 &&
                    tagEditText.getText().length() > 0)
            {
                addTaggedSearch(queryEditText.getText().toString(),
                        tagEditText.getText().toString());
                queryEditText.setText("");
                tagEditText.setText("");
            }
            else
            {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(R.string.missingMessage);

                builder.setPositiveButton(R.string.OK, null);

                AlertDialog errorDialog = builder.create();
                errorDialog.show();
            }
        }
    };

    private void addTaggedSearch(String query, String tag)
    {
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply();

        if (!tags.contains(tag))
        {
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String tag = ((TextView) view).getText().toString();
            String urlString = getString(R.string.searchURL) +
                    Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(urlString));

            startActivity(webIntent);
        }
    };

    OnItemLongClickListener itemLongClickListener =
            new OnItemLongClickListener()
            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
                {
                    final String tag = ((TextView) view).getText().toString();

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle(
                            getString(R.string.shareEditDeleteTitle, tag));

                    builder.setItems(R.array.dialog_items,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    switch (which)
                                    {
                                        case 0:
                                            shareSearch(tag);
                                            break;
                                        case 1:
                                            tagEditText.setText(tag);
                                            queryEditText.setText(
                                                    savedSearches.getString(tag, ""));
                                            break;
                                        case 2:
                                            deleteSearch(tag);
                                            break;

                                    }
                                }
                            }
                    );

                    builder.setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.cancel();
                                }
                            }
                    );

                    builder.create().show();
                    return true;
                }

            };

    private void shareSearch(String tag)
    {
        String urlString = getString(R.string.searchURL) +
                Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.shareSubject));
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.shareMessage, urlString));
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent,
                getString(R.string.shareSearch)));
    }

    private void deleteSearch(final String tag)
    {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);

        confirmBuilder.setMessage(
                getString(R.string.confirmMessage, tag));

        confirmBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                }
        );

        confirmBuilder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        tags.remove(tag);

                        SharedPreferences.Editor preferencesEditor =
                                savedSearches.edit();
                        preferencesEditor.remove(tag);
                        preferencesEditor.apply();

                        adapter.notifyDataSetChanged();
                    }
                }
        );

        confirmBuilder.create().show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public AdapterView getListView() {
        return (ListView)findViewById(R.id.list);
    }
}
