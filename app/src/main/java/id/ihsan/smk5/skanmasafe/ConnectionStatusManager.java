package id.ihsan.smk5.skanmasafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

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

        if (nc == null) {
            updateStatusBar(0);
            if (isConnected) {
                isConnected = false;
                showConnectionLostDialog();
            }
            return;
        }

        // Tes koneksi internet sungguhan (bukan hanya Wi-Fi tersambung)
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean hasInternet = hasInternetAccess();

            ((Activity) context).runOnUiThread(() -> {
                if (hasInternet) {
                    int downSpeed = nc.getLinkDownstreamBandwidthKbps();
                    updateStatusBar(downSpeed);
                    isConnected = true;
                } else {
                    updateStatusBar(1); // dianggap lemah karena tidak ada akses internet
                    if (isConnected) {
                        isConnected = false;
                        showConnectionLostDialog();
                    }
                }
            });
        });
    }

    /** Cek koneksi internet nyata dengan HTTP ke Google (204 response = OK) */
    public boolean hasInternetAccess() {
        try {
            HttpURLConnection urlConnection =
                    (HttpURLConnection) new URL("https://clients3.google.com/generate_204").openConnection();
            urlConnection.setConnectTimeout(2000);
            urlConnection.setReadTimeout(2000);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();
            return urlConnection.getResponseCode() == 204 && urlConnection.getContentLength() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    private void updateStatusBar(int speedKbps) {
        ((Activity) context).runOnUiThread(() -> {
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
		Activity activity = (Activity) context;

		activity.runOnUiThread(() -> {
			if (activity.isFinishing()) return;

			// Tambahkan flag agar onWindowFocusChanged tahu ini dialog aman
			if (activity instanceof ExamActivity) {
				((ExamActivity) activity).isShowingConnectionDialog = true;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("Koneksi Terputus")
					.setMessage("Tidak ada akses internet. Periksa jaringan Anda.")
					.setCancelable(false)
					.setPositiveButton("OK", (dialog, which) -> {
						if (activity instanceof ExamActivity) {
							((ExamActivity) activity).isShowingConnectionDialog = false;
						}
						dialog.dismiss();
					})
					.setOnCancelListener(dialog -> {
						if (activity instanceof ExamActivity) {
							((ExamActivity) activity).isShowingConnectionDialog = false;
						}
					});

			AlertDialog dialog = builder.create();

			// Pastikan dialog di atas activity yang sama (bukan overlay)
			dialog.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);

			dialog.show();
		});
	}

}
