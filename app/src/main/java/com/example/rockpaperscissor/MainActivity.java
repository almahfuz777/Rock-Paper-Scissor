package com.example.rockpaperscissor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    // to the main game area
    public void playSinglePlayer(View view) {
        Intent intent = new Intent(this, Playground.class);
        startActivity(intent);
    }
    public void playMultiplayer(View view) {
        Intent intent = new Intent(this, MultiplayerActivity.class);
//        intent.putExtra("GAMEMODE","multiplayer");
        startActivity(intent);
    }
    public void stats(View view) {
        Intent intent = new Intent(this, GameStats.class);
        startActivity(intent);
    }
    public void info(View view) {
        Intent intent = new Intent(this, Info.class);
        startActivity(intent);
    }
}