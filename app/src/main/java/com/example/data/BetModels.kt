package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val homeTeam: String,
    val awayTeam: String,
    val sport: String,            // "Football", "Basketball", "Tennis"
    val league: String,           // e.g. "Champions League", "Premier League"
    val homeOdds: Double,         // 1
    val drawOdds: Double,         // X
    val awayOdds: Double,         // 2
    val overOdds: Double,         // Over 2.5
    val underOdds: Double,        // Under 2.5
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val minute: Int = 0,
    val status: String = "UPCOMING", // "UPCOMING", "LIVE", "FINISHED"
    val startTime: Long
)

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 10000.0 // Starting balance: 10,000 FCFA / KES
)

@Entity(tableName = "placed_bets")
data class PlacedBetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val stake: Double,
    val totalOdds: Double,
    val potentialWin: Double,
    val winBonusPercent: Int, // e.g. 10 for 10%
    val bonusAmount: Double,
    val totalPayout: Double,
    val status: String = "PENDING", // "PENDING", "WON", "LOST"
    val selectionsJson: String      // JSON representation of List<BetSelection>
)

@Entity(tableName = "jackpot_tickets")
data class JackpotTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val predictionsJson: String, // JSON list of predictions (e.g. score or outcome predictions)
    val status: String = "PENDING", // "PENDING", "WON", "LOST"
    val correctPredictions: Int = 0,
    val totalMatches: Int = 6,
    val jackpotName: String = "Pawa6"
)

// Plain Kotlin class for representing selections in memory and JSON
data class BetSelection(
    val matchId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val sport: String,
    val betType: String,       // "1X2" or "OverUnder"
    val selection: String,     // "1", "X", "2", "Over", "Under"
    val odds: Double,
    val status: String = "PENDING", // "PENDING", "WON", "LOST"
    val currentMatchScore: String = "0-0"
)
