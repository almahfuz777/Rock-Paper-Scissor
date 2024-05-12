package com.example.rockpaperscissor;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

import pl.droidsonroids.gif.GifImageView;

public class Playground extends AppCompatActivity {
    private int comScore = 0;
    private int playerScore = 0;
    private final String[] choices = {"rock", "paper", "scissor"};
    private ImageView display;
    private MediaPlayer rockSound, paperSound, scissorSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.playground);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // inside onCreate
        rockSound = MediaPlayer.create(this, R.raw.rock_sound);
        paperSound = MediaPlayer.create(this, R.raw.paper_sound);
        scissorSound = MediaPlayer.create(this, R.raw.scissor_sound);

    }

    // Generate random choice from com
    private String comChoice(){
        Random random = new Random();
        int rand = random.nextInt(3);
        return choices[rand];
    }
    private void updateDisplay(String gifName){
        int resourceId = getResources().getIdentifier(gifName, "drawable", getPackageName());

        display = findViewById(R.id.display);
        GifImageView imageView = (GifImageView) display;
        imageView.setBackgroundResource(resourceId);
    }
    private void updateResult(String com, String player){
        Log.d("Update Result", com+" "+player);
        if(com.equals(player)){
            scoring("both");
        }
        else if((com.equals("rock")&&player.equals("paper")) ||
                (com.equals("paper")&&player.equals("scissor")) ||
                (com.equals("scissor")&&player.equals("rock")) ){
            scoring("player");
        }
        else{
            scoring("com");
        }
    }
    private void scoring(String scorer){
        TextView comScoreTextView = findViewById(R.id.comScore);
        TextView playerScoreTextView = findViewById(R.id.playerScore);

        Animation blink_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinktext); // textanimation

        if(scorer.equals("both")){
            playerScore++;
            comScore++;
            comScoreTextView.setText("Comp: "+comScore);    // set new score
            playerScoreTextView.setText("You: "+playerScore);
            comScoreTextView.setTextColor(getResources().getColor(R.color.green));  // set different color for scorer
            playerScoreTextView.setTextColor(getResources().getColor(R.color.green));

            playerScoreTextView.startAnimation(blink_anim); // blink the scorer's score
            comScoreTextView.startAnimation(blink_anim);
        }
        else if(scorer.equals("player")){
            playerScore++;
            playerScoreTextView.setText("You: " + playerScore);
            playerScoreTextView.setTextColor(getResources().getColor(R.color.green));
            comScoreTextView.setTextColor(getResources().getColor(R.color.black));

            playerScoreTextView.startAnimation(blink_anim);
        }
        else {
            comScore++;
            comScoreTextView.setText("Comp: " + comScore);
            comScoreTextView.setTextColor(getResources().getColor(R.color.green));
            playerScoreTextView.setTextColor(getResources().getColor(R.color.black));

            comScoreTextView.startAnimation(blink_anim);
        }

        if(comScore==5||playerScore==5){
            displayWinner();
        }
    }

    // selection
    public void rockSelected(View view) {
        rockSound.start();  // sound effect
        String com = comChoice();
        String gif = com+"_rock";
        updateDisplay(gif);
        updateResult(com, "rock");
    }
    public void paperSelected(View view) {
        paperSound.start();  // sound effect
        String com = comChoice();
        String gif = com+"_paper";
        updateDisplay(gif);
        updateResult(com, "paper");
    }
    public void scissorSelected(View view) {
        scissorSound.start();  // sound effect
        String com = comChoice();
        String gif = com+"_scissor";
        updateDisplay(gif);
        updateResult(com, "scissor");
    }
    private void displayWinner(){

        TextView result = findViewById(R.id.result);
        if(comScore==playerScore){
            result.setText("🤝Match Draw!🤝\n"+comScore+" : "+playerScore);
        }
        else if(playerScore>comScore){
            result.setText("🎉You Win!🎉\n"+comScore+" : "+playerScore);
        }
        else {
            result.setText("😔You Lose!😔\n"+comScore+" : "+playerScore);
        }
        result.setVisibility(View.VISIBLE);

        RelativeLayout layout = findViewById(R.id.selectMoveLayout);
        layout.setVisibility(View.GONE);

        Button resetBtn = findViewById(R.id.resetButton);
        resetBtn.setVisibility(View.VISIBLE);
    }
    public void resetGame(View view) {
        TextView result = findViewById(R.id.result);
        RelativeLayout layout = findViewById(R.id.selectMoveLayout);
        Button resetBtn = findViewById(R.id.resetButton);

        result.setVisibility(View.GONE);
        resetBtn.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);

        playerScore = 0;
        comScore = 0;
        TextView comScoreTextView = findViewById(R.id.comScore);
        TextView playerScoreTextView = findViewById(R.id.playerScore);
        comScoreTextView.setText("Comp: "+comScore);
        playerScoreTextView.setText("You: "+playerScore);
        comScoreTextView.setTextColor(getResources().getColor(R.color.black));
        playerScoreTextView.setTextColor(getResources().getColor(R.color.black));
    }

}


