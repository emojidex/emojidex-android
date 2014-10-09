package org.genshin.emojidexandroid2;

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

    /**
     * Construct EmojiManager object.
     */
    public EmojiManager()
    {
        // nop
    }

    /**
     * Add emoji from json file.
     * @param kind  Emoji kind.
     */
    public void add(String kind)
    {
        ArrayList<Emoji> newEmojies = null;

        // Load emoji from json.
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper();
            final File file = new File(PathGenerator.getLocalRootPath(), PathGenerator.getJsonRelativePath(kind));
            final InputStream is = new FileInputStream(file);
            newEmojies = objectMapper.readValue(is, new TypeReference<ArrayList<Emoji>>(){});
            is.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        // Initialize and add emoji.
        emojies.ensureCapacity(emojies.size() + newEmojies.size());
        emojies.addAll(newEmojies);

        for(Emoji emoji : newEmojies)
        {
            // Initialize.
            emoji.initialize(kind);

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
    public Collection<Emoji> getEmojiList(String category)
    {
        return categorizedEmojies.get(category);
    }

    /**
     * Ger all emoji list.
     * @return  All emoji list.
     */
    public Collection<Emoji> getAllEmojiList()
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
