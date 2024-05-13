package com.example.rockpaperscissor;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class GameStats extends AppCompatActivity {
    private GameStatsManager gameStatsManager;
    private SharedPreferences sp;

    // onCreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.game_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // SharedPreferences
        sp = getSharedPreferences("gameStats", Context.MODE_PRIVATE);
        gameStatsManager = new GameStatsManager(this);

        updateStats();

    }
    private void updateStats(){
        TextView gamesPlayedTextView = findViewById(R.id.gamesPlayed);
        TextView gamesWonTextView = findViewById(R.id.gamesWon);
        TextView gamesLostTextView = findViewById(R.id.gamesLost);
        TextView gamesDrawnTextView = findViewById(R.id.gamesDrawn);
        TextView longestStreakTextView = findViewById(R.id.longestStreak);
        TextView currentStreakTextView = findViewById(R.id.currentStreak);

//        int gamesPlayed = Integer.parseInt(gamesPlayedTextView.getText().toString());
//        int gamesWon = Integer.parseInt(gamesWonTextView.getText().toString());
//        int gamesLost = Integer.parseInt(gamesLostTextView.getText().toString());
//        int gamesDrawn = Integer.parseInt(gamesDrawnTextView.getText().toString());
//        int longestStreak = Integer.parseInt(longestStreakTextView.getText().toString());
//        int currentStreak = Integer.parseInt(currentStreakTextView.getText().toString());

        int gamesPlayed = gameStatsManager.getGamesPlayed();
        int gamesWon = gameStatsManager.getGamesWon();
        int gamesLost = gameStatsManager.getGamesLost();
        int gamesDrawn = gameStatsManager.getGamesDrawn();
        int longestStreak = gameStatsManager.getLongestStreak();
        int currentStreak = gameStatsManager.getCurrentStreak();

        gamesPlayedTextView.setText(String.valueOf(gamesPlayed));
        gamesWonTextView.setText(String.valueOf(gamesWon));
        gamesLostTextView.setText(String.valueOf(gamesLost));
        gamesDrawnTextView.setText(String.valueOf(gamesDrawn));
        longestStreakTextView.setText(String.valueOf(longestStreak));
        currentStreakTextView.setText(String.valueOf(currentStreak));

        animateTextView(0,gamesPlayed, gamesPlayedTextView);
        animateTextView(0,gamesWon, gamesWonTextView);
        animateTextView(0,gamesLost, gamesLostTextView);
        animateTextView(0,gamesDrawn, gamesDrawnTextView);
        animateTextView(0,longestStreak, longestStreakTextView);
        animateTextView(0,currentStreak, currentStreakTextView);

    }


    // text animation (number increasing)
    public void animateTextView(int initialValue, int finalValue, final TextView  textview) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        if(finalValue<=150) valueAnimator.setDuration(finalValue*10);
        else valueAnimator.setDuration(1500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textview.setText(valueAnimator.getAnimatedValue().toString());

            }
        });
        valueAnimator.start();
    }
}
