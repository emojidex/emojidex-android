package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Service.User;
import com.emojidex.libemojidex.HistoryItemVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kou on 16/04/12.
 */
public class HistoryManager extends SaveDataManager
{
    static final String TAG = MainActivity.TAG + "::HistoryManager";

    private static HistoryManager instance;

    private ThreadTask task;
    private List<String> emojiNames = new ArrayList<String>();

    public static HistoryManager getInstance(Context context)
    {
        if(instance == null)
            instance = new HistoryManager(context);
        return instance;
    }

    private HistoryManager(Context context)
    {
        super(context, Type.History);
    }

    public void loadFromUser()
    {
        final UserData userdata =  UserData.getInstance();
        if(     !isNetworkEnabled()
            ||  !userdata.isLogined()
            ||  (task != null && task.getStatus() == AsyncTask.Status.RUNNING)  )
        {
            Log.d(TAG, "Skip load user history.");
            return;
        }

        task = new ThreadTask();
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Runnable(){
            @Override
            public void run()
            {
                final String username = userdata.getUsername();
                final String authtoken = userdata.getAuthToken();
                final Client client = new Client();
                final User user = client.getUser();

                if( !user.authorize(username, authtoken) )
                    return;

                final HistoryItemVector history = user.syncHistory(1, getCapacity());

                clear();
                final long size = history.size();
                for(int i = 0;  i < size;  ++i)
                    addLast(history.get(i).getEmoji_code());
                Log.d(TAG, "Load user history.");
            }
        });
    }

    public void saveToUser()
    {
        final UserData userdata =  UserData.getInstance();
        if(     !isNetworkEnabled()
            ||  !userdata.isLogined()
            ||  (task != null && task.getStatus() == AsyncTask.Status.RUNNING)
            ||  emojiNames.isEmpty()    )
        {
            Log.d(TAG, "Skip save user history.");
            return;
        }

        final List<String> updateEmojies = new ArrayList<String>(emojiNames);
        emojiNames.clear();

        task = new ThreadTask();
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Runnable(){
            @Override
            public void run()
            {
                final String username = userdata.getUsername();
                final String authtoken = userdata.getAuthToken();
                final Client client = new Client();
                final User user = client.getUser();

                if( !user.authorize(username, authtoken) )
                    return;

                final Iterator<String> it = updateEmojies.iterator();
                while(it.hasNext())
                {
                    final String emojiName = it.next();
                    if(user.addHistory(emojiName))
                        it.remove();
                }

                for(int i = updateEmojies.size()-1;  i >= 0;  --i)
                    emojiNames.add(updateEmojies.get(i));

                Log.d(TAG, "Save user history.");
            }
        });
    }

    @Override
    public void load()
    {
        super.load();
    }

    @Override
    public void save()
    {
        super.save();
        saveToUser();
    }

    @Override
    public void addFirst(String emojiName)
    {
        super.addFirst(emojiName);

        if(UserData.getInstance().isLogined())
            emojiNames.add(emojiName);
    }

    @Override
    public void addLast(String emojiName)
    {
        super.addLast(emojiName);
    }

    @Override
    public void remove(String emojiName)
    {
        super.remove(emojiName);
    }

    @Override
    public void clear()
    {
        super.clear();
    }

    @Override
    public boolean deleteFile()
    {
        return super.deleteFile();
    }

    private boolean isNetworkEnabled()
    {
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null ? info.isConnected() : false;
    }

    private class ThreadTask extends AsyncTask<Runnable, Void, Void>
    {
        @Override
        protected Void doInBackground(Runnable... params)
        {
            for(Runnable runnable : params)
                runnable.run();
            return null;
        }
    }
}
