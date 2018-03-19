package com.example.herma.lab2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by herma on 19.03.2018.
 */

public class ReadRss extends AsyncTask<Void,Void,Exception>
{
    private Exception exception = null;
    private Context context;

    public ReadRss(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Exception doInBackground(Void... voids)
    {
        try
        {

            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String urlString = sharedPref.getString(context.getString(R.string.text_preference_url_key), "http://feed.androidauthority.com/");
            int maxItemsFetched = sharedPref.getInt(context.getString(R.string.text_preference_num_key), 10);

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
                            MainActivity.titles.add(xpp.nextText());
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("link"))
                    {
                        if (insideItem)
                        {
                            MainActivity.links.add(xpp.nextText());
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
    }
}
