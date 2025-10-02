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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String DEFAULT_URL; // tidak final
	private TextView tvGreeting;
	private TextView tvSubGreeting;
    private ImageView greetingIcon;
    private ImageView greetingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		DEFAULT_URL = getString(R.string.default_url);

        Button btnInputLink = findViewById(R.id.btnInputLink);
        Button btnMulaiUjian = findViewById(R.id.btnMulaiUjian);
        Button btnPeraturan = findViewById(R.id.btnPeraturan);
        Button btnAbout = findViewById(R.id.btnAbout);

        btnInputLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InputLinkActivity.class);
            startActivity(intent);
        });

        btnMulaiUjian.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExamActivity.class);
            intent.putExtra("url", DEFAULT_URL);
            startActivity(intent);
        });

        btnPeraturan.setOnClickListener(v -> {
			// Inflate layout WebView
			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
			View dialogView = inflater.inflate(R.layout.dialog_peraturan, null);
			WebView webView = dialogView.findViewById(R.id.webViewDialog);
			
			webView.getSettings().setJavaScriptEnabled(false);
			webView.getSettings().setAllowFileAccess(true);
			webView.getSettings().setAllowContentAccess(true);

			webView.loadUrl("https://ihsan.smkn5sukoharjo.sch.id/skanmasafe/peraturan.html");

			new AlertDialog.Builder(MainActivity.this)
					.setView(dialogView)
					.setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
					.show();
		});

        btnAbout.setOnClickListener(v -> {
			// Inflate layout WebView
			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
			View dialogView = inflater.inflate(R.layout.dialog_tentang, null);
			WebView webView = dialogView.findViewById(R.id.webViewTentang);
			
			webView.getSettings().setJavaScriptEnabled(false);
			webView.getSettings().setAllowFileAccess(true);
			webView.getSettings().setAllowContentAccess(true);

			webView.loadUrl("https://ihsan.smkn5sukoharjo.sch.id/skanmasafe/about.html");

			new AlertDialog.Builder(MainActivity.this)
					.setView(dialogView)
					.setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
					.show();
		});
		
		tvGreeting = findViewById(R.id.tvGreeting);
		tvSubGreeting = findViewById(R.id.tvSubGreeting);
        greetingIcon = findViewById(R.id.greetingIcon);
        greetingImage = findViewById(R.id.greetingImage);

        setGreetingByTime();
    }
	
	//Greeting
	private void setGreetingByTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 10) {
            // Pagi
            tvGreeting.setText("Hay, Selamat Pagi !");
			tvSubGreeting.setText("Selamat Datang di SMKN 5 Sukoharjo.");
            greetingIcon.setImageResource(R.drawable.ic_sun); // ikon matahari
            greetingImage.setImageResource(R.drawable.school_day); // gambar sekolah pagi
        } else if (hour >= 10 && hour < 15) {
            // Siang
            tvGreeting.setText("Selamat Siang, Kawan !");
			tvSubGreeting.setText("Selamat Datang di SMKN 5 Sukoharjo.");
            greetingIcon.setImageResource(R.drawable.ic_sun); 
            greetingImage.setImageResource(R.drawable.school_day);
        } else if (hour >= 15 && hour < 18) {
            // Sore
            tvGreeting.setText("Selamat Sore !");
			tvSubGreeting.setText("Selamat menikmati senja ini.");
            greetingIcon.setImageResource(R.drawable.ic_sun);
            greetingImage.setImageResource(R.drawable.school_day);
		} else if (hour >= 18 && hour < 21) {
            // Sore
            tvGreeting.setText("Helo, Selamat Malam !");
			tvSubGreeting.setText("Selamat belajar, untuk masa depan.");
            greetingIcon.setImageResource(R.drawable.ic_moon);
            greetingImage.setImageResource(R.drawable.studying);
        } else {
            // Malam
            tvGreeting.setText("Helo, Selamat Malam !");
			tvSubGreeting.setText("Selamat beristirahat, jangan lupa berdoa.");
            greetingIcon.setImageResource(R.drawable.ic_moon); // ikon bulan
            greetingImage.setImageResource(R.drawable.take_sleep); // gambar tidur/bintang/lampu
        }
    }
}
