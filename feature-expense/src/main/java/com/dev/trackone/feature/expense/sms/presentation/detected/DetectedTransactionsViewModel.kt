package com.dev.trackone.feature.expense.sms.presentation.detected

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.feature.expense.sms.domain.model.DetectedTransaction
import com.dev.trackone.feature.expense.sms.domain.repository.DetectedTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetectedTransactionsViewModel @Inject constructor(
    private val repository: DetectedTransactionRepository,
) : ViewModel() {

    /** Rows the user just accepted or declined, hidden before the database catches up. */
    private val handledIds = MutableStateFlow<Set<Long>>(emptySet())

    val pendingCount: StateFlow<Int> = repository.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val items: StateFlow<List<DetectedTransaction>> =
        combine(repository.observePending(), handledIds) { pending, handled ->
            pending.filterNot { it.id in handled }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun accept(id: Long) = handle(id) { repository.accept(id) }

    fun decline(id: Long) = handle(id) { repository.decline(id) }

    /** Optimistic: the row leaves the list at once, and comes back only if the write fails. */
    private fun handle(id: Long, action: suspend () -> Unit) {
        handledIds.update { it + id }
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handledIds.update { it - id }
            }
        }
    }
}
