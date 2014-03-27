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
    @JsonProperty("frames")         private ArrayList<EmojidexEmojiData> frames;
    @JsonProperty("frames_delay")   private ArrayList<Integer> framesDelay;
    @JsonProperty("category")       private String category;
    @JsonProperty("tags")           private ArrayList<String> tags;

    // for frames
    @JsonProperty("emoji_id")       private String emojiId;
    @JsonProperty("delay")          private int delay;

    public String getId()
    {
        return id;
    }

    private String getEmojiId()
    {
        return emojiId;
    }

    public String getEmojiId(int index)
    {
        return frames.get(index).getEmojiId();
    }
}
