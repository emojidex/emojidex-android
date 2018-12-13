package com.emojidex.emojidexandroid.imageloader;

import android.content.res.Resources;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;

class ImageLoadArguments {
    private Resources res;
    private EmojiFormat format;
    private String emojiName;

    public ImageLoadArguments(Resources res, String emojiName)
    {
        this.res = res;
        format = Emojidex.getInstance().getDefaultFormat();
        this.emojiName = emojiName;
    }

    public Resources getResources()
    {
        return res;
    }

    public ImageLoadArguments setResouces(Resources res)
    {
        this.res = res;
        return this;
    }

    public EmojiFormat getFormat()
    {
        return format;
    }

    public ImageLoadArguments setFormat(EmojiFormat format)
    {
        this.format = format;
        return this;
    }

    public String getEmojiName()
    {
        return emojiName;
    }

    public ImageLoadArguments setEmojiName(String emojiName)
    {
        this.emojiName = emojiName;
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof ImageLoadArguments) )
            return false;

        final ImageLoadArguments arg = (ImageLoadArguments)obj;

        return      objEquals(res, arg.res)
                &&  objEquals(format, arg.format)
                &&  objEquals(emojiName, arg.emojiName)
                ;
    }

    private static <T> boolean objEquals(T v1, T v2)
    {
        if(v1 == null)
        {
            if(v2 != null)
                return false;
        }
        else
        {
            if(v2 == null)
                return false;

            return v1.equals(v2);
        }

        return true;
    }
}
