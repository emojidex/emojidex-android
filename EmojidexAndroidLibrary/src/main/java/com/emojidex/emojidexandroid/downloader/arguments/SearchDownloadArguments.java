package com.emojidex.emojidexandroid.downloader.arguments;

/**
 * Created by kou on 17/08/29.
 */

public class SearchDownloadArguments extends AbstractJsonDownloadArguments<SearchDownloadArguments> {
    private String word;
    private String category;

    /**
     * Construct object.
     * @param word      Search word.
     */
    public SearchDownloadArguments(String word)
    {
        super();

        setWord(word);
        category = null;
    }

    /**
     * Get search word.
     * @return      Search word.
     */
    public String getWord()
    {
        return word;
    }

    /**
     * Set search word.
     * @param word      Search word.
     * @return          Self.
     */
    public SearchDownloadArguments setWord(String word)
    {
        this.word = (word != null) ? word : "";
        return this;
    }

    /**
     * Get search category.
     * @return      Search category.
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * Set search category.
     * @param category      Search category.(Default value is null)
     * @return              Self.
     */
    public SearchDownloadArguments setCategory(String category)
    {
        this.category = category;
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof SearchDownloadArguments) )
            return false;

        final SearchDownloadArguments arg = (SearchDownloadArguments)obj;

        return      ArgumentsUtils.equals(word, arg.word)
                &&  ArgumentsUtils.equals(category, arg.category)
                &&  super.equals(obj)
                ;
    }
}
