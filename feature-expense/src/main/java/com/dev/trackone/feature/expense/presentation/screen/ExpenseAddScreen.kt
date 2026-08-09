package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneDatePicker
import com.dev.trackone.core.ui.component.TrackOneDropdownField
import com.dev.trackone.core.ui.component.TrackOneTextField
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.presentation.ExpenseFormState

@Composable
fun ExpenseAddScreen(
    formState: ExpenseFormState,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (Boolean) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onDateChange: (Long?) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) onSaveSuccess()
    }

    val categories = if (formState.isIncome) ExpenseCategory.getIncomeCategories() else ExpenseCategory.getExpenseCategories()
    val categoryNames = categories.map { it.displayName }
    val paymentModeNames = PaymentMode.entries.map { it.displayName }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Type + amount + title
        FormCard(title = "Details") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !formState.isIncome,
                    onClick = { onTypeChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Expense") }
                SegmentedButton(
                    selected = formState.isIncome,
                    onClick = { onTypeChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Income") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TrackOneTextField(
                value = formState.amount,
                onValueChange = onAmountChange,
                label = "Amount *",
                placeholder = "0.00",
                leadingIcon = Icons.Default.Payment,
                keyboardType = KeyboardType.Decimal,
                isError = formState.amountError != null,
                errorMessage = formState.amountError,
            )
            Spacer(modifier = Modifier.height(12.dp))
            TrackOneTextField(
                value = formState.title,
                onValueChange = onTitleChange,
                label = "Title *",
                placeholder = "e.g., Groceries, Salary",
                leadingIcon = Icons.Default.Title,
                isError = formState.titleError != null,
                errorMessage = formState.titleError,
            )
        }

        // Category + payment + date
        FormCard(title = "Categorize") {
            TrackOneDropdownField(
                selectedValue = formState.category?.displayName ?: "",
                options = categoryNames,
                onOptionSelected = { name -> categories.find { it.displayName == name }?.let(onCategoryChange) },
                label = "Category",
            )
            Spacer(modifier = Modifier.height(12.dp))
            TrackOneDropdownField(
                selectedValue = formState.paymentMode?.displayName ?: "",
                options = paymentModeNames,
                onOptionSelected = { name -> PaymentMode.entries.find { it.displayName == name }?.let(onPaymentModeChange) },
                label = "Mode of Payment *",
            )
            if (formState.paymentModeError != null) {
                Text(
                    text = formState.paymentModeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TrackOneTextField(
                    value = formState.date?.let { DateUtils.formatDate(it) } ?: "",
                    onValueChange = {},
                    label = "Date *",
                    leadingIcon = Icons.Default.CalendarMonth,
                    readOnly = true,
                    isError = formState.dateError != null,
                    errorMessage = formState.dateError,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("Pick") }
            }
        }

        // Notes
        FormCard(title = "Notes") {
            TrackOneTextField(
                value = formState.notes,
                onValueChange = onNotesChange,
                label = "Notes (Optional)",
                placeholder = "Add notes here...",
                leadingIcon = Icons.Default.Description,
                singleLine = false,
                maxLines = 3,
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isSaving,
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = if (formState.isSaving) "Saving..." else if (formState.isEditing) "Update" else "Save",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showDatePicker) {
        TrackOneDatePicker(
            onDateSelected = { onDateChange(it) },
            onDismiss = { showDatePicker = false },
            initialDate = formState.date,
            title = "Select Date",
        )
    }
}

@Composable
private fun FormCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
