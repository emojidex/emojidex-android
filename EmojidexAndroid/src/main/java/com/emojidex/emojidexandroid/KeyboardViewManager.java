package com.emojidex.emojidexandroid;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/29.
 */
public class KeyboardViewManager {
    private final int PAGE_COUNT = 3;
    private final EmojidexKeyboardView[] views = new EmojidexKeyboardView[PAGE_COUNT];
    private final EmojidexKeyboard[] keyboards = new EmojidexKeyboard[PAGE_COUNT];
    private final ArrayList<List<Emoji>> pages = new ArrayList<List<Emoji>>();
    private final Context context;

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
        this.context = context;

        for(int i = 0;  i < PAGE_COUNT;  ++i)
        {
            views[i] = new EmojidexKeyboardView(context, null, R.attr.keyboardViewStyle);
            views[i].setOnTouchListener(onTouchListener);
            views[i].setOnKeyboardActionListener(onKeyboardActionListener);
            views[i].setPreviewEnabled(false);
            keyboards[i] = EmojidexKeyboard.create(context);
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
        initialize(emojies, 0);
    }

    /**
     * Initialize manager.
     * @param emojies       Emoji of regist to manager.
     * @param defaultPage   Default page number.
     */
    public void initialize(List<Emoji> emojies, int defaultPage)
    {
        pages.clear();

        // Create pages.
        final int keyCountMax = keyboards[0].getKeyCountMax();
        final int emojiCount = emojies == null ? 0 : emojies.size();
        ArrayList<Emoji> page = new ArrayList<Emoji>();
        for(int i = 0;  i < emojiCount;  ++i)
        {
            if(page.size() >= keyCountMax)
            {
                pages.add(page);
                page = new ArrayList<Emoji>();
            }
            final Emoji emoji = emojies.get(i);
            if(emoji != null)
                page.add(emoji);
        }
        pages.add(page);

        // Set default page.
        currentPage = defaultPage % pages.size();

        // Apply page to view.
        initializePage(currentView, currentPage);
    }

    /**
     * Initialize manager from emoji name.
     * @param emojiNames    Emoji name of regist to manager.
     */
    public void initializeFromName(List<String> emojiNames)
    {
        initializeFromName(emojiNames, 0);
    }

    /**
     * Initialize manager from emoji name.
     * @param emojiNames    Emoji name of regist to manager.
     * @param defaultPage   Default page number.
     */
    public void initializeFromName(List<String> emojiNames, int defaultPage)
    {
        initializeFromName(emojiNames, defaultPage, false);
    }

    /**
     * Initialize manager from emoji name.
     * @param emojiNames    Emoji name of regist to manager.
     * @param defaultPage   Default page number.
     * @param standardOnly  Standard emoji only
     */
    public void initializeFromName(List<String> emojiNames, int defaultPage, boolean standardOnly)
    {
        final Emojidex emojidex = Emojidex.getInstance();
        final ArrayList<Emoji> emojies = new ArrayList<Emoji>(emojiNames.size());
        for(String emojiName : emojiNames) {
            Emoji emoji = emojidex.getEmoji(emojiName);
            if (!standardOnly || emoji.isStandard()) emojies.add(emoji);
        }
        initialize(emojies, defaultPage);
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
     * Change view to specified page.
     * @param page  page number.
     */
    public void setPage(int page)
    {
        currentView = (currentView + 1) % PAGE_COUNT;
        currentPage = page % pages.size();

        initializePage(currentView, currentPage);
    }

    /**
     * Change view to specified emoji name.
     * @param emojiName     Emoji name.
     */
    public void setPage(String emojiName)
    {
        final int count = pages.size();
        for(int i = 0;  i < count;  ++i)
        {
            final List<Emoji> page = pages.get(i);
            for(Emoji emoji : page)
            {
                if( emoji.getCode().equals(emojiName) )
                {
                    setPage(i);
                    return;
                }
            }
        }

        // If not found.
        setPage(0);
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
     * Get current page number.
     * @return  Current page number.
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * Get page count.
     * @return  Page count.
     */
    public int getPageCount()
    {
        return pages.size();
    }

    /**
     * Initialize page.
     * @param destIndex
     * @param pageIndex
     */
    private void initializePage(int destIndex, int pageIndex)
    {
        final List<Emoji> page = pages.get(pageIndex);
        if(keyboards[destIndex].getKeys().size() != page.size())
            keyboards[destIndex] = EmojidexKeyboard.create(context);
        keyboards[destIndex].initialize(page);
        views[destIndex].setKeyboard(keyboards[destIndex]);
    }
}
