package com.emojidex.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kou on 18/01/23.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class MojiCodes {
    private String moji_string = null;
    private List<String> moji_array = null;
    private HashMap<String, String> moji_index = null;

    @JsonProperty("moji_string")
    public String getMojiString()
    {
        return moji_string;
    }

    public void setMojiString(String moji_string)
    {
        this.moji_string = moji_string;
    }

    @JsonProperty("moji_array")
    public List<String> getMojiArray()
    {
        if(moji_array == null)
            moji_array = new ArrayList<String>();
        return moji_array;
    }

    public void setMojiArray(List<String> moji_array)
    {
        this.moji_array = moji_array;
    }

    @JsonProperty("moji_index")
    public HashMap<String, String> getMojiIndex()
    {
        if(moji_index == null)
            moji_index = new HashMap<String, String>();
        return moji_index;
    }

    public void setMojiIndex(HashMap<String, String> moji_index)
    {
        this.moji_index = moji_index;
    }
}
