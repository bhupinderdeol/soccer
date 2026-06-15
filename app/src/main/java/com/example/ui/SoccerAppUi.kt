package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoccerAppUi(viewModel: SoccerViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val squad by viewModel.mySquad.collectAsState()
    val unlocked by viewModel.unlockedPlayers.collectAsState()
    val allPls by viewModel.allPlayers.collectAsState()

    Scaffold(
        bottomBar = {
            if (viewModel.currentScreen != AppScreen.GAME_PLAY && viewModel.currentScreen != AppScreen.PVP_GAME) {
                SoccerBottomNavigation(
                    currentScreen = viewModel.currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkEmerald, DarkCharcoal)
                    )
                )
                .padding(innerPadding)
        ) {
            when (viewModel.currentScreen) {
                AppScreen.HOME -> HomeScreen(viewModel, stats, squad)
                AppScreen.SQUAD -> SquadScreen(viewModel, squad, unlocked)
                AppScreen.PLAYREST_SELECTION -> SelectionScreen(viewModel)
                AppScreen.GAME_PLAY -> GameplayScreen(viewModel)
                AppScreen.PVP_HUB -> PvpHubScreen(viewModel, stats)
                AppScreen.PVP_GAME -> PvpGameScreen(viewModel)
                AppScreen.SHOP -> ShopScreen(viewModel, stats)
                AppScreen.ROSTERS_VIEWER -> RostersViewerScreen(viewModel, allPls)
            }

            // Pack opening overlay trigger
            if (viewModel.unpackedPlayer != null || viewModel.isOpeningPack || viewModel.packError != null) {
                PackOpeningOverlay(
                    isOpening = viewModel.isOpeningPack,
                    player = viewModel.unpackedPlayer,
                    error = viewModel.packError,
                    onDismiss = { viewModel.dismissPackReveal() }
                )
            }
        }
    }
}

