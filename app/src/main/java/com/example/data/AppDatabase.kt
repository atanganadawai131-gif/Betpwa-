package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromSelectionsList(value: List<BetSelection>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, BetSelection::class.java)
        val adapter = moshi.adapter<List<BetSelection>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toSelectionsList(value: String?): List<BetSelection>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, BetSelection::class.java)
        val adapter = moshi.adapter<List<BetSelection>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(value)
    }
}

@Database(
    entities = [
        MatchEntity::class,
        WalletEntity::class,
        PlacedBetEntity::class,
        JackpotTicketEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun walletDao(): WalletDao
    abstract fun placedBetDao(): PlacedBetDao
    abstract fun jackpotTicketDao(): JackpotTicketDao
}
