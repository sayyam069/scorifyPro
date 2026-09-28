package com.example.scorifypro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "scorify.db";
    private static final int DB_VERSION = 5;

    // Existing tables...
    private static final String TABLE_TEAMS = "teams";
    private static final String COL_TEAM_ID = "id";
    private static final String COL_TEAM_NAME = "name";

    private static final String TABLE_PLAYERS = "players";
    private static final String COL_PLAYER_ID = "id";
    private static final String COL_PLAYER_NAME = "name";
    private static final String COL_PLAYER_TEAM_ID = "team_id";

    private static final String TABLE_MATCHES = "matches";
    private static final String COL_MATCH_ID = "id";
    private static final String COL_MATCH_TEAMA = "teamA";
    private static final String COL_MATCH_TEAMB = "teamB";
    private static final String COL_MATCH_SCOREA = "scoreA";
    private static final String COL_MATCH_SCOREB = "scoreB";
    private static final String COL_MATCH_WICKETS_A = "wicketsA";
    private static final String COL_MATCH_WICKETS_B = "wicketsB";
    private static final String COL_MATCH_OVERS_A = "oversA";
    private static final String COL_MATCH_OVERS_B = "oversB";
    private static final String COL_MATCH_RESULT = "result";
    private static final String COL_MATCH_POTM = "potm";
    private static final String COL_MATCH_DATE = "match_date";

    // New tables for full scorecard
    private static final String TABLE_SCORECARD_BATTING = "scorecard_batting";
    private static final String TABLE_SCORECARD_BOWLING = "scorecard_bowling";
    
    // Common columns for stats
    private static final String COL_STAT_MATCH_ID = "match_id";
    private static final String COL_STAT_PLAYER_NAME = "player_name";
    private static final String COL_STAT_INNINGS = "innings_no"; // 1 or 2

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TEAMS + " (" + COL_TEAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TEAM_NAME + " TEXT UNIQUE NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_PLAYERS + " (" + COL_PLAYER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PLAYER_NAME + " TEXT NOT NULL, " + COL_PLAYER_TEAM_ID + " INTEGER NOT NULL)");
        
        db.execSQL("CREATE TABLE " + TABLE_MATCHES + " (" +
                COL_MATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MATCH_TEAMA + " TEXT, " +
                COL_MATCH_TEAMB + " TEXT, " +
                COL_MATCH_SCOREA + " INTEGER, " +
                COL_MATCH_SCOREB + " INTEGER, " +
                COL_MATCH_WICKETS_A + " INTEGER, " +
                COL_MATCH_WICKETS_B + " INTEGER, " +
                COL_MATCH_OVERS_A + " TEXT, " +
                COL_MATCH_OVERS_B + " TEXT, " +
                COL_MATCH_RESULT + " TEXT, " +
                COL_MATCH_POTM + " TEXT, " +
                COL_MATCH_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE " + TABLE_SCORECARD_BATTING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STAT_MATCH_ID + " INTEGER, " +
                COL_STAT_INNINGS + " INTEGER, " +
                COL_STAT_PLAYER_NAME + " TEXT, " +
                "runs INTEGER, balls INTEGER, fours INTEGER, sixes INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_SCORECARD_BOWLING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STAT_MATCH_ID + " INTEGER, " +
                COL_STAT_INNINGS + " INTEGER, " +
                COL_STAT_PLAYER_NAME + " TEXT, " +
                "overs REAL, runs_given INTEGER, wickets INTEGER)");

        db.execSQL("INSERT INTO " + TABLE_TEAMS + " (" + COL_TEAM_NAME + ") VALUES ('Pakistan')");
        db.execSQL("INSERT INTO " + TABLE_TEAMS + " (" + COL_TEAM_NAME + ") VALUES ('India')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORECARD_BATTING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORECARD_BOWLING);
        onCreate(db);
    }

    public long addMatchExtended(String teamA, String teamB, int scoreA, int scoreB, int wicketsA, int wicketsB, 
                               String oversA, String oversB, String result, String potm,
                               List<ScorecardActivity.PlayerStats> batA, List<ScorecardActivity.PlayerStats> batB,
                               List<ScorecardActivity.PlayerStats> bowlA, List<ScorecardActivity.PlayerStats> bowlB) {
        
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MATCH_TEAMA, teamA);
        cv.put(COL_MATCH_TEAMB, teamB);
        cv.put(COL_MATCH_SCOREA, scoreA);
        cv.put(COL_MATCH_SCOREB, scoreB);
        cv.put(COL_MATCH_WICKETS_A, wicketsA);
        cv.put(COL_MATCH_WICKETS_B, wicketsB);
        cv.put(COL_MATCH_OVERS_A, oversA);
        cv.put(COL_MATCH_OVERS_B, oversB);
        cv.put(COL_MATCH_RESULT, result);
        cv.put(COL_MATCH_POTM, potm);
        
        long matchId = db.insert(TABLE_MATCHES, null, cv);
        
        if (matchId != -1) {
            // Save Batting Stats Innings 1
            for (ScorecardActivity.PlayerStats s : batA) {
                ContentValues v = new ContentValues();
                v.put(COL_STAT_MATCH_ID, matchId);
                v.put(COL_STAT_INNINGS, 1);
                v.put(COL_STAT_PLAYER_NAME, s.name);
                v.put("runs", s.runs);
                v.put("balls", s.balls);
                v.put("fours", s.fours);
                v.put("sixes", s.sixes);
                db.insert(TABLE_SCORECARD_BATTING, null, v);
            }
            // Save Batting Stats Innings 2
            for (ScorecardActivity.PlayerStats s : batB) {
                ContentValues v = new ContentValues();
                v.put(COL_STAT_MATCH_ID, matchId);
                v.put(COL_STAT_INNINGS, 2);
                v.put(COL_STAT_PLAYER_NAME, s.name);
                v.put("runs", s.runs);
                v.put("balls", s.balls);
                v.put("fours", s.fours);
                v.put("sixes", s.sixes);
                db.insert(TABLE_SCORECARD_BATTING, null, v);
            }
            // Save Bowling Stats Innings 1
            for (ScorecardActivity.PlayerStats s : bowlA) {
                ContentValues v = new ContentValues();
                v.put(COL_STAT_MATCH_ID, matchId);
                v.put(COL_STAT_INNINGS, 1);
                v.put(COL_STAT_PLAYER_NAME, s.name);
                v.put("overs", s.overs);
                v.put("runs_given", s.runsGiven);
                v.put("wickets", s.wickets);
                db.insert(TABLE_SCORECARD_BOWLING, null, v);
            }
            // Save Bowling Stats Innings 2
            for (ScorecardActivity.PlayerStats s : bowlB) {
                ContentValues v = new ContentValues();
                v.put(COL_STAT_MATCH_ID, matchId);
                v.put(COL_STAT_INNINGS, 2);
                v.put(COL_STAT_PLAYER_NAME, s.name);
                v.put("overs", s.overs);
                v.put("runs_given", s.runsGiven);
                v.put("wickets", s.wickets);
                db.insert(TABLE_SCORECARD_BOWLING, null, v);
            }
        }
        return matchId;
    }

    public List<ScorecardActivity.PlayerStats> getBattingStats(long matchId, int innings) {
        List<ScorecardActivity.PlayerStats> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_SCORECARD_BATTING + 
                " WHERE " + COL_STAT_MATCH_ID + "=? AND " + COL_STAT_INNINGS + "=?", 
                new String[]{String.valueOf(matchId), String.valueOf(innings)});
        if (c.moveToFirst()) {
            do {
                ScorecardActivity.PlayerStats s = new ScorecardActivity.PlayerStats(c.getString(3));
                s.runs = c.getInt(4);
                s.balls = c.getInt(5);
                s.fours = c.getInt(6);
                s.sixes = c.getInt(7);
                list.add(s);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<ScorecardActivity.PlayerStats> getBowlingStats(long matchId, int innings) {
        List<ScorecardActivity.PlayerStats> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_SCORECARD_BOWLING + 
                " WHERE " + COL_STAT_MATCH_ID + "=? AND " + COL_STAT_INNINGS + "=?", 
                new String[]{String.valueOf(matchId), String.valueOf(innings)});
        if (c.moveToFirst()) {
            do {
                ScorecardActivity.PlayerStats s = new ScorecardActivity.PlayerStats(c.getString(3));
                s.overs = c.getFloat(4);
                s.runsGiven = c.getInt(5);
                s.wickets = c.getInt(6);
                list.add(s);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean addMatch(String teamA, String teamB, int scoreA, int scoreB, int wicketsA, int wicketsB, String oversA, String oversB, String result, String potm) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MATCH_TEAMA, teamA);
        cv.put(COL_MATCH_TEAMB, teamB);
        cv.put(COL_MATCH_SCOREA, scoreA);
        cv.put(COL_MATCH_SCOREB, scoreB);
        cv.put(COL_MATCH_WICKETS_A, wicketsA);
        cv.put(COL_MATCH_WICKETS_B, wicketsB);
        cv.put(COL_MATCH_OVERS_A, oversA);
        cv.put(COL_MATCH_OVERS_B, oversB);
        cv.put(COL_MATCH_RESULT, result);
        cv.put(COL_MATCH_POTM, potm);
        return getWritableDatabase().insert(TABLE_MATCHES, null, cv) != -1;
    }

    public List<MatchHistory> getAllMatches() {
        List<MatchHistory> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_MATCHES + " ORDER BY " + COL_MATCH_ID + " DESC", null);
        if (c.moveToFirst()) {
            do {
                MatchHistory m = new MatchHistory();
                m.id = c.getInt(0);
                m.teamA = c.getString(1);
                m.teamB = c.getString(2);
                m.scoreA = c.getInt(3);
                m.scoreB = c.getInt(4);
                m.wicketsA = c.getInt(5);
                m.wicketsB = c.getInt(6);
                m.oversA = c.getString(7);
                m.oversB = c.getString(8);
                m.result = c.getString(9);
                m.potm = c.getString(10);
                m.date = c.getString(11);
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public static class MatchHistory {
        public int id;
        public String teamA, teamB, result, potm, date, oversA, oversB;
        public int scoreA, scoreB, wicketsA, wicketsB;
    }

    public List<String> getAllTeamNames() {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT " + COL_TEAM_NAME + " FROM " + TABLE_TEAMS, null);
        if (c.moveToFirst()) {
            do { list.add(c.getString(0)); } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Team> getAllTeams() {
        List<Team> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_TEAMS, null);
        if (c.moveToFirst()) {
            do {
                Team t = new Team();
                t.id = c.getInt(0);
                t.name = c.getString(1);
                list.add(t);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public int getTeamIdByName(String teamName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_TEAM_ID + " FROM " + TABLE_TEAMS + " WHERE " + COL_TEAM_NAME + " = ?", new String[]{teamName});
        int id = -1;
        if (c != null && c.moveToFirst()) {
            id = c.getInt(0);
        }
        if (c != null) c.close();
        return id;
    }

    public boolean addTeam(String name) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TEAM_NAME, name);
        return getWritableDatabase().insert(TABLE_TEAMS, null, cv) != -1;
    }

    public boolean deleteTeam(String name) {
        int teamId = getTeamIdByName(name);
        if (teamId != -1) {
            getWritableDatabase().delete(TABLE_PLAYERS, COL_PLAYER_TEAM_ID + "=?", new String[]{String.valueOf(teamId)});
        }
        return getWritableDatabase().delete(TABLE_TEAMS, COL_TEAM_NAME + "=?", new String[]{name}) > 0;
    }

    public List<Player> getPlayerListByTeam(int teamId) {
        List<Player> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_PLAYERS + " WHERE " + COL_PLAYER_TEAM_ID + " = ?", new String[]{String.valueOf(teamId)});
        if (c.moveToFirst()) {
            do {
                Player p = new Player();
                p.id = c.getInt(0);
                p.name = c.getString(1);
                p.teamId = c.getInt(2);
                list.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean addPlayer(String name, int teamId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PLAYER_NAME, name);
        cv.put(COL_PLAYER_TEAM_ID, teamId);
        return getWritableDatabase().insert(TABLE_PLAYERS, null, cv) != -1;
    }

    public boolean deletePlayer(int playerId) {
        return getWritableDatabase().delete(TABLE_PLAYERS, COL_PLAYER_ID + "=?", new String[]{String.valueOf(playerId)}) > 0;
    }

    public static class Team {
        public int id;
        public String name;
    }

    public static class Player {
        public int id;
        public String name;
        public int teamId;

        @Override
        public String toString() {
            return name;
        }
    }
}