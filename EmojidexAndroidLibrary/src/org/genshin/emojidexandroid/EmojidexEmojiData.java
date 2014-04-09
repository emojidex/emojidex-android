package org.genshin.emojidexandroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by nazuki on 2014/03/26.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmojidexEmojiData extends EmojiData
{
    @JsonProperty("id")             private String id;
    @JsonProperty("code")           private String tmpName;
    @JsonProperty("link")           private String link;
    @JsonProperty("is_wide")        private boolean isWide;
    @JsonProperty("loops")          private int loops;
    @JsonProperty("frames")         private ArrayList<EmojidexEmojiData> frames;
    @JsonProperty("frames_delay")   private ArrayList<Integer> framesDelay;
    @JsonProperty("category")       private String tmpCategory;
    @JsonProperty("tags")           private ArrayList<String> tags;

    // for frames
    @JsonProperty("emoji_id")       private String emojiId;
    @JsonProperty("delay")          private int delay;

    private Context context;

    public void initialize(int code, Context context)
    {
        this.context = context;

        moji = new String(Character.toChars(code));
        final int count = moji.codePointCount(0, moji.length());
        int next = 0;
        for(int i = 0;  i < count;  ++i)
        {
            final int codePoint = moji.codePointAt(next);
            next += Character.charCount(codePoint);

            // Ignore variation selectors.
            if(Character.getType(codePoint) != Character.NON_SPACING_MARK)
                codes.add(codePoint);
        }
        if(codes.size() < count)
        {
            moji = "";
            for(Integer current : codes)
                moji += String.valueOf(Character.toChars(current));
        }

        name = tmpName;
        category = tmpCategory;
        isOriginal = true;

        // Get the icon from local.
        boolean exist = getIconFromLocalFile();

        // When the icon does not exist in the local, get the icon from emojidex site.
        if (!exist)
            getIconFromEmojidexSite();
    }

    /**
     * Get the icon form local.
     * @return
     */
    private boolean getIconFromLocalFile()
    {
        boolean exist = false;
        try
        {
            InputStream in = context.openFileInput(name + ".png");
            icon = Drawable.createFromStream(in, name);
            exist = true;
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        return exist;
    }

    /**
     * Get the icon from emojidex site.
     */
    private void getIconFromEmojidexSite()
    {
        // Get the image from emojidex site.
        try
        {
            id = URLEncoder.encode(id, "UTF-8");
        }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        String imgUri = "http://assets.emojidex.com/emoji/" + id + "/px128.png";
        Uri.Builder imgUriBuilder = new Uri.Builder();
        AsyncHttpRequestForGetImage getImgTask = new AsyncHttpRequestForGetImage(imgUri);
        getImgTask.execute(imgUriBuilder);
        try
        {
            icon = getImgTask.get();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }

    public String getId()
    {
        return id;
    }

    public String getLink()
    {
        return link;
    }

    public boolean isWide()
    {
        return isWide;
    }

    public int getLoops()
    {
        return loops;
    }

    public ArrayList<EmojidexEmojiData> getFrames()
    {
        return frames;
    }

    public ArrayList<Integer> getFramesDelay()
    {
        return framesDelay;
    }

    public ArrayList<String> getTags()
    {
        return tags;
    }

    private String getEmojiId()
    {
        return emojiId;
    }

    public String getEmojiId(int index)
    {
        return frames.get(index).getEmojiId();
    }

    private int getDelay()
    {
        return delay;
    }

    public int getDelay(int index)
    {
        return frames.get(index).getDelay();
    }
}
