package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.os.AsyncTask;

import com.emojidex.emojidexandroid.downloader.arguments.ArgumentsInterface;
import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ExtendedDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.IndexDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.SearchDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.UTFDownloadArguments;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by kou on 17/08/28.
 */

class TaskManager {
    public final static int TASK_PRIORITY_JSON = 1000;
    public final static int TASK_PRIORITY_IMAGE = 2000;

    private final Map<Integer, ArrayDeque<AbstractDownloadTask>> idleTasks;
    private final Collection<AbstractDownloadTask> runningTasks;

    private final int runningCountMax;

    /**
     * Construct object.
     */
    public TaskManager()
    {
        idleTasks = new TreeMap<Integer, ArrayDeque<AbstractDownloadTask>>();
        runningTasks = new LinkedList<AbstractDownloadTask>();

        runningCountMax = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR).getCorePoolSize();
    }

    /**
     * Regist task of download UTF emojies.
     * @param arguments     Task arguments.
     * @return              Task handle.
     */
    public int registUTF(UTFDownloadArguments arguments)
    {
        return registTask(
                TaskType.UTF,
                arguments,
                new TaskGeneratorInterface() {
                    @Override
                    public AbstractDownloadTask generate(ArgumentsInterface arguments)
                    {
                        return new UTFJsonDownloadTask((UTFDownloadArguments)arguments);
                    }
                },
                TASK_PRIORITY_JSON
        );
    }

    /**
     * Regist task of download extended emojies.
     * @param arguments     Task arguments.
     * @return              Task handle.
     */
    public int registExtended(ExtendedDownloadArguments arguments)
    {
        return registTask(
                TaskType.EXTENDED,
                arguments,
                new TaskGeneratorInterface() {
                    @Override
                    public AbstractDownloadTask generate(ArgumentsInterface arguments)
                    {
                        return new ExtendedJsonDownloadTask((ExtendedDownloadArguments)arguments);
                    }
                },
                TASK_PRIORITY_JSON
        );
    }

    /**
     * Regist task of download index emojies.
     * @param arguments     Task arguments.
     * @return              Task handle.
     */
    public int registIndex(IndexDownloadArguments arguments)
    {
        return registTask(
                TaskType.INDEX,
                arguments,
                new TaskGeneratorInterface() {
                    @Override
                    public AbstractDownloadTask generate(ArgumentsInterface arguments)
                    {
                        return new IndexJsonDownloadTask((IndexDownloadArguments)arguments);
                    }
                },
                TASK_PRIORITY_JSON
        );
    }

    /**
     * Regist task of download search emojies.
     * @param arguments     Task arguments.
     * @return              Task handle.
     */
    public int registSearch(SearchDownloadArguments arguments)
    {
        return registTask(
                TaskType.SEARCH,
                arguments,
                new TaskGeneratorInterface() {
                    @Override
                    public AbstractDownloadTask generate(ArgumentsInterface arguments)
                    {
                        return new SearchJsonDownloadTask((SearchDownloadArguments)arguments);
                    }
                },
                TASK_PRIORITY_JSON
        );
    }

    /**
     * Regist task of download emoji.
     * @param arguments     Task arguments.
     * @return              Task handle.
     */
    public int registEmoji(EmojiDownloadArguments arguments)
    {
        return registTask(
                TaskType.EMOJI,
                arguments,
                new TaskGeneratorInterface() {
                    @Override
                    public AbstractDownloadTask generate(ArgumentsInterface arguments)
                    {
                        return new EmojiJsonDownloadTask((EmojiDownloadArguments)arguments);
                    }
                },
                TASK_PRIORITY_JSON
        );
    }

    /**
     * Regist task of download emoji image.
     * @param arguments     Task arguments.
     * @param context       Context.
     * @return              Task handle.
     */
    public int registImage(ImageDownloadArguments arguments, final Context context)
    {
        return registTask(
                TaskType.IMAGE,
                arguments,
                new TaskGeneratorInterface() {
                    @Override
                    public AbstractDownloadTask generate(ArgumentsInterface arguments)
                    {
                        return new ImageDownloadTask((ImageDownloadArguments)arguments, context);
                    }
                },
                TASK_PRIORITY_IMAGE
        );
    }

    /**
     * Regist task.
     * @param type          Task type.
     * @param arguments     Task arguments.
     * @param generator     Task generator.
     * @param priority      Task priority.
     * @return              Task handle when create new task.
     *                      If create failed, return EmojiDownloader.HANDLE_NULL.
     */
    private int registTask(TaskType type, ArgumentsInterface arguments, TaskGeneratorInterface generator, int priority)
    {
        AbstractDownloadTask task;

        // Skip if has task and already running.
        task = findTaskFromRunning(type, arguments);
        if(task != null)
            return EmojiDownloader.HANDLE_NULL;

        // Replace to head if has task.
        task = findTaskFromIdle(type, arguments);
        if(task != null)
        {
            replaceTask(task);
            return EmojiDownloader.HANDLE_NULL;
        }

        // Generate task.
        task = generator.generate(arguments);
        addIdleTask(priority, task);

//        // Run next task.
//        runNextTasks();

        return task.getHandle();
    }

    /**
     * Add idle task.
     * @param priority      Task priority.
     * @param task          Task.
     */
    private void addIdleTask(int priority, AbstractDownloadTask task)
    {
        synchronized(idleTasks)
        {
            ArrayDeque<AbstractDownloadTask> tasks = idleTasks.get(priority);
            if(tasks == null)
            {
                tasks = new ArrayDeque<AbstractDownloadTask>();
                idleTasks.put(priority, tasks);
            }
            tasks.addFirst(task);
        }
    }

    /**
     * Replace task.
     * @param task      Task.
     */
    private void replaceTask(AbstractDownloadTask task)
    {
        synchronized(idleTasks)
        {
            for(ArrayDeque<AbstractDownloadTask> tasks : idleTasks.values())
            {
                if(tasks.contains(task))
                {
                    tasks.remove(task);
                    tasks.addFirst(task);
                    break;
                }
            }
        }
    }

    /**
     * Find task from idle tasks.
     * @param type          Task type.
     * @param arguments     Task arguments.
     * @return              Find task or null if task is not found.
     */
    private AbstractDownloadTask findTaskFromIdle(TaskType type, ArgumentsInterface arguments)
    {
        synchronized(idleTasks)
        {
            for(ArrayDeque<AbstractDownloadTask> tasks : idleTasks.values())
            {
                for(AbstractDownloadTask task : tasks)
                {
                    if(     type.equals(task.getType())
                        &&  arguments.equals(task.getArguments())   )
                        return task;
                }
            }
        }
        return null;
    }

    /**
     * Find task from running tasks.
     * @param type          Task type.
     * @param arguments     Task arguments.
     * @return              Find task or null if task is not found.
     */
    private AbstractDownloadTask findTaskFromRunning(TaskType type, ArgumentsInterface arguments)
    {
        synchronized(runningTasks)
        {
            for(AbstractDownloadTask task : runningTasks)
            {
                if(     type.equals(task.getType())
                    &&  arguments.equals(task.getArguments())   )
                    return task;
            }
        }
        return null;
    }

    /**
     * Run next tasks.
     */
    public void runNextTasks()
    {
//        AbstractDownloadTask task = null;

        synchronized(runningTasks)
        {
            // Skip if running tasks is full.
            if(runningTasks.size() >= runningCountMax)
                return;

            // Get next task.
            synchronized(idleTasks)
            {
                for(Map.Entry<Integer, ArrayDeque<AbstractDownloadTask>> entry : idleTasks.entrySet())
                {
                    final ArrayDeque<AbstractDownloadTask> tasks = entry.getValue();
                    AbstractDownloadTask task;
                    while( (task = tasks.pollFirst()) != null)
                    {
                        // Add running task.
                        runningTasks.add(task);

                        // Run task.
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        // Skip if running task count is full.
                        if(runningTasks.size() >= runningCountMax)
                            return;
                    }
                }
            }
        }
    }

    /**
     * Finish task.
     * @param handle        Task handle.
     */
    void finishTask(int handle)
    {
        synchronized(runningTasks)
        {
            // Remove running task.
            final Iterator<AbstractDownloadTask> it = runningTasks.iterator();
            while(it.hasNext())
            {
                final AbstractDownloadTask task = it.next();
                if(task.getHandle() == handle)
                {
                    it.remove();
                    break;
                }
            }
        }

        // Run next task.
        runNextTasks();
    }

    /**
     * Cancel task.
     * @param handle    Task handle.
     */
    public void cancelTask(int handle)
    {
        // Find from idle.
        synchronized(idleTasks)
        {
            for(ArrayDeque<AbstractDownloadTask> tasks : idleTasks.values())
            {
                final Iterator<AbstractDownloadTask> it = tasks.iterator();
                while(it.hasNext())
                {
                    final AbstractDownloadTask task = it.next();
                    if(task.getHandle() == handle)
                    {
                        it.remove();
                        task.onCancelled(false);
                        return;
                    }
                }
            }
        }

        // Find from running.
        synchronized(runningTasks)
        {
            final Iterator<AbstractDownloadTask> it = runningTasks.iterator();
            while(it.hasNext())
            {
                final AbstractDownloadTask task = it.next();
                if(task.getHandle() == handle)
                {
                    it.remove();
                    task.cancel(true);
                    return;
                }
            }
        }
    }

    /**
     * Task generator interface.
     */
    private interface TaskGeneratorInterface
    {
        AbstractDownloadTask generate(ArgumentsInterface arguments);
    }
}
