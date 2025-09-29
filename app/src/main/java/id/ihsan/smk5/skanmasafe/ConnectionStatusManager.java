/*
 * Copyright (C) 2025 Muhammad Ihsan
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
package id.ihsan.smk5.skanmasafe;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectionStatusManager {

    private final Context context;
    private final ImageView statusIcon;
    private final TextView statusText;

    private boolean isConnected = true;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ConnectionStatusManager(Context context, ImageView statusIcon, TextView statusText) {
        this.context = context;
        this.statusIcon = statusIcon;
        this.statusText = statusText;
    }

    public void startMonitoring() {
        handler.post(connectionCheckRunnable);
    }

    public void stopMonitoring() {
        handler.removeCallbacks(connectionCheckRunnable);
    }

    private final Runnable connectionCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkConnection();
            handler.postDelayed(this, 5000); // cek setiap 5 detik
        }
    };

    private void checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        NetworkCapabilities nc = cm.getNetworkCapabilities(network);

        if (nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            int downSpeed = nc.getLinkDownstreamBandwidthKbps();
            updateStatusBar(downSpeed);
            isConnected = true;
        } else if (nc != null) {
            updateStatusBar(1); // koneksi lemah
            isConnected = true;
        } else {
            updateStatusBar(0); // koneksi hilang
            if (isConnected) {
                isConnected = false;
                showConnectionLostDialog();
            }
        }
    }

    private void updateStatusBar(int speedKbps) {
        ((android.app.Activity) context).runOnUiThread(() -> {
            if (speedKbps > 500) {
                statusIcon.setImageResource(R.drawable.ic_signal_good);
                statusText.setText("Koneksi bagus");
                statusText.setTextColor(context.getResources().getColor(R.color.connection_good));
            } else if (speedKbps > 0) {
                statusIcon.setImageResource(R.drawable.ic_signal_warning);
                statusText.setText("Koneksi lemah");
                statusText.setTextColor(context.getResources().getColor(R.color.connection_warning));
            } else {
                statusIcon.setImageResource(R.drawable.ic_signal_bad);
                statusText.setText("Koneksi hilang");
                statusText.setTextColor(context.getResources().getColor(R.color.connection_bad));
            }
        });
    }

    private void showConnectionLostDialog() {
        ((android.app.Activity) context).runOnUiThread(() -> {
            new AlertDialog.Builder(context)
                    .setTitle("Koneksi Terputus")
                    .setMessage("Koneksi internet terputus. Silakan periksa jaringan Anda.")
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show();
        });
    }
}
