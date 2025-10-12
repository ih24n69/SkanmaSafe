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
import id.ihsan.smk5.skanmasafe.ConnectionStatusManager;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExamActivity extends AppCompatActivity {

    private MediaPlayer alarmPlayer;
    private boolean isFabOpen = false;
    private FloatingActionButton fabMain, fabMenu1, fabMenu2;
    private boolean isShowingExitDialog = false;

    // Tambahan jam & baterai
    private TextView tvClock, tvBattery;
    private ImageView ivBattery;
    private Handler clockHandler = new Handler();
    private Runnable clockRunnable;
	
	// Monitoring Koneksi
	private ConnectionStatusManager connectionStatusManager;
	
	private boolean isLegitExit = false;
	public boolean isShowingConnectionDialog = false;

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        int batteryPct = (int) (level * 100 / (float) scale);
        tvBattery.setText(batteryPct + "%");

        // === Fallback ke vector asset aplikasi sendiri ===
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                ivBattery.setImageResource(R.drawable.ic_battery_charging);
            } else {
                if (batteryPct >= 95) {
                    ivBattery.setImageResource(R.drawable.ic_battery_full);
                } else if (batteryPct >= 80) {
                    ivBattery.setImageResource(R.drawable.ic_battery_80);
				} else if (batteryPct >= 70) {
                    ivBattery.setImageResource(R.drawable.ic_battery_70);
				} else if (batteryPct >= 50) {
                    ivBattery.setImageResource(R.drawable.ic_battery_50);
				} else if (batteryPct >= 40) {
                    ivBattery.setImageResource(R.drawable.ic_battery_40);
				} else if (batteryPct >= 30) {
                    ivBattery.setImageResource(R.drawable.ic_battery_30);
				} else if (batteryPct >= 20) {
                    ivBattery.setImageResource(R.drawable.ic_battery_20);
                } else {
                    ivBattery.setImageResource(R.drawable.ic_battery_alert);
                }
            }
    }
};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
		
		ImageView statusIcon = findViewById(R.id.statusIcon);
        TextView statusText = findViewById(R.id.statusText);

		/** Monitoring Koneksi */
        connectionStatusManager = new ConnectionStatusManager(this, statusIcon, statusText);
        connectionStatusManager.startMonitoring();

        /** Blokir screenshot & rekam layar. Layar tetap hidup */
        secureAndKeepScreenOn();

        WebView webView = findViewById(R.id.webView);
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				// Diam saja supaya WebView tidak ubah tampilan atau reload
				Log.w("ExamActivity", "Koneksi error (ignore): " + error.getDescription());
			}

			@Override
			public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
				// Diam juga untuk HTTP error (misal 404 saat koneksi putus)
				Log.w("ExamActivity", "HTTP error (ignore): " + errorResponse.getStatusCode());
			}

			@Override
			public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
				// Log dulu biar tahu apa yang terjadi
				Log.e("ExamActivity", "Render process gone (crash or low memory)");

				// Kalau kamu mau biar WebView tidak reload otomatis:
				// cukup return true -> berarti kamu "menangani" event-nya sendiri.
				return true;
			}
		});

        // FAB Menu
        fabMain = findViewById(R.id.fabMain);
        fabMenu1 = findViewById(R.id.fabRefresh);
        fabMenu2 = findViewById(R.id.fabExit);

        fabMain.setOnClickListener(v -> toggleFabMenu());
        fabMenu1.setOnClickListener(v -> webView.reload());
        fabMenu2.setOnClickListener(v -> showExitDialog());

        // Inisialisasi jam & baterai
        tvClock = findViewById(R.id.tvClock);
        tvBattery = findViewById(R.id.tvBattery);
        ivBattery = findViewById(R.id.ivBattery);

        // Update jam realtime
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(new Date());
                tvClock.setText(currentTime);
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);

        // Register battery receiver
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // WebView setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadsImagesAutomatically(true);

        // Blokir long-press (anti copy paste, keyboard tetap jalan)
        webView.setOnLongClickListener(v -> true);
        webView.setLongClickable(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(
                        "document.getElementsByTagName('html')[0].setAttribute('translate','no');",
                        null
                );
            }
        });

        // Load URL
        String rawUrl = getIntent().getStringExtra("url");
        String url = normalizeUrl(rawUrl);

        if (isConnected() && url != null) {
            webView.loadUrl(url);
        } else {
            Toast.makeText(this,
                    "Tidak ada koneksi internet atau URL tidak valid",
                    Toast.LENGTH_LONG).show();
        }

        // Lock ke exam mode
        lockExamMode();
        startLockTaskMode();

        // Cek multiwindow / overlay
        blockMultiWindowAndPip();
    }

    @Override
    protected void onResume() {
        super.onResume();
		checkOverlayAndSecurity();
		updateFabVisibility();
    }

	/** Anti Multi Window */
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (isInMultiWindowMode) {
            Toast.makeText(this, "Mode multi-window tidak diizinkan saat ujian", Toast.LENGTH_LONG).show();
            stopLockTask();
            finish();
        }
    }

	/** Anti PiP */
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            Toast.makeText(this, "Mode Picture-in-Picture tidak diizinkan saat ujian", Toast.LENGTH_LONG).show();
            stopLockTask();
            finish();
        }
    }

    /** Anti Floating Window */
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!hasFocus && !isShowingExitDialog && !isShowingConnectionDialog) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
				Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
				// Android 12 (S) & 13 (Tiramisu) → kasih delay supaya "Got it" / "Viewing full screen" nggak salah deteksi
				new Handler(Looper.getMainLooper()).postDelayed(() -> {
					if (!hasWindowFocus() && !isShowingExitDialog && !isShowingConnectionDialog) {
						Toast.makeText(this,
								"Aplikasi overlay terdeteksi, tutup dulu untuk melanjutkan ujian",
								Toast.LENGTH_LONG).show();
						stopLockTask();
						finish();
					}
				}, 2000); // 1 detik delay
			} else {
				// Android 6–11, 14+ → langsung eksekusi
				Toast.makeText(this,
						"Aplikasi overlay terdeteksi, tutup dulu untuk melanjutkan ujian",
						Toast.LENGTH_LONG).show();
				stopLockTask();
				finish();
			}
		}
	}
		
	/** Anti Overlay */
	private void checkOverlayAndSecurity() {
		/** Android 6 - 11 → hanya bisa warning overlay dan kembali ke halaman awal */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
				Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
			if (Settings.canDrawOverlays(this)) {
				Toast.makeText(this,
						"Nonaktifkan aplikasi overlay (chat head, filter layar, dsb) sebelum melanjutkan ujian!",
						Toast.LENGTH_LONG).show();
				stopLockTask();
				finish();
				return;
			}
		}

		/** Blokir overlay eksternal (Android 12+) */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			try {
				getWindow().setHideOverlayWindows(true);
			} catch (SecurityException e) {
				e.printStackTrace();
				Toast.makeText(this,
						"Nonaktifkan aplikasi overlay (chat head, filter layar, dsb) sebelum ujian!",
						Toast.LENGTH_LONG).show();
				stopLockTask();
				finish();
				return;
			}

			/** Cegah multi-window / PiP */
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				if (isInMultiWindowMode() || isInPictureInPictureMode()) {
					Toast.makeText(this,
							"Mode multi-window / PiP tidak diizinkan saat ujian",
							Toast.LENGTH_LONG).show();
					stopLockTask();
					finish();
				}
			}
		}
	}
	
	/** Anti Screenshood,screenrecord, layar tetap nyala ketika ujian */
	private void secureAndKeepScreenOn() {
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();

		// Aktifkan FLAG_SECURE jika belum aktif
		if ((params.flags & WindowManager.LayoutParams.FLAG_SECURE) == 0) {
			window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		}

		// Pastikan layar tetap hidup
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

    /** Konfirmasi keluar ujian */
    private void showExitDialog() {
        isShowingExitDialog = true;
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Yakin ingin keluar dari ujian?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    isShowingExitDialog = false;
					isLegitExit = true;
                    stopLockTask();		
					triggerNormalExitAlarm();
                    finish();
                })
                .setNegativeButton("Tidak", (dialog, which) -> {
                    isShowingExitDialog = false;
                    dialog.dismiss();
                })
                .setOnDismissListener(d -> isShowingExitDialog = false)
                .show();
    }

    /** Tolak multi-window dan Picture-in-Picture */
    private void blockMultiWindowAndPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInMultiWindowMode() || isInPictureInPictureMode()) {
                Toast.makeText(this,
                        "Mode multi-window / PiP tidak diizinkan saat ujian",
                        Toast.LENGTH_LONG).show();
                stopLockTask();
                finish();
            }
        }
    }

    /** Animasi FAB Menu */
    private void toggleFabMenu() {
        float translationY1 = -150f;
        float translationY2 = -300f;

        if (!isFabOpen) {
            fabMenu1.setVisibility(View.VISIBLE);
            fabMenu2.setVisibility(View.VISIBLE);

            fabMenu1.animate().translationY(translationY1).setInterpolator(new OvershootInterpolator()).start();
            fabMenu2.animate().translationY(translationY2).setInterpolator(new OvershootInterpolator()).start();

            ObjectAnimator rotate = ObjectAnimator.ofFloat(fabMain, "rotation", 0f, 180f);
            rotate.setDuration(200);
            rotate.start();

            isFabOpen = true;
        } else {
            fabMenu1.animate().translationY(0).withEndAction(() -> fabMenu1.setVisibility(View.GONE)).start();
            fabMenu2.animate().translationY(0).withEndAction(() -> fabMenu2.setVisibility(View.GONE)).start();

            ObjectAnimator rotate = ObjectAnimator.ofFloat(fabMain, "rotation", 180f, 0f);
            rotate.setDuration(200);
            rotate.start();

            isFabOpen = false;
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /** Screen pinning */
    private void startLockTaskMode() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
			// sebelum pinning → sembunyikan FAB
			updateFabVisibility();
			startLockTask();
		}
	}

	/** Merubah inputan URL */
    private String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.contains(":")) {
                url = "http://" + url;
            } else {
                url = "https://" + url;
            }
        }
        return url;
    }

    private void lockExamMode() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
		stopLockTask();
        if (!isLegitExit) {
            triggerAlarm();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Tombol kembali dinonaktifkan saat ujian", Toast.LENGTH_SHORT).show();
    }

    private void triggerAlarm() {
        try {
            AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);

            alarmPlayer = MediaPlayer.create(this,
                    Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.exit_alarm));
            alarmPlayer.setLooping(false);
            alarmPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockRunnable);
        unregisterReceiver(batteryReceiver);
    }
	
	/** Sembunyikan / tampilkan FAB sesuai kondisi screen pinning */
	private void updateFabVisibility() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		boolean isPinned;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			isPinned = am.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
		} else {
			isPinned = am.isInLockTaskMode();
		}

		if (isPinned) {
			// Sembunyikan FAB saat pinning aktif
			fabMain.setVisibility(View.GONE);
			fabMenu1.setVisibility(View.GONE);
			fabMenu2.setVisibility(View.GONE);
		} else {
			// Munculkan lagi jika pinning selesai
			fabMain.setVisibility(View.VISIBLE);
		}
	}
	
	/** Keluar ujian dengan Sah */
	private void triggerNormalExitAlarm() {
		Log.d("Exam", "Ujian selesai dengan sah");
        try {
            AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);

            alarmPlayer = MediaPlayer.create(this,
                    Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.legit_alarm));
            alarmPlayer.setLooping(false);
            alarmPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}