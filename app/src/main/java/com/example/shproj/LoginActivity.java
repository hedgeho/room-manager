package com.example.shproj;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.security.MessageDigest;

import static com.example.shproj.MainActivity.connect;
import static com.example.shproj.MainActivity.log;
import static com.example.shproj.MainActivity.loge;

public class LoginActivity extends AppCompatActivity {

    MaterialButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn = findViewById(R.id.btn_go);
        EditText et_login = findViewById(R.id.et_login);
        EditText et_password = findViewById(R.id.et_password);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(et_login.getText().toString().replaceAll(" ", "").equals("")
                        || et_password.getText().toString().replaceAll(" ", "").equals("")) {
                    btn.setEnabled(false);
                    log("no data");
//                    btn.setBackground();
                    btn.setBackgroundColor(getColor(android.R.color.darker_gray));
                } else {
                    btn.setEnabled(true);
                    btn.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        et_login.addTextChangedListener(watcher);
        et_password.addTextChangedListener(watcher);

        btn.setOnClickListener(v -> {
            String login = et_login.getText().toString().trim();
            String pw = et_password.getText().toString().trim();

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(pw.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();

                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }

                new Thread(() -> {
                    String cookie = connect("login?username=" + login + "&password=" + hexString, null);
                    if(cookie.length() > 4) {
                        getSharedPreferences("pref", MODE_PRIVATE).edit().putString("cookie", cookie)
                                .putString("login", login).putString("password", pw).apply();

                        setResult(1);
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } catch (Exception e) {
                loge(e);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
