package com.emojidex.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by kou on 14/10/10.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleJsonParam {
    @JsonProperty("code")       private String code = null;
    @JsonProperty("moji")       private String moji = null;
    @JsonProperty("category")   private String category = null;
    @JsonProperty("variants")   private List<String> variants = null;
    @JsonProperty("base")       private String base = null;
    @JsonProperty("score")      private int score = 0;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMoji()
    {
        return moji;
    }

    public void setMoji(String moji)
    {
        this.moji = moji;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public List<String> getVariants()
    {
        return variants;
    }

    public void setVariants(List<String> variants)
    {
        this.variants = variants;
    }

    public String getBase()
    {
        return base;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }
}
