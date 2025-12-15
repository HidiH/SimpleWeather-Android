package com.thewizrd.simpleweather.ui.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.thewizrd.shared_resources.Constants
import com.thewizrd.simpleweather.ui.navigation.Screen
import com.thewizrd.simpleweather.ui.theme.WearAppTheme
import com.thewizrd.simpleweather.ui.utils.rememberFocusRequester

@Composable
fun Setup(
    modifier: Modifier = Modifier
) {
    val navController = rememberSwipeDismissableNavController()

    WearAppTheme {
        AppScaffold(
            modifier = modifier,
        ) {
            val swipeFocusRequester = rememberFocusRequester()

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Screen.Setup.route
            ) {
                composable(
                    route = Screen.Setup.route
                ) {
                    SetupScreen(
                        navController = navController,
                        focusRequester = swipeFocusRequester
                    )
                }

                composable(
                    route = Screen.SetupLocationSearch.route
                ) {
                    SetupLocationSearchScreen(
                        navController = navController
                    )
                }

                composable(
                    route = Screen.SetupLocationList.route + "?${Constants.KEY_SEARCH}={${Constants.KEY_SEARCH}}",
                    arguments = listOf(
                        navArgument(Constants.KEY_SEARCH) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    SetupLocationListScreen(
                        navController = navController,
                        backStackEntry = backStackEntry,
                        focusRequester = swipeFocusRequester
                    )
                }

                composable(
                    route = Screen.SetupSync.route
                ) {
                    SetupSyncScreen(
                        navController = navController,
                        focusRequester = swipeFocusRequester
                    )
                }
            }
        }
    }
}