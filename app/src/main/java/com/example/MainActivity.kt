package com.example

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.example.notification.NotificationHelper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.QueryBuilder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.AppThemeMode
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DayStatus
import com.example.domain.BrazilianHolidayProvider
import com.example.domain.ScheduleEngine
import com.example.ui.components.AssistantBottomSheet
import com.example.ui.components.DayDetailBottomSheet
import com.example.ui.dialogs.AppointmentDialog
import com.example.ui.dialogs.DayOverrideDialog
import com.example.ui.dialogs.OnboardingWizardDialog
import com.example.ui.dialogs.UpdateDialog
import com.example.ui.dialogs.VacationDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QuickCheckScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShiftViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AppScreen(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Início", Icons.Filled.Home, Icons.Outlined.Home),
    CALENDAR("Calendário", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    SIMULATOR("Simulador", Icons.Filled.QueryBuilder, Icons.Outlined.QueryBuilder),
    REPORTS("Relatório FT", Icons.Filled.Paid, Icons.Outlined.Paid),
    SETTINGS("Escalas", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ShiftViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannels(this)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = themeMode) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result handled */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!NotificationHelper.hasNotificationPermission(this@MainActivity)) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

                var showDayDetailSheet by remember { mutableStateOf(false) }
                var showAssistantSheet by remember { mutableStateOf(false) }
                var showAppointmentDialog by remember { mutableStateOf(false) }
                var showFtDialog by remember { mutableStateOf(false) }
                var showSwapDialog by remember { mutableStateOf(false) }
                var showVacationDialog by remember { mutableStateOf(false) }
                var showOnboardingWizard by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("escala_medina_prefs", Context.MODE_PRIVATE) }
                var wizardDismissedManually by remember {
                    mutableStateOf(prefs.getBoolean("wizard_dismissed", false))
                }

                var actionDate by remember { mutableStateOf(LocalDate.now()) }

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val assistantSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // Intercept back navigation when not on DASHBOARD
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    currentScreen = AppScreen.DASHBOARD
                }

                // If not yet configured, automatically ask questions on app launch only after config has finished loading
                val isWizardActive = (uiState.isConfigLoaded && !uiState.config.isConfigured && !wizardDismissedManually) || showOnboardingWizard

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_app_icon),
                                        contentDescription = "Logo Escala Medina",
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Escala Medina",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.toggleTheme() },
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = when (themeMode) {
                                            AppThemeMode.DARK -> Icons.Filled.LightMode
                                            AppThemeMode.NATURAL -> Icons.Filled.DarkMode
                                            AppThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                                        },
                                        contentDescription = "Alternar Tema: ${themeMode.label}",
                                        tint = when (themeMode) {
                                            AppThemeMode.DARK -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            AppScreen.values().forEach { screen ->
                                val selected = currentScreen == screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateToCalendar = { currentScreen = AppScreen.CALENDAR },
                                onNavigateToQuickCheck = { currentScreen = AppScreen.SIMULATOR },
                                onOpenAssistant = { showAssistantSheet = true },
                                onOpenVacationDialog = { showVacationDialog = true },
                                onOpenOnboardingWizard = { showOnboardingWizard = true },
                                onSelectDate = { date ->
                                    actionDate = date
                                    viewModel.onSelectDate(date)
                                    showDayDetailSheet = true
                                },
                                onNavigateToCalendarWithDate = { targetDate ->
                                    viewModel.selectDate(targetDate)
                                    currentScreen = AppScreen.CALENDAR
                                }
                            )
                            AppScreen.CALENDAR -> CalendarScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onOpenVacationDialog = { showVacationDialog = true },
                                onSelectDate = { date ->
                                    actionDate = date
                                    viewModel.onSelectDate(date)
                                    showDayDetailSheet = true
                                }
                            )
                            AppScreen.SIMULATOR -> QuickCheckScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onSelectDateForAction = { date ->
                                    actionDate = date
                                    viewModel.onSelectDate(date)
                                    showDayDetailSheet = true
                                }
                            )
                            AppScreen.REPORTS -> ReportsScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                            AppScreen.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onOpenWizard = { showOnboardingWizard = true },
                                onSaved = {
                                    currentScreen = AppScreen.CALENDAR
                                }
                            )
                        }

                        // Floating Assistant Bottom Sheet
                        if (showAssistantSheet) {
                            AssistantBottomSheet(
                                sheetState = assistantSheetState,
                                viewModel = viewModel,
                                uiState = uiState,
                                onDismiss = { showAssistantSheet = false },
                                onSelectDate = { date ->
                                    actionDate = date
                                    viewModel.onSelectDate(date)
                                    showDayDetailSheet = true
                                }
                            )
                        }

                        // Day Detail Bottom Sheet
                        if (showDayDetailSheet) {
                            val effectiveStatus = ScheduleEngine.calculateEffectiveStatus(
                                actionDate,
                                uiState.config,
                                uiState.overrides[actionDate],
                                uiState.vacations
                            )
                            val holiday = BrazilianHolidayProvider.getHolidayOn(actionDate, uiState.config.selectedUf)
                            val dayAppointments = uiState.appointments.filter { it.date == actionDate }
                            val override = uiState.overrides[actionDate]

                            DayDetailBottomSheet(
                                sheetState = sheetState,
                                date = actionDate,
                                status = effectiveStatus,
                                period = uiState.config.period,
                                holiday = holiday,
                                override = override,
                                appointments = dayAppointments,
                                onDismiss = { showDayDetailSheet = false },
                                onAddAppointment = {
                                    showAppointmentDialog = true
                                },
                                onMarkFt = {
                                    showFtDialog = true
                                },
                                onMarkSwap = {
                                    showSwapDialog = true
                                },
                                onRestoreScale = {
                                    viewModel.removeOverride(actionDate)
                                },
                                onDeleteAppointment = { appt ->
                                    viewModel.deleteAppointment(appt)
                                }
                            )
                        }

                        // Appointment Dialog
                        if (showAppointmentDialog) {
                            val effectiveStatus = ScheduleEngine.calculateEffectiveStatus(
                                actionDate,
                                uiState.config,
                                uiState.overrides[actionDate],
                                uiState.vacations
                            )
                            AppointmentDialog(
                                date = actionDate,
                                dayStatus = effectiveStatus,
                                onDismiss = { showAppointmentDialog = false },
                                onSave = { title, time, notes, reminderMinutes, isShiftSwap, partnerName ->
                                    viewModel.addAppointment(
                                        date = actionDate,
                                        time = time,
                                        title = title,
                                        notes = notes,
                                        reminderMinutes = reminderMinutes,
                                        isShiftSwap = isShiftSwap,
                                        swapPartnerName = partnerName
                                    )
                                    if (isShiftSwap) {
                                        viewModel.registerSwap(actionDate, partnerName, notes)
                                    }
                                    showAppointmentDialog = false
                                },
                                onRequestSwapFromCollision = {
                                    showAppointmentDialog = false
                                    showSwapDialog = true
                                }
                            )
                        }

                        // FT Dialog
                        if (showFtDialog) {
                            DayOverrideDialog(
                                date = actionDate,
                                targetStatus = DayStatus.FT,
                                config = uiState.config,
                                overrides = uiState.overrides,
                                vacations = uiState.vacations,
                                onDismiss = { showFtDialog = false },
                                onConfirm = { _, valueOrLocation, notes, _ ->
                                    viewModel.registerFt(actionDate, valueOrLocation, notes)
                                    showFtDialog = false
                                }
                            )
                        }

                        // Swap Dialog
                        if (showSwapDialog) {
                            DayOverrideDialog(
                                date = actionDate,
                                targetStatus = DayStatus.SWAP,
                                config = uiState.config,
                                overrides = uiState.overrides,
                                vacations = uiState.vacations,
                                onDismiss = { showSwapDialog = false },
                                onConfirm = { partner, valueOrLocation, notes, paybackDate ->
                                    val compensationNote = if (!valueOrLocation.isNullOrBlank()) "Compensar: $valueOrLocation. $notes" else notes
                                    viewModel.registerSwap(actionDate, partner, compensationNote, paybackDate)
                                    showSwapDialog = false
                                }
                            )
                        }

                        // Vacation Dialog (Full month or broken dates)
                        if (showVacationDialog) {
                            VacationDialog(
                                vacations = uiState.vacations,
                                onDismiss = { showVacationDialog = false },
                                onSaveVacation = { startDate, endDate, title, notes ->
                                    viewModel.addVacation(startDate, endDate, title, notes)
                                    showVacationDialog = false
                                },
                                onDeleteVacation = { vacation ->
                                    viewModel.deleteVacation(vacation)
                                }
                            )
                        }

                        // Onboarding / Reconfiguration Wizard Dialog
                        if (isWizardActive) {
                            OnboardingWizardDialog(
                                initialName = uiState.config.userName,
                                onDismiss = {
                                    wizardDismissedManually = true
                                    prefs.edit().putBoolean("wizard_dismissed", true).apply()
                                    showOnboardingWizard = false
                                },
                                onSkipWithDefault = {
                                    viewModel.skipOnboardingWithDefault()
                                    wizardDismissedManually = true
                                    prefs.edit().putBoolean("wizard_dismissed", true).apply()
                                    showOnboardingWizard = false
                                },
                                onComplete = { name, type, refDate, refIsWork, period, start, end, uf, workCount, offCount, pattern ->
                                    viewModel.saveCompleteOnboarding(
                                        userName = name,
                                        type = type,
                                        refDate = refDate,
                                        refIsWork = refIsWork,
                                        period = period,
                                        startTime = start,
                                        endTime = end,
                                        uf = uf,
                                        workCount = workCount,
                                        offCount = offCount,
                                        customPattern = pattern
                                    )
                                    showOnboardingWizard = false
                                    wizardDismissedManually = true
                                    prefs.edit().putBoolean("wizard_dismissed", true).apply()
                                }
                            )
                        }

                        // App Update Dialog
                        if (uiState.showUpdateDialog && uiState.activeRelease != null) {
                            UpdateDialog(
                                release = uiState.activeRelease!!,
                                currentVersion = viewModel.updateManager.currentVersionName,
                                downloadState = uiState.downloadState,
                                onStartDownload = {
                                    viewModel.startDownloadingActiveRelease()
                                },
                                onInstallDownloadedApk = {
                                    viewModel.installDownloadedApk()
                                },
                                onOpenBrowser = {
                                    viewModel.openBrowserForActiveRelease()
                                },
                                onDismiss = {
                                    viewModel.dismissUpdateDialog()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
