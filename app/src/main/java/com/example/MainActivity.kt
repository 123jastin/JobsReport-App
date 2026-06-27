package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.JobRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database and Repository natively
    val database = AppDatabase.getDatabase(this)
    val repository = JobRepository(database.jobDao())
    val factory = MainViewModelFactory(application, repository)
    val viewModel: MainViewModel by viewModels { factory }

    setContent {
      MyApplicationTheme {
        MainAppShell(viewModel)
      }
    }
  }
}

sealed class Screen(val route: String, val title: String) {
  object Splash : Screen("splash", "Splash")
  object Home : Screen("home", "Home")
  object IntelligenceFeed : Screen("intelligence_feed", "Intelligence Feed")
  object Companies : Screen("companies", "Companies")
  object Regions : Screen("regions", "Regions")
  object Dashboard : Screen("dashboard", "Explore")
  object Reports : Screen("reports", "Job Reports")
  object AdminStudio : Screen("admin_studio", "Admin Studio")
  object Tracker : Screen("tracker", "Tracker")
  object AiAdvisor : Screen("ai_advisor", "AI Advisor")
  object Profile : Screen("profile", "My Profile")
  object AboutUs : Screen("about_us", "About Us")
  object ContactUs : Screen("contact_us", "Contact Us")
  object Disclaimer : Screen("disclaimer", "Disclaimer")
  object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy")
  object TermsOfService : Screen("terms_of_service", "Terms of Service")
}

