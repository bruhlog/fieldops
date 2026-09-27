@file:OptIn(ExperimentalMaterial3Api::class)

package com.zaidsiddique.fieldops.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.zaidsiddique.fieldops.data.CheckIn
import com.zaidsiddique.fieldops.data.SyncStatus
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun CheckInListScreen(
    onNewCheckIn: () -> Unit,
    viewModel: CheckInListViewModel = hiltViewModel()
) {
    val checkIns by viewModel.checkIns.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FieldOps") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewCheckIn
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New check-in"
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(checkIns) { checkIn ->
                CheckInRow(checkIn)
            }
        }
    }
}

@Composable
fun CheckInRow(checkIn: CheckIn) {

    val fmt = remember {
        SimpleDateFormat(
            "MMM d, h:mm a",
            Locale.getDefault()
        )
    }

    ListItem(
        headlineContent = {
            Text(
                checkIn.address ?: checkIn.qrCode
            )
        },
        supportingContent = {
            Text(
                fmt.format(Date(checkIn.timestamp))
            )
        },
        trailingContent = {
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        if (checkIn.syncStatus == SyncStatus.SYNCED)
                            "SYNCED"
                        else
                            "PENDING"
                    )
                }
            )
        }
    )
}