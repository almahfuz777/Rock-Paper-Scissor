package com.example.rockpaperscissor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import pl.droidsonroids.gif.GifImageView;


public class MultiplayerPlayground extends AppCompatActivity {
    private int opponentScore = 0;
    private int playerScore = 0;
    private String gameID, playerID, opponentID;
    private boolean isMyTurn = false;

    private ImageView display;
    private RelativeLayout selectMoveLayout;
    private TextView commentaryTextView;
    private TextView comScoreTextView, playerScoreTextView;
    private MediaPlayer rockSound, paperSound, scissorSound;
    private DatabaseReference gameRef;
    private GameStatsManager gameStatsManager;

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

        // inside OnCreate
        Intent intent = getIntent();
        gameID = intent.getStringExtra("GAMEID");
        playerID = intent.getStringExtra("PLAYERID");
        opponentID = playerID.equals("player1") ? "player2" : "player1";

        // Firebase
        gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameID);

        // Game Stats Manager
        gameStatsManager = new GameStatsManager(this);

        // views
        commentaryTextView = findViewById(R.id.commentary);
        selectMoveLayout = findViewById(R.id.selectMoveLayout);
        comScoreTextView = findViewById(R.id.comScore);
        playerScoreTextView = findViewById(R.id.playerScore);

