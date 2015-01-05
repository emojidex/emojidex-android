package com.emojidex.emojidexandroid;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kou on 14/05/27.
 */
public class PreferenceManager {
    static final String TAG = MainActivity.TAG + "::PreferenceManager";

    public static final int CAPACITY_INFINITY = 0;

    private final Context context;
    private final Type type;
    private final LinkedList<String> emojiNames;
    private int capacity;

    /**
     * Preference type.
     */
    public enum Type
    {
        History("history.json", 18),
        Search("search.json", CAPACITY_INFINITY),
        ;

        private final String fileName;
        private final int defaultCapacity;

        private Type(String fileName, int defaultCapacity)
        {
            this.fileName = fileName;
            this.defaultCapacity = defaultCapacity;
        }
    };

    /**
     * Construct HistoryManager object.
     * @param context   Context.
     * @param type      Type.
     */
    public PreferenceManager(Context context, Type type)
    {
        this.context = context.getApplicationContext();
        this.type = type;
        emojiNames = new LinkedList<String>();
        capacity = type.defaultCapacity;
    }

    /**
     * Load emoji names from file.
     */
    public void load()
    {
        emojiNames.clear();

        try
        {
            final ObjectMapper objectMapper = new ObjectMapper();
            final InputStream is = context.openFileInput(type.fileName);
            final TypeReference<LinkedHashSet<String>> typeReference = new TypeReference<LinkedHashSet<String>>(){};
            final LinkedHashSet<String> loadEmojiNames = objectMapper.readValue(is, typeReference);
            emojiNames.addAll(loadEmojiNames);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        Log.d(TAG, "Load \"" + type.fileName + "\". (size = " + emojiNames.size() + ")");
    }

    /**
     * Save emoji names to file.
     */
    public void save()
    {
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper();
            final OutputStream os = context.openFileOutput(type.fileName, Context.MODE_PRIVATE);
            objectMapper.writeValue(os, emojiNames);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        Log.d(TAG, "Save \"" + type.fileName + "\". (size = " + emojiNames.size() + ")");
    }

    /**
     * Add emoji name.
     * @param emojiName     Emoji name.
     */
    public void add(String emojiName)
    {
        // Already add.
        final int index = emojiNames.indexOf(emojiName);
        if(index != -1)
           emojiNames.remove(index);

        // Regist.
        emojiNames.addFirst(emojiName);

        // Capacity over.
        if(capacity != CAPACITY_INFINITY && capacity < emojiNames.size())
            emojiNames.removeLast();
    }

    /**
     * Remove emoji name.
     * @param emojiName     Emoji name.
     */
    public void remove(String emojiName)
    {
        emojiNames.remove(emojiName);
    }

    /**
     * Clear emoji names.
     */
    public void clear()
    {
        emojiNames.clear();
    }

    /**
     * Set emoji names capacity.
     * @param newCapacity   New capacity.
     */
    public void setCapacity(int newCapacity)
    {
        capacity = Math.max(newCapacity, 0);
        while(capacity < emojiNames.size())
            emojiNames.removeLast();
    }

    /**
     * Get emoji names capacity.
     */
    public int getCapacity()
    {
        return capacity;
    }

    /**
     * Get emoji names.
     * @return  Emoji names list.
     */
    public List<String> getEmojiNames()
    {
        return emojiNames;
    }
}
