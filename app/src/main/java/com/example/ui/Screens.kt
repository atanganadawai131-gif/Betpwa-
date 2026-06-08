package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.BuildConfig
import kotlinx.coroutines.delay
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// MATCHES SCREEN
// ==========================================
@Composable
fun MatchesScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val slipSelections by viewModel.slipSelections.collectAsState()

    var activeSubTab by remember { mutableStateOf("PARIER") }
    var selectedSport by remember { mutableStateOf("Tous") }
    var showOnlyLive by remember { mutableStateOf(false) }

    val sports = listOf("Tous", "Football", "Basketball", "Tennis")

    // Filter logic for matches
    val filteredMatches = matches.filter { match ->
        val sportMatch = if (selectedSport == "Tous") true else match.sport == selectedSport
        val liveMatch = if (showOnlyLive) match.status == "LIVE" else true
        sportMatch && liveMatch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        // High density sub tabs row to switch between Betting and aggregated Sports News
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubTab = "PARIER" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "PARIER") PawaGreen else DarkSurface,
                    contentColor = if (activeSubTab == "PARIER") Color.White else TextPrimary
                ),
                modifier = Modifier.weight(1f).height(44.dp).testTag("tab_sports_betting"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (activeSubTab == "PARIER") PawaGreen else DarkBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (activeSubTab == "PARIER") Color.White else TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paris en Direct", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeSubTab = "ACTUS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "ACTUS") PawaGreen else DarkSurface,
                    contentColor = if (activeSubTab == "ACTUS") Color.White else TextPrimary
                ),
                modifier = Modifier.weight(1f).height(44.dp).testTag("tab_sports_news"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (activeSubTab == "ACTUS") PawaGreen else DarkBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (activeSubTab == "ACTUS") Color.White else TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Actus Sportives", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (activeSubTab == "PARIER") {
            // Hero Live betting indicator banner
            LiveSimulationBanner(viewModel)

            Spacer(modifier = Modifier.height(12.dp))

            // Sports Filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sports.forEach { sport ->
                    val isSelected = selectedSport == sport
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSport = sport },
                        label = { 
                            Text(
                                text = sport, 
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurface,
                            selectedContainerColor = PawaGreen,
                            labelColor = TextPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = BorderStroke(1.dp, if (isSelected) PawaGreen else DarkBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle Live only Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .clickable { showOnlyLive = !showOnlyLive }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stream,
                        contentDescription = "Live",
                        tint = if (showOnlyLive) PawaGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Afficher uniquement les matchs En Direct",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = showOnlyLive,
                    onCheckedChange = { showOnlyLive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PawaGreen,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkBorder
                    ),
                    modifier = Modifier.testTag("toggle_live_filter_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredMatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Aucun match",
                            tint = TextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun match disponible pour le moment.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("match_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredMatches, key = { it.id }) { match ->
                        MatchBetCard(
                            match = match,
                            slipSelections = slipSelections,
                            onOddSelected = { betType, outcome, odd ->
                                viewModel.selectOdd(match, betType, outcome, odd)
                            }
                        )
                    }
                }
            }
        } else {
            // Sports News Feed Section Aggregator!
            NewsFeedSection(viewModel)
        }
    }
}

