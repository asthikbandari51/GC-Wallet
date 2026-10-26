package com.giftvault.app.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.giftvault.app.ui.activity.ActivityScreen
import com.giftvault.app.ui.addcard.AddCardStepperScreen
import com.giftvault.app.ui.backup.BackupScreen
import com.giftvault.app.ui.bankcards.ManageBankCardsScreen
import com.giftvault.app.ui.carddetails.CardDetailsScreen
import com.giftvault.app.ui.claims.TruzteeClaimsScreen
import com.giftvault.app.ui.home.HomeScreen
import com.giftvault.app.ui.receipts.ReceiptsScreen
import com.giftvault.app.ui.reminders.RemindersScreen
import com.giftvault.app.ui.rupay.RuPayRedeemScreen
import com.giftvault.app.ui.settings.SettingsScreen
import com.giftvault.app.ui.vault.VaultScreen

enum class Screen(val route: String) {
    HOME("home"),
    VAULT("vault"),
    ADD_CARD("add_card?cardId={cardId}"),
    CARD_DETAILS("card_details/{cardId}"),
    MANAGE_BANK_CARDS("manage_bank_cards"),
    RUPAY_REDEEM("rupay_redeem"),
    TRUZTEE_CLAIMS("truztee_claims"),
    REMINDERS("reminders"),
    RECEIPTS("receipts"),
    ACTIVITY("activity"),
    BACKUP("backup"),
    SETTINGS("settings");

    companion object {
        fun cardDetailsRoute(cardId: String): String = "card_details/$cardId"
        fun addCardRoute(cardId: String? = null): String =
            if (cardId != null) "add_card?cardId=$cardId" else "add_card"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftVaultNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.HOME.route,
        modifier = modifier,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        composable(Screen.HOME.route) {
            HomeScreen(
                onNavigateToAddCard = {
                    navController.navigate(Screen.addCardRoute())
                },
                onNavigateToCardDetails = { cardId ->
                    navController.navigate(Screen.cardDetailsRoute(cardId))
                },
                onNavigateToVault = {
                    navController.navigate(Screen.VAULT.route) {
                        popUpTo(Screen.HOME.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToManageCards = {
                    navController.navigate(Screen.MANAGE_BANK_CARDS.route)
                },
                onNavigateToRuPayRedeem = {
                    navController.navigate(Screen.RUPAY_REDEEM.route)
                }
            )
        }
        composable(Screen.VAULT.route) {
            VaultScreen(
                onNavigateToCardDetails = { cardId ->
                    navController.navigate(Screen.cardDetailsRoute(cardId))
                },
                onNavigateToAddCard = {
                    navController.navigate(Screen.addCardRoute())
                }
            )
        }
        composable(
            route = Screen.ADD_CARD.route,
            arguments = listOf(
                navArgument("cardId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            AddCardStepperScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.CARD_DETAILS.route,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) {
            CardDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditCard = { cardId ->
                    navController.navigate(Screen.addCardRoute(cardId))
                }
            )
        }
        composable(Screen.MANAGE_BANK_CARDS.route) {
            ManageBankCardsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RUPAY_REDEEM.route) {
            RuPayRedeemScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenAddCard = { cardId -> navController.navigate(Screen.addCardRoute(cardId)) }
            )
        }
        composable(Screen.TRUZTEE_CLAIMS.route) {
            TruzteeClaimsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.REMINDERS.route) {
            RemindersScreen(
                onNavigateToCardDetails = { cardId ->
                    navController.navigate(Screen.cardDetailsRoute(cardId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RECEIPTS.route) {
            ReceiptsScreen(
                onNavigateToCardDetails = { cardId ->
                    navController.navigate(Screen.cardDetailsRoute(cardId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ACTIVITY.route) {
            ActivityScreen(
                onNavigateToCardDetails = { cardId ->
                    navController.navigate(Screen.cardDetailsRoute(cardId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.BACKUP.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SETTINGS.route) {
            SettingsScreen(
                onNavigateToBackup = { navController.navigate(Screen.BACKUP.route) },
                onNavigateToReminders = { navController.navigate(Screen.REMINDERS.route) },
                onNavigateToManageCards = { navController.navigate(Screen.MANAGE_BANK_CARDS.route) },
                onNavigateToRuPayRedeem = { navController.navigate(Screen.RUPAY_REDEEM.route) },
                onNavigateToClaims = { navController.navigate(Screen.TRUZTEE_CLAIMS.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
