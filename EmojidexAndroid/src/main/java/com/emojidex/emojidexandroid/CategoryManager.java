package com.emojidex.emojidexandroid;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by kou on 15/01/13.
 */
public class CategoryManager {
    private static final CategoryManager INSTANCE = new CategoryManager();

    private final ArrayList<CategoryParam> params = new ArrayList<CategoryParam>();

    /**
     * Category parameter.
     */
    private static class CategoryParam
    {
        public final String id;
        public final String text;

        private CategoryParam(String id, String text)
        {
            this.id = id;
            this.text = text;
        }
    }

    /**
     * Get singleton instance.
     * @return  Singleton instance.
     */
    public static CategoryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Initialize object.
     * @param context   Context.
     */
    public void initialize(Context context)
    {
        context = context.getApplicationContext();

        // Declare initialize parameter table.
        final int[][] initParams = {
                { R.string.ime_category_id_faces            , R.string.ime_category_text_faces },
                { R.string.ime_category_id_people           , R.string.ime_category_text_people },
                { R.string.ime_category_id_gestures         , R.string.ime_category_text_gestures },
                { R.string.ime_category_id_food             , R.string.ime_category_text_food },
                { R.string.ime_category_id_objects          , R.string.ime_category_text_objects },
                { R.string.ime_category_id_tools            , R.string.ime_category_text_tools },
                { R.string.ime_category_id_nature           , R.string.ime_category_text_nature },
                { R.string.ime_category_id_cosmos           , R.string.ime_category_text_cosmos },
                { R.string.ime_category_id_places           , R.string.ime_category_text_places },
                { R.string.ime_category_id_transportation   , R.string.ime_category_text_transportation },
                { R.string.ime_category_id_abstract         , R.string.ime_category_text_abstract },
                { R.string.ime_category_id_symbols          , R.string.ime_category_text_symbols },
        };

        // Clear categories.
        params.clear();

        // Add categories.
        for(int[] initParam : initParams)
        {
            add(
                    context.getString(initParam[0]),
                    context.getString(initParam[1])
            );
        }
    }

    /**
     * Add category.
     * @param id        Category ID.
     * @param text      Category text.
     */
    public void add(String id, String text)
    {
        // Skip if always have id.
        for(CategoryParam param : params)
        {
            if(param.id.equals(id))
                return;
        }

        // Add category.
        params.add(new CategoryParam(id, text));
    }

    /**
     * Get category count.
     * @return  Category count.
     */
    public int getCategoryCount()
    {
        return params.size();
    }

    /**
     * Get category ID.
     * @param index     Index.
     * @return          Category ID.
     */
    public String getCategoryId(int index)
    {
        return params.get(index).id;
    }

    /**
     * Get category text.
     * @param index     Index.
     * @return          Category text.
     */
    public String getCategoryText(int index)
    {
        return params.get(index).text;
    }

    /**
     * Construct object.(private)
     */
    private CategoryManager()
    {
        // nop
    }
}