@Composable
fun SoccerBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = StadiumSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(1.dp, Color.White.copy(alpha = 0.05f))
            .navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPitch,
                selectedTextColor = EmeraldPitch,
                unselectedIconColor = PlatinumSilver.copy(alpha = 0.6f),
                unselectedTextColor = PlatinumSilver.copy(alpha = 0.5f),
                indicatorColor = DarkEmerald
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SQUAD,
            onClick = { onNavigate(AppScreen.SQUAD) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Squad") },
            label = { Text("My Squad", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPitch,
                selectedTextColor = EmeraldPitch,
                unselectedIconColor = PlatinumSilver.copy(alpha = 0.6f),
                unselectedTextColor = PlatinumSilver.copy(alpha = 0.5f),
                indicatorColor = DarkEmerald
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.ROSTERS_VIEWER,
            onClick = { onNavigate(AppScreen.ROSTERS_VIEWER) },
            icon = { Icon(Icons.Default.List, contentDescription = "Rosters") },
            label = { Text("Rosters", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPitch,
                selectedTextColor = EmeraldPitch,
                unselectedIconColor = PlatinumSilver.copy(alpha = 0.6f),
                unselectedTextColor = PlatinumSilver.copy(alpha = 0.5f),
                indicatorColor = DarkEmerald
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.PVP_HUB,
            onClick = { onNavigate(AppScreen.PVP_HUB) },
            icon = { Icon(Icons.Default.Star, contentDescription = "PVP") },
            label = { Text("PVP League", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPitch,
                selectedTextColor = EmeraldPitch,
                unselectedIconColor = PlatinumSilver.copy(alpha = 0.6f),
                unselectedTextColor = PlatinumSilver.copy(alpha = 0.5f),
                indicatorColor = DarkEmerald
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SHOP,
            onClick = { onNavigate(AppScreen.SHOP) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Packs") },
            label = { Text("Packs Store", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPitch,
                selectedTextColor = EmeraldPitch,
                unselectedIconColor = PlatinumSilver.copy(alpha = 0.6f),
                unselectedTextColor = PlatinumSilver.copy(alpha = 0.5f),
                indicatorColor = DarkEmerald
            )
        )
    }
}

@Composable
fun PremiumHeader(stats: UserStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Account Profile Design
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp)
            ) {
                // Profile Circle (re-creating the slate background + emerald-500 border)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(DarkEmerald)
                        .border(1.5.dp, EmeraldPitch, CircleShape)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FC",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                // Level label badge at top-left/bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .background(EmeraldPitch, RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "LV.24",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkCharcoal
                    )
                }
            }

            Column {
                Text(
                    text = "CAPTAIN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = PlatinumSilver.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "AISTUDIO.FC",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Right Column: Division Mapped + Coin Badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coin pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(StadiumSurface.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(NeonGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪙", fontSize = 9.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.coins}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Division pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(StadiumSurface.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("🛡️", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = stats.division.replace("Division ", "D"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldPitch
                )
            }
        }
    }
}

// HOME DASHBOARD
@Composable
fun HomeScreen(
    viewModel: SoccerViewModel,
    stats: UserStats,
    squad: List<PlayerCard>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium Sophisticated Header
        item {
            PremiumHeader(stats = stats)
        }

        // App Title Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, NeonGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                // Diagonal grass lines style
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val brush = Brush.linearGradient(
                        colors = listOf(DarkEmerald, EmeraldPitch.copy(alpha = 0.4f))
                    )
                    drawRect(brush)
                    // Draw lines
                    for (i in -10..10) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = Offset(i * 100f, 0f),
                            end = Offset((i + 5) * 100f, size.height),
                            strokeWidth = 35f
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "FIFA SOCCER",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGold,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            "ULTIMATE MOBILE EDITION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlatinumSilver,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚽", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "5v5 Field Match Controller ready",
                                fontSize = 12.sp,
                                color = EmeraldPitch,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text("🏆", fontSize = 54.sp)
                }
            }
        }

        // Coins & Ranking statistics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL COINS", fontSize = 10.sp, color = PlatinumSilver.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${stats.coins}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("LEAGUE DIVISION", fontSize = 10.sp, color = PlatinumSilver.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stats.division,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlatinumSilver
                            )
                        }
                    }
                }
            }
        }

        // Ultimate Team Rating Badge
        item {
            val avgRating = if (squad.isNotEmpty()) squad.map { it.rating }.average().roundToInt() else 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonGold.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ULTIMATE TEAM OVERALL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Squad size: ${squad.size}/5 positions filled",
                            fontSize = 13.sp,
                            color = PlatinumSilver
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Brush.radialGradient(listOf(NeonGold, CardBackgroundGold)),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            "$avgRating",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkCharcoal
                        )
                    }
                }
            }
        }

        // Action Buttons Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.PLAYREST_SELECTION) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("arcade_match_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎮  PLAY VS FIELD ROSTER (AI)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.PVP_HUB) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("pvp_league_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StadiumSurface),
                    border = BorderStroke(1.dp, NeonGold.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚔️  MULTIPLAYER PVP RANKED", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonGold)
                    }
                }
            }
        }

        // History / Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CLUB TROPHY HISTORY & RECORDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PlatinumSilver
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("MATCHES PLAYED", fontSize = 10.sp, color = PlatinumSilver.copy(alpha = 0.5f))
                            Text("${stats.matchesPlayed}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("MATCHES WON", fontSize = 10.sp, color = PlatinumSilver.copy(alpha = 0.5f))
                            Text("${stats.matchesWon}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldPitch)
                        }
                        Column {
                            Text("GOALS INFLICTED", fontSize = 10.sp, color = PlatinumSilver.copy(alpha = 0.5f))
                            Text("${stats.goalsScored}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonGold)
                        }
                    }
                }
            }
        }

        // Reset details
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Reset Club Assets",
                    fontSize = 11.sp,
                    color = CrimsonRed.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { viewModel.resetAllData() }
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ROSTER VIEWER
@Composable
fun RostersViewerScreen(viewModel: SoccerViewModel, allPlayersList: List<PlayerCard>) {
    var selectedTeamTab by remember { mutableStateOf("Real Madrid") }
    val teams = listOf("Real Madrid", "Manchester City", "FC Bayern", "FC Barcelona", "PSG", "Liverpool", "Arsenal")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "OFFICIAL REAL TEAM ROSTERS",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NeonGold
        )
        Text(
            "Inspect competitive rosters and ratings used in Match opponent selection.",
            fontSize = 11.sp,
            color = PlatinumSilver.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Tabs Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(teams) { team ->
                val isSelected = team == selectedTeamTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(if (isSelected) NeonGold else StadiumSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(30.dp))
                        .clickable { selectedTeamTab = team }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        team,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) DarkCharcoal else PlatinumSilver
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Players roster list
        val filtered = allPlayersList.filter { it.team == selectedTeamTab }
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { player ->
                    PlayerRosterRow(player)
                }
            }
        }
    }
}

