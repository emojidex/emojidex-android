package com.emojidex.emojidexandroid;

import java.util.ArrayList;

/**
 * Created by nazuki on 2013/11/18.
 */
public class CategorizedKeyboard {
    private ArrayList<EmojidexKeyboard> keyboards;
    private int nowPage;
    private int maxPage;

    public CategorizedKeyboard()
    {
        keyboards = new ArrayList<EmojidexKeyboard>();
        nowPage = 1;
        maxPage = 1;
    }

    public void setKeyboard(EmojidexKeyboard keyboard)
    {
        keyboards.add(keyboard);
        setMaxPage(keyboards.size());
    }

    public ArrayList<EmojidexKeyboard> getKeyboards()
    {
        return keyboards;
    }

    public void setNowPage(int num)
    {
        nowPage = num;
    }

    public int getNowPage()
    {
        return nowPage;
    }

    public void setMaxPage(int num)
    {
        maxPage = num;
    }

    public int getMaxPage()
    {
        return  maxPage;
    }
}
