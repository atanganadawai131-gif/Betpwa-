package com.example.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SportsRepository(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "betpawa_database"
    ).fallbackToDestructiveMigration().build()

    private val matchDao = db.matchDao()
    private val walletDao = db.walletDao()
    private val placedBetDao = db.placedBetDao()
    private val jackpotTicketDao = db.jackpotTicketDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val selectionsType = Types.newParameterizedType(List::class.java, BetSelection::class.java)

    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatchesFlow().flowOn(Dispatchers.IO)
    val userWallet: Flow<WalletEntity?> = walletDao.getWalletFlow().flowOn(Dispatchers.IO)
    val placedBets: Flow<List<PlacedBetEntity>> = placedBetDao.getAllPlacedBetsFlow().flowOn(Dispatchers.IO)
    val jackpotTickets: Flow<List<JackpotTicketEntity>> = jackpotTicketDao.getAllTicketsFlow().flowOn(Dispatchers.IO)

    // Ensure wallet exists
    suspend fun initializeWalletIfNeeded() = withContext(Dispatchers.IO) {
        val current = walletDao.getWallet()
        if (current == null) {
            walletDao.insertWallet(WalletEntity(id = 1, balance = 10000.0))
        }
    }

    // Seed matches if empty
    suspend fun seedMatchesIfNeeded() = withContext(Dispatchers.IO) {
        val list = matchDao.getAllMatches()
        if (list.isEmpty()) {
            val seeded = createDemoMatches()
            matchDao.insertMatches(seeded)
        }
    }

    private fun createDemoMatches(): List<MatchEntity> {
        val now = System.currentTimeMillis()
        val HOUR = 3600 * 1000L
        return listOf(
            MatchEntity(
                homeTeam = "Arsenal",
                awayTeam = "Chelsea",
                sport = "Football",
                league = "Premier League",
                homeOdds = 1.85,
                drawOdds = 3.60,
                awayOdds = 4.20,
                overOdds = 1.70,
                underOdds = 2.10,
                status = "LIVE",
                minute = 32,
                homeScore = 1,
                awayScore = 0,
                startTime = now - HOUR
            ),
            MatchEntity(
                homeTeam = "Real Madrid",
                awayTeam = "Barcelona",
                sport = "Football",
                league = "La Liga",
                homeOdds = 2.15,
                drawOdds = 3.80,
                awayOdds = 3.10,
                overOdds = 1.60,
                underOdds = 2.30,
                status = "UPCOMING",
                minute = 0,
                homeScore = 0,
                awayScore = 0,
                startTime = now + 2 * HOUR
            ),
            MatchEntity(
                homeTeam = "Bayern Munich",
                awayTeam = "Dortmund",
                sport = "Football",
                league = "Bundesliga",
                homeOdds = 1.50,
                drawOdds = 4.75,
                awayOdds = 5.80,
                overOdds = 1.45,
                underOdds = 2.70,
                status = "LIVE",
                minute = 54,
                homeScore = 2,
                awayScore = 2,
                startTime = now - HOUR
            ),
            MatchEntity(
                homeTeam = "PSG",
                awayTeam = "Marseille",
                sport = "Football",
                league = "Ligue 1",
                homeOdds = 1.40,
                drawOdds = 5.00,
                awayOdds = 7.50,
                overOdds = 1.55,
                underOdds = 2.45,
                status = "UPCOMING",
                minute = 0,
                homeScore = 0,
                awayScore = 0,
                startTime = now + 4 * HOUR
            ),
            MatchEntity(
                homeTeam = "Man United",
                awayTeam = "Man City",
                sport = "Football",
                league = "Premier League",
                homeOdds = 3.90,
                drawOdds = 3.70,
                awayOdds = 1.90,
                overOdds = 1.65,
                underOdds = 2.20,
                status = "UPCOMING",
                minute = 0,
                homeScore = 0,
                awayScore = 0,
                startTime = now + HOUR
            ),
            MatchEntity(
                homeTeam = "LA Lakers",
                awayTeam = "Golden State",
                sport = "Basketball",
                league = "NBA",
                homeOdds = 1.70,
                drawOdds = 13.00,
                awayOdds = 2.20,
                overOdds = 1.85,  // Over 215.5
                underOdds = 1.85, // Under 215.5
                status = "UPCOMING",
                minute = 0,
                homeScore = 0,
                awayScore = 0,
                startTime = now + 5 * HOUR
            ),
            MatchEntity(
                homeTeam = "Novak Djokovic",
                awayTeam = "Carlos Alcaraz",
                sport = "Tennis",
                league = "Roland Garros",
                homeOdds = 1.80,
                drawOdds = 50.00, // drawing in tennis is not possible, odds are dummy or unused
                awayOdds = 2.00,
                overOdds = 1.90,  // Over 3.5 Sets
                underOdds = 1.80, // Under 3.5 Sets
                status = "LIVE",
                minute = 12, // Game count or set indicator represented simply
                homeScore = 1, // Set score
                awayScore = 1,
                startTime = now - 2 * HOUR
            ),
            MatchEntity(
                homeTeam = "Liverpool",
                awayTeam = "Everton",
                sport = "Football",
                league = "Premier League",
                homeOdds = 1.35,
                drawOdds = 5.25,
                awayOdds = 8.50,
                overOdds = 1.50,
                underOdds = 2.50,
                status = "UPCOMING",
                minute = 0,
                homeScore = 0,
                awayScore = 0,
                startTime = now + 8 * HOUR
            ),
            MatchEntity(
                homeTeam = "Juventus",
                awayTeam = "AC Milan",
                sport = "Football",
                league = "Serie A",
                homeOdds = 2.30,
                drawOdds = 3.20,
                awayOdds = 3.30,
                overOdds = 2.05,
                underOdds = 1.75,
                status = "UPCOMING",
                minute = 0,
                homeScore = 0,
                awayScore = 0,
                startTime = now + 3 * HOUR
            )
        )
    }

    // Process a simulated time and score increment
    suspend fun simulateLiveMatchesStep() = withContext(Dispatchers.IO) {
        val matches = matchDao.getAllMatches()
        val updated = matches.map { match ->
            if (match.status == "LIVE") {
                val nextMin = match.minute + Random.nextInt(1, 4)
                if (nextMin >= 90) {
                    match.copy(minute = 90, status = "FINISHED")
                } else {
                    // Randomly add goals/points and fluctuate odds slightly!
                    val homeGoal = Random.nextInt(1, 100) > 94
                    val awayGoal = Random.nextInt(1, 100) > 95
                    val extraHome = if (homeGoal) 1 else 0
                    val extraAway = if (awayGoal) 1 else 0
                    
                    // Live odds swing based on time passing and goals scored
                    val deltaHome = if (homeGoal) -0.40 else if (awayGoal) 0.35 else (Random.nextDouble(-0.06, 0.05))
                    val deltaAway = if (awayGoal) -0.40 else if (homeGoal) 0.35 else (Random.nextDouble(-0.06, 0.05))
                    val deltaDraw = if (homeGoal || awayGoal) 0.20 else (Random.nextDouble(-0.04, 0.04))
                    
                    val fHome = Math.max(1.02, Math.min(25.0, Math.round((match.homeOdds + deltaHome) * 100.0) / 100.0))
                    val fAway = Math.max(1.02, Math.min(25.0, Math.round((match.awayOdds + deltaAway) * 100.0) / 100.0))
                    val fDraw = Math.max(1.02, Math.min(25.0, Math.round((match.drawOdds + deltaDraw) * 100.0) / 100.0))

                    match.copy(
                        minute = nextMin,
                        homeScore = match.homeScore + extraHome,
                        awayScore = match.awayScore + extraAway,
                        homeOdds = fHome,
                        awayOdds = fAway,
                        drawOdds = fDraw
                    )
                }
            } else if (match.status == "UPCOMING" && Random.nextInt(1, 100) > 97) {
                // Kick off upcoming match to LIVE!
                match.copy(status = "LIVE", minute = 1, homeScore = 0, awayScore = 0)
            } else {
                match
            }
        }
        matchDao.insertMatches(updated)
        evaluatePendingBets()
    }

    // Force finalize all current LIVE/UPCOMING matches for easy testing & immediate rewards
    suspend fun forceCompleteAllMatches() = withContext(Dispatchers.IO) {
        val matches = matchDao.getAllMatches()
        val updated = matches.map { match ->
            if (match.status != "FINISHED") {
                // If it was upcoming or live, generate realistic final score and complete it
                val homeS = if (match.sport == "Basketball") Random.nextInt(90, 115) else Random.nextInt(0, 4)
                val awayS = if (match.sport == "Basketball") Random.nextInt(90, 115) else Random.nextInt(0, 4)
                match.copy(
                    status = "FINISHED",
                    minute = 90,
                    homeScore = homeS,
                    awayScore = awayS
                )
            } else {
                match
            }
        }
        matchDao.insertMatches(updated)
        evaluatePendingBets()
    }

    // Reset simulator (restart matches with random starting minutes/upcoming states)
    suspend fun resetAllMatches() = withContext(Dispatchers.IO) {
        matchDao.clearAllMatches()
        val seeded = createDemoMatches()
        matchDao.insertMatches(seeded)
    }

    // Deposit funds
    suspend fun depositFunds(amount: Double) = withContext(Dispatchers.IO) {
        val currentWallet = walletDao.getWallet() ?: WalletEntity()
        walletDao.updateWallet(currentWallet.copy(balance = currentWallet.balance + amount))
    }

    // Withdraw funds
    suspend fun withdrawFunds(amount: Double): Boolean = withContext(Dispatchers.IO) {
        val currentWallet = walletDao.getWallet() ?: WalletEntity()
        if (currentWallet.balance >= amount) {
            walletDao.updateWallet(currentWallet.copy(balance = currentWallet.balance - amount))
            true
        } else {
            false
        }
    }

    // Place a Slip Bet
    suspend fun placeBet(selections: List<BetSelection>, stake: Double): Result<PlacedBetEntity> = withContext(Dispatchers.IO) {
        val wallet = walletDao.getWallet() ?: return@withContext Result.failure(Exception("Portefeuille introuvable."))
        if (wallet.balance < stake) {
            return@withContext Result.failure(Exception("Solde insuffisant pour placer ce pari."))
        }

        if (selections.isEmpty()) {
            return@withContext Result.failure(Exception("Le panier est vide."))
        }

        // Subtract wallet balance
        walletDao.updateWallet(wallet.copy(balance = wallet.balance - stake))

        // Calculate odds
        val totalOdds = selections.fold(1.0) { acc, sel -> acc * sel.odds }
        val rawPotentialWin = stake * totalOdds

        // Win bonus calculations based on number of selections (Famous BetPawa feature!)
        // 3 legs = 3%, 5 legs = 10%, 10 legs = 35%, 15 legs = 60%, 20+ legs = 100%, 30+ legs = 500%
        val legCount = selections.size
        val bonusPercent = when {
            legCount >= 30 -> 500
            legCount >= 20 -> 100
            legCount >= 15 -> 60
            legCount >= 10 -> 35
            legCount >= 5 -> 10
            legCount >= 3 -> 3
            else -> 0
        }
        val bonusAmount = rawPotentialWin * (bonusPercent / 100.0)
        val totalPayout = rawPotentialWin + bonusAmount

        val listAdapter = moshi.adapter<List<BetSelection>>(selectionsType)
        val jsonPayload = listAdapter.toJson(selections)

        val newBet = PlacedBetEntity(
            stake = stake,
            totalOdds = Math.round(totalOdds * 100.0) / 100.0,
            potentialWin = Math.round(rawPotentialWin * 100.0) / 100.0,
            winBonusPercent = bonusPercent,
            bonusAmount = Math.round(bonusAmount * 100.0) / 100.0,
            totalPayout = Math.round(totalPayout * 100.0) / 100.0,
            status = "PENDING",
            selectionsJson = jsonPayload
        )

        placedBetDao.insertPlacedBet(newBet)
        Result.success(newBet)
    }

    // Submit a Jackpot ticket
    suspend fun submitJackpotTicket(predictions: Map<Int, String>): Result<JackpotTicketEntity> = withContext(Dispatchers.IO) {
        val wallet = walletDao.getWallet() ?: return@withContext Result.failure(Exception("Portefeuille introuvable."))
        // Jackpot on BetPawa is extremely cheap (e.g., 50 FCFA)
        val ticketCost = 50.0
        if (wallet.balance < ticketCost) {
            return@withContext Result.failure(Exception("Solde insuffisant pour participer au Jackpot (Requis: 50 FCFA)."))
        }

        // Deduct ticket cost
        walletDao.updateWallet(wallet.copy(balance = wallet.balance - ticketCost))

        // Serialize predictions dictionary
        // Map of MatchId to Outcome prediction ("1", "X", or "2")
        val mapType = Types.newParameterizedType(Map::class.java, Integer::class.java, String::class.java)
        val adapter = moshi.adapter<Map<Int, String>>(mapType)
        val jsonString = adapter.toJson(predictions)

        val newTicket = JackpotTicketEntity(
            predictionsJson = jsonString,
            status = "PENDING",
            correctPredictions = 0,
            totalMatches = predictions.size
        )

        jackpotTicketDao.insertTicket(newTicket)
        Result.success(newTicket)
    }

    // Periodically evaluate pending bets against matches
    suspend fun evaluatePendingBets() = withContext(Dispatchers.IO) {
        val pendingBets = placedBetDao.getAllPlacedBets().filter { it.status == "PENDING" }
        if (pendingBets.isEmpty()) return@withContext

        val listAdapter = moshi.adapter<List<BetSelection>>(selectionsType)

        for (bet in pendingBets) {
            val selections = listAdapter.fromJson(bet.selectionsJson) ?: continue
            var allFinished = true
            var overallStatus = "WON" // Assume won until proven lost/pending

            val updatedSelections = selections.map { sel ->
                val match = matchDao.getMatchById(sel.matchId)
                if (match == null) {
                    sel // shouldn't happen, fallback unchanged
                } else if (match.status != "FINISHED") {
                    allFinished = false
                    sel.copy(status = "PENDING", currentMatchScore = "${match.homeScore}-${match.awayScore}")
                } else {
                    // Match has finished! Compute outcome of the leg
                    val isWon = evaluateSelectionOutcome(sel, match.homeScore, match.awayScore)
                    val legScore = "${match.homeScore}-${match.awayScore}"
                    val legStatus = if (isWon) "WON" else "LOST"
                    if (!isWon) {
                        overallStatus = "LOST"
                    }
                    sel.copy(status = legStatus, currentMatchScore = legScore)
                }
            }

            if (!allFinished) {
                // At least one match is still playing or upcoming
                // Update selection snapshots anyway, but keep bet PENDING
                val newJson = listAdapter.toJson(updatedSelections)
                placedBetDao.updatePlacedBet(bet.copy(selectionsJson = newJson))
            } else {
                // All matches evaluated
                val finalStatus = if (overallStatus == "WON") "WON" else "LOST"
                val newJson = listAdapter.toJson(updatedSelections)
                val finishedBet = bet.copy(status = finalStatus, selectionsJson = newJson)
                placedBetDao.updatePlacedBet(finishedBet)

                // If won, credit wallet
                if (finalStatus == "WON") {
                    val wallet = walletDao.getWallet() ?: WalletEntity()
                    walletDao.updateWallet(wallet.copy(balance = wallet.balance + bet.totalPayout))
                }
            }
        }

        // Also evaluate Jackpot tickets
        evaluateJackpotTickets()
    }

    // Helper logic to evaluate matches
    private fun evaluateSelectionOutcome(sel: BetSelection, homeScore: Int, awayScore: Int): Boolean {
        return when (sel.betType) {
            "1X2" -> {
                when (sel.selection) {
                    "1" -> homeScore > awayScore
                    "X" -> homeScore == awayScore
                    "2" -> homeScore < awayScore
                    else -> false
                }
            }
            "OverUnder" -> {
                val totalGoals = homeScore + awayScore
                when (sel.selection) {
                    "Over" -> totalGoals > 2.5
                    "Under" -> totalGoals < 2.5
                    else -> false
                }
            }
            else -> false
        }
    }

    // Evaluate Jackpot predictions
    private suspend fun evaluateJackpotTickets() {
        // Fetch all pending tickets
        val tickets = jackpotTicketDao.getAllTicketsFlow().firstOrNull() ?: return
        val pendingTickets = tickets.filter { it.status == "PENDING" }
        if (pendingTickets.isEmpty()) return

        val mapType = Types.newParameterizedType(Map::class.java, Integer::class.java, String::class.java)
        val mapAdapter = moshi.adapter<Map<Int, String>>(mapType)

        for (ticket in pendingTickets) {
            val predictions = mapAdapter.fromJson(ticket.predictionsJson) ?: continue
            var allMatchesFinished = true
            var correctCount = 0

            for ((matchId, selection) in predictions) {
                val match = matchDao.getMatchById(matchId)
                if (match == null || match.status != "FINISHED") {
                    allMatchesFinished = false
                    break
                } else {
                    // Match has finished. Evaluate prediction
                    val actualOutcome = when {
                        match.homeScore > match.awayScore -> "1"
                        match.homeScore == match.awayScore -> "X"
                        else -> "2"
                    }
                    if (actualOutcome == selection) {
                        correctCount++
                    }
                }
            }

            if (allMatchesFinished) {
                // Let's decide if ticket is a winner: full marks (6/6 score wins top prize)
                // BetPawa Jackpot award scale:
                // 5/6 correct gets 1000 FCFA consolation
                // 6/6 correct gets 1,000,000 FCFA jackpot!
                val status = if (correctCount == ticket.totalMatches) "WON" else "LOST"
                val updatedTicket = ticket.copy(
                    status = status,
                    correctPredictions = correctCount
                )
                jackpotTicketDao.updateTicket(updatedTicket)

                // Standard credits reward
                val prize = when (correctCount) {
                    ticket.totalMatches -> 1000000.0
                    ticket.totalMatches - 1 -> 5000.0
                    else -> 0.0
                }
                if (prize > 0.0) {
                    val wallet = walletDao.getWallet() ?: WalletEntity()
                    walletDao.updateWallet(wallet.copy(balance = wallet.balance + prize))
                }
            }
        }
    }
}
