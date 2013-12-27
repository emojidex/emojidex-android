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
    private static final EmojiDataManager instance = new EmojiDataManager();

    private final Map<String, List<EmojiData>> categorizedLists = new HashMap<String, List<EmojiData>>();
    private final Map<String, EmojiData> nameTable = new HashMap<String, EmojiData>();
    private final Map<List<Integer>, EmojiData> codeTable = new HashMap<List<Integer>, EmojiData>();

    private List<CategoryData> categories = null;

    private boolean isInitialized = false;


    /**
     * Create EmojiDataManager object.
     * @param context
     * @return      EmojiDataManager object.
     */
    public static EmojiDataManager create(Context context)
    {
        if( !instance.isInitialized )
        {
            instance.initialize(context);
            instance.isInitialized = true;
        }
        return instance;
    }

    /**
     * Construct EmojiDataManager object.
     */
    private EmojiDataManager()
    {
        // Nothing is implemented.
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
     * Get EmojiData object by emoji name.
     * @param name      Emoji name.
     * @return          EmojiData object.
     */
    public EmojiData getEmojiData(String name)
    {
        return nameTable.get(name);
    }

    /**
     * Get EmojiData object by emoji codes.
     * @param codes     Emoji codes.
     * @return          EmojiData object.
     */
    public EmojiData getEmojiData(List<Integer> codes)
    {
        return codeTable.get(codes);
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

        android.util.Log.d("lib", "Device DPI = " + dpi + ", Assets directory = " + dpiDir);

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

        categorizedLists.put(context.getString(R.string.all_category), newEmojiList);

        // Initialize emoji.
        int nextCode = res.getInteger(R.integer.original_code_start);
        for(EmojiData emoji : newEmojiList)
        {
            if(emoji.hasCode())
                emoji.initialize(res, dpiDir);
            else
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
            // Add to name table.
            nameTable.put(emoji.getName(), emoji);

            // Add to emoji code table.
            codeTable.put(emoji.getCodes(), emoji);
        }

        /*
        ArrayList<EmojiData> testData = new ArrayList<EmojiData>();
        for (int i = 0; i < 100; i++)
        {
            testData.add(newEmojiList.get(i));
        }
        categorizedLists.put(context.getString(R.string.all_category), testData);
        */

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
