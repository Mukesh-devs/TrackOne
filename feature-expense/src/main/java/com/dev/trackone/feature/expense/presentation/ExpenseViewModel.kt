package com.dev.trackone.feature.expense.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dev.trackone.core.common.util.DateUtils
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    init {
        loadData()
    }

    private fun getDateRange(period: TimePeriod, dateMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        return when (period) {
            TimePeriod.DAY -> {
                DateUtils.getStartOfDay(dateMillis) to DateUtils.getEndOfDay(dateMillis)
            }
            TimePeriod.MONTH -> {
                DateUtils.getStartOfMonth(dateMillis) to DateUtils.getEndOfMonth(dateMillis)
            }
            TimePeriod.YEAR -> {
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                start to cal.timeInMillis
            }
        }
    }

    private fun loadData() {
        val state = _uiState.value
        val (start, end) = getDateRange(state.selectedPeriod, state.currentDate)

        viewModelScope.launch {
            combine(
                repository.getByDateRange(start, end),
                repository.getTotalIncome(start, end),
                repository.getTotalExpense(start, end),
            ) { items, totalInc, totalExp ->
                _uiState.update { s ->
                    s.copy(
                        isLoading = false,
                        items = items,
                        incomeItems = items.filter { it.isIncome },
                        expenseItems = items.filter { !it.isIncome },
                        totalIncome = totalInc,
                        totalExpense = totalExp,
                        balance = totalInc - totalExp,
                        error = null,
                    )
                }
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
                .collect {}
        }
    }

    // ========== Period Navigation ==========

    fun setTimePeriod(period: TimePeriod) {
        _uiState.update { it.copy(selectedPeriod = period, currentDate = System.currentTimeMillis()) }
        loadData()
    }

    fun navigateDate(forward: Boolean) {
        val state = _uiState.value
        val cal = Calendar.getInstance().apply { timeInMillis = state.currentDate }
        when (state.selectedPeriod) {
            TimePeriod.DAY -> cal.add(Calendar.DAY_OF_MONTH, if (forward) 1 else -1)
            TimePeriod.MONTH -> cal.add(Calendar.MONTH, if (forward) 1 else -1)
            TimePeriod.YEAR -> cal.add(Calendar.YEAR, if (forward) 1 else -1)
        }
        _uiState.update { it.copy(currentDate = cal.timeInMillis) }
        loadData()
    }

    fun getDateLabel(): String {
        val state = _uiState.value
        return when (state.selectedPeriod) {
            TimePeriod.DAY -> DateUtils.formatDate(state.currentDate)
            TimePeriod.MONTH -> DateUtils.formatMonthYear(state.currentDate)
            TimePeriod.YEAR -> DateUtils.formatDate(state.currentDate, "yyyy")
        }
    }

    // ========== Search ==========

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadData()
        } else {
            viewModelScope.launch {
                repository.search(query)
                    .catch { }
                    .collect { results ->
                        _uiState.update { it.copy(items = results) }
                    }
            }
        }
    }

    fun toggleSearch() {
        _uiState.update {
            val newActive = !it.isSearchActive
            if (!newActive) {
                loadData()
            }
            it.copy(isSearchActive = newActive, searchQuery = if (!newActive) "" else it.searchQuery)
        }
    }

    // ========== Filtering ==========
    fun setCategoryFilter(category: ExpenseCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            if (category == null) {
                loadData()
            } else {
                repository.getByCategory(category.name).collect { results ->
                    _uiState.update { it.copy(items = results) }
                }
            }
        }
    }

    // ========== CRUD ==========

    fun deleteItem(item: ExpenseItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    // ========== Statistics ==========

    fun setStatsDateRange(start: Long?, end: Long?) {
        _uiState.update { it.copy(statsStartDate = start, statsEndDate = end) }
        loadStatsData()
    }

    private fun loadStatsData() {
        val state = _uiState.value
        if (state.statsStartDate == null || state.statsEndDate == null) {
            _uiState.update { it.copy(
                statsItems = emptyList(),
                statsTotalIncome = 0.0,
                statsTotalExpense = 0.0
            ) }
            return
        }

        val endOfDay = com.dev.trackone.core.common.util.DateUtils.getEndOfDay(state.statsEndDate)

        viewModelScope.launch {
            repository.getByDateRange(state.statsStartDate, endOfDay).collect { results ->
                val income = results.filter { it.isIncome }.sumOf { it.amount }
                val expense = results.filter { !it.isIncome }.sumOf { it.amount }

                _uiState.update { it.copy(
                    statsItems = results,
                    statsTotalIncome = income,
                    statsTotalExpense = expense
                ) }
            }
        }
    }

    fun generateCsvData(): String {
        val items = _uiState.value.statsItems
        if (items.isEmpty()) return ""

        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Title,Amount,Type,Category,Mode of Payment,Date,Notes\n")

        items.forEach { item ->
            val dateStr = com.dev.trackone.core.common.util.DateUtils.formatDate(item.date)
            val type = if (item.isIncome) "Income" else "Expense"
            val notes = item.notes?.replace(",", " ") ?: "" // simple escaping
            val title = item.title.replace(",", " ")
            
            csvBuilder.append("${item.id},${title},${item.amount},${type},${item.category.displayName},${item.paymentMode.displayName},${dateStr},${notes}\n")
        }

        return csvBuilder.toString()
    }

    // ========== Form ==========

    fun initAddForm(isIncome: Boolean = false) {
        _formState.value = ExpenseFormState(isIncome = isIncome)
    }

    fun initEditForm(item: ExpenseItem) {
        _formState.value = ExpenseFormState(
            id = item.id,
            title = item.title,
            amount = item.amount.toString(),
            isIncome = item.isIncome,
            category = item.category,
            paymentMode = item.paymentMode,
            date = item.date,
            notes = item.notes ?: "",
            isEditing = true,
        )
    }

    fun updateFormTitle(value: String) {
        _formState.update { it.copy(title = value, titleError = null) }
    }

    fun updateFormAmount(value: String) {
        _formState.update { it.copy(amount = value, amountError = null) }
    }

    fun updateFormType(isIncome: Boolean) {
        _formState.update { 
            val newCategory = if (it.isIncome == isIncome) it.category else null
            it.copy(isIncome = isIncome, category = newCategory, categoryError = null) 
        }
    }

    fun updateFormCategory(value: ExpenseCategory) {
        _formState.update { it.copy(category = value, categoryError = null) }
    }

    fun updateFormDate(value: Long?) {
        _formState.update { it.copy(date = value, dateError = null) }
    }

    fun updateFormNotes(value: String) {
        _formState.update { it.copy(notes = value) }
    }

    fun updateFormPaymentMode(value: PaymentMode) {
        _formState.update { it.copy(paymentMode = value, paymentModeError = null) }
    }

    fun saveForm() {
        val form = _formState.value

        // Validate
        var hasError = false
        if (form.title.isBlank()) {
            _formState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        val amountValue = form.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _formState.update { it.copy(amountError = "Valid amount is required") }
            hasError = true
        }
        if (form.paymentMode == null) {
            _formState.update { it.copy(paymentModeError = "Payment mode is required") }
            hasError = true
        }
        if (form.date == null) {
            _formState.update { it.copy(dateError = "Date is required") }
            hasError = true
        }
        if (hasError) return

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val item = ExpenseItem(
                    id = if (form.isEditing) form.id else 0,
                    title = form.title.trim(),
                    amount = amountValue!!,
                    isIncome = form.isIncome,
                    category = form.category ?: if (form.isIncome) ExpenseCategory.OTHER_INCOME else ExpenseCategory.OTHER_EXPENSE,
                    paymentMode = form.paymentMode!!,
                    date = form.date!!,
                    notes = form.notes.ifBlank { null },
                )
                if (form.isEditing) {
                    repository.update(item)
                } else {
                    repository.insert(item)
                }
                _formState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}
