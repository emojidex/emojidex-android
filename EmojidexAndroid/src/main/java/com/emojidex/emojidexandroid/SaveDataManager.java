package com.emojidex.emojidexandroid;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kou on 14/05/27.
 */
public class SaveDataManager {
    static final String TAG = MainActivity.TAG + "::SaveDataManager";

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
        Favorite("favorite.json", CAPACITY_INFINITY),
        CatalogHistory("catalog_history.json", 18),
        CatalogSearch("catalog_search.json", CAPACITY_INFINITY),
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
    public SaveDataManager(Context context, Type type)
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
     * Add emoji name to first.
     * @param emojiName     Emoji name.
     */
    public void addFirst(String emojiName)
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
     * Add emoji name to last.
     * @param emojiName     Emoji name.
     */
    public void addLast(String emojiName)
    {
        // Already add.
        final int index = emojiNames.indexOf(emojiName);
        if(index != -1)
            emojiNames.remove(index);

        // Regist.
        emojiNames.addLast(emojiName);

        // Capacity over.
        if(capacity != CAPACITY_INFINITY && capacity < emojiNames.size())
            emojiNames.removeFirst();
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
     * Delete save data file.
     * @return  true if delete succeeded.
     */
    public boolean deleteFile()
    {
        clear();

        final File file = context.getFileStreamPath(type.fileName);
        if(file.exists())
            return file.delete();
        return true;
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
