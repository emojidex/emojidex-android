package com.emojidex.emojidexandroid.downloader.arguments;

/**
 * Created by kou on 17/08/29.
 */

public class IndexDownloadArguments extends AbstractJsonDownloadArguments<IndexDownloadArguments> {
    private int limit;
    private int startPage;
    private int endPage;

    /**
     * Construct object.
     */
    public IndexDownloadArguments()
    {
        super();

        limit = 50;
        startPage = 1;
        endPage = 1;
    }

    /**
     * Get limit.
     * @return      Limit.
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Set limit.
     * @param limit     Limit.(Default value is 50)
     * @return          Self.
     */
    public IndexDownloadArguments setLimit(int limit)
    {
        this.limit = limit;
        return this;
    }

    /**
     * Get start page.
     * @return      Start page.
     */
    public int getStartPage()
    {
        return startPage;
    }

    /**
     * Set start page.
     * @param startPage     Start page.(Default value is 1)
     * @return              Self.
     */
    public IndexDownloadArguments setStartPage(int startPage)
    {
        this.startPage = startPage;
        return this;
    }

    /**
     * Get end page.
     * @return      End page.
     */
    public int getEndPage()
    {
        return endPage;
    }

    /**
     * Set end page.
     * @param endPage       End page.(Default value is 1);
     * @return              Self.
     */
    public IndexDownloadArguments setEndPage(int endPage)
    {
        this.endPage = endPage;
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof IndexDownloadArguments) )
            return false;

        final IndexDownloadArguments arg = (IndexDownloadArguments)obj;

        return      limit != arg.limit
                &&  startPage != arg.startPage
                &&  endPage != arg.endPage
                &&  super.equals(obj)
                ;
    }
}
