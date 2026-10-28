package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Listing
import com.example.data.model.UserRole
import com.example.ui.components.CodelligenceTopBar
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    FEED,
    CATEGORIES,
    SUBMIT,
    CHATBOT,
    DETAIL,
    SAVED,
    MY_LISTINGS,
    BUSINESS_EDIT,
    MODERATION_ADMIN,
    NOTIFICATIONS,
    SETTINGS,
    HELP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CodelligenceApp()
            }
        }
    }
}

@Composable
fun CodelligenceApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var selectedListingId by remember { mutableStateOf<String?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initFirebase(context)
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()
    val pendingListings by viewModel.pendingListings.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != AppScreen.FEED && currentScreen != AppScreen.SPLASH) {
        when (currentScreen) {
            AppScreen.DETAIL, AppScreen.BUSINESS_EDIT -> currentScreen = AppScreen.FEED
            AppScreen.NOTIFICATIONS, AppScreen.SETTINGS, AppScreen.HELP, AppScreen.CHATBOT -> currentScreen = AppScreen.FEED
            else -> currentScreen = AppScreen.FEED
        }
    }

    if (currentScreen == AppScreen.SPLASH) {
        SplashScreen(
            onSplashFinished = { currentScreen = AppScreen.FEED }
        )
        return
    }

    if (currentScreen == AppScreen.ONBOARDING) {
        OnboardingScreen(
            onGetStarted = { currentScreen = AppScreen.FEED }
        )
        return
    }

    val screenTitle = when (currentScreen) {
        AppScreen.FEED -> "Free Promotions"
        AppScreen.CATEGORIES -> "Browse Sectors"
        AppScreen.SUBMIT -> "Post Free Listing"
        AppScreen.CHATBOT -> "Gemini AI Advisor"
        AppScreen.DETAIL -> "Listing Details"
        AppScreen.SAVED -> "Saved Bookmarks"
        AppScreen.MY_LISTINGS -> "My Business Hub"
        AppScreen.BUSINESS_EDIT -> "Edit Profile"
        AppScreen.MODERATION_ADMIN -> "Moderation & Admin"
        AppScreen.NOTIFICATIONS -> "Notifications"
        AppScreen.SETTINGS -> "Platform Settings"
        AppScreen.HELP -> "Help & Support"
        else -> "Codelligence Ads"
    }

    val isSubScreen = currentScreen in listOf(
        AppScreen.DETAIL,
        AppScreen.BUSINESS_EDIT,
        AppScreen.NOTIFICATIONS,
        AppScreen.SETTINGS,
        AppScreen.HELP
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CodelligenceTopBar(
                title = screenTitle,
                currentUser = currentUser,
                unreadNotificationsCount = notifications.count { !it.isRead },
                onRoleSwitchClick = { showRoleDialog = true },
                onNotificationsClick = { currentScreen = AppScreen.NOTIFICATIONS },
                onNavigationClick = if (isSubScreen) {
                    { currentScreen = AppScreen.FEED }
                } else null
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    Triple(AppScreen.FEED, "Feed", Icons.Default.Home to Icons.Outlined.Home),
                    Triple(AppScreen.CATEGORIES, "Sectors", Icons.Default.Category to Icons.Outlined.Category),
                    Triple(AppScreen.SUBMIT, "Post Ad", Icons.Default.AddCircle to Icons.Outlined.AddCircleOutline),
                    Triple(AppScreen.CHATBOT, "AI Chat", Icons.Default.SmartToy to Icons.Outlined.SmartToy),
                    Triple(AppScreen.MY_LISTINGS, "My Hub", Icons.Default.Business to Icons.Outlined.Business),
                    Triple(AppScreen.MODERATION_ADMIN, "Admin", Icons.Default.Shield to Icons.Outlined.Shield),
                    Triple(AppScreen.SETTINGS, "Settings", Icons.Default.Settings to Icons.Outlined.Settings)
                )

                navItems.forEach { (screen, label, icons) ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            if (screen == AppScreen.MODERATION_ADMIN && pendingListings.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = AmberPending,
                                            contentColor = NavyBlack
                                        ) {
                                            Text("${pendingListings.size}", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) icons.first else icons.second,
                                        contentDescription = label,
                                        tint = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) icons.first else icons.second,
                                    contentDescription = label,
                                    tint = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ElectricBlue.copy(alpha = 0.2f),
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${label.lowercase().replace(" ", "_")}")
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
                AppScreen.FEED -> HomeScreen(
                    viewModel = viewModel,
                    onListingClick = { listing ->
                        selectedListingId = listing.id
                        currentScreen = AppScreen.DETAIL
                    },
                    onNavigateSubmit = { currentScreen = AppScreen.SUBMIT },
                    onNavigateCategories = { currentScreen = AppScreen.CATEGORIES }
                )

                AppScreen.CATEGORIES -> CategoryBrowseScreen(
                    viewModel = viewModel,
                    onCategorySelected = { categoryName ->
                        currentScreen = AppScreen.FEED
                    }
                )

                AppScreen.SUBMIT -> SubmitListingScreen(
                    viewModel = viewModel,
                    onSubmissionSuccess = {
                        currentScreen = AppScreen.MY_LISTINGS
                    }
                )

                AppScreen.CHATBOT -> ChatbotScreen(viewModel = viewModel)

                AppScreen.DETAIL -> {
                    selectedListingId?.let { id ->
                        ListingDetailScreen(
                            listingId = id,
                            viewModel = viewModel,
                            onBackClick = { currentScreen = AppScreen.FEED }
                        )
                    } ?: run {
                        currentScreen = AppScreen.FEED
                    }
                }

                AppScreen.SAVED -> SavedListingsScreen(
                    viewModel = viewModel,
                    onListingClick = { listing ->
                        selectedListingId = listing.id
                        currentScreen = AppScreen.DETAIL
                    },
                    onExploreClick = { currentScreen = AppScreen.FEED }
                )

                AppScreen.MY_LISTINGS -> MyListingsScreen(
                    viewModel = viewModel,
                    onListingClick = { listing ->
                        selectedListingId = listing.id
                        currentScreen = AppScreen.DETAIL
                    },
                    onNavigateSubmit = { currentScreen = AppScreen.SUBMIT },
                    onNavigateEditProfile = { currentScreen = AppScreen.BUSINESS_EDIT }
                )

                AppScreen.BUSINESS_EDIT -> BusinessProfileEditScreen(
                    viewModel = viewModel,
                    onSaved = { currentScreen = AppScreen.MY_LISTINGS }
                )

                AppScreen.MODERATION_ADMIN -> ModeratorAdminScreen(
                    viewModel = viewModel,
                    onRoleSwitchClick = { showRoleDialog = true }
                )

                AppScreen.NOTIFICATIONS -> NotificationsScreen(
                    viewModel = viewModel
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateHelp = { currentScreen = AppScreen.HELP }
                )

                AppScreen.HELP -> HelpSupportScreen()

                else -> HomeScreen(
                    viewModel = viewModel,
                    onListingClick = { listing ->
                        selectedListingId = listing.id
                        currentScreen = AppScreen.DETAIL
                    },
                    onNavigateSubmit = { currentScreen = AppScreen.SUBMIT },
                    onNavigateCategories = { currentScreen = AppScreen.CATEGORIES }
                )
            }
        }
    }

    if (showRoleDialog) {
        RoleSwitcherDialog(
            currentRole = currentUser.role,
            onRoleSelected = { role ->
                viewModel.switchRole(role)
            },
            onDismiss = { showRoleDialog = false }
        )
    }
}
