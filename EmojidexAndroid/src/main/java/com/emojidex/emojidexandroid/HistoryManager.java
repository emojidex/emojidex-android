package com.emojidex.emojidexandroid;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kou on 14/05/27.
 */
public class HistoryManager {
    static final String TAG = MainActivity.TAG + "::HistoryManager";

    private final String saveFileName = FileOperation.HISTORIES;
    private final Context context;
    private final LinkedList<String> histories;
    private int capacity = 18;

    /**
     * Construct HistoryManager object.
     */
    public HistoryManager(Context context)
    {
        this.context = context.getApplicationContext();
        histories = new LinkedList<String>();
    }

    /**
     * Load input history from file.
     */
    public void load()
    {
        histories.clear();
        histories.addAll( FileOperation.load(this.context, saveFileName) );

        Log.d(TAG, "Load history. (size = " + histories.size() + ")");
    }

    /**
     * Save input history to file.
     */
    public void save()
    {
        JSONArray jsonArray = new JSONArray();
        for (String emojiName : histories)
            jsonArray.put(emojiName);
        FileOperation.saveFileToLocal(context, saveFileName, jsonArray.toString());
        Log.d(TAG, "Save history. (size = " + histories.size() + ")");
    }

    /**
     * Clear input history.
     */
    public void clear()
    {
        histories.clear();
    }

    /**
     * Regist input history.
     * @param emojiName
     */
    public void regist(String emojiName)
    {
        // Already regist.
        final int index = histories.indexOf(emojiName);
        if(index != -1)
           histories.remove(index);

        // Regist.
        histories.addFirst(emojiName);

        // Capacity over.
        if(histories.size() > capacity)
            histories.removeLast();
    }

    /**
     * Set history capacity.
     * @param newCapacity   New capacity.
     */
    public void setCapacity(int newCapacity)
    {
        capacity = Math.max(newCapacity, 0);
    }

    /**
     * Get history capacity.
     */
    public int getCapacity()
    {
        return capacity;
    }

    /**
     * Get histories list.
     * @return  Histories list.
     */
    public List<String> getHistories()
    {
        return histories;
    }
}
