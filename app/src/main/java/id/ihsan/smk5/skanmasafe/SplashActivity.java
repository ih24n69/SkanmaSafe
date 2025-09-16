package id.ihsan.smk5.skanmasafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);

        // Animasi logo: fade + scale + rotate
        logo.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .rotationBy(360f)
                .setDuration(1200)
                .withEndAction(() -> {
                    // Logo kembali ke normal size
                    logo.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start();
                })
                .start();

        // Animasi teks app name
        appName.animate()
                .alpha(1f)
                .translationYBy(-20f)
                .setStartDelay(1000)
                .setDuration(800)
                .start();

        // Delay pindah ke MainActivity
        new Handler().postDelayed(() -> {
            // Fade out logo & text
            logo.animate().alpha(0f).setDuration(600).start();
            appName.animate().alpha(0f).setDuration(600).withEndAction(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }).start();
        }, SPLASH_DURATION);
    }
}
