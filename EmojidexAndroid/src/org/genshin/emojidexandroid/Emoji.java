package org.genshin.emojidexandroid;

/**
 * Created by nazuki on 2013/08/28.
 */
public class Emoji
{
    String moji;
    String name;
    String nameJa;
    String category;
    String unicode;

    public Emoji()
    {
        moji = "";
        name = "";
        nameJa = "";
        category = "";
        unicode = "";
    }

    public Object getMoji()
    {
        return moji;
    }

    public String getName()
    {
        return name;
    }

    public String getNameJa()
    {
        return nameJa;
    }

    public String getCategory()
    {
        return category;
    }

    public String getUnicode()
    {
        return unicode;
    }

    public void setMoji(String moji)
    {
        this.moji = moji;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setNameJa(String nameJa)
    {
        this.nameJa = nameJa;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public void setUnicode(String unicode)
    {
        this.unicode = unicode;
    }
}
