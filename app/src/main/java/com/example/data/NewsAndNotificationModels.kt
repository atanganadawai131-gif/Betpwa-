package com.example.data

data class NewsItem(
    val id: Int,
    val title: String,
    val summary: String,
    val content: String,
    val source: String,       // e.g. "betPawa Nouvelles", "L'Équipe", "Radio Foot"
    val sport: String,        // "Football", "Basketball", "Tennis"
    val league: String,       // e.g. "Premier League", "NBA", "Roland Garros"
    val team: String,         // e.g. "Arsenal", "Lakers", "Chelsea"
    val timeAgo: String
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val type: String,         // "BET_SETTLED", "PROMOTION", "MATCH_START", "LIVE_EVENT"
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
)
