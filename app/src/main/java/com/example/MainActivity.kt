package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.*
import com.example.data.*
import androidx.compose.ui.zIndex

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val viewModel: SportsViewModel = viewModel()
    val wallet by viewModel.wallet.collectAsState()
    val slipSelections by viewModel.slipSelections.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val activeNotice by viewModel.activeLiveNotification.collectAsState()

    var currentTab by remember { mutableStateOf("matchs") }

    val formattedBalance = wallet?.balance?.toInt() ?: 10000
    val slipCount = slipSelections.size

    Box(modifier = Modifier.fillMaxSize()) {
        // Slide-in heads up notification toast bar at the absolute top layer
        activeNotice?.let { notice ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp)
                    .zIndex(9999f) // Elevated over everything
            ) {
                HeadsUpNotification(
                    notification = notice,
                    onDismiss = { viewModel.dismissActiveNotification() }
                )
            }
        }

        if (!isLoggedIn) {
            // Secure connection gate with pattern and biometric inputs
            LoginScreen(viewModel)
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // betPawa Premium High Density Logo
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PawaGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "P",
                                            color = PawaGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "BETPAWA",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Paris Sportifs",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Wallet Balance display right in header - High Density Pawa Style
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(PawaDarkGreen)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(30.dp))
                                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        .testTag("top_balance_display")
                                ) {
                                    Text(
                                        text = "$formattedBalance F",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(PawaGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            color = PawaGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = PawaGreen,
                            titleContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .border(BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .testTag("main_nav_bar")
                    ) {
                        // Matches Tab
                        NavigationBarItem(
                            selected = currentTab == "matchs",
                            onClick = { currentTab = "matchs" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = "Matchs"
                                )
                            },
                            label = { Text("Matchs", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PawaGreen,
                                selectedTextColor = PawaGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = PawaLightGreen
                            ),
                            modifier = Modifier.testTag("nav_matchs")
                        )

                        // Slip Tab (Panier) with Badge!
                        NavigationBarItem(
                            selected = currentTab == "panier",
                            onClick = { currentTab = "panier" },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (slipCount > 0) {
                                            Badge(
                                                containerColor = PawaOrange,
                                                contentColor = Color.White
                                            ) {
                                                Text(text = "$slipCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Panier de Paris"
                                    )
                                }
                            },
                            label = { Text("Panier", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PawaGreen,
                                selectedTextColor = PawaGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = PawaLightGreen
                            ),
                            modifier = Modifier.testTag("nav_panier")
                        )

                        // Jackpot Tab
                        NavigationBarItem(
                            selected = currentTab == "jackpot",
                            onClick = { currentTab = "jackpot" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = "Jackpot"
                                )
                            },
                            label = { Text("Jackpot", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PawaGreen,
                                selectedTextColor = PawaGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = PawaLightGreen
                            ),
                            modifier = Modifier.testTag("nav_jackpot")
                        )

                        // Placed Bets Tab
                        NavigationBarItem(
                            selected = currentTab == "paris",
                            onClick = { currentTab = "paris" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Mes Paris"
                                )
                            },
                            label = { Text("Paris", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PawaGreen,
                                selectedTextColor = PawaGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = PawaLightGreen
                            ),
                            modifier = Modifier.testTag("nav_paris")
                        )

                        // Management / Wallet Tab
                        NavigationBarItem(
                            selected = currentTab == "compte",
                            onClick = { currentTab = "compte" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Mon Compte"
                                )
                            },
                            label = { Text("Compte", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PawaGreen,
                                selectedTextColor = PawaGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = PawaLightGreen
                            ),
                            modifier = Modifier.testTag("nav_compte")
                        )
                    }
                },
                containerColor = DarkBg
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        "matchs" -> MatchesScreen(viewModel)
                        "panier" -> SlipScreen(viewModel)
                        "jackpot" -> JackpotScreen(viewModel)
                        "paris" -> PlacedBetsScreen(viewModel)
                        "compte" -> WalletScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HeadsUpNotification(
    notification: NotificationItem,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .border(BorderStroke(2.dp, PawaGold), RoundedCornerShape(12.dp))
            .testTag("in_app_push_alert"),
        colors = CardDefaults.cardColors(containerColor = PawaOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "BET_SETTLED" -> Icons.Default.EmojiEvents
                        "PROMOTION" -> Icons.Default.Campaign
                        "LIVE_EVENT" -> Icons.Default.NotificationImportant
                        else -> Icons.Default.NotificationsActive
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
