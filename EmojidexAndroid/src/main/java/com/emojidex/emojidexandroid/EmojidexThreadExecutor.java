package com.emojidex.emojidexandroid;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by kou on 17/08/25.
 */

class EmojidexThreadExecutor implements Executor {
    final ArrayDeque<Runnable> tasks = new ArrayDeque<Runnable>();

    @Override
    public void execute(@NonNull final Runnable command)
    {
        synchronized (tasks)
        {
            tasks.offer(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        command.run();
                    }
                    finally
                    {
                        startNextTask();
                    }
                }
            });
        }

        final ThreadPoolExecutor tpe = (ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR;
        if(tpe.getActiveCount() < tpe.getCorePoolSize())
            startNextTask();
    }

    private void startNextTask()
    {
        synchronized (tasks)
        {
            final Runnable task = tasks.poll();
            if(task != null)
                AsyncTask.THREAD_POOL_EXECUTOR.execute(task);
        }
    }
}