@Composable
fun MainAppShell(viewModel: MainViewModel) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val uriHandler = LocalUriHandler.current
  
  // Selected Country & Admin flows
  val selectedCountry by viewModel.selectedCountry.collectAsState()
  val isAdmin by viewModel.isAdmin.collectAsState()

  // Dropdown menu state
  var isCountryMenuExpanded by remember { mutableStateOf(false) }
  var isMoreMenuExpanded by remember { mutableStateOf(false) }

  // Pulsing Live Indicator
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alphaPulse by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  // Hide bottom/top navigation on job detail or splash screens
  val isDetailScreen = currentDestination?.route?.startsWith("job_detail/") == true
  val isSplashActive = currentDestination?.route == Screen.Splash.route
  val showChrome = !isDetailScreen && !isSplashActive

  val countriesList = listOf(
    "Worldwide" to "🌍",
    "Tanzania" to "🇹🇿",
    "United States" to "🇺🇸",
    "United Kingdom" to "🇬🇧",
    "Germany" to "🇩🇪",
    "Kenya" to "🇰🇪",
    "South Africa" to "🇿🇦"
  )

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = showChrome,
    drawerContent = {
      if (showChrome) {
        ModalDrawerSheet(
          drawerContainerColor = Color(0xFF1E293B),
          drawerContentColor = Color.White,
          modifier = Modifier.width(310.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            // Header
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    Brush.horizontalGradient(
                      colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text("JR", fontWeight = FontWeight.Bold, color = Color.White)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "JobsReport",
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp,
                  color = Color.White
                )
                Text(
                  text = "ONLINE TERMINAL v2.4.0",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = Color(0xFF10B981)
                )
              }
            }

            Divider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(16.dp))

            // Drawer navigation links
            val drawerItems = listOf(
              Triple(Screen.Home, "Home Dashboard", Icons.Filled.Home),
              Triple(Screen.IntelligenceFeed, "Intelligence Feed", Icons.Filled.TrendingUp),
              Triple(Screen.Companies, "Companies & Employers", Icons.Filled.Business),
              Triple(Screen.Regions, "Jobs by Regions", Icons.Filled.Map),
              Triple(Screen.Dashboard, "All Jobs List", Icons.Filled.Work),
              Triple(Screen.Reports, "All Job Reports", Icons.Filled.Analytics),
              Triple(Screen.Tracker, "Saved & Applied Tracker", Icons.Filled.Bookmark)
            )

            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(drawerItems) { (screen, label, icon) ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavigationDrawerItem(
                  icon = { Icon(icon, contentDescription = null) },
                  label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                  selected = isSelected,
                  onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(screen.route) {
                      popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                      }
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFF3B82F6).copy(alpha = 0.15f),
                    selectedIconColor = Color(0xFF60A5FA),
                    selectedTextColor = Color(0xFF60A5FA),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFFE2E8F0)
                  ),
                  modifier = Modifier.height(48.dp)
                )
              }

              item {
                NavigationDrawerItem(
                  icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Post Job Icon", tint = Color(0xFF10B981)) },
                  label = {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text("Post Job", fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                      Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = "External link icon",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  },
                  selected = false,
                  onClick = {
                    scope.launch { drawerState.close() }
                    try {
                      uriHandler.openUri("https://jobsreport.online/post-job")
                    } catch (e: Exception) {
                      // ignore
                    }
                  },
                  colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    selectedContainerColor = Color.Transparent,
                    unselectedIconColor = Color(0xFF10B981),
                    unselectedTextColor = Color(0xFF10B981)
                  ),
                  modifier = Modifier.height(48.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer metadata
            Text(
              text = "DATA MODE: VERIFIED TELEMETRY",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF64748B),
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp
            )
            Text(
              text = "SYNC STABLE // LOCAL CACHE ON",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF10B981),
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp
            )
          }
        }
      }
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color(0xFF0F172A),
      topBar = {
        if (showChrome) {
          Surface(
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp,
            modifier = Modifier.statusBarsPadding()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Left: Hamburger Menu and Title
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { scope.launch { drawerState.open() } },
                  modifier = Modifier.testTag("menu_drawer_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer Menu",
                    tint = Color.White
                  )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "JobsReport",
                      fontWeight = FontWeight.Black,
                      fontSize = 17.sp,
                      color = Color.White
                    )
                    Text(
                      text = ".online",
                      fontSize = 11.sp,
                      color = Color(0xFF3B82F6),
                      fontWeight = FontWeight.Bold
                    )
                  }
                  
                  // Pulsing status label
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = alphaPulse))
                    )
                    Text(
                      text = "LIVE TELEMETRY ACTIVE",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 8.sp,
                      color = Color(0xFF10B981),
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              // Right: Country Select + Search + Notification
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                // Country dropdown button
                Box {
                  val currentFlag = countriesList.firstOrNull { it.first == selectedCountry }?.second ?: "🌍"
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFF1E293B))
                      .clickable { isCountryMenuExpanded = true }
                      .padding(horizontal = 8.dp, vertical = 6.dp)
                      .testTag("country_select_trigger")
                  ) {
                    Text(text = "$currentFlag ", fontSize = 16.sp)
                    Text(
                      text = if (selectedCountry == "Worldwide") "Global" else selectedCountry,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.widthIn(max = 68.dp)
                    )
                  }

                  DropdownMenu(
                    expanded = isCountryMenuExpanded,
                    onDismissRequest = { isCountryMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                  ) {
                    countriesList.forEach { (countryName, flag) ->
                      DropdownMenuItem(
                        text = {
                          Row {
                            Text(text = "$flag  ", fontSize = 16.sp)
                            Text(
                              text = if (countryName == "Worldwide") "Worldwide" else countryName,
                              color = Color.White,
                              fontWeight = FontWeight.Medium
                            )
                          }
                        },
                        onClick = {
                          viewModel.setSelectedCountry(countryName)
                          isCountryMenuExpanded = false
                        },
                        modifier = Modifier.testTag("country_option_$countryName")
                      )
                    }
                  }
                }

                IconButton(
                  onClick = {
                    navController.navigate(Screen.Dashboard.route) {
                      popUpTo(navController.graph.findStartDestination().id)
                    }
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Jobs",
                    tint = Color.White
                  )
                }

                Box {
                  IconButton(onClick = {}) {
                    Icon(
                      imageVector = Icons.Default.Notifications,
                      contentDescription = "Notifications",
                      tint = Color.White
                    )
                  }
                  // Small active notification dot
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF3B82F6))
                      .align(Alignment.TopEnd)
                      .offset(x = (-4).dp, y = 4.dp)
                  )
                }

                // Three-dot menu
                Box {
                  IconButton(onClick = { isMoreMenuExpanded = true }) {
                    Icon(
                      imageVector = Icons.Default.MoreVert,
                      contentDescription = "More Options",
                      tint = Color.White
                    )
                  }
                  DropdownMenu(
                    expanded = isMoreMenuExpanded,
                    onDismissRequest = { isMoreMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                  ) {
                    DropdownMenuItem(
                      text = { Text("Home Dashboard", color = Color.White) },
                      onClick = {
                        isMoreMenuExpanded = false
                        navController.navigate(Screen.Home.route) {
                          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Intelligence Feed", color = Color.White) },
                      onClick = {
                        isMoreMenuExpanded = false
                        navController.navigate(Screen.IntelligenceFeed.route) {
                          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Companies & Employers", color = Color.White) },
                      onClick = {
                        isMoreMenuExpanded = false
                        navController.navigate(Screen.Companies.route) {
                          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Jobs by Regions", color = Color.White) },
                      onClick = {
                        isMoreMenuExpanded = false
                        navController.navigate(Screen.Regions.route) {
                          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("AI Career Co-Pilot", color = Color.White) },
                      onClick = {
                        isMoreMenuExpanded = false
                        navController.navigate(Screen.AiAdvisor.route) {
                          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                          launchSingleTop = true
                          restoreState = true
                        }
                      }
                    )
                    DropdownMenuItem(
                      text = {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                          Text("Post Job", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                          Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = "External link icon",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                          )
                        }
                      },
                      onClick = {
                        isMoreMenuExpanded = false
                        try {
                          uriHandler.openUri("https://jobsreport.online/post-job")
                        } catch (e: Exception) {
                          // ignore
                        }
                      }
                    )
                  }
                }
              }
            }
          }
        }
      }
    ) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.padding(innerPadding)
      ) {
        composable(Screen.Splash.route) {
          SplashScreen(
            onSplashComplete = {
              navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
              }
            }
          )
        }

        composable(Screen.Home.route) {
          HomeScreen(
            viewModel = viewModel,
            onNavigateToJobs = {
              navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            },
            onNavigateToReports = {
              navController.navigate(Screen.Reports.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToRegions = {
              navController.navigate(Screen.Regions.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToCompanies = {
              navController.navigate(Screen.Companies.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToAboutUs = {
              navController.navigate(Screen.AboutUs.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToContactUs = {
              navController.navigate(Screen.ContactUs.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToDisclaimer = {
              navController.navigate(Screen.Disclaimer.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToPrivacyPolicy = {
              navController.navigate(Screen.PrivacyPolicy.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToTermsOfService = {
              navController.navigate(Screen.TermsOfService.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onCategoryClick = { categorySlug ->
              navController.navigate("category/$categorySlug")
            }
          )
        }

        composable(Screen.AboutUs.route) {
          AboutUsScreen(
            onBackClick = {
              navController.popBackStack()
            }
          )
        }

        composable(Screen.ContactUs.route) {
          ContactUsScreen(
            onBackClick = {
              navController.popBackStack()
            }
          )
        }

        composable(Screen.Disclaimer.route) {
          DisclaimerScreen(
            onBackClick = {
              navController.popBackStack()
            }
          )
        }

        composable(Screen.PrivacyPolicy.route) {
          PrivacyPolicyScreen(
            onBackClick = {
              navController.popBackStack()
            }
          )
        }

        composable(Screen.TermsOfService.route) {
          TermsOfServiceScreen(
            onBackClick = {
              navController.popBackStack()
            }
          )
        }

        composable(
          route = "country/{countrySlug}",
          arguments = listOf(navArgument("countrySlug") { type = NavType.StringType })
        ) { backStackEntry ->
          val countrySlug = backStackEntry.arguments?.getString("countrySlug") ?: ""
          val countryName = when (countrySlug.lowercase().replace("-", " ")) {
            "tanzania" -> "Tanzania"
            "united states" -> "United States"
            "united kingdom" -> "United Kingdom"
            "germany" -> "Germany"
            "kenya" -> "Kenya"
            "south africa" -> "South Africa"
            else -> "Worldwide"
          }

          LaunchedEffect(countryName) {
            viewModel.setSelectedCountry(countryName)
          }

          HomeScreen(
            viewModel = viewModel,
            onNavigateToJobs = {
              navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            },
            onNavigateToReports = {
              navController.navigate(Screen.Reports.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToRegions = {
              navController.navigate(Screen.Regions.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToCompanies = {
              navController.navigate(Screen.Companies.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToAboutUs = {
              navController.navigate(Screen.AboutUs.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToContactUs = {
              navController.navigate(Screen.ContactUs.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToDisclaimer = {
              navController.navigate(Screen.Disclaimer.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToPrivacyPolicy = {
              navController.navigate(Screen.PrivacyPolicy.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToTermsOfService = {
              navController.navigate(Screen.TermsOfService.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onCategoryClick = { categorySlug ->
              navController.navigate("category/$categorySlug/$countrySlug")
            }
          )
        }

        composable(
          route = "category/{categorySlug}",
          arguments = listOf(navArgument("categorySlug") { type = NavType.StringType })
        ) { backStackEntry ->
          val categorySlug = backStackEntry.arguments?.getString("categorySlug") ?: ""
          CategoryScreen(
            viewModel = viewModel,
            categorySlug = categorySlug,
            countrySlug = null,
            onBackClick = { navController.popBackStack() },
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            },
            onNavigateToAiAdvisor = {
              navController.navigate(Screen.AiAdvisor.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }

        composable(
          route = "category/{categorySlug}/{countrySlug}",
          arguments = listOf(
            navArgument("categorySlug") { type = NavType.StringType },
            navArgument("countrySlug") { type = NavType.StringType }
          )
        ) { backStackEntry ->
          val categorySlug = backStackEntry.arguments?.getString("categorySlug") ?: ""
          val countrySlug = backStackEntry.arguments?.getString("countrySlug") ?: ""
          CategoryScreen(
            viewModel = viewModel,
            categorySlug = categorySlug,
            countrySlug = countrySlug,
            onBackClick = { navController.popBackStack() },
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            },
            onNavigateToAiAdvisor = {
              navController.navigate(Screen.AiAdvisor.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }

        composable(Screen.IntelligenceFeed.route) {
          IntelligenceFeedScreen(
            viewModel = viewModel,
            onNavigateToJobs = {
              navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.IntelligenceFeed.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }

        composable(Screen.Companies.route) {
          CompaniesScreen(
            viewModel = viewModel,
            onCompanyClick = { _ ->
              navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.IntelligenceFeed.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            }
          )
        }

        composable(Screen.Regions.route) {
          RegionsScreen(
            viewModel = viewModel,
            onRegionClick = { regionName ->
              navController.navigate("region_detail/$regionName")
            },
            onCountryClick = { countrySlug ->
              navController.navigate("country/$countrySlug")
            }
          )
        }

        composable(
          route = "region_detail/{regionName}",
          arguments = listOf(navArgument("regionName") { type = NavType.StringType })
        ) { backStackEntry ->
          val regionName = backStackEntry.arguments?.getString("regionName") ?: "Worldwide"
          RegionDetailScreen(
            viewModel = viewModel,
            regionName = regionName,
            onBackClick = { navController.popBackStack() },
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            },
            onNavigateToDashboard = {
              navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.IntelligenceFeed.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }

        composable(Screen.Dashboard.route) {
          DashboardScreen(
            viewModel = viewModel,
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            },
            onNavigateToAdmin = {
              navController.navigate(Screen.AdminStudio.route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }

        composable(Screen.Reports.route) {
          JobReportsScreen(
            viewModel = viewModel,
            onNavigateToReportDetail = { slug ->
              navController.navigate("report_detail/$slug")
            }
          )
        }

        composable(
          route = "report_detail/{slug}",
          arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
          val slug = backStackEntry.arguments?.getString("slug") ?: ""
          ReportDetailScreen(
            slug = slug,
            viewModel = viewModel,
            onBackClick = {
              navController.popBackStack()
            },
            onNavigateToJobDetail = { jobId ->
              navController.navigate("job_detail/$jobId")
            }
          )
        }

        composable(Screen.AdminStudio.route) {
          AdminStudioScreen(viewModel = viewModel)
        }
        
        composable(Screen.Tracker.route) {
          SavedJobsScreen(
            viewModel = viewModel,
            onJobClick = { jobId ->
              navController.navigate("job_detail/$jobId")
            }
          )
        }
        
        composable(Screen.AiAdvisor.route) {
          AiCareerScreen(viewModel = viewModel)
        }
        
        composable(Screen.Profile.route) {
          ProfileScreen(viewModel = viewModel)
        }

        composable(
          route = "job_detail/{jobId}",
          arguments = listOf(navArgument("jobId") { type = NavType.IntType })
        ) { backStackEntry ->
          val jobId = backStackEntry.arguments?.getInt("jobId") ?: -1
          JobDetailScreen(
            jobId = jobId,
            viewModel = viewModel,
            onNavigateToAi = {
              navController.navigate(Screen.AiAdvisor.route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onBackClick = {
              navController.popBackStack()
            }
          )
        }
      }
    }
  }
}

