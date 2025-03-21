package com.fadhlillahb.covidtracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class primaryMenu extends AppCompatActivity {

    Button btnHome, btnHistory, btnHelp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary_menu);

        btnHome = findViewById(R.id.home);
        btnHistory = findViewById(R.id.history);
        btnHelp = findViewById(R.id.help);

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(this, menuController.class);
            i.putExtra("frgToLoad", 1);
            startActivity(i);
        });

        btnHistory.setOnClickListener(v -> {
            Intent i = new Intent(this, menuController.class);
            i.putExtra("frgToLoad", 2);
            startActivity(i);
        });

        btnHelp.setOnClickListener(v -> {
            Intent i = new Intent(this, menuController.class);
            i.putExtra("frgToLoad", 3);
            startActivity(i);
        });


    }


}
