package com.example.amadapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class ResourceDetailActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvTitle, tvDate, tvContent;
    private Button btnReadMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Init Views
        ivImage = findViewById(R.id.ivDetailImage);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDate = findViewById(R.id.tvDetailDate);
        tvContent = findViewById(R.id.tvDetailContent);
        btnReadMore = findViewById(R.id.btnReadMore);

        // Get Data
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String date = getIntent().getStringExtra("date");
        String image = getIntent().getStringExtra("image");
        String url = getIntent().getStringExtra("url");

        // Set Data
        tvTitle.setText(title);
        tvContent.setText(content);
        tvDate.setText(date);

        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).into(ivImage);
        }

        // Handle URL click
        if (url != null && !url.isEmpty()) {
            btnReadMore.setVisibility(View.VISIBLE);
            btnReadMore.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            });
        } else {
            btnReadMore.setVisibility(View.GONE);
        }
    }
}