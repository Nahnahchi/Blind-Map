package com.example.blind_map_v3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

public class UserGuide extends AppCompatActivity {


    PDFView userguide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        userguide = (PDFView) findViewById(R.id.pdfuserguide);
        userguide.fromAsset("userguide.pdf").load();

    }
}
