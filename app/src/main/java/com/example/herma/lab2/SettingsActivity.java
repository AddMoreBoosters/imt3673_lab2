package com.example.herma.lab2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by herma on 17.03.2018.
 */

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ArrayList<String> updateOptions = new ArrayList<String>();
        updateOptions.add(getString(R.string.text_update_10_minutes));
        updateOptions.add(getString(R.string.text_update_1_hour));
        updateOptions.add(getString(R.string.text_update_24_hours));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, updateOptions);

        ListView updateOptionsList = (ListView) findViewById(R.id.list_update_options);
        updateOptionsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        updateOptionsList.setAdapter(adapter);
    }

    public void onConfirmSettings(View view) {
        EditText textUrl = (EditText) findViewById(R.id.text_edit_url);
        EditText numItems = (EditText) findViewById(R.id.text_edit_num_items);
        ListView updateOptions = (ListView) findViewById(R.id.list_update_options);

        String url = String.valueOf(textUrl.getText());
        int itemsToDisplay = Integer.parseInt(String.valueOf(numItems.getText()));
        int updateInterval = updateOptions.getCheckedItemPosition();

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (!url.isEmpty())
        {
            editor.putString(getString(R.string.text_preference_url_key), url);
        }
        if (updateInterval != -1)
        {
            editor.putInt(getString(R.string.text_preference_interval), updateInterval);
        }
        editor.putInt(getString(R.string.text_preference_num_key), itemsToDisplay);
        editor.apply();

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
