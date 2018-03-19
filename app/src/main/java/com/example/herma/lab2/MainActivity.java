package com.example.herma.lab2;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    ListView listRss;
    ProgressBar loading;
    static ArrayList<String> titles;
    static ArrayList<String> links;
    static ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titles = new ArrayList<String>();
        links = new ArrayList<String>();
        listRss = (ListView) findViewById(R.id.listRss);

        listRss.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                String url = links.get(i);
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("link", url);
                startActivity(intent);
            }
        });


        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, MainActivity.titles);
        listRss.setAdapter(adapter);
        updateLists(MainActivity.this);

        loading = (ProgressBar) findViewById(R.id.loadingSymbol);
        loading.setVisibility(View.GONE);

        Intent intent = new Intent(this, BackgroundUpdate.class);
        startService(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            updateLists(MainActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLists(MainActivity.this);
    }

    public static void updateLists(Context context)
    {
        titles.clear();
        links.clear();
        new ReadRss(context).execute();
        adapter.notifyDataSetChanged();
    }

    public void goToSettings(View view)
    {
        Intent toSettings = new Intent(this, SettingsActivity.class);
        final int result = 1;
        startActivityForResult(toSettings, result);
    }
}
