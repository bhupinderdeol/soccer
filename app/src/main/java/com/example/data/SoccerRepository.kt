package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class SoccerRepository(private val soccerDao: SoccerDao) {
    val allPlayers: Flow<List<PlayerCard>> = soccerDao.getAllPlayers()
    val mySquad: Flow<List<PlayerCard>> = soccerDao.getMySquad()
    val unlockedPlayers: Flow<List<PlayerCard>> = soccerDao.getUnlockedPlayers()
    val userStats: Flow<UserStats?> = soccerDao.getUserStatsFlow()

    suspend fun updatePlayerSquadStatus(id: String, isMySquad: Boolean) {
        soccerDao.updateSqaudStatus(id, isMySquad)
    }

    suspend fun updatePlayer(player: PlayerCard) {
        soccerDao.updatePlayer(player)
    }

    suspend fun getUserStatsDirect(): UserStats {
        return soccerDao.getUserStats() ?: UserStats()
    }

    suspend fun rewardCoins(amount: Int, won: Boolean, goals: Int) {
        val currentStats = getUserStatsDirect()
        val extraPoints = if (won) 3 else if (goals > 0) 1 else 0
        val newPoints = currentStats.rankingPoints + (if (won) 25 else if (goals > 0) 5 else -10).coerceAtLeast(0)
        
        // Let's determine Division
        val newDivision = when {
            newPoints >= 4200 -> "Division 1"
            newPoints >= 3000 -> "Division 2"
            newPoints >= 2000 -> "Division 3"
            newPoints >= 1500 -> "Division 4"
            newPoints >= 1000 -> "Division 5"
            newPoints >= 700 -> "Division 6"
            newPoints >= 500 -> "Division 7"
            newPoints >= 300 -> "Division 8"
            newPoints >= 150 -> "Division 9"
            else -> "Division 10"
        }

        val updated = currentStats.copy(
            coins = currentStats.coins + amount,
            matchesPlayed = currentStats.matchesPlayed + 1,
            matchesWon = currentStats.matchesWon + (if (won) 1 else 0),
            goalsScored = currentStats.goalsScored + goals,
            rankingPoints = newPoints,
            division = newDivision
        )
        soccerDao.updateUserStats(updated)
    }

    // Returns the player card that was unlocked, or null if purchase failed (insufficient coins, etc.)
    suspend fun purchasePack(packType: String, cost: Int): PlayerCard? {
        val currentStats = getUserStatsDirect()
        if (currentStats.coins < cost) return null

        val players = soccerDao.getAllPlayers().firstOrNull() ?: listOf()
        // Filter players that are currently NOT unlocked
        val lockedPlayers = players.filter { !it.isUnlocked }
        if (lockedPlayers.isEmpty()) return null

        // Select player based on package conditions
        val chosenPlayer = when (packType) {
            "ROOKIE" -> {
                // Prefers ratings < 85
                val eligible = lockedPlayers.filter { it.rating < 85 }
                if (eligible.isNotEmpty()) eligible.random() else lockedPlayers.random()
            }
            "GOLD" -> {
                // Prefers 84-88 rating
                val eligible = lockedPlayers.filter { it.rating in 84..88 }
                if (eligible.isNotEmpty()) eligible.random() else lockedPlayers.random()
            }
            "ELITE" -> {
                // Prefers rating >= 88
                val eligible = lockedPlayers.filter { it.rating >= 88 }
                if (eligible.isNotEmpty()) eligible.random() else lockedPlayers.random()
            }
            else -> lockedPlayers.random()
        }

        // Deduct coins & Unlock player in database
        val updatedStats = currentStats.copy(coins = currentStats.coins - cost)
        soccerDao.updateUserStats(updatedStats)

        val unlockedPlayer = chosenPlayer.copy(isUnlocked = true)
        soccerDao.updatePlayer(unlockedPlayer)

        return unlockedPlayer
    }
    
    suspend fun resetData() {
        val currentStats = UserStats()
        soccerDao.insertUserStats(currentStats)
        // Reset unlocking and squad selections
        val players = soccerDao.getAllPlayers().firstOrNull() ?: listOf()
        for (player in players) {
            val isStarter = player.id.startsWith("start_")
            soccerDao.updatePlayer(player.copy(isUnlocked = isStarter, isMySquad = isStarter))
        }
    }
}
