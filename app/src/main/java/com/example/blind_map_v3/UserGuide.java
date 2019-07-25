package com.example.blind_map_v3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class UserGuide extends AppCompatActivity {


    PDFView userguide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        userguide = findViewById(R.id.pdfuserguide);
        String locale = Locale.getDefault().toString();
        if(locale.equals("lv_LV"))
            userguide.fromAsset("USERGUIDE_LV.pdf").load();
        else if(locale.equals("ru_RU"))
            userguide.fromAsset("USERGUIDE_RU.pdf").load();
        else
            userguide.fromAsset("USERGUIDE_ENG.pdf").load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item2, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.back) {
            Intent i = new Intent(UserGuide.this, MainActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
