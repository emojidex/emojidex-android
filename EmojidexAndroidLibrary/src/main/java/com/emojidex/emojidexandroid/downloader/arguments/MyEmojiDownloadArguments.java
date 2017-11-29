package com.emojidex.emojidexandroid.downloader.arguments;

/**
 * Created by Yoshida on 2017/11/29.
 */

public class MyEmojiDownloadArguments extends AbstractJsonDownloadArguments<MyEmojiDownloadArguments> {
    private String username;
    private int limit;
    private int startPage;

    /**
     * Construct object.
     */
    public MyEmojiDownloadArguments(String username)
    {
        super();
        this.username = username;
        limit = 50;
        startPage = 1;
    }

    /**
     * Get username.
     * @return username.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Get start page.
     * @return start page.
     */
    public int getStartPage()
    {
        return startPage;
    }

    /**
     * Get limit.
     * @return limit.
     */
    public int getLimit()
    {
        return limit;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof MyEmojiDownloadArguments)) return false;

        final MyEmojiDownloadArguments arg = (MyEmojiDownloadArguments)obj;

        return      ArgumentsUtils.equals(username, arg.username)
                &&  limit != arg.limit
                &&  startPage != arg.startPage
                &&  super.equals(obj)
                ;
    }
}
