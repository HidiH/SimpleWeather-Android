package com.thewizrd.simpleweather.ui.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
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