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

    private static final SaveDataManager[] managers = new SaveDataManager[Type.values().length];
    private static final ObjectMapper mapper = new ObjectMapper();

    protected final Context context;
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
        Index("index.json", CAPACITY_INFINITY),
        MyEmoji("my_emoji.json", CAPACITY_INFINITY),
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

    public static SaveDataManager getInstance(Context context, Type type)
    {
        if(managers[type.ordinal()] == null)
        {
            if(type == Type.History)
                managers[type.ordinal()] = HistoryManager.getInstance(context);
            else if(type == Type.Favorite)
                managers[type.ordinal()] = FavoriteManager.getInstance(context);
            else
                managers[type.ordinal()] = new SaveDataManager(context, type);
        }
        return managers[type.ordinal()];
    }

    /**
     * Construct HistoryManager object.
     * @param context   Context.
     * @param type      Type.
     */
    protected SaveDataManager(Context context, Type type)
    {
        this.context = context.getApplicationContext();
        this.type = type;
        emojiNames = new LinkedList<String>();
        capacity = type.defaultCapacity;
    }

    /**
     * Save emoji names to file.
     */
    public void save()
    {
        saveToFile(type.fileName);
    }

    /**
     * Load emoji names from file.
     */
    public void load()
    {
        loadFromFile(type.fileName);
    }

    /**
     * Save emoji names to file.
     */
    public void saveBackup()
    {
        saveToFile(type.fileName + ".bak");
    }

    /**
     * Load emoji names from file.
     */
    public void loadBackup()
    {
        loadFromFile(type.fileName + ".bak");
        saveToFile(type.fileName);
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
     *
     * @param emojiName     Emoji name.
     * @return      true if contains emojiName.
     */
    public boolean contains(String emojiName)
    {
        return emojiNames.contains(emojiName);
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

    /**
     * Save emoji names to file.
     * @param filename      File name.
     */
    private void saveToFile(String filename)
    {
        try
        {
            final OutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
            mapper.writeValue(os, emojiNames);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        Log.d(TAG, "Save \"" + filename + "\". (size = " + emojiNames.size() + ")");
    }

    /**
     * Load emoji names from file.
     * @param filename      File name.
     */
    private void loadFromFile(String filename)
    {
        emojiNames.clear();

        try
        {
            final InputStream is = context.openFileInput(filename);
            final TypeReference<LinkedHashSet<String>> typeReference = new TypeReference<LinkedHashSet<String>>(){};
            final LinkedHashSet<String> loadEmojiNames = mapper.readValue(is, typeReference);
            is.close();
            emojiNames.addAll(loadEmojiNames);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        Log.d(TAG, "Load \"" + filename + "\". (size = " + emojiNames.size() + ")");
    }
}