@Composable
fun PlayerRosterRow(player: PlayerCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Rating Big Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.radialGradient(listOf(NeonGold.copy(0.4f), Color.Transparent)),
                        shape = CircleShape
                    )
                    .border(2.dp, NeonGold, CircleShape)
            ) {
                Text(
                    "${player.rating}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(player.position, fontSize = 10.sp, color = PlatinumSilver, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(player.team, fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.6f))
                }
            }

            // Simple Stat mini grid
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatMini("PAC", player.pace)
                    StatMini("SHO", player.shooting)
                    StatMini("PAS", player.passing)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatMini("DRI", player.dribbling)
                    StatMini("DEF", player.defense)
                    StatMini("PHY", player.physical)
                }
            }
        }
    }
}

@Composable
fun StatMini(label: String, value: Int) {
    Row {
        Text("$label:", fontSize = 9.sp, color = PlatinumSilver.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.width(2.dp))
        Text("$value", fontSize = 9.sp, color = NeonGold, fontWeight = FontWeight.Bold)
    }
}

// MY SQUAD & ROSTER SWAPPING
@Composable
fun SquadScreen(viewModel: SoccerViewModel, squad: List<PlayerCard>, unlockedList: List<PlayerCard>) {
    var tabSelected by remember { mutableStateOf("SQUAD") } // SQUAD, RESERVE

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ULTIMATE SQUAD ASSEMBLY", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeonGold)
                Text("Select up to 5 players as active starting line up. Packs shop to unlock elites.", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Screen selection tabs
        Row(
            modifier = Modifier.fillMaxWidth().background(StadiumSurface, RoundedCornerShape(12.dp)).padding(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (tabSelected == "SQUAD") EmeraldPitch else Color.Transparent)
                    .clickable { tabSelected = "SQUAD" }
                    .padding(vertical = 10.dp)
            ) {
                Text("Active Lineup (${squad.size}/5)", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (tabSelected == "RESERVE") EmeraldPitch else Color.Transparent)
                    .clickable { tabSelected = "RESERVE" }
                    .padding(vertical = 10.dp)
            ) {
                Text("Bench/Reserve Pack", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tabSelected == "SQUAD") {
            // Visual green pitch represent!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkEmerald)
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
            ) {
                // drawing football pitch markings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // lines in pitch
                    drawRect(color = EmeraldPitch.copy(alpha = 0.25f))
                    // goal box outer lines
                    drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 80f, center = Offset(size.width / 2, size.height / 2), style = Stroke(width = 3f))
                    drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 3f)
                }

                // Placing active players in a mini 5-futsal schema tactical arrangement
                val placements = listOf(
                    Offset(0.5f, 0.85f), // GK
                    Offset(0.25f, 0.6f), // DEF L
                    Offset(0.75f, 0.6f), // DEF R
                    Offset(0.5f, 0.42f), // MID
                    Offset(0.5f, 0.2f)   // FWD
                )

                squad.forEachIndexed { index, player ->
                    val placement = placements.getOrNull(index) ?: Offset(0.5f, 0.5f)
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val px = placement.x * maxWidth.value
                        val py = placement.y * maxHeight.value
                        
                        Box(
                            modifier = Modifier
                                .offset(px.dp - 40.dp, py.dp - 48.dp)
                                .size(width = 80.dp, height = 96.dp)
                        ) {
                            TacticalPlayerCard(player = player) {
                                viewModel.toggleSquadStatus(player)
                            }
                        }
                    }
                }
            }
        } else {
            // RESERVE LIST
            val reserves = unlockedList.filter { !it.isMySquad }
            if (reserves.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("👋 Reserve inventory is currently empty!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PlatinumSilver)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Head to the 'Packs Store' and use your starter coins to unpack competitive superstars!",
                            fontSize = 11.sp,
                            color = PlatinumSilver.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = { viewModel.navigateTo(AppScreen.SHOP) }, colors = ButtonDefaults.buttonColors(containerColor = NeonGold)) {
                            Text("Open Pack", color = DarkCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reserves) { reserve ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                            border = BorderStroke(1.dp, Color.White.copy(0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(36.dp).background(NeonGold, shape = CircleShape)
                                ) {
                                    Text("${reserve.rating}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = DarkCharcoal)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reserve.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${reserve.position}  •  ${reserve.team}", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.6f))
                                }
                                Button(
                                    onClick = { viewModel.toggleSquadStatus(reserve) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch)
                                ) {
                                    Text("Swap In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalPlayerCard(player: PlayerCard, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onRemove() }
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundGold),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${player.rating}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = DarkCharcoal,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                player.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCharcoal)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    player.position,
                    fontSize = 9.sp,
                    color = NeonGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// PACKS SHOP
@Composable
fun ShopScreen(viewModel: SoccerViewModel, stats: UserStats) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ULTIMATE PACKS STORE", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeonGold)
                Text("Use your gathered coins to sign contract cards for elite champions.", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.5f))
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                border = BorderStroke(1.dp, NeonGold.copy(0.3f))
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${stats.coins}", fontSize = 13.sp, color = NeonGold, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PackShopItem(
                    title = "ROOKIE SCOUT PACK",
                    description = "Contains 1 player card. Prefers overall ratings < 85. Perfect to fill roster roles.",
                    cost = 500,
                    packColor = PlatinumSilver,
                    tag = "ROOKIE",
                    userCoins = stats.coins
                ) {
                    viewModel.openPack("ROOKIE", 500)
                }
            }
            item {
                PackShopItem(
                    title = "PREMIUM GOLD PACK",
                    description = "Contains 1 highly skilled gold player card. Guarantee ratings between 84 and 88 overall.",
                    cost = 1000,
                    packColor = NeonGold,
                    tag = "GOLD",
                    userCoins = stats.coins
                ) {
                    viewModel.openPack("GOLD", 1000)
                }
            }
            item {
                PackShopItem(
                    title = "ELITE CHAMPIONS PACK",
                    description = "Contains 1 world champion superstar card. Guarantees 88+ rating! Mbappe, Haaland, De Bruyne odds ultra high.",
                    cost = 2000,
                    packColor = EpicPurple,
                    tag = "ELITE",
                    userCoins = stats.coins
                ) {
                    viewModel.openPack("ELITE", 2000)
                }
            }
        }
    }
}

