package de.timschubert.mediiva;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors
{

    private static final Object LOCK = new Object();
    private static AppExecutors instance;

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO,
                         Executor networkIO,
                         Executor mainThread)
    {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public Executor diskIO() { return diskIO; }
    public Executor networkIO() { return networkIO; }
    public Executor mainThread() { return mainThread; }

    public static AppExecutors getInstance()
    {
        if(instance == null)
        {
            synchronized (LOCK)
            {
                instance = new AppExecutors(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        new MainThreadExecutor());
            }
        }

        return instance;
    }

    private static class MainThreadExecutor implements Executor
    {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable)
        {
            mainThreadHandler.post(runnable);
        }
    }
}