@Composable
fun LiveSimulationBanner(viewModel: SportsViewModel) {
    val isSimulating by viewModel.isSimulating.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val liveCount = matches.count { it.status == "LIVE" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(1.dp, PawaGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSimulating) PawaGreen else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSimulating) "Simulation Temps Réel active" else "Autoplay en Pause",
                        color = if (isSimulating) PawaGreen else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Les scores et cotes évoluent sous vos yeux ! ($liveCount matchs live)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = { viewModel.toggleSimulation() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PawaGreen)
                    .testTag("toggle_simulator_btn")
            ) {
                Icon(
                    imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Simulateur play/pause",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MatchBetCard(
    match: MatchEntity,
    slipSelections: List<BetSelection>,
    onOddSelected: (String, String, Double) -> Unit
) {
    val isLive = match.status == "LIVE"
    val isFinished = match.status == "FINISHED"

    val current1X2Selection = slipSelections.find { it.matchId == match.id && it.betType == "1X2" }?.selection
    val currentOU25Selection = slipSelections.find { it.matchId == match.id && it.betType == "OverUnder" }?.selection

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isLive) PawaGreen.copy(alpha = 0.5f) else DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: League, Sport, and Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sportIcon = when (match.sport) {
                        "Football" -> Icons.Default.SportsSoccer
                        "Basketball" -> Icons.Default.SportsBasketball
                        "Tennis" -> Icons.Default.SportsTennis
                        else -> Icons.Default.Sports
                    }
                    Icon(
                        imageVector = sportIcon,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${match.league} • ${match.sport}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isLive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PawaOrange)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE ${match.minute}'",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                } else if (isFinished) {
                    Text(
                        text = "Terminé",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    val date = Date(match.startTime)
                    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = "À venir - ${formatter.format(date)}",
                        color = PawaGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score and team block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.homeTeam,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.awayTeam,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isLive || isFinished) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${match.homeScore}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLive) PawaGreen else Color.White
                        )
                        Divider(
                            modifier = Modifier
                                .width(16.dp)
                                .padding(vertical = 2.dp),
                            color = TextTertiary,
                            thickness = 1.dp
                        )
                        Text(
                            text = "${match.awayScore}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLive) PawaGreen else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Odds grid
            Text(
                text = "1X2 (Résultat du match)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Home odds (1)
                OddsButton(
                    label = "1",
                    odds = match.homeOdds,
                    isSelected = current1X2Selection == "1",
                    enabled = !isFinished,
                    onClick = { onOddSelected("1X2", "1", match.homeOdds) },
                    modifier = Modifier.weight(1f)
                )

                // Draw odds (X)
                OddsButton(
                    label = "Nul",
                    odds = match.drawOdds,
                    isSelected = current1X2Selection == "X",
                    enabled = !isFinished && match.sport != "Tennis", // Tennis doesn't have draws
                    onClick = { onOddSelected("1X2", "X", match.drawOdds) },
                    modifier = Modifier.weight(1f)
                )

                // Away odds (2)
                OddsButton(
                    label = "2",
                    odds = match.awayOdds,
                    isSelected = current1X2Selection == "2",
                    enabled = !isFinished,
                    onClick = { onOddSelected("1X2", "2", match.awayOdds) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Over / Under odds options for diversity
            Text(
                text = "Nombre de buts / points (Plus / Moins de 2.5)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OddsButton(
                    label = "Plus de 2.5",
                    odds = match.overOdds,
                    isSelected = currentOU25Selection == "Over",
                    enabled = !isFinished,
                    onClick = { onOddSelected("OverUnder", "Over", match.overOdds) },
                    modifier = Modifier.weight(1f)
                )
                OddsButton(
                    label = "Moins de 2.5",
                    odds = match.underOdds,
                    isSelected = currentOU25Selection == "Under",
                    enabled = !isFinished,
                    onClick = { onOddSelected("OverUnder", "Under", match.underOdds) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun OddsButton(
    label: String,
    odds: Double,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonBg = when {
        isSelected -> PawaGreen
        !enabled -> DarkSurface.copy(alpha = 0.5f)
        else -> DarkSurfaceElevated
    }
    val contentColor = when {
        isSelected -> Color.White
        !enabled -> TextTertiary
        else -> TextPrimary
    }
    val borderStroke = when {
        isSelected -> BorderStroke(1.dp, PawaGreen)
        else -> BorderStroke(1.dp, DarkBorder)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(buttonBg)
            .border(borderStroke, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(Locale.US, "%.2f", odds),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}


// ==========================================
// JACKPOT SCREEN (PAWA6 STYLE!)
// ==========================================
@Composable
fun JackpotScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val tickets by viewModel.jackpotTickets.collectAsState()
    val wallet by viewModel.wallet.collectAsState()

    // Select the first 6 Football matches for the Jackpot
    val jackpotMatches = remember(matches) {
        matches.filter { it.sport == "Football" }.take(6)
    }

    // Keep track of our selections in a dictionary (matchId -> predicted outcome)
    val predictions = remember { mutableStateMapOf<Int, String>() }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var successTicketPlaced by remember { mutableStateOf(false) }

    val formattedBalance = wallet?.balance?.toInt() ?: 10000

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), PawaGreen)
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Jackpot",
                                tint = PawaGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JACKPOT PAWA6",
                                color = PawaGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1 000 000 FCFA à gagner !",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pronostiquez le vainqueur des 6 matchs. Coût: 50 FCFA. Consolation à 5/6 (5000 FCFA) !",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        if (jackpotMatches.size < 6) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Données insuffisantes pour former le Jackpot. Besoin de 6 matchs football.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Jackpot match lists
            items(jackpotMatches.indices.toList()) { index ->
                val match = jackpotMatches[index]
                val currentChoice = predictions[match.id]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Match ${index + 1} de 6",
                                color = PawaGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = match.league,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${match.homeTeam}  vs  ${match.awayTeam}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (match.status == "FINISHED") {
                                val actualWin = when {
                                    match.homeScore > match.awayScore -> "1"
                                    match.homeScore == match.awayScore -> "X"
                                    else -> "2"
                                }
                                Text(
                                    text = "Résultat: $actualWin (${match.homeScore}-${match.awayScore})",
                                    color = PawaGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selection row: 1, X, 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1", "X", "2").forEach { choice ->
                                val isSelected = currentChoice == choice
                                val label = when (choice) {
                                    "1" -> "1 (Victoire ${match.homeTeam})"
                                    "X" -> "Nul"
                                    "2" -> "2 (Victoire ${match.awayTeam})"
                                    else -> ""
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PawaGold else DarkSurfaceElevated)
                                        .border(BorderStroke(1.dp, if (isSelected) PawaGold else DarkBorder), RoundedCornerShape(8.dp))
                                        .clickable {
                                            predictions[match.id] = choice
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (choice == "X") "Nul" else choice,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Jackpot placement action button
            item {
                Spacer(modifier = Modifier.height(10.dp))

                feedbackMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = if (successTicketPlaced) PawaGreen else PawaOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val allPredicted = predictions.size == 6

                Button(
                    onClick = {
                        if (!allPredicted) {
                            feedbackMessage = "Veuillez remplir les 6 pronostics avant de valider."
                            successTicketPlaced = false
                            return@Button
                        }
                        viewModel.submitJackpot(
                            predictions = predictions.toMap(),
                            onSuccess = {
                                predictions.clear()
                                feedbackMessage = "Super ! Ticket Jackpot placé (Déduit 50 FCFA des fonds)."
                                successTicketPlaced = true
                            },
                            onError = { err ->
                                feedbackMessage = err
                                successTicketPlaced = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_jackpot_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allPredicted) PawaGold else Color.Gray,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Valider Jackpot (50 FCFA)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // List past tickets
        if (tickets.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mes grilles Jackpot soumises",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(tickets) { ticket ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ticket #${ticket.id} (${ticket.jackpotName})",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            val statusBannerBg = when (ticket.status) {
                                "PENDING" -> DarkBorder
                                "WON" -> PawaGreen.copy(alpha = 0.2f)
                                else -> Color.Red.copy(alpha = 0.2f)
                            }
                            val statusBannerText = when (ticket.status) {
                                "PENDING" -> "En attente"
                                "WON" -> "GAGNÉ"
                                else -> "Perdu"
                            }
                            val statusColor = when (ticket.status) {
                                "PENDING" -> TextSecondary
                                "WON" -> PawaGreen
                                else -> Color.Red
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusBannerBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusBannerText,
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Corrects: ${ticket.correctPredictions} / ${ticket.totalMatches}",
                            color = if (ticket.correctPredictions == ticket.totalMatches) PawaGold else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// SLIP SCREEN (PANIER)
// ==========================================
@Composable
fun SlipScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val selections by viewModel.slipSelections.collectAsState()
    val stakeStr by viewModel.stakeInput.collectAsState()
    val wallet by viewModel.wallet.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    val totalOdds = remember(selections) {
        selections.fold(1.0) { acc, sel -> acc * sel.odds }
    }
    val legCount = selections.size
    val bonusPercent = viewModel.getWinBonusPercent(legCount)

    val currentStake = stakeStr.toDoubleOrNull() ?: 0.0
    val rawPotentialWin = currentStake * totalOdds
    val bonusAmount = rawPotentialWin * (bonusPercent / 100.0)
    val grandTotalPayout = rawPotentialWin + bonusAmount

    val currency = "FCFA"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mon Panier de Paris ($legCount)",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            if (selections.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearSlip() },
                    colors = ButtonDefaults.textButtonColors(contentColor = PawaOrange)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tout effacer", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = "Panier vide",
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Votre panier est vide.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sélectionnez des cotes de matchs pour composer votre pari combiné !",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            // Scrollable list of selections
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(selections, key = { "${it.matchId}_${it.betType}_${it.selection}" }) { selection ->
                    SlipItemCard(
                        selection = selection,
                        onDeleteClick = { viewModel.removeSelection(selection) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Betting Slip controls / Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Win bonus announcement! Famous in BetPawa!
                    if (bonusPercent > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PawaGreen.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, PawaGreen), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = null, tint = PawaGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BetPawa Bonus Combiné: +$bonusPercent% !" ,
                                    color = PawaGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Total odds sum
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total des cotes", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = String.format(Locale.US, "%.2f", totalOdds),
                            color = PawaGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stake numeric Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mise ($currency)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = stakeStr,
                            onValueChange = { viewModel.setStake(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(130.dp)
                                .height(50.dp)
                                .testTag("stake_input_field"),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.End
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PawaGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Stake shortcuts selectors
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("100", "200", "500", "1000", "5000").forEach { quickValue ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .clickable { viewModel.setStake(quickValue) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$quickValue",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                    // Gains summaries
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gains potentiels", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "${rawPotentialWin.toInt()} $currency",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    if (bonusPercent > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bonus Win (+$bonusPercent%)", color = PawaGreen, fontSize = 11.sp)
                            Text("+${bonusAmount.toInt()} $currency", color = PawaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gain Payout total", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${grandTotalPayout.toInt()} $currency",
                            color = PawaGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (isSuccessMessage) PawaGreen else PawaOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    // Submission action button
                    Button(
                        onClick = {
                            viewModel.placeBet(
                                onSuccess = {
                                    statusMessage = "Pari validé avec succès !"
                                    isSuccessMessage = true
                                },
                                onError = { error ->
                                    statusMessage = error
                                    isSuccessMessage = false
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("place_bet_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PawaGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "PLACER LE PARI (${currentStake.toInt()} FCFA)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlipItemCard(
    selection: BetSelection,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PawaGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = selection.sport,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawaGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${selection.homeTeam} vs ${selection.awayTeam}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (selection.betType == "1X2") "Option" else "Buts"}: ",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = when(selection.selection) {
                            "1" -> "Victoire: ${selection.homeTeam}"
                            "2" -> "Victoire: ${selection.awayTeam}"
                            "X" -> "Match Nul"
                            else -> selection.selection
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = "@cote",
                        fontSize = 9.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", selection.odds),
                        color = PawaGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Supprimer la sélection",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


// ==========================================
// PLACED BETS SCREEN
// ==========================================
@Composable
fun PlacedBetsScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val placedBets by viewModel.placedBets.collectAsState()

    var selectedStatusTab by remember { mutableStateOf("Tous") }

    val filteredBets = remember(placedBets, selectedStatusTab) {
        when (selectedStatusTab) {
            "En cours" -> placedBets.filter { it.status == "PENDING" }
            "Terminés" -> placedBets.filter { it.status != "PENDING" }
            else -> placedBets
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Segment selector tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Tous", "En cours", "Terminés").forEach { tab ->
                val isSelected = selectedStatusTab == tab
                Button(
                    onClick = { selectedStatusTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PawaGreen else DarkSurface,
                        contentColor = if (isSelected) Color.Black else TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val count = when (tab) {
                        "Tous" -> placedBets.size
                        "En cours" -> placedBets.count { it.status == "PENDING" }
                        "Terminés" -> placedBets.count { it.status != "PENDING" }
                        else -> 0
                    }
                    Text(
                        text = "$tab ($count)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredBets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Aucun pari",
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucun pari enregistré dans cette catégorie.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredBets, key = { it.id }) { bet ->
                    PlacedBetListItem(bet = bet)
                }
            }
        }
    }
}

@Composable
fun PlacedBetListItem(bet: PlacedBetEntity) {
    var expanded by remember { mutableStateOf(false) }

    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(bet.timestamp) { formatter.format(Date(bet.timestamp)) }

    val selections = remember(bet.selectionsJson) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, BetSelection::class.java)
        val adapter = moshi.adapter<List<BetSelection>>(type)
        adapter.fromJson(bet.selectionsJson) ?: emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bet_card_${bet.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (bet.status == "WON") PawaGreen.copy(alpha = 0.5f) else DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            // Top block: Status Badge and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pari #${bet.id} • $formattedDate",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                val badgeBg = when (bet.status) {
                    "PENDING" -> DarkSurfaceElevated
                    "WON" -> PawaGreen.copy(alpha = 0.15f)
                    else -> Color.Red.copy(alpha = 0.15f)
                }
                val badgeColor = when (bet.status) {
                    "PENDING" -> TextPrimary
                    "WON" -> PawaGreen
                    else -> Color.Red
                }
                val badgeText = when (bet.status) {
                    "PENDING" -> "En cours"
                    "WON" -> "GAGNÉ"
                    else -> "Perdu"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mise: ${bet.stake.toInt()} FCFA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selections.size} sélections • Cote: ${String.format(Locale.US, "%.2f", bet.totalOdds)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val label = if (bet.status == "WON") "Gains payés" else "Payout potentiel"
                    Text(
                        text = label,
                        color = if (bet.status == "WON") PawaGreen else TextTertiary,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${bet.totalPayout.toInt()} FCFA",
                        color = if (bet.status == "WON") PawaGreen else PawaGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (bet.winBonusPercent > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = null, tint = PawaGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bonus Combiné inclus: +${bet.winBonusPercent}% (+${bet.bonusAmount.toInt()} FCFA)",
                        color = PawaGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Expanding sub selections list
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)
                    selections.forEach { sel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${sel.homeTeam} - ${sel.awayTeam}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${sel.sport} • Choix: ",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = sel.selection,
                                        color = PawaGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "@ ${String.format(Locale.US, "%.2f", sel.odds)}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val legStatusColor = when (sel.status) {
                                    "WON" -> PawaGreen
                                    "LOST" -> Color.Red
                                    else -> TextSecondary
                                }
                                val scoreTxt = if (sel.currentMatchScore.isNotEmpty()) " (Score: ${sel.currentMatchScore})" else ""
                                Text(
                                    text = when(sel.status) {
                                        "WON" -> "Validé$scoreTxt"
                                        "LOST" -> "Échoué$scoreTxt"
                                        else -> "En cours$scoreTxt"
                                    },
                                    color = legStatusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextTertiary
                )
            }
        }
    }
}


// ==========================================
// WALLET / ACCOUNT MANAGEMENT SCREEN
// ==========================================
@Composable
fun WalletScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.wallet.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()

    var depositAmountText by remember { mutableStateOf("") }
    var withdrawAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Wave") }

    var localError by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }

    var showFlutterwaveCheckout by remember { mutableStateOf(false) }
    var flutterwaveAmount by remember { mutableStateOf(0.0) }

    val formattedBalance = wallet?.balance?.toInt() ?: 10000

    // Fetch dynamic Flutterwave key from BuildConfig (auto-generated by Secrets plugin)
    val flutterwavePublicKey = BuildConfig.FLUTTERWAVE_PUBLIC_KEY

    // Trigger Flutterwave payment modal
    if (showFlutterwaveCheckout) {
        FlutterwaveCheckoutDialog(
            amount = flutterwaveAmount,
            pubKey = flutterwavePublicKey,
            onSuccess = { amt ->
                viewModel.deposit(amt)
                localSuccess = "Félicitations ! Recharge via Flutterwave de ${amt.toInt()} FCFA validée en direct."
                localError = null
            },
            onDismiss = {
                showFlutterwaveCheckout = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // User Session info header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, PawaGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PawaGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PawaGreen)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Session Active", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(if (isLoggedIn) currentUserEmail else "Utilisateur Invité", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (isLoggedIn) {
                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp).testTag("logout_btn")
                        ) {
                            Text("Déconnecter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Balance Summary Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("balance_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PawaGreen),
                border = BorderStroke(1.dp, PawaGold)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SOLDE DE VOTRE COMPTE",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$formattedBalance FCFA",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Smartphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paiements Flutterwave", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PawaGold.copy(alpha = 0.25f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PawaGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Secured Transactions", color = PawaGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Error & Success Feedbacks
        if (localError != null || localSuccess != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (localError != null) Color.Red.copy(alpha = 0.12f) else PawaGreen.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            if (localError != null) Color.Red.copy(alpha = 0.8f) else PawaGreen.copy(alpha = 0.8f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                        .testTag("wallet_feedback_banner")
                ) {
                    Text(
                        text = localError ?: localSuccess ?: "",
                        color = if (localError != null) Color.Red else PawaGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // NEW FLUTTERWAVE RECHARGE CARD INTERACTIVE
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("flutterwave_deposit_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recharge via Flutterwave®",
                            color = PawaGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        ImageVectorIcon(imageVector = Icons.Default.OfflineBolt, tint = PawaGold, size = 18.dp)
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Intégrez notre passerelle de test Flutterwave par défaut pour créditer instantanément vos gains de paris.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // API Key Display Notice showing actual key
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBg)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Clef Publique : ${flutterwavePublicKey.take(24)}...",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode pick row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("M-Money", "Wave", "Carte Bancaire").forEach { method ->
                            val isSelected = paymentMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PawaLightGreen else Color.Transparent)
                                    .border(BorderStroke(1.dp, if (isSelected) PawaGreen else DarkBorder), RoundedCornerShape(8.dp))
                                    .clickable { paymentMethod = method }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    color = if (isSelected) PawaGreen else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick-preset amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(500, 1000, 5000, 10000).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PawaLightGreen.copy(alpha = 0.5f))
                                    .border(BorderStroke(1.dp, PawaGreen.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                                    .clickable { depositAmountText = amt.toString() }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${amt}F",
                                    color = PawaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = depositAmountText,
                            onValueChange = { depositAmountText = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Ex: 5000 FCFA", color = TextTertiary, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("payment_amount_input"),
                            textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PawaGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = depositAmountText.toDoubleOrNull()
                                if (amt == null || amt <= 0.0) {
                                    localError = "Montant de recharge invalide."
                                    localSuccess = null
                                } else {
                                    flutterwaveAmount = amt
                                    showFlutterwaveCheckout = true
                                    depositAmountText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PawaGreen, contentColor = Color.White),
                            modifier = Modifier.height(48.dp).testTag("flutterwave_submit_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RECHARGER", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }

        // Direct Withdrawal inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Retirer mes gains de paris",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Retrait direct vers vos portefeuilles Wave ou Mobile Money.", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = withdrawAmountText,
                            onValueChange = { withdrawAmountText = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Ex: 2000 FCFA", color = TextTertiary, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("withdraw_input"),
                            textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PawaOrange,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = withdrawAmountText.toDoubleOrNull()
                                if (amt == null || amt <= 0.0) {
                                    localError = "Montant de retrait invalide."
                                    localSuccess = null
                                } else if (amt > formattedBalance) {
                                    localError = "Fonds insuffisants."
                                    localSuccess = null
                                } else {
                                    viewModel.withdraw(
                                        amount = amt,
                                        onError = { error ->
                                            localError = error
                                            localSuccess = null
                                        }
                                    )
                                    withdrawAmountText = ""
                                    localError = null
                                    localSuccess = "Demande de retrait de ${amt.toInt()} FCFA enregistrée."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PawaOrange, contentColor = Color.White),
                            modifier = Modifier.height(48.dp).testTag("withdraw_submit_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RETIRER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // PUSH NOTIFICATIONS SETTINGS & DEMO TRIGGER
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("notifications_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Paramètres de Notifications Push",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Configurez la réception des alertes de buts, résultats de bulletins et offres exclusives.", color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    // Notification toggle item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBg)
                            .clickable { viewModel.toggleNotificationsSetting() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = PawaGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Autoriser Notifications Push", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = isNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotificationsSetting() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PawaGreen,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = DarkBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Demo Simulation Notice and Simulation Triggers
                    Text("Tester les flux de Notifications Directes :", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerDemoNotification("PROMOTION") },
                            colors = ButtonDefaults.buttonColors(containerColor = PawaLightGreen, contentColor = PawaGreen),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("trigger_notification_promo")
                        ) {
                            Text("Promo Offre", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.triggerDemoNotification("TEAM_NEWS") },
                            colors = ButtonDefaults.buttonColors(containerColor = PawaLightGreen, contentColor = PawaGreen),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("trigger_notification_news")
                        ) {
                            Text("Alerte Onze", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (notificationsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dernières Alertes (${notificationsList.size})", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Effacer tout",
                                color = TextTertiary,
                                fontSize = 10.sp,
                                modifier = Modifier.clickable { viewModel.clearNotifications() }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            notificationsList.take(3).forEach { notice ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, DarkBorder), RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(notice.title, color = PawaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (notice.type == "PROMOTION") PawaOrange else PawaGreen)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(notice.message, color = TextSecondary, fontSize = 10.sp, lineHeight = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Administrative simulator management controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Panneau de Contrôle & Test",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Utilisez ces options pour simuler les issues des matchs réels et tester la résolution et l'encaissement automatique de vos gains !",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Force finish all matches button
                    Button(
                        onClick = {
                            viewModel.forceCompleteAllMatches()
                            localSuccess = "Tous les matchs en cours sont passés en statut 'Terminé' !"
                            localError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("force_complete_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PawaLightGreen, contentColor = PawaGreen),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PawaGreen.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.SportsScore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Forcer la fin des matchs (Settle Bets)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Restore matches button
                    Button(
                        onClick = {
                            viewModel.resetMatches()
                            localSuccess = "Les matchs et scores ont été réinitialisés !"
                            localError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_matches_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBg, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Réinitialiser les Matchs", fontSize = 11.sp, fontWeight = FontWeight.Normal)
                    }
                }
            }
        }

        // DOWNLOAD THE OFFICIAL APPLICATIONS BANNER (Requested for Android & iPhone)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("app_downloads_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, PawaGreen.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Téléchargez l'application officielle",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Accédez à betPawa à tout moment avec une fluidité totale !",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Android Badge
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PawaGreen)
                                .clickable { localSuccess = "Téléchargement de betPawa.apk démarré !" }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Android (.APK)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // iOS Badge
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TextPrimary)
                                .clickable { localSuccess = "Redirection vers le Store iOS initié !" }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("iPhone (.IPA)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SPORTS NEWS AGGREGATION COMPONENTS
// ==========================================
@Composable
fun NewsFeedSection(viewModel: SportsViewModel) {
    val newsFeed by viewModel.newsFeed.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var newsSportFilter by remember { mutableStateOf("Tous") }

    val filteredNews = newsFeed.filter { item ->
        val matchesSport = if (newsSportFilter == "Tous") true else item.sport.equals(newsSportFilter, ignoreCase = true)
        val matchesSearch = if (searchQuery.trim().isEmpty()) true else {
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.summary.contains(searchQuery, ignoreCase = true) ||
            item.content.contains(searchQuery, ignoreCase = true) ||
            item.league.contains(searchQuery, ignoreCase = true) ||
            item.team.contains(searchQuery, ignoreCase = true)
        }
        matchesSport && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // High density news search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher par équipe, ligue, actu...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("news_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawaGreen,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Sport filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Tous", "Football", "Basketball", "Tennis").forEach { sport ->
                val isSelected = newsSportFilter == sport
                FilterChip(
                    selected = isSelected,
                    onClick = { newsSportFilter = sport },
                    label = { Text(sport, color = if (isSelected) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = PawaGreen,
                    ),
                    border = BorderStroke(1.dp, if (isSelected) PawaGreen else DarkBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredNews.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Aucun article trouvé pour cette recherche.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).testTag("news_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredNews) { item ->
                    NewsCard(item)
                }
            }
        }
    }
}

@Composable
fun NewsCard(item: NewsItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("news_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Source & TimeAgo header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PawaGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.source,
                            color = PawaGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.sport + " • " + item.league,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Il y a " + item.timeAgo,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Article Title
            Text(
                text = item.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Body text
            Text(
                text = if (expanded) item.content else item.summary,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Équipe : ${item.team}",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                Text(
                    text = if (expanded) "Réduire ▲" else "Lire la suite ▼",
                    color = PawaGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ==========================================
// FLUTTERWAVE CHECKOUT MODAL DIALOG
// ==========================================
@Composable
fun FlutterwaveCheckoutDialog(
    amount: Double,
    pubKey: String,
    onSuccess: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf("PAY_FORM") } // PAY_FORM -> LOADING -> SUCCESS
    
    LaunchedEffect(step) {
        if (step == "LOADING") {
            delay(1800)
            step = "SUCCESS"
        }
    }

    AlertDialog(
        onDismissRequest = { if (step != "LOADING") onDismiss() },
        confirmButton = {},
        dismissButton = {},
        containerColor = Color(0xFF091E42), // Flutterwave brand dark blue
        shape = RoundedCornerShape(16.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Flutterwave Header Brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "flutterwave",
                            color = Color(0xFFF5A623), // Flutterwave Gold
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "SECURE AGGREGATION CHECKOUT",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "SANDBOX",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                when (step) {
                    "PAY_FORM" -> {
                        Text(
                            text = "Recharge de Compte betPawa",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${amount.toInt()} FCFA",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Public Key: ${pubKey.take(15)}...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Simulated details
                        OutlinedTextField(
                            value = "0700000000",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Numéro mobile de facturation", color = Color.White.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedBorderColor = Color(0xFFF5A623),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { step = "LOADING" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("flutterwave_modal_pay_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "PAYER ${amount.toInt()} FCFA",
                                color = Color(0xFF091E42),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Green, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Secured by Flutterwave • SSL 256-bit", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                    }
                    "LOADING" -> {
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(color = Color(0xFFF5A623), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Traitement sécurisé de la transaction...",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Text(
                            "Veuillez patienter quelques instants.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    "SUCCESS" -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "PAIEMENT RÉUSSI !",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Votre portefeuille betPawa a été crédité de ${amount.toInt()} FCFA.",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                onSuccess(amount)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("flutterwave_modal_close_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retourner à betPawa", color = Color(0xFF091E42), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    )
}


// ==========================================
// HIGH-DENSITY LOGIN SCREEN WITH BIOMETRIC & PATTERN
// ==========================================
@Composable
fun LoginScreen(viewModel: SportsViewModel) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phonePrefix by remember { mutableStateOf("+225") } // Default Ivory Coast prefix

    // Authenticators views state
    var selectedTab by remember { mutableStateOf("CREDENTIALS") } // CREDENTIALS -> PATTERN -> FINGERPRINT
    var isFingerprintScanning by remember { mutableStateOf(false) }
    var fingerprintScanningStep by remember { mutableStateOf("TAP_SCAN") } // TAP_SCAN -> SCANNING -> CONFIRMED
    
    val drawPattern = remember { mutableStateListOf<Int>() }

    var localLoginError by remember { mutableStateOf<String?>(null) }
    var localLoginSuccess by remember { mutableStateOf<String?>(null) }

    val countries = listOf(
        "+225" to "CI 🇨🇮",
        "+221" to "SN 🇸🇳",
        "+229" to "BJ 🇧🇯",
        "+237" to "CM 🇨🇲"
    )

    LaunchedEffect(isFingerprintScanning) {
        if (isFingerprintScanning) {
            fingerprintScanningStep = "SCANNING"
            delay(1500)
            fingerprintScanningStep = "CONFIRMED"
            delay(800)
            viewModel.login("biometrie_pawa@client.bet")
            isFingerprintScanning = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Brand Header Image block
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PawaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = PawaGold, fontWeight = FontWeight.Black, fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("betPawa Paris Sportifs", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Authentification sécurisée de votre compte", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Sub Tab Options bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .padding(4.dp)
            ) {
                listOf(
                    "CREDENTIALS" to "Standard",
                    "PATTERN" to "Schéma Tactile",
                    "FINGERPRINT" to "Empreinte digitale"
                ).forEach { (mode, label) ->
                    val isSelected = selectedTab == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PawaGreen else Color.Transparent)
                            .clickable { selectedTab = mode }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    when (selectedTab) {
                        "CREDENTIALS" -> {
                            Text("Connexion via Mobile & Mot de passe", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Phone & Country Row
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                // Prefix Picker Chooser
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, DarkBorder), RoundedCornerShape(8.dp))
                                        .background(DarkBg)
                                        .clickable {
                                            // Circular switch prefix on tap!
                                            val currIdx = countries.indexOfFirst { it.first == phonePrefix }
                                            val nextIndex = (currIdx + 1) % countries.size
                                            phonePrefix = countries[nextIndex].first
                                        }
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = countries.find { it.first == phonePrefix }?.second ?: "Prefix",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it.filter { c -> c.isDigit() } },
                                    placeholder = { Text("Numéro de téléphone", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("login_phone_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PawaGreen,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedContainerColor = DarkBg,
                                        unfocusedContainerColor = DarkBg
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                placeholder = { Text("Mot de passe sécurisé", fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_password_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PawaGreen,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedContainerColor = DarkBg,
                                        unfocusedContainerColor = DarkBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    if (emailInput.trim().length < 5) {
                                        localLoginError = "Veuillez entrer un numéro de téléphone valide."
                                    } else {
                                        localLoginError = null
                                        viewModel.login("$phonePrefix$emailInput")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PawaGreen, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("login_submit_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("SE CONNECTER", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                            }
                        }

                        "PATTERN" -> {
                            Text("Déverrouillage par schéma tactile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Reliez au moins 4 points de la matrice ci-dessous :", color = TextSecondary, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Matrix grid 3x3 dots
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                for (row in 0 until 3) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                        for (col in 0 until 3) {
                                            val index = row * 3 + col
                                            val isCon = drawPattern.contains(index)
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isCon) PawaGold else DarkBg)
                                                    .border(BorderStroke(1.dp, if (isCon) PawaGreen else DarkBorder), CircleShape)
                                                    .clickable {
                                                        if (!drawPattern.contains(index)) {
                                                            drawPattern.add(index)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "${index + 1}", color = if (isCon) PawaGreen else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (drawPattern.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Points reliés : ${drawPattern.map { it + 1 }.joinToString(" ➔ ")}",
                                    color = PawaGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { drawPattern.clear() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBg, contentColor = TextPrimary),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Effacer", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        if (drawPattern.size >= 4) {
                                            viewModel.login("schema_pawa@client.bet")
                                        } else {
                                            localLoginError = "Schéma trop court (minimum 4 points reliés)."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PawaGreen),
                                    modifier = Modifier.weight(1.5f).height(40.dp).testTag("login_pattern_submit"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Déverrouiller", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "FINGERPRINT" -> {
                            Text("Déverrouillage par empreinte digitale", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Touchez le lecteur pour simuler la biométrie Android.", color = TextSecondary, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(28.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(if (fingerprintScanningStep == "CONFIRMED") Color.Green.copy(alpha = 0.15f) else PawaGreen.copy(alpha = 0.1f))
                                        .border(BorderStroke(2.dp, if (fingerprintScanningStep == "CONFIRMED") Color.Green else PawaGreen), CircleShape)
                                        .clickable { isFingerprintScanning = true }
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Scanner",
                                        tint = if (fingerprintScanningStep == "CONFIRMED") Color.Green else PawaGreen,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = when (fingerprintScanningStep) {
                                        "SCANNING" -> "Scanning biométrique en cours..."
                                        "CONFIRMED" -> "Empreinte validée !"
                                        else -> "Appuyez sur l'empreinte pour vous connecter"
                                    },
                                    color = if (fingerprintScanningStep == "CONFIRMED") Color.Green else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            if (localLoginError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(localLoginError!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // APK Store Downloads layout banner for pre-connected landing experiences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pas de compte ? Téléchargez l'App",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Android Download Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PawaGreen)
                                .clickable { localLoginSuccess = "betPawa.apk prêt à installer !" }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Android (.apk)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // iPhone Download Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TextPrimary)
                                .clickable { localLoginSuccess = "Redirection vers Safari l'App..." }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("iPhone (.ipa)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (localLoginSuccess != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(localLoginSuccess!!, color = PawaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ImageVectorIcon(imageVector: ImageVector, tint: Color, size: androidx.compose.ui.unit.Dp) {
    Icon(imageVector = imageVector, contentDescription = null, tint = tint, modifier = Modifier.size(size))
}

