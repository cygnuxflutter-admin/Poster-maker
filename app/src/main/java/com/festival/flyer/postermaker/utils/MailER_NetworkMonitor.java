package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;

public class MailER_NetworkMonitor {

    private static final String TAG = "MailER_NetworkMonitor";
    private static MailER_NetworkMonitor instance;
    private final CopyOnWriteArrayList<NetworkStateListener> listeners = new CopyOnWriteArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isRegistered = false;
    private Boolean lastState = null;

    public interface NetworkStateListener {
        void onNetworkStateChanged(boolean isConnected);
    }

    private MailER_NetworkMonitor() {}

    public static synchronized MailER_NetworkMonitor getInstance() {
        if (instance == null) {
            instance = new MailER_NetworkMonitor();
        }
        return instance;
    }

    public void addListener(NetworkStateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkStateListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public synchronized void startMonitoring(Context context) {
        if (context == null || isRegistered) return;

        Context appContext = context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        boolean initialState = MailER_NetworkUtils.isNetworkAvailable(appContext);
        lastState = initialState;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    notifyStateChange(appContext, true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    // Check if any network remains available
                    boolean isConnected = MailER_NetworkUtils.isNetworkAvailable(appContext);
                    notifyStateChange(appContext, isConnected);
                }
            };
            try {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
                isRegistered = true;
                Log.d(TAG, "Registered DefaultNetworkCallback successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error registering NetworkCallback: " + e.getMessage());
            }
        } else {
            NetworkRequest request = new NetworkRequest.Builder().build();
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    notifyStateChange(appContext, true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    boolean isConnected = MailER_NetworkUtils.isNetworkAvailable(appContext);
                    notifyStateChange(appContext, isConnected);
                }
            };
            try {
                connectivityManager.registerNetworkCallback(request, networkCallback);
                isRegistered = true;
            } catch (Exception e) {
                Log.e(TAG, "Error registering NetworkCallback: " + e.getMessage());
            }
        }
    }

    private void notifyStateChange(Context context, boolean isConnected) {
        mainHandler.post(() -> {
            boolean currentState = isConnected && MailER_NetworkUtils.isNetworkAvailable(context);
            if (lastState == null || lastState != currentState) {
                lastState = currentState;
                Log.d(TAG, "Network state changed: " + currentState);
                for (NetworkStateListener listener : listeners) {
                    try {
                        listener.onNetworkStateChanged(currentState);
                    } catch (Exception e) {
                        Log.e(TAG, "Error notifying listener: " + e.getMessage());
                    }
                }
            }
        });
    }

    public boolean isConnected(Context context) {
        return MailER_NetworkUtils.isNetworkAvailable(context);
    }
}
