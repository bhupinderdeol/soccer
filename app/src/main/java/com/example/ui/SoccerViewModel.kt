package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

enum class AppScreen {
    HOME,
    SQUAD,
    PLAYREST_SELECTION,
    GAME_PLAY,
    PVP_HUB,
    PVP_GAME,
    SHOP,
    ROSTERS_VIEWER
}

enum class MatchMode {
    VS_AI,
    SIM_PVP
}

// 2D Pitch player coordinate holder
data class PitchPlayer(
    val id: String,
    val name: String,
    val rating: Int,
    val position: String,
    val team: String,
    var x: Float,
    var y: Float,
    val isUserTeam: Boolean,
    var isGK: Boolean = false,
    var speed: Float = 4.0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f
)

class SoccerViewModel(application: Application) : AndroidViewModel(application) {
    private val database: SoccerDatabase = SoccerDatabase.getDatabase(application, viewModelScope)
    private val repository = SoccerRepository(database.soccerDao())

    // UI Navigation
    var currentScreen by mutableStateOf(AppScreen.HOME)
        private set

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
    }

    // Shared Flow States
    val userStats: StateFlow<UserStats> = repository.userStats
        .map { it ?: UserStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val mySquad: StateFlow<List<PlayerCard>> = repository.mySquad
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedPlayers: StateFlow<List<PlayerCard>> = repository.unlockedPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlayers: StateFlow<List<PlayerCard>> = repository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pack shop rewards state
    var isOpeningPack by mutableStateOf(false)
        private set
    var unpackedPlayer by mutableStateOf<PlayerCard?>(null)
        private set
    var packError by mutableStateOf<String?>(null)
        private set

    // Matchmaking PVP state
    var matchmakingState by mutableStateOf("IDLE") // IDLE, SEARCHING, FOUND, CONNECTING, FINISHED
    var matchedOpponentName by mutableStateOf("")
    var matchedOpponentRating by mutableStateOf(80)
    var searchTimer by mutableStateOf(0)
    
    // Sim PVP match states
    var pvpMatchMinute by mutableStateOf(0)
    var pvpUserScore by mutableStateOf(0)
    var pvpOpponentScore by mutableStateOf(0)
    var pvpMatchLogs = mutableListOf<String>()
    var pvpCurrentEventTitle by mutableStateOf("")
    var pvpCurrentEventText by mutableStateOf("")
    var pvpEventOptions by mutableStateOf<List<String>>(emptyList())
    var isShowingPvpEventDialog by mutableStateOf(false)
    var pvpMatchActive by mutableStateOf(false)
    private var pvpOpponentTeamName = "Slayer FC"
    
    // Gameplay VS AI Match State
    var aiOpponentTeam by mutableStateOf("Real Madrid")
    var matchScoreUser by mutableStateOf(0)
    var matchScoreOpponent by mutableStateOf(0)
    var matchTimeMinute by mutableStateOf(0)
    var matchHalfTime by mutableStateOf(1)
    var matchState by mutableStateOf("PRE_MATCH") // PRE_MATCH, PLAYING, GOAL, HALF_TIME, FULL_TIME
    var matchGoalScorer by mutableStateOf("")
    var commentaryText by mutableStateOf("Welcome to the Match!")
    var commentaryLogs = mutableListOf<String>()

    // Statistics
    var statsPossessionUser by mutableStateOf(50)
    var statsShotsUser by mutableStateOf(0)
    var statsShotsOpponent by mutableStateOf(0)
    var statsPassesUser by mutableStateOf(0)
    val statsPassesOpponent = mutableStateOf(0)
    var statsTacklesUser by mutableStateOf(0)
    var statsTacklesOpponent by mutableStateOf(0)

    // 2D Soccer Pitch Game Loop Entities
    val pitchBallX = mutableStateOf(300f) // Canonical field scale is 600 width x 900 height
    val pitchBallY = mutableStateOf(450f)
    val pitchBallVX = mutableStateOf(0f)
    val pitchBallVY = mutableStateOf(0f)
    
    var userPlayers = mutableListOf<PitchPlayer>()
    var opponentPlayers = mutableListOf<PitchPlayer>()
    var joystickCurrentX by mutableStateOf(0f)
    var joystickCurrentY by mutableStateOf(0f)
    var selectedPlayerIndex by mutableStateOf(0) // Index of user player active under manual touch
    var ballPossessionPlayerId by mutableStateOf<String?>(null) // ID of player with ball
    var ballPossessionIsUser by mutableStateOf(true)

    private var gameLoopJob: Job? = null
    private var pvpGameJob: Job? = null

    // Setups and Operations
    fun openPack(type: String, cost: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isOpeningPack = true
            packError = null
            unpackedPlayer = null
            delay(1800) // Beautiful pack opening reveal suspense
            val player = repository.purchasePack(type, cost)
            if (player != null) {
                unpackedPlayer = player
            } else {
                packError = "Insufficient coins or roster is fully unlocked!"
            }
            isOpeningPack = false
        }
    }

    fun dismissPackReveal() {
        unpackedPlayer = null
        packError = null
    }

    fun toggleSquadStatus(player: PlayerCard) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSquad = mySquad.value
            if (!player.isMySquad) {
                // Let's check limits (5 for mini-futsal team match)
                val countInPosition = currentSquad.size
                if (countInPosition >= 5) {
                    // Fail gracefully
                    return@launch
                }
                repository.updatePlayerSquadStatus(player.id, true)
            } else {
                // Minimum 3 in squad so database does not go empty
                if (currentSquad.size <= 3) return@launch
                repository.updatePlayerSquadStatus(player.id, false)
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetData()
        }
    }

    // PVP Matchmaking Mechanism
    fun startPvPMatchmaking() {
        matchmakingState = "SEARCHING"
        searchTimer = 0
        viewModelScope.launch {
            for (i in 1..4) {
                delay(1000)
                searchTimer = i
            }
            matchmakingState = "FOUND"
            val names = listOf("ApexStriker_7", "FifaLord_99", "GamerFC_X", "TacticalPep", "CR7_Lover", "SakaTaka_Arsenal")
            matchedOpponentName = names.random()
            matchedOpponentRating = Random.nextInt(82, 91)
            pvpOpponentTeamName = matchedOpponentName + " FC"
            
            delay(1500)
            matchmakingState = "CONNECTING"
            delay(1200)
            navigateTo(AppScreen.PVP_GAME)
            startPvpSimulation()
        }
    }

    fun leavePvp() {
        pvpGameJob?.cancel()
        pvpMatchActive = false
        matchmakingState = "IDLE"
        navigateTo(AppScreen.PVP_HUB)
    }

    // PVP Sim Game Loop
    private fun startPvpSimulation() {
        pvpGameJob?.cancel()
        pvpMatchMinute = 0
        pvpUserScore = 0
        pvpOpponentScore = 0
        pvpMatchLogs.clear()
        pvpMatchLogs.add("👥 Match kicked off! ${matchedOpponentName} against Ultimate FC.")
        pvpMatchActive = true
        isShowingPvpEventDialog = false
        
        pvpGameJob = viewModelScope.launch {
            while (pvpMatchMinute < 90 && pvpMatchActive) {
                delay(1500) // Sim speeds: 1.5 seconds = 5-10 minutes in game
                pvpMatchMinute += Random.nextInt(6, 12)
                if (pvpMatchMinute > 90) pvpMatchMinute = 90
                
                // Trigger quick PVP choice event half way or at select points (e.g. 30' and 70')
                if (pvpMatchMinute in 25..45 && !isShowingPvpEventDialog && pvpMatchMinute < 45) {
                    triggerPvpChoiceEvent("🚨 Counter Attack Incident (35')", "Opponent is blazing forward with raw pace. Your defense line is backing off!", listOf("Slide Tackle (Risky)", "Tactical Block (Safe)", "Offside Trap (Aggressive)"))
                    // pause sim wait until user resolves
                    while (isShowingPvpEventDialog) { delay(200) }
                } else if (pvpMatchMinute in 65..80 && !isShowingPvpEventDialog && pvpMatchMinute < 80) {
                    triggerPvpChoiceEvent("🔥 Penalty Box Scramble (72')", "You got the ball in front of the goal. Opponent GK is rushing out!", listOf("Power Shot Bottom Corner", "Deceptive Chip Shot", "Pass to Teammate"))
                    while (isShowingPvpEventDialog) { delay(200) }
                } else {
                    // Standard simulated updates
                    val eventChance = Random.nextFloat()
                    if (eventChance < 0.22f) {
                        // User goal
                        val scorer = mySquad.value.randomOrNull()?.name ?: "Striker"
                        pvpUserScore++
                        pvpMatchLogs.add("⚽ GOAL! Ultimate FC (${pvpMatchMinute}'). Excellent build-up and slotted in by $scorer!")
                    } else if (eventChance < 0.42f) {
                        // Opponent goal
                        pvpOpponentScore++
                        pvpMatchLogs.add("⚽ GOAL! $pvpOpponentTeamName (${pvpMatchMinute}'). Opponent splits your defense and scores!")
                    } else {
                        val actions = listOf(
                            "Great intercept from your midfield.",
                            "Tense physical duel in the center circle.",
                            "Opponent shot saved with dynamic reflexes!",
                            "Corner kick cleared safely.",
                            "Ultimate FC possession dominance increases."
                        )
                        pvpMatchLogs.add("⏱️ [${pvpMatchMinute}'] " + actions.random())
                    }
                }
            }
            
            // Full-time reward logic
            pvpMatchLogs.add("🏁 Full Time! Final Score: Ultimate FC $pvpUserScore - $pvpOpponentScore $pvpOpponentTeamName")
            val result = if (pvpUserScore > pvpOpponentScore) "WIN" else if (pvpUserScore == pvpOpponentScore) "DRAW" else "LOSS"
            val coinsEarned = if (result == "WIN") 400 else if (result == "DRAW") 150 else 50
            
            repository.rewardCoins(coinsEarned, result == "WIN", pvpUserScore)
            pvpMatchActive = false
        }
    }

    private fun triggerPvpChoiceEvent(title: String, description: String, options: List<String>) {
        pvpCurrentEventTitle = title
        pvpCurrentEventText = description
        pvpEventOptions = options
        isShowingPvpEventDialog = true
    }

    fun makePvpChoice(index: Int) {
        isShowingPvpEventDialog = false
        viewModelScope.launch {
            delay(500)
            val isSuccess = Random.nextFloat() < 0.65f // 65% chance of successful tactic
            if (isSuccess) {
                if (index == 0) {
                    // slide tackle or bottom corner shot
                    if (pvpCurrentEventTitle.contains("Counter")) {
                        pvpMatchLogs.add("✅ Clean slide tackle from your squad! Danger cleared.")
                    } else {
                        pvpUserScore++
                        pvpMatchLogs.add("⚽ GOAL! You chose precise bottom-corner shooting and found the net!")
                    }
                } else if (index == 1) {
                    if (pvpCurrentEventTitle.contains("Counter")) {
                        pvpMatchLogs.add("🛡️ Perfect tactical block. Ball retrieved safely.")
                    } else {
                        pvpUserScore++
                        pvpMatchLogs.add("⚽ GOAL! Striker chips the goalkeeper with supreme precision!")
                    }
                } else {
                    if (pvpCurrentEventTitle.contains("Counter")) {
                        pvpMatchLogs.add("🏃 Offside Trap worked! Referee signals opponent offside.")
                    } else {
                        pvpMatchLogs.add("🤝 Unselfish pass, but opponent defender blocked the scoring lane.")
                    }
                }
            } else {
                if (pvpCurrentEventTitle.contains("Counter")) {
                    pvpOpponentScore++
                    pvpMatchLogs.add("❌ Tactical mistake! Opponent slips through and scores.")
                } else {
                    pvpMatchLogs.add("⚠️ Shot blocked! GK claims the rebound.")
                }
            }
        }
    }

    // AI Match Setup
    fun selectAiOpponent(teamName: String) {
        aiOpponentTeam = teamName
    }

    fun startAiMatch() {
        matchScoreUser = 0
        matchScoreOpponent = 0
        matchTimeMinute = 0
        matchHalfTime = 1
        commentaryLogs.clear()
        commentaryLogs.add("🏟️ High tension at the stadium! kickoff is next.")
        
        // Build 2D Pitch player coordinates
        setupPitchPlayers()

        // Kick off loop
        matchState = "PLAYING"
        commentaryText = "Match has started! Control active player with Joystick."
        
        startGameLoop()
    }

    private fun getRosterPlayersForTeam(teamName: String): List<PlayerCard> {
        val all = listOfDefaultPlayers()
        return all.filter { it.team == teamName }
    }

    private fun setupPitchPlayers() {
        userPlayers.clear()
        opponentPlayers.clear()

        // User Squad - load from room or fallback to start players
        val squad = mySquad.value.ifEmpty {
            listOfDefaultPlayers().filter { it.id.startsWith("start_") }
        }

        // Opponent squad
        val oppSquad = getRosterPlayersForTeam(aiOpponentTeam).ifEmpty {
            listOfDefaultPlayers().subList(0, 5)
        }

        // Position on 2D Canonical Pitch (Width 600 x Height 900)
        // User attacks top (towards Goal Y=20), defends bottom (Goal Y=880)
        // 5v5 Tactical formation: GK, Left DEF, Right DEF, Midfielder, Attacker
        
        // User slots
        userPlayers.addAll(listOf(
            PitchPlayer(squad.getOrNull(0)?.id ?: "u1", squad.getOrNull(0)?.name ?: "GK", squad.getOrNull(0)?.rating ?: 80, "GK", "Ultimate FC", 300f, 850f, true, isGK = true),
            PitchPlayer(squad.getOrNull(1)?.id ?: "u2", squad.getOrNull(1)?.name ?: "DEF L", squad.getOrNull(1)?.rating ?: 80, "DEF", "Ultimate FC", 180f, 650f, true),
            PitchPlayer(squad.getOrNull(2)?.id ?: "u3", squad.getOrNull(2)?.name ?: "DEF R", squad.getOrNull(2)?.rating ?: 80, "DEF", "Ultimate FC", 420f, 650f, true),
            PitchPlayer(squad.getOrNull(3)?.id ?: "u4", squad.getOrNull(3)?.name ?: "MID", squad.getOrNull(3)?.rating ?: 80, "MID", "Ultimate FC", 300f, 500f, true),
            PitchPlayer(squad.getOrNull(4)?.id ?: "u5", squad.getOrNull(4)?.name ?: "FWD", squad.getOrNull(4)?.rating ?: 80, "FWD", "Ultimate FC", 300f, 400f, true)
        ))

        // Opponent slots (Opponent GK defends Y=50, attacks Y=880)
        opponentPlayers.addAll(listOf(
            PitchPlayer(oppSquad.getOrNull(0)?.id ?: "o1", oppSquad.getOrNull(0)?.name ?: "GK", oppSquad.getOrNull(0)?.rating ?: 80, "GK", aiOpponentTeam, 300f, 50f, false, isGK = true),
            PitchPlayer(oppSquad.getOrNull(1)?.id ?: "o2", oppSquad.getOrNull(1)?.name ?: "DEF L", oppSquad.getOrNull(1)?.rating ?: 80, "DEF", aiOpponentTeam, 200f, 250f, false),
            PitchPlayer(oppSquad.getOrNull(2)?.id ?: "o3", oppSquad.getOrNull(2)?.name ?: "DEF R", oppSquad.getOrNull(2)?.rating ?: 80, "DEF", aiOpponentTeam, 400f, 250f, false),
            PitchPlayer(oppSquad.getOrNull(3)?.id ?: "o4", oppSquad.getOrNull(3)?.name ?: "MID", oppSquad.getOrNull(3)?.rating ?: 80, "MID", aiOpponentTeam, 300f, 380f, false),
            PitchPlayer(oppSquad.getOrNull(4)?.id ?: "o5", oppSquad.getOrNull(4)?.name ?: "FWD", oppSquad.getOrNull(4)?.rating ?: 80, "FWD", aiOpponentTeam, 300f, 480f, false)
        ))

        selectedPlayerIndex = 4 // Control our forward initially
        pitchBallX.value = 300f
        pitchBallY.value = 450f
        pitchBallVX.value = 0f
        pitchBallVY.value = 0f
        ballPossessionPlayerId = "start_jackson"
        ballPossessionIsUser = true
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            statsPossessionUser = 50
            statsShotsUser = 0
            statsShotsOpponent = 0
            statsPassesUser = 0
            statsPassesOpponent.value = 0
            statsTacklesUser = 0
            statsTacklesOpponent = 0

            var loopCounter = 0
            while (matchState == "PLAYING" || matchState == "GOAL") {
                delay(33) // ~30 FPS coordinate update logic
                loopCounter++

                if (matchState == "PLAYING") {
                    // Update Match Timer Clock
                    if (loopCounter % 30 == 0) { // every 1 second real-time is 1-2 minutes in match clock
                        matchTimeMinute += 1
                        if (matchTimeMinute == 45 && matchHalfTime == 1) {
                            matchState = "HALF_TIME"
                        } else if (matchTimeMinute >= 90) {
                            matchState = "FULL_TIME"
                            finishMatch()
                        }
                    }

                    // 1. Move User Players
                    updateUserPlayerMovement()

                    // 2. Move Opponent AI Players
                    updateOpponentPlayerMovement()

                    // 3. Keep Ball Physics in frame
                    updateBallPhysics()

                    // 4. Collision/Possession check
                    checkBallCollisions()
                } else if (matchState == "GOAL") {
                    // Goal celebration loop sleep
                    delay(2000)
                    matchState = "PLAYING"
                    commentaryText = "Game restart!"
                    pitchBallX.value = 300f
                    pitchBallY.value = 450f
                    pitchBallVX.value = 0f
                    pitchBallVY.value = 0f
                    ballPossessionPlayerId = null
                }
            }
        }
    }

    private fun updateUserPlayerMovement() {
        // Move Active Selected User player by virtual joysticks
        val active = userPlayers[selectedPlayerIndex]
        active.x += joystickCurrentX *6f
        active.y += joystickCurrentY *6f
        
        // Constrain in pitch borders
        active.x = active.x.coerceIn(40f, 560f)
        active.y = active.y.coerceIn(800f * 0.1f, 850f)

        // Automatically make other user teammates position strategically relative to ball or defending positions
        val bX = pitchBallX.value
        val bY = pitchBallY.value
        
        userPlayers.forEachIndexed { index, teammate ->
            if (index != selectedPlayerIndex) {
                if (teammate.isGK) {
                    // GK defends goal post (Y=850-870, width 250-350)
                    teammate.targetX = bX.coerceIn(240f, 360f)
                    teammate.targetY = 860f
                } else {
                    // Simple marking positioning
                    teammate.targetX = (teammate.targetX + bX) / 2
                    teammate.targetY = (teammate.targetY + bY) / 2
                }
                
                // Move towards targets gently
                teammate.x += (teammate.targetX - teammate.x) * 0.04f
                teammate.y += (teammate.targetY - teammate.y) * 0.04f
            }
        }
    }

    private fun updateOpponentPlayerMovement() {
        val bX = pitchBallX.value
        val bY = pitchBallY.value

        // AI opponent behaviour: runs towards the ball if user has it, defends goal, or goal rushes
        opponentPlayers.forEach { opponent ->
            if (opponent.isGK) {
                // Opponent GK stays around goal post (Y=40, width 240-360)
                opponent.targetX = bX.coerceIn(240f, 360f)
                opponent.targetY = 40f
            } else {
                // Chase the ball
                opponent.targetX = bX
                opponent.targetY = bY
            }

            // Move opponent players towards their objective with a rate determined by rating (higher rating = faster defense chase)
            val pursuitSpeed = (opponent.rating / 90f) * 4.2f
            val dx = opponent.targetX - opponent.x
            val dy = opponent.targetY - opponent.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > 5f) {
                opponent.x += (dx / dist) * pursuitSpeed
                opponent.y += (dy / dist) * pursuitSpeed
            }
        }
    }

    private fun updateBallPhysics() {
        // Apply velocity decay (friction of the grass floor)
        val friction = 0.96f
        pitchBallVX.value *= friction
        pitchBallVY.value *= friction

        pitchBallX.value += pitchBallVX.value
        pitchBallY.value += pitchBallVY.value

        // Boundary bounce rules
        // Bottom Goal outline (X between 230 & 370, Y >= 880)
        if (pitchBallX.value in 220f..380f && pitchBallY.value >= 880f) {
            triggerGoal(false) // Opponent scores!
        }
        // Top Goal outline (X between 230 & 370, Y <= 20)
        else if (pitchBallX.value in 220f..380f && pitchBallY.value <= 20f) {
            triggerGoal(true) // User scores!
        }

        // Normal limits & Wall bounces
        if (pitchBallX.value < 20f) {
            pitchBallX.value = 20f
            pitchBallVX.value *= -0.5f // reverse velocity
        }
        if (pitchBallX.value > 580f) {
            pitchBallX.value = 580f
            pitchBallVX.value *= -0.5f
        }
        if (pitchBallY.value < 20f) {
            pitchBallY.value = 20f
            pitchBallVY.value *= -0.5f
        }
        if (pitchBallY.value > 880f) {
            pitchBallY.value = 880f
            pitchBallVY.value *= -0.5f
        }
    }

    private fun checkBallCollisions() {
        val bX = pitchBallX.value
        val bY = pitchBallY.value

        // If ball possessed by someone, the ball stays locked slightly in front of them
        val possessedPlayer = ballPossessionPlayerId?.let { playerId ->
            userPlayers.find { it.id == playerId } ?: opponentPlayers.find { it.id == playerId }
        }

        if (possessedPlayer != null) {
            // Keep ball glued to their dribbling foot!
            val offsetAngle = if (possessedPlayer.isUserTeam) -15f else 15f
            pitchBallX.value = possessedPlayer.x
            pitchBallY.value = possessedPlayer.y + offsetAngle
            pitchBallVX.value = 0f
            pitchBallVY.value = 0f
            return
        }

        // Otherwise, can be picked up by player hitting the radius of 15dp
        // User team pick-up Check
        userPlayers.forEachIndexed { index, player ->
            val dist = sqrt((player.x - bX) * (player.x - bX) + (player.y - bY) * (player.y - bY))
            if (dist < 28f) {
                ballPossessionPlayerId = player.id
                ballPossessionIsUser = true
                selectedPlayerIndex = index // auto switch control to the player holding the ball
                commentaryText = "${player.name} has the ball!"
            }
        }

        // Opponent team pick-up Check
        opponentPlayers.forEach { player ->
            val dist = sqrt((player.x - bX) * (player.x - bX) + (player.y - bY) * (player.y - bY))
            if (dist < 28f) {
                ballPossessionPlayerId = player.id
                ballPossessionIsUser = false
                commentaryText = "${player.name} intercepts the ball!"
            }
        }
    }

    // Touch Actions (Pass, Shoot, Tackle)
    fun triggerPass() {
        val userId = ballPossessionPlayerId
        if (userId != null && ballPossessionIsUser) {
            // Let's pass to the closest forward teammate in the direction
            val passer = userPlayers.find { it.id == userId } ?: return
            val targets = userPlayers.filter { it.id != userId }
            val closest = targets.minByOrNull { t ->
                val dx = t.x - passer.x
                val dy = t.y - passer.y
                sqrt(dx*dx + dy*dy)
            }
            
            if (closest != null) {
                // Free the ball and give velocity towards them!
                ballPossessionPlayerId = null
                statsPassesUser++
                
                val dx = closest.x - passer.x
                val dy = closest.y - passer.y
                val dist = sqrt(dx*dx + dy*dy)
                
                pitchBallVX.value = (dx / dist) * 16f
                pitchBallVY.value = (dy / dist) * 16f
                commentaryText = "Spectacular pass towards ${closest.name}!"
            }
        }
    }

    fun triggerShoot() {
        val userId = ballPossessionPlayerId
        if (userId != null && ballPossessionIsUser) {
            val shooter = userPlayers.find { it.id == userId } ?: return
            
            // Goal target center is (300f, 25f)
            ballPossessionPlayerId = null
            statsShotsUser++

            val dx = 300f - shooter.x
            val dy = 25f - shooter.y
            val dist = sqrt(dx*dx + dy*dy)

            // Dynamic shooting velocity based on shooter's stats
            val shotPower = 20f + (shooter.rating / 95f) * 6f
            pitchBallVX.value = (dx / dist) * shotPower
            pitchBallVY.value = (dy / dist) * shotPower
            commentaryText = "${shooter.name} UNLEASHES A POWERFUL SHOT!"
        }
    }

    fun triggerTackle() {
        if (!ballPossessionIsUser && ballPossessionPlayerId != null) {
            // Tackle active player
            statsTacklesUser++
            val userP = userPlayers[selectedPlayerIndex]
            val oppId = ballPossessionPlayerId
            val holdingOpponent = opponentPlayers.find { it.id == oppId }
            
            if (holdingOpponent != null) {
                val dist = sqrt((userP.x - holdingOpponent.x)*(userP.x - holdingOpponent.x) + (userP.y - holdingOpponent.y)*(userP.y - holdingOpponent.y))
                if (dist < 45f) {
                    // Tackle successful, ball goes loose!
                    ballPossessionPlayerId = userP.id
                    ballPossessionIsUser = true
                    commentaryText = "Brilliant slide tackle by ${userP.name} to steal the ball!"
                } else {
                    commentaryText = "Tackle missed! Opponent shields the ball."
                }
            }
        }
    }

    private fun triggerGoal(byUser: Boolean) {
        matchState = "GOAL"
        if (byUser) {
            matchScoreUser++
            matchGoalScorer = userPlayers.find { it.id == ballPossessionPlayerId }?.name ?: "Ultimate FC"
            commentaryText = "💥 GOAAAAAL! $matchGoalScorer scores!"
            commentaryLogs.add("⚽ [${matchTimeMinute}'] GOAL! Ultimate FC scores courtesy of $matchGoalScorer!")
        } else {
            matchScoreOpponent++
            matchGoalScorer = opponentPlayers.find { it.id == ballPossessionPlayerId }?.name ?: aiOpponentTeam
            commentaryText = "⚽ Opponent Goal! $matchGoalScorer finds the back of the net!"
            commentaryLogs.add("⚽ [${matchTimeMinute}'] GOAL! $aiOpponentTeam strikes back through $matchGoalScorer!")
        }
        ballPossessionPlayerId = null
    }

    private fun finishMatch() {
        gameLoopJob?.cancel()
        val result = if (matchScoreUser > matchScoreOpponent) "WIN" else if (matchScoreUser == matchScoreOpponent) "DRAW" else "LOSS"
        val coinsReward = if (result == "WIN") 250 else if (result == "DRAW") 100 else 40
        
        viewModelScope.launch {
            repository.rewardCoins(coinsReward, result == "WIN", matchScoreUser)
        }
        matchState = "FULL_TIME"
        commentaryText = "Match Finished! You earned $coinsReward coins."
    }

    fun resumeHalfTime() {
        matchHalfTime = 2
        matchState = "PLAYING"
        pitchBallX.value = 300f
        pitchBallY.value = 450f
        pitchBallVX.value = 0f
        pitchBallVY.value = 0f
        ballPossessionPlayerId = null
        startGameLoop()
    }

    fun quitAiMatch() {
        gameLoopJob?.cancel()
        matchState = "PRE_MATCH"
        navigateTo(AppScreen.HOME)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        pvpGameJob?.cancel()
    }
}
