package id.ihsan.smk5.skanmasafe;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExamActivity extends AppCompatActivity {

    private MediaPlayer alarmPlayer;
    private boolean isFabOpen = false;
    private FloatingActionButton fabMain, fabMenu1, fabMenu2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        WebView webView = findViewById(R.id.webView);
        //Button exitButton = findViewById(R.id.exitButton);

        // ==================== FAB Menu ==================== //
        fabMain = findViewById(R.id.fabMain);
        fabMenu1 = findViewById(R.id.fabRefresh);
        fabMenu2 = findViewById(R.id.fabExit);

        fabMain.setOnClickListener(v -> toggleFabMenu());
        fabMenu1.setOnClickListener(v -> Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show());
        fabMenu2.setOnClickListener(v -> Toast.makeText(this, "Keluar", Toast.LENGTH_SHORT).show());
        // ================================================== //

        // WebView setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setSaveFormData(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadsImagesAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(
                        "document.getElementsByTagName('html')[0].setAttribute('translate','no');",
                        null
                );
            }
        });

        String rawUrl = getIntent().getStringExtra("url");
        String url = normalizeUrl(rawUrl);

        if (isConnected() && url != null) {
            webView.loadUrl(url);
        } else {
            Toast.makeText(this,
                    "Tidak ada koneksi internet atau URL tidak valid",
                    Toast.LENGTH_LONG).show();
        }

        lockExamMode();
        startLockTaskMode();
		fabMenu1.setOnClickListener(v -> {
            webView.reload();
        });
        fabMenu2.setOnClickListener(v -> finish());
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

    private void startLockTaskMode() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
            startLockTask();
        }
    }

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
        stopLockTask();
        super.onStop();
        triggerAlarm();
    }
	
	@Override
	public void onBackPressed() {
		Toast.makeText(this, "Tombol kembali dinonaktifkan saat ujian", Toast.LENGTH_SHORT).show();
	}

    private void triggerAlarm() {
        try {
            AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int newVol = (int) (maxVol * 0.9);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);

            alarmPlayer = MediaPlayer.create(this,
                    Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.exit_alarm));
            alarmPlayer.setLooping(false);
            alarmPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