//        comScoreTextView.setText(getOpponentUsername());

        playerScoreTextView.setText(intent.getStringExtra("USERNAME")+": ");
        getOpponentUsername(opponentUsername -> {
            comScoreTextView.setText(opponentUsername+": ");
        });

        // Set the player's disconnection status
        gameRef.child(playerID).onDisconnect().setValue("🔃");
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                checkAndDeleteGame(snapshot);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });

        // sound effects
        rockSound = MediaPlayer.create(this, R.raw.rock_sound);
        paperSound = MediaPlayer.create(this, R.raw.paper_sound);
        scissorSound = MediaPlayer.create(this, R.raw.scissor_sound);

        setupGameListeners();

    }

    // Method to check if both players have left and delete the game
    private void checkAndDeleteGame(DataSnapshot snapshot) {
        String player1Status = snapshot.child("player1").getValue(String.class);
        String player2Status = snapshot.child("player2").getValue(String.class);

        if ("🔃".equals(player1Status) && "🔃".equals(player2Status)) {
            gameRef.removeValue();
        }
    }

    private void setupGameListeners() {
        gameRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String turn = snapshot.getValue(String.class);
                isMyTurn = playerID.equals(turn);
                updateTurnIndicator();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });

        gameRef.child("choices").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(playerID) && snapshot.hasChild(opponentID)) {
                    String myChoice = snapshot.child(playerID).getValue(String.class);
                    String opponentChoice = snapshot.child(opponentID).getValue(String.class);
                    resolveRound(myChoice, opponentChoice);
                    updateDisplay(myChoice, opponentChoice);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }
    private String getOpponentUsername(OnOpponentUsernameRetrievedListener listener){
        gameRef.child(opponentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String opponentUsername = snapshot.getValue(String.class);
                    listener.onOpponentUsernameRetrieved(opponentUsername);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
        return "opponent";
    }
    public interface OnOpponentUsernameRetrievedListener {
        void onOpponentUsernameRetrieved(String username);
    }

    private void updateTurnIndicator() {
        if (isMyTurn) {
            commentaryTextView.setText("Your turn");
            commentaryTextView.setTextColor(getResources().getColor(R.color.green));
            selectMoveLayout.setClickable(true);
        } else {
            commentaryTextView.setText("Opponent's turn");
            commentaryTextView.setTextColor(getResources().getColor(R.color.red));
            selectMoveLayout.setClickable(false);
        }
    }

    public void rockSelected(View view) {
        if (!isMyTurn) return;
        rockSound.start();
        makeChoice("rock");
    }

    public void paperSelected(View view) {
        if (!isMyTurn) return;
        paperSound.start();
        makeChoice("paper");
    }

    public void scissorSelected(View view) {
        if (!isMyTurn) return;
        scissorSound.start();
        makeChoice("scissor");
    }

    private void makeChoice(String choice) {
        gameRef.child("choices").child(playerID).setValue(choice);
        isMyTurn = false;
        gameRef.child("turn").setValue(opponentID);
    }

    private void updateDisplay(String myChoice, String opponentChoice){
        String gifName = opponentChoice+"_"+myChoice;

        int resourceId = getResources().getIdentifier(gifName, "drawable", getPackageName());

        display = findViewById(R.id.display);
        GifImageView imageView = (GifImageView) display;
        imageView.setBackgroundResource(resourceId);
    }

    private void resolveRound(String myChoice, String opponentChoice) {
        if (myChoice.equals(opponentChoice)) {
            scoring("both");
        } else if ((myChoice.equals("rock") && opponentChoice.equals("scissor")) ||
                (myChoice.equals("paper") && opponentChoice.equals("rock")) ||
                (myChoice.equals("scissor") && opponentChoice.equals("paper"))) {
            scoring(playerID);
        } else {
            scoring(opponentID);
        }
        resetChoices();
    }

    private void scoring(String scorer) {
        TextView playerScoreTextView = findViewById(R.id.playerScore);
        TextView opponentScoreTextView = findViewById(R.id.comScore);

        String comChecked = "comCheckbox";
        String playerChecked = "playerCheckbox";

        Animation blink_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinktext);

        int checkedIcon = getResources().getIdentifier("checked", "drawable", getPackageName());

        if (scorer.equals("both")) {
            playerScore++;
            opponentScore++;

            // create checkbox id name in string
            comChecked+= opponentScore;
            playerChecked+= playerScore;

            // create id from String
            int comResourceId = getResources().getIdentifier(comChecked, "id", getPackageName());
            int playerResourceId = getResources().getIdentifier(playerChecked, "id", getPackageName());

            ImageView playerScoreImageView = findViewById(playerResourceId);
            ImageView opponentScoreImageView = findViewById(comResourceId);

            // update checkbox
            playerScoreImageView.setBackgroundResource(checkedIcon);
            opponentScoreImageView.setBackgroundResource(checkedIcon);

            playerScoreTextView.setTextColor(getResources().getColor(R.color.green));
            opponentScoreTextView.setTextColor(getResources().getColor(R.color.green));

            playerScoreTextView.startAnimation(blink_anim);
            opponentScoreTextView.startAnimation(blink_anim);
        }
        else if (scorer.equals(playerID)) {
            playerScore++;

            playerChecked += playerScore;
            int playerResourceId = getResources().getIdentifier(playerChecked, "id", getPackageName());

            ImageView playerScoreImageView = findViewById(playerResourceId);
            playerScoreImageView.setBackgroundResource(checkedIcon);

            playerScoreTextView.setTextColor(getResources().getColor(R.color.green));
            opponentScoreTextView.setTextColor(getResources().getColor(R.color.black));

            playerScoreTextView.startAnimation(blink_anim);
        }
        else {
            opponentScore++;

            comChecked += opponentScore;
            int opponentResourceId = getResources().getIdentifier(comChecked, "id", getPackageName());
            ImageView opponentScoreImageView = findViewById(opponentResourceId);
            opponentScoreImageView.setBackgroundResource(checkedIcon);

            opponentScoreTextView.setTextColor(getResources().getColor(R.color.green));
            playerScoreTextView.setTextColor(getResources().getColor(R.color.black));

            opponentScoreTextView.startAnimation(blink_anim);
        }

        if (playerScore == 5 || opponentScore == 5) {
            displayWinner();
        }
    }

    private void resetChoices() {
        gameRef.child("choices").child(playerID).removeValue();
        gameRef.child("choices").child(opponentID).removeValue();
    }

    private void displayWinner() {
        TextView result = findViewById(R.id.result);
        if (playerScore == opponentScore) {
            result.setText("🤝Match Draw!🤝\n" + playerScore + " : " + opponentScore);
            gameStatsManager.updateDraws();
        } else if (playerScore > opponentScore) {
            result.setText("🎉You Win!🎉\n" + opponentScore + " : " + playerScore);
            gameStatsManager.updateWins();
        } else {
            result.setText("😔You Lose!😔\n" + opponentScore + " : " + playerScore);
            gameStatsManager.updateLosses();
        }
        result.setVisibility(View.VISIBLE);

        RelativeLayout layout = findViewById(R.id.selectMoveLayout);
        layout.setVisibility(View.GONE);

        Button resetBtn = findViewById(R.id.resetButton);
        resetBtn.setVisibility(View.VISIBLE);

        commentaryTextView.setVisibility(View.GONE);
    }

    public void resetGame(View view) {
        // Set the player's readiness flag in Firebase
        gameRef.child("ready").child(playerID).setValue(true);

        TextView result = findViewById(R.id.result);
        result.setText("Waiting for opponent...");

        Button resetBtn = findViewById(R.id.resetButton);
        resetBtn.setVisibility(View.GONE);

        // Add a listener to check if both players are ready
        gameRef.child("ready").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean player1Ready = snapshot.hasChild("player1") && snapshot.child("player1").getValue(Boolean.class);
                boolean player2Ready = snapshot.hasChild("player2") && snapshot.child("player2").getValue(Boolean.class);

                if (player1Ready && player2Ready) {
                    // Both players are ready, start a new game
                    startNewGame();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });

    }

    private void startNewGame(){
        playerScore = 0;
        opponentScore = 0;

        TextView result = findViewById(R.id.result);
        RelativeLayout layout = findViewById(R.id.selectMoveLayout);
//        Button resetBtn = findViewById(R.id.resetButton);

        result.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
        commentaryTextView.setVisibility(View.VISIBLE);

        playerScoreTextView.setTextColor(getResources().getColor(R.color.black));
        comScoreTextView.setTextColor(getResources().getColor(R.color.black));

        int checkbox = getResources().getIdentifier("checkbox", "drawable", getPackageName());

        for (int i = 1; i <= 5; i++) {
            String playerChecked = "playerCheckbox"+i;
            int playerResourceId = getResources().getIdentifier(playerChecked, "id", getPackageName());
            ImageView playerScoreImageView = findViewById(playerResourceId);
            playerScoreImageView.setBackgroundResource(checkbox);

            String comChecked = "comCheckbox"+i;
            int comResourceId = getResources().getIdentifier(comChecked, "id", getPackageName());
            ImageView comScoreImageView = findViewById(comResourceId);
            comScoreImageView.setBackgroundResource(checkbox);
        }

        // Reset Firebase database entries
        gameRef.child("choices").removeValue();

        gameRef.child("turn").setValue("player1");

        // Reset the turn indicator
        commentaryTextView.setText("Opponent's turn");
        commentaryTextView.setTextColor(getResources().getColor(R.color.red));
        selectMoveLayout.setClickable(false);

        // Reset the "ready" flag
        gameRef.child("ready").removeValue();
        gameRef.child("choices").removeValue();

        updateTurnIndicator();

        // update to default gif
        int resourceId = getResources().getIdentifier("default_display", "drawable", getPackageName());

        display = findViewById(R.id.display);
        GifImageView imageView = (GifImageView) display;
        imageView.setBackgroundResource(resourceId);

    }
}