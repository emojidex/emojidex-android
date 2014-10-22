package com.emojidex.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by kou on 13/09/06.
 */
public class CategoryData {
    @JsonProperty("category")   private String name;
    @JsonProperty("English")    private String enName;
    @JsonProperty("Japanese")   private String jpName;

    /**
     * Get category name.
     * @return      Category name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get category name of english.
     * @return      Category name of english.
     */
    public String getEnglishName()
    {
        return enName;
    }

    /**
     * Get category name of japanese.
     * @return      Category name of japanese.
     */
    public String getJapaneseName()
    {
        return jpName;
    }
}
