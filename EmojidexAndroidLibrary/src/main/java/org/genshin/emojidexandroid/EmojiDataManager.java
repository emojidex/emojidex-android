package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.genshin.emojidexandroidlibrary.R;

import java.io.File;
import java.io.FileInputStream;
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

    private Context context;
    private boolean isInitialized = false;

    private int nextCode;

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
        this.context = context;
        Resources res = context.getResources();

        // Get device dpi.
        String dpiDir = context.getResources().getString(R.string.dpi);

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

        // Load emoji data from "emoji.json".
        final String[] KINDS = {"utf", "extended"};
        final List<EmojiData> newEmojiList = new ArrayList<EmojiData>();
        nextCode = res.getInteger(R.integer.original_code_start);
        for(String kind : KINDS)
        {
            List<EmojiData> tmpEmojiList = null;
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                InputStream is = new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath() + "/emojidex", kind + "/emoji.json"));
                tmpEmojiList = objectMapper.readValue(is, new TypeReference<ArrayList<EmojiData>>(){});
                is.close();
            }
            catch (JsonParseException e) { e.printStackTrace(); }
            catch (JsonMappingException e) { e.printStackTrace(); }
            catch (IOException e) { e.printStackTrace(); }

            if(tmpEmojiList == null)
                continue;

            // Initialize emoji.
            for(EmojiData emoji : tmpEmojiList)
            {
                if(emoji.hasCode())
                    emoji.initialize(res, kind + "/" + dpiDir);
                else
                    emoji.initialize(res, kind + "/" + dpiDir, nextCode++);
            }

            // Merge list.
            newEmojiList.addAll(tmpEmojiList);
        }

        // All category
        categorizedLists.put(context.getString(R.string.all_category), newEmojiList);

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
    }

    /**
     * Parse the json data.
     * @param json json data from emojidex site.
     */
    private List<EmojidexEmojiData> parse(String json)
    {
        List<EmojidexEmojiData> res = new ArrayList<EmojidexEmojiData>();
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode emojiNode = rootNode.path("emoji");
            res = mapper.readValue(((Object)emojiNode).toString(),
                    new TypeReference<ArrayList<EmojidexEmojiData>>(){});
        }
        catch (JsonProcessingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        return res;
    }

    /**
     * Add categorized emoji.
     * @param jsonData
     * @param categoryName
     */
    public void addCategorizedEmoji(String jsonData, String categoryName)
    {
        List<EmojidexEmojiData> downloadEmojiList = parse(jsonData);
        for (EmojidexEmojiData emoji : downloadEmojiList)
        {
            emoji.initialize(nextCode++, context);
        }

        // When there is no image, remove emoji.
        for (int i = downloadEmojiList.size() - 1; i >= 0; i--)
        {
            if (downloadEmojiList.get(i).getIcon() == null)
                downloadEmojiList.remove(i);
        }

        List<EmojiData> convertList = new ArrayList<EmojiData>();
        for (EmojidexEmojiData emoji : downloadEmojiList)
        {
            convertList.add(emoji);
        }

        categorizedLists.put(categoryName, convertList);
        for(EmojiData emoji : convertList)
        {
            nameTable.put(emoji.getName(), emoji);
            codeTable.put(emoji.getCodes(), emoji);
        }
    }

    /**
     * Delete categorized emoji.
     * @param categoryName
     */
    public void deleteCategorizedEmoji(String categoryName)
    {
        // delete old keyboard
        categorizedLists.remove(categoryName);
    }
}
