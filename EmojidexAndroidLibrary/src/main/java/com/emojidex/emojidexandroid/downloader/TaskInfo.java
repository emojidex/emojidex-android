package com.emojidex.emojidexandroid.downloader;

/**
 * Download task information.
 */
class TaskInfo
{
    private final TYPE type;
    private final String param;

    /**
     * Task type.
     */
    enum TYPE
    {
        UTF,
        EXTENDED,
        INDEX,
        SEARCH,
        EMOJI
    }

    public TaskInfo(TYPE type)
    {
        this(type, null);
    }

    public TaskInfo(TYPE type, String param)
    {
        this.type = type;
        this.param = param;
    }

    public TYPE getType()
    {
        return type;
    }

    public String getParam()
    {
        return param;
    }

    @Override
    public boolean equals(Object obj)
    {
        return (this == obj)
            || (
                    obj instanceof TaskInfo
                &&  this.type == ((TaskInfo)obj).type
                &&  (
                        this.param == null ?
                                this.param == ((TaskInfo)obj).param :
                                this.param.equals(((TaskInfo)obj).param)
                )
            );
    }
}
