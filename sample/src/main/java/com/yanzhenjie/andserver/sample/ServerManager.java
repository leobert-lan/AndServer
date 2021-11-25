/*
 * Copyright © 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.andserver.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Created by Zhenjie Yan on 2018/6/9.
 */
public class ServerManager extends BroadcastReceiver {

    private static final String ACTION = "com.yanzhenjie.andserver.receiver";

    private static final String CMD_KEY = "CMD_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    private static final int CMD_VALUE_START = 1;
    private static final int CMD_VALUE_ERROR = 2;
    private static final int CMD_VALUE_STOP = 4;

    /**
     * Notify serverStart.
     *
     * @param context context.
     */
    public static void onServerStart(Context context, String hostAddress) {
        Log.e("lmsg", "sendB:server started");
        sendBroadcast(context, CMD_VALUE_START, hostAddress);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void onServerError(Context context, String error) {
        sendBroadcast(context, CMD_VALUE_ERROR, error);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void onServerStop(Context context) {
        sendBroadcast(context, CMD_VALUE_STOP);
    }

    private static void sendBroadcast(Context context, int cmd) {
        sendBroadcast(context, cmd, null);
    }

    private static void sendBroadcast(Context context, int cmd, String message) {
        Intent broadcast = new Intent(ACTION);
        broadcast.putExtra(CMD_KEY, cmd);
        broadcast.putExtra(MESSAGE_KEY, message);
        context.sendBroadcast(broadcast);
    }


    private static ServerManager instance = null;

    public static ServerManager getInstance(ContextWrapper contextWrapper, @Nullable OnServerStateChangedListener onServerStateChangedListener) {
        if (instance == null)
            instance = new ServerManager(contextWrapper, onServerStateChangedListener);
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            instance.stopServer();
            instance.unRegister();
            instance = null;
        }
    }

    private final ContextWrapper contextWrapper;
    private final Intent mService;

    @Nullable
    private final OnServerStateChangedListener onServerStateChangedListener;

    private ServerManager(ContextWrapper contextWrapper, @Nullable OnServerStateChangedListener onServerStateChangedListener) {
        this.contextWrapper = contextWrapper;
        mService = new Intent(contextWrapper, CoreService.class);
        this.onServerStateChangedListener = onServerStateChangedListener;
    }

    /**
     * Register broadcast.
     */
    public void register() {
        IntentFilter filter = new IntentFilter(ACTION);
        contextWrapper.registerReceiver(this, filter);
    }

    /**
     * UnRegister broadcast.
     */
    private void unRegister() {
        contextWrapper.unregisterReceiver(this);
    }

    public void startServer() {
        contextWrapper.startService(mService);
    }

    public void stopServer() {
        contextWrapper.stopService(mService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("lmsg", "receivedB:"+intent.getAction()+" "+intent.getIntExtra(CMD_KEY, 0));
        if (onServerStateChangedListener == null) return;
        String action = intent.getAction();
        if (ACTION.equals(action)) {
            int cmd = intent.getIntExtra(CMD_KEY, 0);
            switch (cmd) {
                case CMD_VALUE_START: {
                    String ip = intent.getStringExtra(MESSAGE_KEY);
                    onServerStateChangedListener.onServerStart(ip);
                    break;
                }
                case CMD_VALUE_ERROR: {
                    String error = intent.getStringExtra(MESSAGE_KEY);
                    onServerStateChangedListener.onServerError(error);
                    break;
                }
                case CMD_VALUE_STOP: {
                    onServerStateChangedListener.onServerStop();
                    break;
                }
            }
        }
    }

    public interface OnServerStateChangedListener {
        /**
         * Start notify.
         */
        void onServerStart(String ip);

        /**
         * Error notify.
         */
        void onServerError(String message);

        /**
         * Stop notify.
         */
        void onServerStop();
    }
}