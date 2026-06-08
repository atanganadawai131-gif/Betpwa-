package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SportsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SportsRepository(application)

    // Match stream
    val matches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallet stream
    val wallet: StateFlow<WalletEntity?> = repository.userWallet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Placed Bets stream
    val placedBets: StateFlow<List<PlacedBetEntity>> = repository.placedBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Jackpot tickets stream
    val jackpotTickets: StateFlow<List<JackpotTicketEntity>> = repository.jackpotTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Slip Selections (local view state)
    private val _slipSelections = MutableStateFlow<List<BetSelection>>(emptyList())
    val slipSelections: StateFlow<List<BetSelection>> = _slipSelections.asStateFlow()

    // Stake text (local view state)
    private val _stakeInput = MutableStateFlow("100")
    val stakeInput: StateFlow<String> = _stakeInput.asStateFlow()

    // Status messages / feedback to show to users
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Simulation job
    private var simulationJob: Job? = null
    private val _isSimulating = MutableStateFlow(true)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // App User Authentication Session
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    // Push Notifications Setting & Alerts Streams
    private val _isNotificationsEnabled = MutableStateFlow(true)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _activeLiveNotification = MutableStateFlow<NotificationItem?>(null)
    val activeLiveNotification: StateFlow<NotificationItem?> = _activeLiveNotification.asStateFlow()

    // Sports News Stream
    private val _newsFeed = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsFeed: StateFlow<List<NewsItem>> = _newsFeed.asStateFlow()

    init {
        seedNewsFeed()
        seedInitialNotifications()
        
        viewModelScope.launch {
            repository.initializeWalletIfNeeded()
            repository.seedMatchesIfNeeded()
            // Start automatic simulation of sports odds by default to present dynamic live updates!
            startSimulationTimer()
        }

        // Live notification agent: listens to matches and alerts when scores change!
        viewModelScope.launch {
            var lastMatchesState: List<MatchEntity>? = null
            matches.collect { currentMatches ->
                if (lastMatchesState != null && _isNotificationsEnabled.value) {
                    currentMatches.forEach { current ->
                        val previous = lastMatchesState?.find { it.id == current.id }
                        if (previous != null) {
                            if (current.homeScore > previous.homeScore) {
                                triggerLiveGoalNotification(current.homeTeam, current.homeScore, current.awayScore)
                            } else if (current.awayScore > previous.awayScore) {
                                triggerLiveGoalNotification(current.awayTeam, current.homeScore, current.awayScore)
                            }
                            if (current.status == "FINISHED" && previous.status == "LIVE") {
                                triggerLiveFinishedNotification(current)
                            }
                        }
                    }
                }
                lastMatchesState = currentMatches
            }
        }

        // Live beta alert: listens to placed bet updates and alerts immediately when outcomes transition!
        viewModelScope.launch {
            var lastBetsState: List<PlacedBetEntity>? = null
            placedBets.collect { currentBets ->
                if (lastBetsState != null && _isNotificationsEnabled.value) {
                    currentBets.forEach { current ->
                        val previous = lastBetsState?.find { it.id == current.id }
                        if (previous != null && current.status != previous.status) {
                            if (current.status == "WON" || current.status == "LOST") {
                                triggerBetResultNotification(current, current.status == "WON")
                            }
                        }
                    }
                }
                lastBetsState = currentBets
            }
        }
    }

    private fun seedNewsFeed() {
        _newsFeed.value = listOf(
            NewsItem(
                id = 1,
                title = "Arsenal - Chelsea : Arteta confiant avant le grand Derby de Londres à l'Emirates",
                summary = "Mikel Arteta se dit serein et prêt pour le grand choc londonien contre Chelsea. Il exhorte ses attaquants à être impitoyables devant le but.",
                content = "Le tacticien d'Arsenal, Mikel Arteta, s'est entretenu avec la presse sportive avant le derby palpitant. 'Nous abordons chaque rencontre à domicile avec l'envie absolue d'imposer notre jeu. Chelsea se renforce de match en match, mais notre concentration est maximale. Bukayo Saka et Martin Ødegaard sont affûtés', a-t-il affirmé.",
                source = "betPawa Nouvelles",
                sport = "Football",
                league = "Premier League",
                team = "Arsenal",
                timeAgo = "11 min"
            ),
            NewsItem(
                id = 2,
                title = "LeBron James marque 41 points décisifs et qualifie d'office les Lakers",
                summary = "Une performance exceptionnelle du King propulse les Lakers lors du match d'ouverture palpitant face aux Warriors.",
                content = "Face aux Golden State Warriors emmenés par Stephen Curry, LeBron James a livré une prestation magistrale à l'âge de 41 ans, totalisant 41 points, 9 rebonds et 11 offrandes décisives. 'La saison est marathonienne. Notre chimie d'équipe se solidifie', a confié LeBron avec un sourire déterminé après la qualification.",
                source = "Radio Foot Direct",
                sport = "Basketball",
                league = "NBA",
                team = "Lakers",
                timeAgo = "34 min"
            ),
            NewsItem(
                id = 3,
                title = "Roland Garros : Carlos Alcaraz détrône Djokovic au terme d’un duel légendaire",
                summary = "Le prodige de Murcie déjoue les pronostics et écarte Novak Djokovic de la demi-finale du Grand Chelem.",
                content = "Sur la terre battue parisienne, Carlos Alcaraz s'est adjugé une victoire monumentale face à Novak Djokovic (6-4, 3-6, 7-6, 7-5) après 4h12 de jeu de très haute intensité. Ce triomphe témoigne de l'avènement incontestable d'un nouveau leader mondial de la discipline.",
                source = "L'Équipe Sportive",
                sport = "Tennis",
                league = "Roland Garros",
                team = "Carlos Alcaraz",
                timeAgo = "1 h"
            ),
            NewsItem(
                id = 4,
                title = "Classique PSG - OM : Dispositif de sécurité historique autour du Parc des Princes",
                summary = "Le Ministère de l'Intérieur interdit fermement le déplacement des supporters olympiens à Paris.",
                content = "En prévision du Classique de la Ligue 1 opposant le PSG à l'Olympique de Marseille, d'immenses forces policières encadreront l'événement. Aucun supporter phocéen ne sera autorisé dans le périmètre du stade de la capitale pour prévenir tout affrontement entre groupes d'ultras.",
                source = "Actu Foot Nationale",
                sport = "Football",
                league = "Ligue 1",
                team = "PSG",
                timeAgo = "2 h"
            ),
            NewsItem(
                id = 5,
                title = "Exclusivité : Cole Palmer incertain à l'entraînement de Chelsea pour blessure légère",
                summary = "Le prodige anglais s'est blessé légèrement au mollet gauche lors de la dernière séance d'entraînement collective.",
                content = "Très mauvaise nouvelle pour Enzo Maresca : Cole Palmer a écourté son échauffement de mi-séance après une alerte musculaire. Bien que le diagnostic écarte une déchirure sévère, Chelsea pourrait envisager de le préserver sur le banc face à Arsenal pour minimiser les risques de rechute.",
                source = "betPawa Nouvelles",
                sport = "Football",
                league = "Premier League",
                team = "Chelsea",
                timeAgo = "4 h"
            )
        )
    }

    private fun seedInitialNotifications() {
        _notifications.value = listOf(
            NotificationItem(
                id = 201,
                title = "🎁 Bonus de Recharge Flutterwave !",
                message = "Rechargez votre portefeuille BetPawa via Flutterwave et obtenez automatiquement +10% de bonus sur vote compte !",
                type = "PROMOTION",
                timestamp = System.currentTimeMillis() - 900000
            ),
            NotificationItem(
                id = 202,
                title = "🔥 Jackpot Pawa6 de 1 000 000 FCFA",
                message = "Les matchs phares débutent bientôt. Prédisez les vainqueurs des 6 matchs pour seulement 50 FCFA !",
                type = "PROMOTION",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
    }

    // Dynamic Notifications Trigger Agents
    private fun triggerLiveGoalNotification(team: String, homeScore: Int, awayScore: Int) {
        val item = NotificationItem(
            id = (1000..9999).random(),
            title = "⚽ BUT EN DIRECT !",
            message = "Incroyable ! Un but vient d'être marqué par $team ! Le tableau affiche désormais $homeScore - $awayScore. Les cotes live s'ajustent !",
            type = "LIVE_EVENT"
        )
        _notifications.value = listOf(item) + _notifications.value
        _activeLiveNotification.value = item
    }

    private fun triggerLiveFinishedNotification(match: MatchEntity) {
        val item = NotificationItem(
            id = (1000..9999).random(),
            title = "🏁 Match Terminé !",
            message = "${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}. Tous les paris en cours ont été résolus !",
            type = "MATCH_START"
        )
        _notifications.value = listOf(item) + _notifications.value
        _activeLiveNotification.value = item
    }

    private fun triggerBetResultNotification(bet: PlacedBetEntity, won: Boolean) {
        val title = if (won) "🎉 PARI REMPORTÉ !" else "📉 Sifflet final : Pari non validé"
        val message = if (won) {
            "Félicitations ! Votre pari de ${bet.stake.toInt()} FCFA (cote globale ${bet.totalOdds}) est gagnant. ${bet.totalPayout.toInt()} FCFA ont été versés dans votre portefeuille !"
        } else {
            "Votre combiné de ${bet.stake.toInt()} FCFA n'a pas abouti sur cette journée. Ne baissez pas les bras, d'autres opportunités de folie vous attendent !"
        }
        val item = NotificationItem(
            id = (1000..9999).random(),
            title = title,
            message = message,
            type = "BET_SETTLED"
        )
        _notifications.value = listOf(item) + _notifications.value
        _activeLiveNotification.value = item
    }

    // Interactive Notifications Helpers
    fun triggerDemoNotification(type: String) {
        val item = when (type) {
            "PROMOTION" -> NotificationItem(
                id = (1000..9999).random(),
                title = "🚀 Promo Flash de la Mi-Temps !",
                message = "Placez un pari de 200 FCFA minimum sur de la NBA ou du Tennis live aujourd'hui et obtenez un Cashback instantané de 100 FCFA !",
                type = "PROMOTION"
            )
            "TEAM_NEWS" -> NotificationItem(
                id = (1000..9999).random(),
                title = "⚠️ Alerte Equipe : Gabriel Jesus titulaire",
                message = "Le 11 de départ d'Arsenal est publié : Gabriel Jesus remplacera Havertz en pointe historique face à Chelsea.",
                type = "TEAM_NEWS"
            )
            else -> NotificationItem(
                id = (1000..9999).random(),
                title = "🔔 Rappel important du Match",
                message = "Le choc Real Madrid vs Barcelone débute dans exactement 20 minutes. Verrouillez vos pronostics !",
                type = "MATCH_START"
            )
        }
        _notifications.value = listOf(item) + _notifications.value
        _activeLiveNotification.value = item
    }

    // Login Form State Operations
    fun login(email: String) {
        _currentUserEmail.value = email
        _isLoggedIn.value = true
        _statusMessage.value = "Connexion réussie ! Heureux de vous revoir sur betPawa."
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = ""
        _statusMessage.value = "Déconnexion effectuée avec succès."
    }

    fun toggleNotificationsSetting() {
        val nextMode = !_isNotificationsEnabled.value
        _isNotificationsEnabled.value = nextMode
        _statusMessage.value = if (nextMode) "Push notifications activées !" else "Push notifications désactivées."
    }

    fun dismissActiveNotification() {
        _activeLiveNotification.value = null
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
        _statusMessage.value = "Historique des notifications effacé."
    }


    fun setStake(stake: String) {
        _stakeInput.value = stake.filter { it.isDigit() }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Toggle selected odds
    fun selectOdd(match: MatchEntity, betType: String, selectionName: String, odds: Double) {
        val currentList = _slipSelections.value.toMutableList()
        val index = currentList.indexOfFirst { it.matchId == match.id }

        if (index != -1) {
            val existing = currentList[index]
            if (existing.betType == betType && existing.selection == selectionName) {
                // Clicking same odd twice removes it
                currentList.removeAt(index)
            } else {
                // Replacing selection for this match with new choice (max 1 selection per match)
                currentList[index] = BetSelection(
                    matchId = match.id,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    sport = match.sport,
                    betType = betType,
                    selection = selectionName,
                    odds = odds,
                    currentMatchScore = "${match.homeScore}-${match.awayScore}"
                )
            }
        } else {
            // Add new selection
            currentList.add(
                BetSelection(
                    matchId = match.id,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    sport = match.sport,
                    betType = betType,
                    selection = selectionName,
                    odds = odds,
                    currentMatchScore = "${match.homeScore}-${match.awayScore}"
                )
            )
        }
        _slipSelections.value = currentList
    }

    fun removeSelection(selection: BetSelection) {
        _slipSelections.value = _slipSelections.value.filterNot { 
            it.matchId == selection.matchId && it.betType == selection.betType && it.selection == selection.selection 
        }
    }

    fun clearSlip() {
        _slipSelections.value = emptyList()
    }

    // Place bet based on current slip
    fun placeBet(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val selections = _slipSelections.value
        if (selections.isEmpty()) {
            onError("Le panier est vide.")
            return
        }

        val stakeVal = _stakeInput.value.toDoubleOrNull()
        if (stakeVal == null || stakeVal <= 0.0) {
            onError("Mise invalide.")
            return
        }

        viewModelScope.launch {
            val result = repository.placeBet(selections, stakeVal)
            result.onSuccess {
                _slipSelections.value = emptyList()
                _statusMessage.value = "Pari placé avec succès de ${stakeVal.toInt()} FCFA !"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Impossible de placer le pari.")
            }
        }
    }

    // Interactive simulate actions
    fun toggleSimulation() {
        if (_isSimulating.value) {
            simulationJob?.cancel()
            _isSimulating.value = false
            _statusMessage.value = "Simulation en pause."
        } else {
            startSimulationTimer()
            _statusMessage.value = "Simulation des scores active."
        }
    }

    private fun startSimulationTimer() {
        simulationJob?.cancel()
        _isSimulating.value = true
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(6000) // update every 6 seconds simulating a quick gaming pace
                repository.simulateLiveMatchesStep()
            }
        }
    }

    fun forceCompleteAllMatches() {
        viewModelScope.launch {
            repository.forceCompleteAllMatches()
            _statusMessage.value = "Tous les matchs en cours sont terminés ! Les paris en cours sont réglés."
        }
    }

    fun resetMatches() {
        viewModelScope.launch {
            repository.resetAllMatches()
            _statusMessage.value = "Matchs réinitialisés à leur état initial."
        }
    }

    // Wallet operations
    fun deposit(amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.depositFunds(amount)
            _statusMessage.value = "Dépôt mobile money de ${amount.toInt()} FCFA effectué !"
        }
    }

    fun withdraw(amount: Double, onError: (String) -> Unit) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val success = repository.withdrawFunds(amount)
            if (success) {
                _statusMessage.value = "Retrait Wave/Mobile money de ${amount.toInt()} FCFA effectué !"
            } else {
                onError("Solde insuffisant pour ce retrait.")
            }
        }
    }

    // Win Bonus calculation helper
    fun getWinBonusPercent(legCount: Int): Int {
        return when {
            legCount >= 30 -> 500
            legCount >= 20 -> 100
            legCount >= 15 -> 60
            legCount >= 10 -> 35
            legCount >= 5 -> 10
            legCount >= 3 -> 3
            else -> 0
        }
    }

    // Submit Jackpot Predictions
    fun submitJackpot(predictions: Map<Int, String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (predictions.isEmpty()) {
            onError("Veuillez sélectionner au moins un pronostic.")
            return
        }
        viewModelScope.launch {
            val result = repository.submitJackpotTicket(predictions)
            result.onSuccess {
                _statusMessage.value = "Ticket Jackpot enregistré avec succès !"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Impossible d'enregistrer le ticket.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
