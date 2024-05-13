package com.example.rockpaperscissor;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class GameStatsManager {
    private static final String PREF_FILE_NAME = "gameStats";
    private static final String GAMES_PLAYED_KEY = "gamesPlayed";
    private static final String GAMES_WON_KEY = "gamesWon";
    private static final String GAMES_LOST_KEY = "gamesLost";
    private static final String GAMES_DRAWN_KEY = "gamesDrawn";
    private static final String LONGEST_STREAK_KEY = "longestStreak";
    private static final String CURRENT_STREAK_KEY = "currentStreak";

    // Declare SharedPreferences
    private final SharedPreferences sp;

    public GameStatsManager(Context context) {
        sp = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
    }

    // Getters
    public int getGamesPlayed() {
        return sp.getInt(GAMES_PLAYED_KEY, 0);
    }
    public int getGamesWon() {
        return sp.getInt(GAMES_WON_KEY, 0);
    }
    public int getGamesLost() {
        return sp.getInt(GAMES_LOST_KEY, 0);
    }
    public int getGamesDrawn() {
        return sp.getInt(GAMES_DRAWN_KEY, 0);
    }
    public int getLongestStreak() {
        return sp.getInt(LONGEST_STREAK_KEY, 0);
    }
    public int getCurrentStreak() {
        return sp.getInt(CURRENT_STREAK_KEY, 0);
    }

    // Setters
    public void setGamesPlayed(int gamesPlayed) {
        sp.edit().putInt(GAMES_PLAYED_KEY, gamesPlayed).apply();
    }
    public void setGamesWon(int gamesWon) {
        sp.edit().putInt(GAMES_WON_KEY, gamesWon).apply();
    }
    public void setGamesLost(int gamesLost) {
        sp.edit().putInt(GAMES_LOST_KEY, gamesLost).apply();
    }
    public void setGamesDrawn(int gamesDrawn) {
        sp.edit().putInt(GAMES_DRAWN_KEY, gamesDrawn).apply();
    }
    public void setLongestStreak(int longestStreak) {
        sp.edit().putInt(LONGEST_STREAK_KEY, longestStreak).apply();
    }
    public void setCurrentStreak(int currentStreak) {
        sp.edit().putInt(CURRENT_STREAK_KEY, currentStreak).apply();
        if(currentStreak>getLongestStreak()) setLongestStreak(currentStreak);
    }

    // Update functions
    public void updateGamesPlayed() {
        setGamesPlayed(getGamesPlayed() + 1);
    }
    public void updateWins() {
        setGamesWon(getGamesWon() + 1);
        updateGamesPlayed();
        setCurrentStreak(getCurrentStreak()+1);
    }
    public void updateLosses() {
        setGamesLost(getGamesLost() + 1);
        updateGamesPlayed();
        setCurrentStreak(0);
    }
    public void updateDraws() {
        setGamesDrawn(getGamesDrawn() + 1);
        updateGamesPlayed();
        setCurrentStreak(0);
    }

}
