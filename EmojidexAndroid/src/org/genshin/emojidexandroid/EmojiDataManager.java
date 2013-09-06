package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nazuki on 2013/08/28.
 */
public class EmojiDataManager
{
    private Map<String, List<EmojiData>> categorizedLists;
    private Map<Integer, EmojiData> emojiTable;
    private List<CategoryData> categories;

    /**
     * Construct EmojiDataManager object.
     * @param context
     */
    public EmojiDataManager(Context context)
    {
        categorizedLists = new HashMap<String, List<EmojiData>>();
        emojiTable = new HashMap<Integer, EmojiData>();

        initialize(context);
    }

    /**
     * Get categorized EmojiData list.
     * @param categoryName
     * @return
     */
    public List<EmojiData> getCategorizedList(String categoryName)
    {
        return categorizedLists.get(categoryName);
    }

    /**
     * Get category name list.
     * @return      Set of category name.
     */
    public List<CategoryData> getCategoryDatas()
    {
        return categories;
    }

    /**
     * Get EmojiData object from emoji code.
     * @param code      Emoji code.
     * @return      EmojiData object.
     */
    public EmojiData getEmojiData(int code)
    {
        return emojiTable.get(code);
    }

    /**
     * Initialize EmojiDataManager object.
     * @param context
     */
    private void initialize(Context context)
    {
        Resources res = context.getResources();

        // Get device dpi.
        int dpi = res.getDisplayMetrics().densityDpi;
        String dpiDir;
        if(dpi < 140)           dpiDir = "ldpi/";
        else if(dpi < 200)      dpiDir = "mdpi/";
        else if(dpi <= 280)     dpiDir = "hdpi/";
        else                    dpiDir = "xhdpi/";

        android.util.Log.d("ime", "Device DPI = " + dpi + ", Assets directory = " + dpiDir);

        // Load emoji data from "index.json".
        List<EmojiData> newEmojiList = null;

        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream is = res.openRawResource(R.raw.index);
            newEmojiList = objectMapper.readValue(is, new TypeReference<ArrayList<EmojiData>>(){});
            is.close();
        }
        catch (JsonParseException e) { e.printStackTrace(); }
        catch (JsonMappingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        categorizedLists.put(context.getString(R.string.ime_all_category_name), newEmojiList);

        // Initialize emoji.
        int nextCode = Character.MAX_CODE_POINT + 1;
        for(EmojiData emoji : newEmojiList)
        {
            emoji.initialize(res, dpiDir, nextCode++);
        }

        // Create categorized list.
        for(EmojiData emoji : newEmojiList)
        {
            String category = emoji.getCategory();

            // Search category from table.
            List<EmojiData> targetList = categorizedLists.get(category);

            // Create new list if category is not found.
            if(targetList == null)
            {
                targetList = new ArrayList<EmojiData>();
                categorizedLists.put(category, targetList);
            }

            // Add emoji to category.
            targetList.add(emoji);
        }

        // Create EmojiData table.
        for(EmojiData emoji : newEmojiList)
        {
            emojiTable.put(emoji.getCode(), emoji);
        }

        // Load category data from "categories.json".
        try
        {
            InputStream is = res.getAssets().open("categories.json");
            ObjectMapper objectMapper = new ObjectMapper();
            categories = objectMapper.readValue(is, new TypeReference<ArrayList<CategoryData>>(){});
            is.close();
        }
        catch (JsonParseException e) { e.printStackTrace(); }
        catch (JsonMappingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
