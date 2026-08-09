package com.dev.trackone.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.feature.credential.domain.repository.CredentialRepository
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import com.dev.trackone.feature.expiry.domain.repository.ExpiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Dashboard home screen.
 * Provides reactive summary data from all 3 modules.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    expiryRepository: ExpiryRepository,
    expenseRepository: ExpenseRepository,
    credentialRepository: CredentialRepository,
) : ViewModel() {

    private val todayStart: Long
    private val todayEnd: Long

    init {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        todayStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        todayEnd = cal.timeInMillis - 1
    }

    /** Count of items whose expiry date falls within today */
    val expiringTodayCount: StateFlow<Int> = expiryRepository
        .countExpiringBetween(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Total expense amount for today (excluding income) */
    val todayExpenseTotal: StateFlow<Double> = expenseRepository
        .getTotalExpense(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** Total number of saved credentials */
    val credentialCount: StateFlow<Int> = credentialRepository
        .countAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
