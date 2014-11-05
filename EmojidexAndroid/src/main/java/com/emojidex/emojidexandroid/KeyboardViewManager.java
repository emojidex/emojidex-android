package com.emojidex.emojidexandroid;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kou on 14/10/29.
 */
public class KeyboardViewManager {
    private final int PAGE_COUNT = 3;
    private final EmojidexKeyboardView[] views = new EmojidexKeyboardView[PAGE_COUNT];
    private final newEmojidexKeyboard[] keyboards = new newEmojidexKeyboard[PAGE_COUNT];
    private final ArrayList<List<Emoji>> pages = new ArrayList<List<Emoji>>();

    private int currentView;
    private int currentPage;


    /**
     * Construct object.
     * @param context
     * @param onKeyboardActionListener
     * @param onTouchListener
     */
    public KeyboardViewManager(Context context, KeyboardView.OnKeyboardActionListener onKeyboardActionListener, View.OnTouchListener onTouchListener)
    {
        for(int i = 0;  i < PAGE_COUNT;  ++i)
        {
            views[i] = new EmojidexKeyboardView(context, null, R.attr.keyboardViewStyle);
            views[i].setOnTouchListener(onTouchListener);
            views[i].setOnKeyboardActionListener(onKeyboardActionListener);
            views[i].setPreviewEnabled(false);
            keyboards[i] = newEmojidexKeyboard.create(context);
            views[i].setKeyboard(keyboards[i]);
        }

        currentView = 0;
    }

    /**
     * Initialize manager.
     * @param emojies   Emoji of regist to manager.
     */
    public void initialize(List<Emoji> emojies)
    {
        currentPage = 0;

        pages.clear();

        final int keyCountMax = keyboards[0].getKeyCountMax();
        final int emojiCount = emojies.size();
        for(int i = 0;  i < emojiCount;  i += keyCountMax)
        {
            pages.add(new ArrayList<Emoji>(
                    emojies.subList(i, Math.min(i + keyCountMax, emojiCount))
            ));
        }

        initializePage(currentView, currentPage);
    }

    public void initializeFromName(List<String> emojiNames)
    {
        final Emojidex emojidex = Emojidex.getInstance();
        final ArrayList<Emoji> emojies = new ArrayList<Emoji>(emojiNames.size());
        for(String emojiName : emojiNames)
            emojies.add(emojidex.getEmoji(emojiName));
        initialize(emojies);
    }

    /**
     * Change view to next.
     */
    public void next()
    {
        currentView = (currentView + 1) % PAGE_COUNT;
        currentPage = (currentPage + 1) % pages.size();

        initializePage(currentView, currentPage);
    }

    /**
     * Change view to prev.
     */
    public void prev()
    {
        currentView = (currentView + PAGE_COUNT - 1) % PAGE_COUNT;
        currentPage = (currentPage + pages.size() - 1) % pages.size();

        initializePage(currentView, currentPage);
    }

    /**
     * Get current view.
     * @return  Current view.
     */
    public EmojidexKeyboardView getCurrentView()
    {
        return views[currentView];
    }

    /**
     * Get next view.
     * @return  Next view.
     */
    public EmojidexKeyboardView getNextView()
    {
        return views[(currentView + 1) % PAGE_COUNT];
    }

    /**
     * Get prev view.
     * @return  Prev view.
     */
    public EmojidexKeyboardView getPrevView()
    {
        return views[(currentView + PAGE_COUNT - 1) % PAGE_COUNT];
    }

    /**
     * Get view array.
     * @return  View array.
     */
    public EmojidexKeyboardView[] getViews()
    {
        return views;
    }

    /**
     * Initialize page.
     * @param destIndex
     * @param pageIndex
     */
    private void initializePage(int destIndex, int pageIndex)
    {
        keyboards[destIndex].initialize(pages.get(pageIndex));
        views[destIndex].setKeyboard(keyboards[destIndex]);
    }
}
