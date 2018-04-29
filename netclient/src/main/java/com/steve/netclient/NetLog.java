package com.steve.netclient;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.steve.netclient.NetClient.applicationName;
import static com.steve.netclient.NetClient.debugMode;

/**
 * Created by Steve Tchatchouang on 26/01/2018
 */

public class NetLog {

    private static volatile NetLog mInstance;

    private static final int CORE_POOL_SIZE  = 1;
    private static final int MAX_POOL_SIZE   = 1;
    private static final int KEEP_ALIVE_TIME = 60;

    private static final String TAG            = NetLog.class.getSimpleName();
    private static final String DATE_FORMAT    = "dd_MM_yy_HH_mm";
    private static final String TIME_FORMAT    = "HH:mm:ss";
    private static final String FILE_EXTENSION = ".txt";


    private OutputStreamWriter writer;
    private Executor           logExecutor;
    private SimpleDateFormat   timeFormat;

    private NetLog() {
        if (!debugMode) {
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.FRENCH);
        timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.FRENCH);
        logExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
        File logDir = new File(
                Environment.getExternalStorageDirectory(),
                applicationName+"_netLogs"
        );
        if (!logDir.exists() && !logDir.mkdirs()) {
            return;
        }

        File logFile = new File(
                logDir,
                applicationName + "_" + dateFormat.format(new Date(System.currentTimeMillis()))
                        + FILE_EXTENSION
        );

        try {
            FileOutputStream fos = new FileOutputStream(logFile);
            writer = new OutputStreamWriter(fos);
            writer.write("-----Network Log for " + applicationName + " -> Date " +
                    dateFormat.format(new Date()) + "----\n\n\n");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized NetLog getInstance() {
        if (mInstance == null) {
            mInstance = new NetLog();
        }
        return mInstance;
    }

    public static void e(final String message, final Throwable e) {
        if (getInstance().writer != null) {
            getInstance().logExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getInstance().writer.write(getInstance().timeFormat.format(new Date()));
                        getInstance().writer.write(" : ERROR : " + message + "\n");
                        if (e != null) {
                            StringWriter w = new StringWriter();
                            e.printStackTrace(new PrintWriter(w));
                            getInstance().writer.write(w.toString());
                        }
                        getInstance().writer.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        Log.e(TAG, "run: ", e1);
                    }
                    if (message != null) {
                        Log.println(Log.ASSERT, TAG, message);
                        Log.e(TAG, message, e);
                    }
                }
            });
        }
    }

    public static void e(Throwable throwable) {
        e(throwable.getMessage(), throwable);
    }

    public static void m(final String message) {
        if (getInstance().writer != null) {
            getInstance().logExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getInstance().writer.write(getInstance().timeFormat.format(new Date()));
                        getInstance().writer.write(" : MESSAGE : " + message + "\n");
                        getInstance().writer.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        Log.e(TAG, "run: ", e1);
                    }
                    Log.println(Log.ASSERT, TAG, message);
                }
            });
        }
    }
}
