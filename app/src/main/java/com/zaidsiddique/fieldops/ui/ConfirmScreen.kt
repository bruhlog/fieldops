package com.zaidsiddique.fieldops.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun ConfirmScreen(
    qrCode: String,
    photoPath: String,
    onSaved: () -> Unit,
    viewModel: NewCheckInViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val state by viewModel.state.collectAsState()

    // ---------------------------------------------------------
    // Location permission launcher
    // ---------------------------------------------------------

    val locationPermissionLauncher =
    rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val fineGranted =
            permissions[
                Manifest.permission.ACCESS_FINE_LOCATION
            ] ?: false

        val coarseGranted =
            permissions[
                Manifest.permission.ACCESS_COARSE_LOCATION
            ] ?: false

        viewModel.saveCheckIn(
            qrCode = qrCode,
            photoPath = photoPath,
            useLocation = fineGranted || coarseGranted
        )
    }

    // ---------------------------------------------------------
    // Navigate back to list after successful local save
    // ---------------------------------------------------------

    LaunchedEffect(state.success) {

        if (state.success) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // -----------------------------------------------------
        // Title
        // -----------------------------------------------------

        Text(
            text = "Confirm Check-in"
        )

        // -----------------------------------------------------
        // QR code
        // -----------------------------------------------------

        Text(
            text = "QR Code:"
        )

        Text(
            text = qrCode
        )

        // -----------------------------------------------------
        // Photo preview
        // -----------------------------------------------------

        Text(
            text = "Photo:"
        )

        Image(
            painter = rememberAsyncImagePainter(
                model = photoPath
            ),
            contentDescription = "Captured photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentScale = ContentScale.Crop
        )

        // -----------------------------------------------------
        // Address
        // -----------------------------------------------------

        if (state.address != null) {

            Text(
                text = "Address:"
            )

            Text(
                text = state.address!!
            )
        }

        // -----------------------------------------------------
        // Error
        // -----------------------------------------------------

        if (state.error != null) {

            Text(
                text = "Error: ${state.error}"
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // -----------------------------------------------------
        // Saving state
        // -----------------------------------------------------

        if (state.isSaving) {

            CircularProgressIndicator()

            Text(
                text = "Getting location and saving check-in..."
            )

        } else {

            // -------------------------------------------------
            // Save button
            // -------------------------------------------------

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    val fineGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    val coarseGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (fineGranted || coarseGranted) {

                        viewModel.saveCheckIn(
                            qrCode = qrCode,
                            photoPath = photoPath
                        )

                    } else {

                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            ) {
                Text(
                    text = "Save Check-in"
                )
            }
        }
    }
}