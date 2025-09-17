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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InputLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_link);

        EditText etLink = findViewById(R.id.etLink);
        Button btnMulai = findViewById(R.id.btnMulaiUjian);

        btnMulai.setOnClickListener(v -> {
		String url = etLink.getText().toString().trim();

		if (!url.isEmpty()) {
			// Tambahkan pengecekan http/https
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "https://" + url;
			}

			Intent intent = new Intent(InputLinkActivity.this, ExamActivity.class);
			intent.putExtra("url", url);
			startActivity(intent);
		} else {
			Toast.makeText(this, "Masukkan URL terlebih dahulu", Toast.LENGTH_SHORT).show();
		}
	});

    }
}
