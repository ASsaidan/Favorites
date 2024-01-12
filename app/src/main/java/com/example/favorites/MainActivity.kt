package com.example.favorites

import AddNewPlaceScreen
import HomeScreen
import SignUpScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.FavoritesTheme
import com.google.firebase.FirebaseApp
import ui.MainScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentResolver = contentResolver
        FirebaseApp.initializeApp(this)
        setContent {
            FavoritesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "login") {
                        composable("login") {
                            HomeScreen(navigateToSignUp = {
                                navController.navigate("signup")
                            }, navigateToMain = {
                                navController.navigate("MainScreen")
                            }, contentResolver = contentResolver)
                        }

                        composable("signup") {
                            SignUpScreen(navigateToLogin = {
                                navController.navigate("login")
                            }, contentResolver = contentResolver)
                        }

                        composable("MainScreen") {
                            MainScreen(navigateToAddPlace = {
                                navController.navigate("AddNewPlaceScreen")
                            }, onPlaceClick = { place ->
                                // Handle place click here
                            }, contentResolver = contentResolver)
                        }

                        composable("AddNewPlaceScreen") {
                            AddNewPlaceScreen(onAddPlace = { place ->
                            }, navController = navController, contentResolver = contentResolver)
                        }
                    }
                }
            }
        }
    }
}