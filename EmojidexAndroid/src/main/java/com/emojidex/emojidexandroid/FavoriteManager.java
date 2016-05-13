package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Service.User;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by kou on 16/04/12.
 */
public class FavoriteManager extends SaveDataManager
{
    static final String TAG = MainActivity.TAG + "::FavoriteManager";

    private static FavoriteManager instance;

    private ThreadTask task;
    private Collection<String> addEmojies = new LinkedHashSet<String>();
    private Collection<String> removeEmojies = new LinkedHashSet<String>();

    public static FavoriteManager getInstance(Context context)
    {
        if(instance == null)
            instance = new FavoriteManager(context);
        return instance;
    }

    private FavoriteManager(Context context)
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
            Log.d(TAG, "Skip load user favorite.");
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

                user.syncFavorites();
                final EmojiVector favorite = user.getFavorites().all();

                clear();
                final long size = favorite.size();
                for(int i = 0;  i < size;  ++i)
                    addLast(favorite.get(i).getCode().replaceAll(" ", "_"));

                Log.d(TAG, "Load user favorite.");
            }
        });
    }

    public void saveToUser()
    {
        final UserData userdata =  UserData.getInstance();
        if(     !isNetworkEnabled()
            ||  !userdata.isLogined()
            ||  (task != null && task.getStatus() == AsyncTask.Status.RUNNING)
            ||  (addEmojies.isEmpty() && removeEmojies.isEmpty())    )
        {
            Log.d(TAG, "Skip save user favorite.");
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

                {
                    final Iterator<String> it = removeEmojies.iterator();
                    while(it.hasNext())
                    {
                        final String emojiName = it.next();
                        user.removeFavorite(emojiName);
                        it.remove();
                    }
                }
                {
                    final Iterator<String> it = addEmojies.iterator();
                    while(it.hasNext())
                    {
                        final String emojiName = it.next();
                        if(user.addFavorite(emojiName))
                            it.remove();
                    }
                }

                Log.d(TAG, "Save user favorite.(error count = " + addEmojies.size() + ")");
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
        {
            if(addEmojies.contains(emojiName))
                addEmojies.remove(emojiName);
            addEmojies.add(emojiName);
        }
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

        if(UserData.getInstance().isLogined())
        {
            if(addEmojies.contains(emojiName))
                addEmojies.remove(emojiName);
            removeEmojies.add(emojiName);
        }
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
