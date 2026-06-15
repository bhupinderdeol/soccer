package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerCard(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Int,
    val position: String, // FWD, MID, DEF, GK
    val team: String,
    val pace: Int,
    val shooting: Int,
    val passing: Int,
    val dribbling: Int,
    val defense: Int,
    val physical: Int,
    val isMySquad: Boolean = false,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 1500, // Starts with 1500 coins to open a couple of packs!
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val goalsScored: Int = 0,
    val division: String = "Division 10",
    val rankingPoints: Int = 100
)

data class MatchHistoryEntry(
    val id: String,
    val opponentTeam: String,
    val userScore: Int,
    val opponentScore: Int,
    val result: String, // WIN, DRAW, LOSS
    val timestamp: Long
)
