package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.asStateFlow
import com.example.BuildConfig
import com.example.notification.NotificationHelper
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity
import com.example.data.model.BrazilianHoliday
import com.example.data.model.DayStatus
import com.example.data.model.MonthSummary
import com.example.data.model.OfferItem
import com.example.data.model.ProductRequestItem
import com.example.data.model.QueryResult
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import com.example.data.repository.ScheduleRepository
import com.example.data.update.AppReleaseInfo
import com.example.data.update.AppUpdateManager
import com.example.data.update.DownloadState
import com.example.data.update.UpdateCheckState
import com.example.domain.BrazilianHolidayProvider
import com.example.domain.QuickQueryParser
import com.example.domain.ScheduleEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class ShiftUiState(
    val config: ScheduleConfigEntity = ScheduleConfigEntity(),
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val overrides: Map<LocalDate, DayOverrideEntity> = emptyMap(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val vacations: List<VacationPeriodEntity> = emptyList(),
    val holidaysForMonth: List<BrazilianHoliday> = emptyList(),
    val monthSummary: MonthSummary? = null,
    val todayStatus: DayStatus = DayStatus.WORK,
    val todayHoursLabel: String = "07:00 às 19:00",
    val nextOffDate: LocalDate = LocalDate.now().plusDays(1),
    val nextWeekendOff: Pair<LocalDate, LocalDate>? = null,
    val searchQuery: String = "",
    val queryResult: QueryResult? = null,
    val quickCheckDate: LocalDate = LocalDate.now().plusDays(7),
    val quickCheckResult: QueryResult? = null,
    val isFirstLaunch: Boolean = false,
    val isConfigLoaded: Boolean = false,
    val updateCheckState: UpdateCheckState = UpdateCheckState.Idle,
    val downloadState: DownloadState = DownloadState.Idle,
    val showUpdateDialog: Boolean = false,
    val activeRelease: AppReleaseInfo? = null,
    val isAutoCheckEnabled: Boolean = true,
    val updateCheckUrl: String = AppUpdateManager.DEFAULT_UPDATE_URL,
    val themeMode: AppThemeMode = AppThemeMode.NATURAL,
    val waterConsumedMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val waterReminderEnabled: Boolean = false,
    val isDeveloperMode: Boolean = false
)

class ShiftViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(AppDatabase.getDatabase(application))
    val updateManager = AppUpdateManager(application)

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _searchQuery = MutableStateFlow("")
    private val _queryResult = MutableStateFlow<QueryResult?>(null)
    private val _quickCheckDate = MutableStateFlow(LocalDate.now().plusDays(7))

    // Update system state flows
    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    private val _showUpdateDialog = MutableStateFlow(false)
    private val _activeRelease = MutableStateFlow<AppReleaseInfo?>(null)
    private val _isAutoCheckEnabled = MutableStateFlow(updateManager.isAutoCheckEnabled)
    private val _updateCheckUrl = MutableStateFlow(updateManager.updateCheckUrl)

    // Theme mode preference & state
    private val prefs = application.getSharedPreferences("escala_medina_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            val saved = prefs.getString("app_theme_mode", AppThemeMode.NATURAL.name) ?: AppThemeMode.NATURAL.name
            AppThemeMode.valueOf(saved)
        } catch (_: Exception) {
            AppThemeMode.NATURAL
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            AppThemeMode.DARK -> AppThemeMode.NATURAL
            AppThemeMode.NATURAL -> AppThemeMode.DARK
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        setThemeMode(next)
    }

    // Hydration tracking flows
    private val todayWaterKey = "water_consumed_${LocalDate.now()}"
    private val _waterGoalMl = MutableStateFlow(prefs.getInt("water_goal_ml", 2000))
    val waterGoalMl: StateFlow<Int> = _waterGoalMl.asStateFlow()

    private val _waterConsumedMl = MutableStateFlow(prefs.getInt(todayWaterKey, 0))
    val waterConsumedMl: StateFlow<Int> = _waterConsumedMl.asStateFlow()

    private val _waterReminderEnabled = MutableStateFlow(prefs.getBoolean("water_reminder_enabled", false))
    val waterReminderEnabled: StateFlow<Boolean> = _waterReminderEnabled.asStateFlow()

    // Developer / Administrative Mode
    private val isEmulatorEnv = Build.FINGERPRINT.startsWith("generic") ||
                              Build.MODEL.contains("google_sdk") ||
                              Build.HARDWARE.contains("goldfish") ||
                              Build.HARDWARE.contains("ranchu") ||
                              BuildConfig.DEBUG

    private val _isDeveloperMode = MutableStateFlow(
        prefs.getBoolean("developer_mode_unlocked", isEmulatorEnv)
    )
    val isDeveloperMode: StateFlow<Boolean> = _isDeveloperMode.asStateFlow()

    fun toggleDeveloperMode(): Boolean {
        val newVal = !_isDeveloperMode.value
        _isDeveloperMode.value = newVal
        prefs.edit().putBoolean("developer_mode_unlocked", newVal).apply()
        return newVal
    }

    private val _offersEnabled = MutableStateFlow(prefs.getBoolean("offers_enabled", true))
    val offersEnabled: StateFlow<Boolean> = _offersEnabled.asStateFlow()

    private val _selectedOfferCategories = MutableStateFlow(
        prefs.getStringSet("offer_categories", setOf("Eletrônicos", "Perfumes", "Casa", "Moda", "Utilidades", "Saúde & Bem-Estar")) ?: setOf("Eletrônicos", "Perfumes", "Casa", "Moda", "Utilidades", "Saúde & Bem-Estar")
    )
    val selectedOfferCategories: StateFlow<Set<String>> = _selectedOfferCategories.asStateFlow()

    private val _selectedOfferMarketplaces = MutableStateFlow(
        prefs.getStringSet("offer_marketplaces", setOf("Mercado Livre", "Shopee", "Amazon")) ?: setOf("Mercado Livre", "Shopee", "Amazon")
    )
    val selectedOfferMarketplaces: StateFlow<Set<String>> = _selectedOfferMarketplaces.asStateFlow()

    private val defaultOffers = emptyList<OfferItem>()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("user_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(prefs.getString("current_user_email", "alldav.am@gmail.com"))
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(prefs.getBoolean("admin_logged_in", false))
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminEmail = MutableStateFlow(prefs.getString("admin_email", "alldav.am@gmail.com"))
    val adminEmail: StateFlow<String?> = _adminEmail.asStateFlow()

    fun loginWithGoogle(email: String) {
        val trimmed = email.trim().lowercase()
        val isAdmin = (trimmed == "alldav.am@gmail.com" || trimmed.contains("alldav"))
        _isLoggedIn.value = true
        _currentUserEmail.value = email.trim()
        _isAdminLoggedIn.value = isAdmin
        if (isAdmin) {
            _isDeveloperMode.value = true
        }
        prefs.edit()
            .putBoolean("user_logged_in", true)
            .putString("current_user_email", email.trim())
            .putBoolean("admin_logged_in", isAdmin)
            .putBoolean("developer_mode_unlocked", isAdmin)
            .putString("admin_email", if (isAdmin) email.trim() else "alldav.am@gmail.com")
            .apply()
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = null
        _isAdminLoggedIn.value = false
        _isDeveloperMode.value = false
        prefs.edit()
            .putBoolean("user_logged_in", false)
            .remove("current_user_email")
            .putBoolean("admin_logged_in", false)
            .putBoolean("developer_mode_unlocked", false)
            .apply()
    }

    fun loginAsAdmin(email: String) {
        loginWithGoogle(email)
    }

    fun logoutAdmin() {
        logout()
    }

    private val _activeOffers = MutableStateFlow(loadStoredOffers())
    val activeOffers: StateFlow<List<OfferItem>> = _activeOffers.asStateFlow()

    private val _productRequests = MutableStateFlow(loadStoredRequests())
    val productRequests: StateFlow<List<ProductRequestItem>> = _productRequests.asStateFlow()

    fun toggleOffersEnabled(enabled: Boolean) {
        _offersEnabled.value = enabled
        prefs.edit().putBoolean("offers_enabled", enabled).apply()
    }

    fun updateOfferCategories(categories: Set<String>) {
        _selectedOfferCategories.value = categories
        prefs.edit().putStringSet("offer_categories", categories).apply()
    }

    fun updateOfferMarketplaces(marketplaces: Set<String>) {
        _selectedOfferMarketplaces.value = marketplaces
        prefs.edit().putStringSet("offer_marketplaces", marketplaces).apply()
    }

    fun postAdminOffer(title: String, description: String, category: String, marketplace: String, couponCode: String?, affiliateUrl: String, priceTag: String, discountBadge: String, imageUrl: String) {
        val newOffer = OfferItem(
            id = "off_${System.currentTimeMillis()}",
            title = title,
            description = description,
            category = category,
            marketplace = marketplace,
            couponCode = couponCode?.trim()?.ifBlank { null },
            affiliateUrl = affiliateUrl,
            priceTag = priceTag,
            discountBadge = discountBadge,
            imageUrl = imageUrl.trim()
        )
        val updated = listOf(newOffer) + _activeOffers.value
        _activeOffers.value = updated
        saveStoredOffers(updated)
    }

    fun deleteAdminOffer(id: String) {
        val updated = _activeOffers.value.filter { it.id != id }
        _activeOffers.value = updated
        saveStoredOffers(updated)
    }

    fun submitProductRequest(productName: String, category: String, userName: String) {
        if (productName.isBlank()) return
        val req = ProductRequestItem(
            id = "req_${System.currentTimeMillis()}",
            productName = productName.trim(),
            category = category,
            userName = userName.trim().ifBlank { "Plantonista" }
        )
        val updated = listOf(req) + _productRequests.value
        _productRequests.value = updated
        saveStoredRequests(updated)
    }

    private fun loadStoredOffers(): List<OfferItem> {
        val jsonStr = prefs.getString("saved_custom_offers", null)
        if (jsonStr.isNullOrBlank()) return defaultOffers
        return try {
            val arr = org.json.JSONArray(jsonStr)
            val list = mutableListOf<OfferItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OfferItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        description = obj.optString("description"),
                        category = obj.optString("category"),
                        marketplace = obj.optString("marketplace"),
                        couponCode = obj.optString("couponCode").takeIf { !it.isNullOrBlank() && it != "null" },
                        affiliateUrl = obj.optString("affiliateUrl"),
                        priceTag = obj.optString("priceTag"),
                        discountBadge = obj.optString("discountBadge"),
                        imageUrl = obj.optString("imageUrl", ""),
                        postedAt = obj.optLong("postedAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            defaultOffers
        }
    }

    private fun saveStoredOffers(list: List<OfferItem>) {
        try {
            val arr = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("description", item.description)
                    put("category", item.category)
                    put("marketplace", item.marketplace)
                    put("couponCode", item.couponCode ?: "")
                    put("affiliateUrl", item.affiliateUrl)
                    put("priceTag", item.priceTag)
                    put("discountBadge", item.discountBadge)
                    put("imageUrl", item.imageUrl)
                    put("postedAt", item.postedAt)
                }
                arr.put(obj)
            }
            prefs.edit().putString("saved_custom_offers", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadStoredRequests(): List<ProductRequestItem> {
        val jsonStr = prefs.getString("saved_product_requests", null)
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(jsonStr)
            val list = mutableListOf<ProductRequestItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ProductRequestItem(
                        id = obj.optString("id"),
                        productName = obj.optString("productName"),
                        category = obj.optString("category"),
                        userName = obj.optString("userName"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveStoredRequests(list: List<ProductRequestItem>) {
        try {
            val arr = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("productName", item.productName)
                    put("category", item.category)
                    put("userName", item.userName)
                    put("createdAt", item.createdAt)
                }
                arr.put(obj)
            }
            prefs.edit().putString("saved_product_requests", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addWater(ml: Int) {
        val todayKey = "water_consumed_${LocalDate.now()}"
        val updated = (_waterConsumedMl.value + ml).coerceAtLeast(0)
        _waterConsumedMl.value = updated
        prefs.edit().putInt(todayKey, updated).apply()
    }

    fun resetWater() {
        val todayKey = "water_consumed_${LocalDate.now()}"
        _waterConsumedMl.value = 0
        prefs.edit().putInt(todayKey, 0).apply()
    }

    fun setWaterGoal(goalMl: Int) {
        _waterGoalMl.value = goalMl
        prefs.edit().putInt("water_goal_ml", goalMl).apply()
    }

    fun toggleWaterReminder(enabled: Boolean) {
        _waterReminderEnabled.value = enabled
        prefs.edit().putBoolean("water_reminder_enabled", enabled).apply()
        val context = getApplication<Application>()
        if (enabled) {
            NotificationHelper.scheduleWaterReminder(context, 2)
        } else {
            NotificationHelper.cancelWaterReminder(context)
        }
    }

    fun sendTestWaterNotification() {
        NotificationHelper.sendWaterReminderNotification(
            context = getApplication(),
            currentMl = _waterConsumedMl.value,
            goalMl = _waterGoalMl.value
        )
    }

    val uiState: StateFlow<ShiftUiState> = combine(
        repository.scheduleConfigFlow,
        repository.allOverridesFlow,
        repository.allAppointmentsFlow,
        repository.allVacationsFlow,
        _currentMonth,
        _selectedDate,
        _updateCheckState,
        _downloadState,
        _showUpdateDialog,
        _activeRelease,
        _themeMode,
        _waterConsumedMl,
        _waterGoalMl,
        _waterReminderEnabled,
        _isDeveloperMode
    ) { args: Array<Any?> ->
        val config = (args[0] as? ScheduleConfigEntity) ?: ScheduleConfigEntity()
        @Suppress("UNCHECKED_CAST")
        val overridesList = (args[1] as? List<DayOverrideEntity>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val appointmentsList = (args[2] as? List<AppointmentEntity>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val vacationsList = (args[3] as? List<VacationPeriodEntity>) ?: emptyList()
        val month = (args[4] as? YearMonth) ?: YearMonth.now()
        val selectedDate = (args[5] as? LocalDate) ?: LocalDate.now()
        val updateCheck = (args[6] as? UpdateCheckState) ?: UpdateCheckState.Idle
        val download = (args[7] as? DownloadState) ?: DownloadState.Idle
        val showDialog = (args[8] as? Boolean) ?: false
        val activeRel = (args[9] as? AppReleaseInfo)
        val currentTheme = (args[10] as? AppThemeMode) ?: _themeMode.value
        val waterConsumed = (args[11] as? Int) ?: 0
        val waterGoal = (args[12] as? Int) ?: 2000
        val waterReminder = (args[13] as? Boolean) ?: false
        val devMode = (args[14] as? Boolean) ?: false

        val overridesMap = overridesList.associateBy { it.date }
        val today = LocalDate.now()

        val todayStatus = ScheduleEngine.calculateEffectiveStatus(today, config, overridesMap[today], vacationsList)
        val todayHours = ScheduleEngine.getShiftHours(today, config)
        val nextOff = ScheduleEngine.findNextOffDay(today, config, overridesMap, vacationsList)
        val nextWeekend = ScheduleEngine.findNextWeekendOff(today, config, overridesMap, vacationsList)
        val holidays = BrazilianHolidayProvider.getHolidaysForYear(month.year, config.selectedUf)
            .filter { it.date.month == month.month }

        val summary = repository.calculateMonthSummary(month, config, overridesMap, vacationsList)

        val quickCheckEval = QuickQueryParser.parseAndEvaluate(
            rawQuery = _quickCheckDate.value.toString(),
            currentDate = today,
            config = config,
            overrides = overridesMap,
            vacations = vacationsList
        )

        ShiftUiState(
            config = config,
            currentMonth = month,
            selectedDate = selectedDate,
            overrides = overridesMap,
            appointments = appointmentsList,
            vacations = vacationsList,
            holidaysForMonth = holidays,
            monthSummary = summary,
            todayStatus = todayStatus,
            todayHoursLabel = todayHours,
            nextOffDate = nextOff,
            nextWeekendOff = nextWeekend,
            searchQuery = _searchQuery.value,
            queryResult = _queryResult.value,
            quickCheckDate = _quickCheckDate.value,
            quickCheckResult = quickCheckEval,
            isFirstLaunch = !config.isConfigured,
            isConfigLoaded = true,
            updateCheckState = updateCheck,
            downloadState = download,
            showUpdateDialog = showDialog,
            activeRelease = activeRel,
            isAutoCheckEnabled = _isAutoCheckEnabled.value,
            updateCheckUrl = _updateCheckUrl.value,
            themeMode = currentTheme,
            waterConsumedMl = waterConsumed,
            waterGoalMl = waterGoal,
            waterReminderEnabled = waterReminder,
            isDeveloperMode = devMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShiftUiState()
    )

    init {
        viewModelScope.launch {
            repository.getOrCreateDefaultConfig()
            evaluateQuickCheck(_quickCheckDate.value)
            // If auto-check enabled on launch, check quietly on physical devices
            val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                             Build.MODEL.contains("google_sdk") ||
                             Build.HARDWARE.contains("goldfish") ||
                             Build.HARDWARE.contains("ranchu")
            if (updateManager.isAutoCheckEnabled && !isEmulator) {
                checkForUpdates(showDialogIfAvailable = true, silentOnUpToDate = true)
            }
        }
    }

    // --- Update Management Methods ---

    fun checkForUpdates(showDialogIfAvailable: Boolean = true, silentOnUpToDate: Boolean = false) {
        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckState.Checking
            val result = updateManager.checkForUpdates(_updateCheckUrl.value)
            _updateCheckState.value = result

            if (result is UpdateCheckState.Available) {
                _activeRelease.value = result.release
                if (showDialogIfAvailable) {
                    _showUpdateDialog.value = true
                }
                NotificationHelper.showUpdateNotification(
                    context = getApplication(),
                    versionName = result.release.versionName,
                    notes = result.release.releaseNotes.substringBefore("\n")
                )
            } else if (result is UpdateCheckState.UpToDate && !silentOnUpToDate) {
                // Kept as UpToDate state so UI displays it
            }
        }
    }

    fun simulateUpdateForTesting() {
        val simulated = updateManager.createSimulatedUpdate()
        _activeRelease.value = simulated
        _updateCheckState.value = UpdateCheckState.Available(simulated)
        _downloadState.value = DownloadState.Idle
        _showUpdateDialog.value = true
    }

    fun startDownloadingActiveRelease() {
        val release = _activeRelease.value ?: return
        viewModelScope.launch {
            updateManager.downloadApk(release) { state ->
                _downloadState.value = state
            }
        }
    }

    fun installDownloadedApk() {
        val state = _downloadState.value
        if (state is DownloadState.ReadyToInstall) {
            updateManager.installApk(state.apkUri)
        }
    }

    fun openBrowserForActiveRelease() {
        val release = _activeRelease.value ?: return
        if (release.downloadUrl.isNotBlank()) {
            updateManager.openInBrowser(release.downloadUrl)
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
        _updateCheckState.value = UpdateCheckState.Idle
        _downloadState.value = DownloadState.Idle
    }

    fun dismissUpdateBanner() {
        _updateCheckState.value = UpdateCheckState.Idle
    }

    fun setAutoCheckEnabled(enabled: Boolean) {
        updateManager.isAutoCheckEnabled = enabled
        _isAutoCheckEnabled.value = enabled
    }

    fun setCustomUpdateUrl(url: String) {
        val trimmed = url.trim().ifBlank { AppUpdateManager.DEFAULT_UPDATE_URL }
        updateManager.updateCheckUrl = trimmed
        _updateCheckUrl.value = trimmed
    }

    fun resetUpdateCheckState() {
        _updateCheckState.value = UpdateCheckState.Idle
    }

    // --- Calendar and Schedule Navigation Methods ---

    fun selectMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        if (_currentMonth.value.month != date.month || _currentMonth.value.year != date.year) {
            _currentMonth.value = YearMonth.from(date)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val config = uiState.value.config
        val overrides = uiState.value.overrides
        val vacations = uiState.value.vacations

        _queryResult.value = QuickQueryParser.parseAndEvaluate(
            rawQuery = query,
            currentDate = LocalDate.now(),
            config = config,
            overrides = overrides,
            vacations = vacations
        )
    }

    fun setQuickCheckDate(date: LocalDate) {
        _quickCheckDate.value = date
        evaluateQuickCheck(date)
    }

    private fun evaluateQuickCheck(date: LocalDate) {
        val config = uiState.value.config
        val overrides = uiState.value.overrides
        val vacations = uiState.value.vacations
        _quickCheckDate.value = date
    }

    fun saveScheduleConfig(
        userName: String,
        type: ScheduleType,
        refDate: LocalDate,
        refIsWork: Boolean,
        period: ShiftPeriod,
        startTime: String,
        endTime: String,
        uf: String,
        workCount: Int = 1,
        offCount: Int = 1,
        customPattern: String = "1,0",
        profession: String = "Geral",
        roleOrTitle: String = "",
        sector: String = ""
    ) {
        viewModelScope.launch {
            val updatedConfig = ScheduleConfigEntity(
                id = 1,
                userName = userName.trim().ifBlank { "Plantonista" },
                isConfigured = true,
                scheduleType = type,
                referenceDate = refDate,
                referenceIsWork = refIsWork,
                period = period,
                customStartTime = startTime,
                customEndTime = endTime,
                selectedUf = uf,
                workDaysCount = workCount,
                offDaysCount = offCount,
                customCyclePattern = customPattern,
                profession = profession.trim().ifBlank { "Geral" },
                roleOrTitle = roleOrTitle.trim(),
                sector = sector.trim()
            )
            repository.saveScheduleConfig(updatedConfig)
        }
    }

    fun updateProfessionProfile(profession: String, roleOrTitle: String, sector: String) {
        viewModelScope.launch {
            repository.updateProfessionProfile(profession, roleOrTitle, sector)
        }
    }

    fun saveVacationPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        title: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val vacation = VacationPeriodEntity(
                startDate = startDate,
                endDate = endDate,
                title = title.trim().ifBlank { "Férias" },
                notes = notes?.trim()?.ifBlank { null }
            )
            repository.insertVacation(vacation)
        }
    }

    fun deleteVacation(vacation: VacationPeriodEntity) {
        viewModelScope.launch {
            repository.deleteVacation(vacation)
        }
    }

    fun registerFt(date: LocalDate, valueOrLocation: String?, notes: String?) {
        viewModelScope.launch {
            val override = DayOverrideEntity(
                date = date,
                status = DayStatus.FT,
                partnerName = null,
                extraValueOrLocation = valueOrLocation,
                notes = notes
            )
            repository.saveDayOverride(override)
        }
    }

    fun registerSwap(
        date: LocalDate,
        targetStatus: DayStatus = DayStatus.SWAP,
        partnerName: String?,
        notes: String?,
        paybackDate: LocalDate? = null
    ) {
        viewModelScope.launch {
            val compensationLabel = if (paybackDate != null) {
                if (targetStatus == DayStatus.SWAP_WORK) {
                    "Devolução: ${paybackDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                } else {
                    "Pagar em: ${paybackDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                }
            } else null

            repository.saveSwapWithPayback(
                mainDate = date,
                mainStatus = targetStatus,
                partnerName = partnerName,
                extraValueOrLocation = compensationLabel,
                notes = notes,
                paybackDate = paybackDate
            )

            if (paybackDate != null) {
                NotificationHelper.scheduleSwapPaybackNotification(
                    context = getApplication(),
                    swapDate = date,
                    paybackDate = paybackDate,
                    partnerName = partnerName,
                    notes = notes
                )
            }
        }
    }

    fun setSwapPaybackCompleted(date: LocalDate, completed: Boolean) {
        viewModelScope.launch {
            repository.updateSwapPaybackStatus(date, completed)
            if (completed) {
                val override = repository.getOverrideForDate(date)
                override?.paybackDate?.let { paybackDate ->
                    NotificationHelper.cancelSwapPaybackNotification(getApplication(), paybackDate)
                }
            }
        }
    }

    fun removeOverride(date: LocalDate) {
        viewModelScope.launch {
            val override = repository.getOverrideForDate(date)
            val paybackDate = override?.paybackDate
            if (paybackDate != null) {
                NotificationHelper.cancelSwapPaybackNotification(getApplication(), paybackDate)
                val reciprocal = repository.getOverrideForDate(paybackDate)
                if (reciprocal != null && reciprocal.paybackDate == date) {
                    repository.removeDayOverride(paybackDate)
                }
            }
            repository.removeDayOverride(date)
        }
    }

    fun addAppointment(
        date: LocalDate,
        time: LocalTime?,
        title: String,
        notes: String?,
        reminderMinutes: Int,
        isShiftSwap: Boolean = false,
        swapPartnerName: String? = null
    ) {
        viewModelScope.launch {
            val appointment = AppointmentEntity(
                date = date,
                time = time,
                title = title,
                notes = notes,
                reminderMinutesBefore = reminderMinutes,
                isShiftSwap = isShiftSwap,
                swapPartnerName = swapPartnerName
            )
            val generatedId = repository.insertAppointment(appointment)
            NotificationHelper.scheduleAppointmentNotification(
                context = getApplication(),
                appointmentId = generatedId,
                title = title,
                notes = notes,
                date = date,
                time = time,
                reminderMinutesBefore = reminderMinutes,
                isShiftSwap = isShiftSwap,
                partnerName = swapPartnerName
            )
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
            NotificationHelper.cancelAppointmentNotification(getApplication(), appointment.id)
        }
    }

    fun sendTestNotification() {
        NotificationHelper.sendImmediateTestNotification(getApplication())
    }

    // Convenience aliases
    fun onSelectDate(date: LocalDate) = selectDate(date)

    fun onMonthChange(month: YearMonth) = selectMonth(month)

    fun runNaturalQuery(query: String) = updateSearchQuery(query)

    fun addVacation(startDate: LocalDate, endDate: LocalDate, title: String, notes: String?) =
        saveVacationPeriod(startDate, endDate, title, notes)

    fun saveCompleteOnboarding(
        userName: String,
        type: ScheduleType,
        refDate: LocalDate,
        refIsWork: Boolean,
        period: ShiftPeriod,
        startTime: String,
        endTime: String,
        uf: String,
        workCount: Int = 1,
        offCount: Int = 1,
        customPattern: String = "1,0",
        profession: String = "Geral",
        roleOrTitle: String = "",
        sector: String = ""
    ) = saveScheduleConfig(userName, type, refDate, refIsWork, period, startTime, endTime, uf, workCount, offCount, customPattern, profession, roleOrTitle, sector)

    fun skipOnboardingWithDefault() {
        saveScheduleConfig(
            userName = "Plantonista",
            type = ScheduleType.TWELVE_THIRTY_SIX,
            refDate = LocalDate.now(),
            refIsWork = true,
            period = ShiftPeriod.DAY,
            startTime = "07:00",
            endTime = "19:00",
            uf = "SP"
        )
    }
}
