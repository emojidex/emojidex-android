package org.genshin.emojidexandroid;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nazuki on 2014/03/26.
 */
public class AsyncHttpRequestForGetImage extends AsyncTask<Uri.Builder, Void, Drawable> {
    private String uri;

    public AsyncHttpRequestForGetImage(String uri) {
        super();
        this.uri = uri;
    }

    @Override
    protected Drawable doInBackground(Uri.Builder... params) {
        Drawable result = null;

        try
        {
            URL url = new URL(uri);
            InputStream is = url.openStream();
            result = Drawable.createFromStream(is, "pngImg");
            is.close();
        }
        catch (MalformedURLException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        return result;
    }

    @Override
    protected void onPostExecute(Drawable result)
    {
        super.onPostExecute(result);
    }
}
