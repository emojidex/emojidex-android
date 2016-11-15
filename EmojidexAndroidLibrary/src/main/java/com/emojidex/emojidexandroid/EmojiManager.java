package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;

import com.emojidex.emojidexandroidlibrary.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
class EmojiManager {
    private final ArrayList<Emoji> emojies = new ArrayList<Emoji>();

    private final HashMap<String, Emoji> emojiTableFromName = new HashMap<String, Emoji>();
    private final HashMap<List<Integer>, Emoji> emojiTableFromCodes = new HashMap<List<Integer>, Emoji>();
    private final HashMap<String, ArrayList<Emoji>> categorizedEmojies = new HashMap<String, ArrayList<Emoji>>();

    private final Context context;

    private int nextOriginalCode;

    /**
     * Construct EmojiManager object.
     */
    public EmojiManager(Context context)
    {
        this.context = context.getApplicationContext();
        reset();
    }

    /**
     * Add emoji from json file.
     * @param path  Json file path.
     */
    public void add(String path)
    {
        ArrayList<Emoji> newEmojies = null;

        // Load emoji from json.
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper();
            final File file = new File(path);
            final InputStream is = new FileInputStream(file);
            newEmojies = objectMapper.readValue(is, new TypeReference<ArrayList<Emoji>>(){});
            is.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        if(newEmojies == null)
            return;

        // Initialize and add emoji.
        emojies.ensureCapacity(emojies.size() + newEmojies.size());
        emojies.addAll(newEmojies);

        for(Emoji emoji : newEmojies)
        {
            // Initialize.
            if(emoji.hasCodes())
                emoji.initialize(context);
            else
                emoji.initialize(context, nextOriginalCode++);

            // Add.
            emojiTableFromName.put(emoji.getName(), emoji);
            emojiTableFromCodes.put(emoji.getCodes(), emoji);

            ArrayList<Emoji> categoryList = categorizedEmojies.get(emoji.getCategory());
            if(categoryList == null)
            {
                categoryList = new ArrayList<Emoji>();
                categorizedEmojies.put(emoji.getCategory(), categoryList);
            }
            categoryList.add(emoji);
        }
    }

    /**
     * Reset manager.
     */
    public void reset()
    {
        emojies.clear();
        emojiTableFromName.clear();
        emojiTableFromCodes.clear();
        categorizedEmojies.clear();

        nextOriginalCode = context.getResources().getInteger(R.integer.original_code_start);
    }

    /**
     * Get emoji from emoji name.
     * @param name  Emoji name.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(String name)
    {
        return emojiTableFromName.get(name);
    }

    /**
     * Get emoji from emoji codes.
     * @param codes Emoji codes.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(List<Integer> codes)
    {
        return emojiTableFromCodes.get(codes);
    }

    /**
     * Get emoji list from category.
     * @param category  Category name.
     * @return          Emoji list.(If emoji list is not found, return null.)
     */
    public List<Emoji> getEmojiList(String category)
    {
        return categorizedEmojies.get(category);
    }

    /**
     * Ger all emoji list.
     * @return  All emoji list.
     */
    public List<Emoji> getAllEmojiList()
    {
        return emojies;
    }

    /**
     * Get category name list.
     * @return  Category name list.
     */
    public Collection<String> getCategoryNames()
    {
        return categorizedEmojies.keySet();
    }
}
