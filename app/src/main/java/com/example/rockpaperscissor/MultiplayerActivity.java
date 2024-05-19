package com.example.rockpaperscissor;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MultiplayerActivity extends AppCompatActivity {
    private String gameID;
    private String joinType;
    private DatabaseReference database;
    private EditText usernameEditText;
    private EditText gameIDeditText;
    private LinearLayout joinTypeLayout, inputAreaLayout, loadingScreen;
    private TextView displayGameID;
    private Button submitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_multiplayer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize game data in the database
        database = FirebaseDatabase.getInstance().getReference("games");

        // views
        usernameEditText = findViewById(R.id.username);
        gameIDeditText = findViewById(R.id.gameID);
        submitBtn = findViewById(R.id.submitBtn);

        joinTypeLayout = findViewById(R.id.joinType);
        inputAreaLayout = findViewById(R.id.inputArea);
        loadingScreen = findViewById(R.id.loadingScreen);
        displayGameID = findViewById(R.id.displayGameID);
    }


    public void joinBtn(View view) {
        joinType = "join";
        updateVisibility();

        gameIDeditText.setInputType(InputType.TYPE_CLASS_NUMBER);   // make editable
        gameIDeditText.setHint("Enter Room ID");
        submitBtn.setText("JOIN GAME");

    }

    public void createBtn(View view) {
        joinType = "create";
        updateVisibility();
        gameID = generateGameID();
        gameIDeditText.setHint("Game ID: "+ gameID);

        gameIDeditText.setClickable(false);
        gameIDeditText.setFocusable(false);
    }
    private String generateGameID() {
        gameID = String.valueOf(new Random().nextInt(9000) + 1000);
        database.child(gameID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    gameID = generateGameID(); // Game ID already exists, generate a new one
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });

        return gameID;
    }
    private void updateVisibility(){
        joinTypeLayout.setVisibility(View.GONE);
        inputAreaLayout.setVisibility(View.VISIBLE);
    }

    public void submit(View view){
        String username = usernameEditText.getText().toString();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }
        if(joinType.equals("create")){
            createGame(username);
        }
        else if(joinType.equals("join")){
            gameID = String.valueOf(gameIDeditText.getText());
            joinGame(username);
        }
    }
    private void createGame(String username){
        DatabaseReference gameRef = database.child(gameID);

        gameRef.child("player1").setValue(username);
        gameRef.child("player2").setValue("🔃");

        gameRef.child("turn").setValue("player1");
        Toast.makeText(this, "GameRoom Created", Toast.LENGTH_SHORT).show();
        waitForOpponent();
    }

    private void waitForOpponent() {
        // show a loading screen
        loadingScreen.setVisibility(View.VISIBLE);
        displayGameID.setText("Game ID: "+gameID);
        inputAreaLayout.setVisibility(View.GONE);

        DatabaseReference gameRef = database.child(gameID);
        gameRef.child("player2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String player2 = snapshot.getValue(String.class);
                if (!player2.equals("🔃")) {
                    // Player 2 has joined
                    loadingScreen.setVisibility(View.GONE);
                    Toast.makeText(MultiplayerActivity.this, player2+" joined", Toast.LENGTH_SHORT).show();
                    startGame("player1");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
                Toast.makeText(MultiplayerActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startGame(String player){
        Intent intent = new Intent(this, Playground.class);
        intent.putExtra("GAMEMODE", "multiplayer");
        intent.putExtra("GAMEID", gameID);
        intent.putExtra("PLAYERID", player);
        startActivity(intent);

    }

    // join Game
    private void joinGame(String username) {
        DatabaseReference gameRef = database.child(gameID);
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("player2").getValue(String.class).equals("🔃")) {
                        gameRef.child("player2").setValue(username);
                        startGame("player2");

                    } else {
                        Toast.makeText(MultiplayerActivity.this, "Game room is full", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MultiplayerActivity.this, "Game room not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MultiplayerActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backToMainMenu(View view){
        Intent intent = new Intent(this, MultiplayerActivity.class);
        startActivity(intent);
    }
}