@Composable
fun PackShopItem(
    title: String,
    description: String,
    cost: Int,
    packColor: Color,
    tag: String,
    userCoins: Int,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, packColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(packColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkCharcoal
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$cost", fontSize = 16.sp, color = NeonGold, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(16.dp))

            val hasCoins = userCoins >= cost
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasCoins) packColor else Color.Gray.copy(0.2f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = hasCoins
            ) {
                Text(
                    if (hasCoins) "TAP TO OPEN PACK" else "INSUFFICIENT COINS",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasCoins) DarkCharcoal else PlatinumSilver.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// PACK REVEAL OVERLAY MODULE
@Composable
fun PackOpeningOverlay(
    isOpening: Boolean,
    player: PlayerCard?,
    error: String?,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(enabled = player != null || error != null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        if (isOpening) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonGold, strokeWidth = 5.dp, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "CONVENING PLAYER CONTRACT...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold,
                    letterSpacing = 2.sp
                )
                Text(
                    "Signing up superstars...",
                    fontSize = 11.sp,
                    color = PlatinumSilver.copy(alpha = 0.6f)
                )
            }
        } else if (error != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("⚠️ OPERATION FAILED", fontSize = 18.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(error, fontSize = 13.sp, color = PlatinumSilver, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tap anywhere to close", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.4f))
            }
        } else if (player != null) {
            // Unpacked Player Card Reveal Animation Frame!
            val infiniteTransition = rememberInfiniteTransition()
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🎉 UNPACKED CHAMPION!",
                    fontSize = 18.sp,
                    color = NeonGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    // Shining backglow halo
                    Canvas(modifier = Modifier.size(220.dp).rotate(angle)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonGold.copy(0.6f), Color.Transparent)
                            ),
                            radius = size.width / 2
                        )
                    }

                    // Realistic Soccer card
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(220.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundGold),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(4.dp, NeonGold)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${player.rating}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DarkCharcoal
                                )
                                Text(
                                    player.position,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DarkCharcoal
                                )
                            }

                            Text(
                                player.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                player.team,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkCharcoal.copy(0.7f),
                                textAlign = TextAlign.Center
                            )

                            // Quick metrics display inside card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkCharcoal, RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text("PAC ${player.pace}", fontSize = 9.sp, color = NeonGold)
                                    Text("SHO ${player.shooting}", fontSize = 9.sp, color = NeonGold)
                                    Text("PAS ${player.passing}", fontSize = 9.sp, color = NeonGold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text("DRI ${player.dribbling}", fontSize = 9.sp, color = NeonGold)
                                    Text("DEF ${player.defense}", fontSize = 9.sp, color = NeonGold)
                                    Text("PHY ${player.physical}", fontSize = 9.sp, color = NeonGold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "COMPLETELY CONCLUDED!",
                    fontSize = 13.sp,
                    color = PlatinumSilver,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tap anywhere to claim and adjust lineup",
                    fontSize = 11.sp,
                    color = PlatinumSilver.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// MATCH SELECTION LOBBY
@Composable
fun SelectionScreen(viewModel: SoccerViewModel) {
    val opponents = listOf("Real Madrid", "Manchester City", "FC Bayern", "FC Barcelona", "PSG", "Liverpool", "Arsenal")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text("SELECT MATCH OPPONENT", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeonGold)
        Text("Challenge prestigious world clubs featuring realistic rosters.", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(opponents) { opponent ->
                val isSelected = viewModel.aiOpponentTeam == opponent
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isSelected) NeonGold else Color.White.copy(0.05f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectAiOpponent(opponent) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StadiumSurface else StadiumSurface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛡️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(opponent, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Official squad including superstars ratings overview", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.5f))
                        }
                        if (isSelected) {
                            Text("👉 SELECTED", fontSize = 10.sp, color = NeonGold, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startAiMatch(); viewModel.navigateTo(AppScreen.GAME_PLAY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("match_kickoff_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch)
                ) {
                    Text("LAUNCH KICKOFF", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}

// 2D FOOTBALL FIELD ARCADE ENGINE SCREEN
@Composable
fun GameplayScreen(viewModel: SoccerViewModel) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoal)
    ) {
        // Score/Time Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StadiumSurface)
                .padding(horizontal = 16.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.quitAiMatch() }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Exit to main", tint = Color.White)
            }
            
            // Score Board represent
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ultimate FC", fontSize = 12.sp, color = NeonGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${viewModel.matchScoreUser} - ${viewModel.matchScoreOpponent}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(viewModel.aiOpponentTeam, fontSize = 12.sp, color = PlatinumSilver, fontWeight = FontWeight.Bold)
            }

            // Game scale minutes
            Box(
                modifier = Modifier
                    .background(EmeraldPitch, RoundedCornerShape(100.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "${viewModel.matchTimeMinute}:00",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Action commentary ticker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.04f))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                viewModel.commentaryText,
                fontSize = 12.sp,
                color = NeonGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
        }

        // The Gameplay Interactive Canvas Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkEmerald)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background grass
                drawRect(color = EmeraldPitch.copy(alpha = 0.6f))

                // Pitch markers
                // Boundaries
                drawRect(
                    color = Color.White,
                    style = Stroke(width = 3f)
                )
                // Halfway center circle
                drawCircle(
                    color = Color.White,
                    radius = 120f,
                    center = Offset(size.width / 2, size.height / 2),
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = Color.White,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 3f
                )

                // Top Goal Box (Y up to 100)
                drawRect(
                    color = Color.White,
                    style = Stroke(width = 3f),
                    size = androidx.compose.ui.geometry.Size(300f, 100f),
                    topLeft = Offset((size.width / 2) - 150f, 0f)
                )

                // Bottom Goal Box (Y down to 100)
                drawRect(
                    color = Color.White,
                    style = Stroke(width = 3f),
                    size = androidx.compose.ui.geometry.Size(300f, 100f),
                    topLeft = Offset((size.width / 2) - 150f, size.height - 100f)
                )

                // Draw Goal outlines
                // Top net
                drawLine(
                    color = NeonGold,
                    start = Offset((size.width / 2) - 80f, 2f),
                    end = Offset((size.width / 2) + 80f, 2f),
                    strokeWidth = 10f
                )
                // Bottom net
                drawLine(
                    color = NeonGold,
                    start = Offset((size.width / 2) - 80f, size.height - 2f),
                    end = Offset((size.width / 2) + 80f, size.height - 2f),
                    strokeWidth = 10f
                )

                // Render User Teammates
                viewModel.userPlayers.forEachIndexed { idx, player ->
                    val isControlled = idx == viewModel.selectedPlayerIndex
                    val scaledX = (player.x / 600f) * size.width
                    val scaledY = (player.y / 900f) * size.height

                    if (isControlled) {
                        drawCircle(
                            color = NeonGold.copy(0.35f),
                            radius = 35f,
                            center = Offset(scaledX, scaledY)
                        )
                    }

                    drawCircle(
                        color = if (player.isGK) NeonGold else Color(0xFF00C8FF),
                        radius = 18f,
                        center = Offset(scaledX, scaledY)
                    )
                }

                // Render Opponent Squad
                viewModel.opponentPlayers.forEach { player ->
                    val scaledX = (player.x / 600f) * size.width
                    val scaledY = (player.y / 900f) * size.height

                    drawCircle(
                        color = Color(0xFFFF3B30),
                        radius = 18f,
                        center = Offset(scaledX, scaledY)
                    )
                }

                // Render soccer ball
                val ballCanvasX = (viewModel.pitchBallX.value / 600f) * size.width
                val ballCanvasY = (viewModel.pitchBallY.value / 900f) * size.height
                
                drawCircle(
                    color = Color.Black.copy(alpha = 0.4f),
                    radius = 12f,
                    center = Offset(ballCanvasX + 3f, ballCanvasY + 3f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(ballCanvasX, ballCanvasY)
                )
                drawCircle(
                    color = DarkCharcoal,
                    radius = 4f,
                    center = Offset(ballCanvasX, ballCanvasY)
                )
            }

            if (viewModel.matchState == "GOAL") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚽ GOAAAL!", fontSize = 36.sp, fontWeight = FontWeight.Black, color = NeonGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(viewModel.matchGoalScorer, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Celebrations sound effects simulation...", fontSize = 11.sp, color = PlatinumSilver.copy(0.7f))
                    }
                }
            } else if (viewModel.matchState == "HALF_TIME") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("🏁 HALF TIME", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeonGold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Score: Ultimate FC ${viewModel.matchScoreUser} - ${viewModel.matchScoreOpponent} ${viewModel.aiOpponentTeam}",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.resumeHalfTime() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch)
                        ) {
                            Text("START SECOND HALF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (viewModel.matchState == "FULL_TIME") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("🏆 FULL TIME SUMMARY", fontSize = 26.sp, fontWeight = FontWeight.Black, color = NeonGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Final Score: Ultimate FC ${viewModel.matchScoreUser} - ${viewModel.matchScoreOpponent} ${viewModel.aiOpponentTeam}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("MATCH STATS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PlatinumSilver)
                                Spacer(modifier = Modifier.height(8.dp))
                                StatsRow("Possession %", "${viewModel.statsPossessionUser}%", "50%")
                                StatsRow("Shots", "${viewModel.statsShotsUser}", "${viewModel.statsShotsOpponent}")
                                StatsRow("Successful Passes", "${viewModel.statsPassesUser}", "${viewModel.statsPassesOpponent.value}")
                                StatsRow("Inter tackles", "${viewModel.statsTacklesUser}", "${viewModel.statsTacklesOpponent}")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.quitAiMatch() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGold)
                        ) {
                            Text("CLAIM REWARDS & RETURN", color = DarkCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StadiumSurface)
                .padding(horizontal = 16.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                offsetX = 0f
                                offsetY = 0f
                                viewModel.joystickCurrentX = 0f
                                viewModel.joystickCurrentY = 0f
                            },
                            onDragCancel = {
                                offsetX = 0f
                                offsetY = 0f
                                viewModel.joystickCurrentX = 0f
                                viewModel.joystickCurrentY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                                val dist = kotlin.math.sqrt(offsetX * offsetX + offsetY * offsetY)
                                val maxDist = 80f
                                if (dist > maxDist) {
                                    offsetX = (offsetX / dist) * maxDist
                                    offsetY = (offsetY / dist) * maxDist
                                }
                                viewModel.joystickCurrentX = offsetX / maxDist
                                viewModel.joystickCurrentY = offsetY / maxDist
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(0.1f), CircleShape)
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .background(NeonGold, shape = CircleShape)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.triggerTackle() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFE74C3C), shape = CircleShape)
                        .testTag("tackle_touch_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛡️", fontSize = 16.sp)
                        Text("TACKLE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                IconButton(
                    onClick = { viewModel.triggerPass() },
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF3498DB), shape = CircleShape)
                        .testTag("pass_touch_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚽", fontSize = 18.sp)
                        Text("PASS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                IconButton(
                    onClick = { viewModel.triggerShoot() },
                    modifier = Modifier
                        .size(66.dp)
                        .background(NeonGold, shape = CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .testTag("shoot_touch_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥", fontSize = 20.sp)
                        Text("SHOOT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = DarkCharcoal)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsRow(label: String, valUser: String, valOpp: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(valUser, fontSize = 12.sp, color = NeonGold, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = PlatinumSilver.copy(0.6f))
        Text(valOpp, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// PVP ONLINE LEAGUE MODULE
@Composable
fun PvpHubScreen(viewModel: SoccerViewModel, stats: UserStats) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("RANKED PVP CHAMPIONSHIP", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeonGold)
                Text("Compete in real-time tactical duels with actual managers.", fontSize = 11.sp, color = PlatinumSilver.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.matchmakingState == "SEARCHING") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StadiumSurface)
                    .border(1.dp, NeonGold.copy(0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonGold, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("FINDING ACTIVE MANAGERS...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeonGold)
                    Text("Searching Division lobbies (seconds: ${viewModel.searchTimer})", fontSize = 11.sp, color = PlatinumSilver.copy(0.6f))
                }
            }
        } else if (viewModel.matchmakingState == "FOUND" || viewModel.matchmakingState == "CONNECTING") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StadiumSurface)
                    .border(2.dp, EmeraldPitch, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤝 MANAGER MATCH FOUND!", fontSize = 18.sp, fontWeight = FontWeight.Black, color = EmeraldPitch)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(viewModel.matchedOpponentName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Squad Rating: ${viewModel.matchedOpponentRating} OVR", fontSize = 13.sp, color = NeonGold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Establishing synchronized gameplay channel... (42ms ping)", fontSize = 11.sp, color = PlatinumSilver.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                        border = BorderStroke(1.dp, Color.White.copy(0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("CURRENT STANDINGS", fontSize = 10.sp, color = PlatinumSilver.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stats.division, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${stats.rankingPoints} PTS", fontSize = 18.sp, color = NeonGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.startPvPMatchmaking() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("pvp_match_search_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("FIND PVP RANKED OPPONENT", color = DarkCharcoal, fontWeight = FontWeight.Black)
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StadiumSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("GLOBAL DIVISION LEADERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PlatinumSilver)
                            Spacer(modifier = Modifier.height(10.dp))
                            LeaderRow(1, "SakaTaka_Arsenal", 4320, true)
                            LeaderRow(2, "CR7_Lover", 3950, false)
                            LeaderRow(3, "ApexStriker_7", 3800, false)
                            LeaderRow(4, "FifaLord_99", 3550, false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderRow(rank: Int, name: String, pts: Int, highlight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text("#$rank", fontSize = 13.sp, color = if (highlight) NeonGold else PlatinumSilver, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(10.dp))
            Text(name, fontSize = 13.sp, color = if (highlight) NeonGold else Color.White)
        }
        Text("$pts PTS", fontSize = 13.sp, color = PlatinumSilver.copy(0.7f), fontWeight = FontWeight.Bold)
    }
}

// PVP REAL-TIME TACTICAL MATCHPLAY SIMULATION
@Composable
fun PvpGameScreen(viewModel: SoccerViewModel) {
    val logs = viewModel.pvpMatchLogs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoal)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.leavePvp() },
                colors = ButtonDefaults.buttonColors(containerColor = StadiumSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Quit League Match", fontSize = 12.sp, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .background(NeonGold, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("LIVE SYNC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumSurface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = EmeraldPitch.copy(alpha = 0.15f))
                    drawLine(Color.White.copy(0.1f), start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2))
                    drawCircle(Color.White.copy(0.1f), radius = 50f, center = Offset(size.width/2, size.height/2))
                }
                
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔰 ULTIMATE FC", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${viewModel.pvpUserScore}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonGold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${viewModel.pvpMatchMinute}'", fontSize = 13.sp, color = NeonGold, fontWeight = FontWeight.Bold)
                        Text("CLOCK", fontSize = 10.sp, color = PlatinumSilver.copy(0.5f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.matchedOpponentName.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${viewModel.pvpOpponentScore}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PlatinumSilver)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("PVP MATCH LOG EVENT EVENTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PlatinumSilver.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(StadiumSurface, RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(0.04f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs.reversed()) { log ->
                Text(log, fontSize = 13.sp, color = PlatinumSilver)
            }
        }

        if (viewModel.isShowingPvpEventDialog) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = DarkCharcoal,
                title = {
                    Text(
                        viewModel.pvpCurrentEventTitle,
                        color = NeonGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        viewModel.pvpCurrentEventText,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.pvpEventOptions.forEachIndexed { idx, option ->
                            Button(
                                onClick = { viewModel.makePvpChoice(idx) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = StadiumSurface),
                                border = BorderStroke(1.dp, NeonGold.copy(0.4f))
                            ) {
                                Text(option, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        }
    }
}
