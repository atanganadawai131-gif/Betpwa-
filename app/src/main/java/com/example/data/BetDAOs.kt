package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY startTime ASC")
    fun getAllMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches")
    suspend fun getAllMatches(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: Int): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches")
    suspend fun clearAllMatches()
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = 1")
    fun getWalletFlow(): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet WHERE id = 1")
    suspend fun getWallet(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)
}

@Dao
interface PlacedBetDao {
    @Query("SELECT * FROM placed_bets ORDER BY timestamp DESC")
    fun getAllPlacedBetsFlow(): Flow<List<PlacedBetEntity>>

    @Query("SELECT * FROM placed_bets")
    suspend fun getAllPlacedBets(): List<PlacedBetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedBet(bet: PlacedBetEntity)

    @Update
    suspend fun updatePlacedBet(bet: PlacedBetEntity)
}

@Dao
interface JackpotTicketDao {
    @Query("SELECT * FROM jackpot_tickets ORDER BY timestamp DESC")
    fun getAllTicketsFlow(): Flow<List<JackpotTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: JackpotTicketEntity)

    @Update
    suspend fun updateTicket(ticket: JackpotTicketEntity)
}
