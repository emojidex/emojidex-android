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
    private static EmojiDataManager instance = new EmojiDataManager();

    private Map<String, List<EmojiData>> categorizedLists;
    private Map<String, EmojiData> nameTable;
    private Map<Integer, EmojiData> codeTable;
    private List<CategoryData> categories;

    private boolean isInitialized;


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
        categorizedLists = new HashMap<String, List<EmojiData>>();
        nameTable = new HashMap<String, EmojiData>();
        codeTable = new HashMap<Integer, EmojiData>();
        isInitialized = false;
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
     * Get EmojiData object from emoji name.
     * @param name      Emoji name.
     * @return          EmojiData object.
     */
    public EmojiData getEmojiData(String name)
    {
        return nameTable.get(name);
    }

    /**
     * Get EmojiData object from emoji code.
     * @param code      Emoji code.
     * @return          EmojiData object.
     */
    public EmojiData getEmojiData(int code)
    {
        return codeTable.get(code);
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
            nameTable.put(emoji.getName(), emoji);
            codeTable.put(emoji.getCode(), emoji);
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
