package org.genshin.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by nazuki on 2013/08/28.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmojidexEmojiData
{
    @JsonProperty("id")             private String id;
    @JsonProperty("code")           private String code;
    @JsonProperty("link")           private String link;
    @JsonProperty("is_wide")        private boolean isWide;
    @JsonProperty("loops")          private int loops;
    @JsonProperty("frames")         private ArrayList<?> frames;
    @JsonProperty("frames_delay")   private ArrayList<Integer> framesDelay;
    @JsonProperty("category")       private String category;
    @JsonProperty("tags")           private ArrayList<String> tags;
    @JsonProperty("url")            private String url;

    public String getId()
    {
        return id;
    }
}
