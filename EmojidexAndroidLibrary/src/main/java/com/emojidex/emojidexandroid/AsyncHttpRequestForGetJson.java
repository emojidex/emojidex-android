package com.emojidex.emojidexandroid;

import android.net.Uri;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by nazuki on 2014/03/26.
 */
public class AsyncHttpRequestForGetJson extends AsyncTask<Uri.Builder, Void, String> {
    private String uri;

    public AsyncHttpRequestForGetJson(String uri) {
        super();
        this.uri = uri;
    }

    @Override
    protected String doInBackground(Uri.Builder... params) {
        String result = null;

        HttpClient httpClient = new DefaultHttpClient();
        HttpUriRequest httpRequest = new HttpGet(uri);
        HttpResponse httpResponse = null;

        try
        {
            httpResponse = httpClient.execute(httpRequest);
        }
        catch (ClientProtocolException e) { e.printStackTrace(); }
        catch (IOException e){ e.printStackTrace(); }

        if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            HttpEntity httpEntity = httpResponse.getEntity();
            try
            {
                result = EntityUtils.toString(httpEntity);
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);
    }
}
