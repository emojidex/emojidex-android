package com.emojidex.emojidexandroid.imageloader;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

class TaskManager {
    static final int HANDLE_NULL = -1;
    private final Collection<ImageLoadTask> tasks;

    public TaskManager()
    {
        tasks = new LinkedList<ImageLoadTask>();
    }

    /**
     * Regist new task.
     * @param arguments     Task arguments.
     * @return      Task handle.
     *              If regist failed, return HANDLE_NULL.
     */
    public int regist(ImageLoadArguments arguments)
    {
        // Find task.
        for(ImageLoadTask task : tasks)
            if(arguments.equals(task.getArguments()))
                return HANDLE_NULL;

        // Run new task.
        final ImageLoadTask task = new ImageLoadTask(arguments);
        task.execute();

        tasks.add(task);

        return task.getHandle();
    }

    /**
     * Finish task.
     * @param handle        Task handle.
     */
    public void finishTask(int handle)
    {
        final Iterator<ImageLoadTask> it = tasks.iterator();
        while(it.hasNext())
        {
            final ImageLoadTask task = it.next();
            if(task.getHandle() == handle)
            {
                it.remove();
                break;
            }
        }
    }
}
