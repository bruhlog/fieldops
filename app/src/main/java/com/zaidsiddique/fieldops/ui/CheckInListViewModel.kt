package com.zaidsiddique.fieldops.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaidsiddique.fieldops.data.CheckIn
import com.zaidsiddique.fieldops.data.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CheckInListViewModel @Inject constructor(
    repository: CheckInRepository
) : ViewModel() {

    val checkIns: StateFlow<List<CheckIn>> =
        repository.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}