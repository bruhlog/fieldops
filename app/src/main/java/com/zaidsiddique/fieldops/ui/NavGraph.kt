package com.zaidsiddique.fieldops.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun FieldOpsNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "list"
    ) {

        // --------------------------------------------------
        // CHECK-IN LIST
        // --------------------------------------------------

        composable("list") {

            CheckInListScreen(
                onNewCheckIn = {
                    navController.navigate("scan")
                }
            )
        }

        // --------------------------------------------------
        // QR SCANNER
        // --------------------------------------------------

        composable("scan") {

            QrScanScreen(
                onScanned = { code ->

                    navController.navigate(
                        "confirm/${Uri.encode(code)}"
                    )
                }
            )
        }

        // --------------------------------------------------
        // QR CONFIRMATION / CONTINUE TO PHOTO
        // --------------------------------------------------

        composable("confirm/{code}") { backStackEntry ->

            val code =
                backStackEntry.arguments
                    ?.getString("code")
                    ?: ""

            ConfirmBeforePhotoScreen(
                qrCode = code,
                onContinue = {

                    navController.navigate(
                        "camera/${Uri.encode(code)}"
                    )
                }
            )
        }

        // --------------------------------------------------
        // CAMERA
        // --------------------------------------------------

        composable("camera/{code}") { backStackEntry ->

            val code =
                backStackEntry.arguments
                    ?.getString("code")
                    ?: ""

            CameraCaptureScreen(
                onCaptured = { filePath ->

                    navController.navigate(
                        "save/${Uri.encode(code)}/${Uri.encode(filePath)}"
                    )
                }
            )
        }

        // --------------------------------------------------
        // FINAL CONFIRMATION + SAVE
        // --------------------------------------------------

        composable("save/{code}/{photoPath}") { backStackEntry ->

            val code =
                backStackEntry.arguments
                    ?.getString("code")
                    ?: ""

            val photoPath =
                backStackEntry.arguments
                    ?.getString("photoPath")
                    ?: ""

            ConfirmScreen(
                qrCode = code,
                photoPath = photoPath,
                onSaved = {

                    navController.navigate("list") {

                        popUpTo("list") {
                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfirmBeforePhotoScreen(
    qrCode: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text("QR Code:")

        Text(qrCode)

        Button(
            onClick = onContinue
        ) {
            Text("Continue to Photo")
        }
    }
}