
package id.ihsan.smk5.skanmasafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText urlInput = findViewById(R.id.urlInput);
        Button startDefault = findViewById(R.id.startDefaultButton);
        Button startManual = findViewById(R.id.startManualButton);

        startDefault.setOnClickListener(v -> {
            String url = getString(R.string.default_url);
            startExam(url);
        });

        startManual.setOnClickListener(v -> {
            String url = urlInput.getText().toString();
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            startExam(url);
        });
    }

    private void startExam(String url) {
        Intent i = new Intent(this, ExamActivity.class);
        i.putExtra("url", url);
        startActivity(i);
    }
}
