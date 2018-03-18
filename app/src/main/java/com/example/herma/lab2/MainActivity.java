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
    ArrayList<String> titles;
    ArrayList<String> links;

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

        loading = (ProgressBar) findViewById(R.id.loadingSymbol);
        loading.setVisibility(View.GONE);

        new ReadRss().execute();

        Intent intent = new Intent(this, BackgroundUpdate.class);
        startService(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            titles = new ArrayList<String>();
            links = new ArrayList<String>();
            new ReadRss().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        titles = new ArrayList<String>();
        links = new ArrayList<String>();
        new ReadRss().execute();
    }

    public class ReadRss extends AsyncTask<Void,Void,Exception>
    {
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Exception doInBackground(Void... voids)
        {
            try
            {
                Context context = MainActivity.this;
                SharedPreferences sharedPref = context.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                String urlString = sharedPref.getString(getString(R.string.text_preference_url_key), "http://feed.androidauthority.com/");
                int maxItemsFetched = sharedPref.getInt(getString(R.string.text_preference_num_key), 10);

                if(!urlString.startsWith("http://") && !urlString.startsWith("https://"))
                {
                    urlString = "http://" + urlString;
                }

                URL url = new URL(urlString);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

                boolean insideItem = false;
                int eventType = xpp.getEventType();
                int itemsFetched = 0;

                while (eventType != XmlPullParser.END_DOCUMENT && itemsFetched < maxItemsFetched)
                {
                    if (eventType == XmlPullParser.START_TAG)
                    {
                        if(xpp.getName().equalsIgnoreCase("item"))
                        {
                            insideItem = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("title"))
                        {
                            if (insideItem)
                            {
                                titles.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link"))
                        {
                            if (insideItem)
                            {
                                links.add(xpp.nextText());
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
                    {
                        insideItem = false;
                        ++itemsFetched;
                    }
                    eventType = xpp.next();
                }
            }
            catch(MalformedURLException e)
            {
                exception = e;
            }
            catch(XmlPullParserException e)
            {
                exception = e;
            }
            catch(IOException e)
            {
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception aVoid) {
            super.onPostExecute(aVoid);
            loading.setVisibility(View.GONE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, titles);
            listRss.setAdapter(adapter);
        }
    }

    public class BackgroundUpdate extends IntentService
    {
        public BackgroundUpdate(String name) {
            super(name);
        }

        public BackgroundUpdate() {
            super("DefaultName");
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(runnable);
        }

        public Context context = null;
        public Handler handler = null;
        public Runnable runnable = null;
        int interval = 0;

        @Override
        public void onCreate() {
            context = this;
            SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            int intervalChoice = sharedPreferences.getInt(getString(R.string.text_preference_interval), 0);

            switch (intervalChoice)
            {
                case 0:
                {
                    //interval = 600000;      // 10 minutes in milliseconds
                    interval = 10000;       //  10 seconds, for testing;
                    break;
                }
                case 1:
                {
                    interval = 3600000;     //  1 hour in milliseconds
                    break;
                }
                case 2:
                {
                    interval = 3600000 * 24;    //  24 hours in milliseconds
                    break;
                }
            }

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    updateLists();
                    handler.postDelayed(runnable, interval);
                }
            };
            new Thread(runnable).start();
        }

        public void updateLists()
        {
            titles = new ArrayList<String>();
            links = new ArrayList<String>();
            new ReadRss().execute();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            //  Literally do nothing
        }
    }

    public void goToSettings(View view)
    {
        Intent toSettings = new Intent(this, SettingsActivity.class);
        final int result = 1;
        startActivityForResult(toSettings, result);
    }
}
