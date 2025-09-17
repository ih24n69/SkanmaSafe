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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String DEFAULT_URL; // tidak final

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
    }
}
