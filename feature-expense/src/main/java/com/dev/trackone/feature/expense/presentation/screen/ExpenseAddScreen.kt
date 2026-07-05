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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneDatePicker
import com.dev.trackone.core.ui.component.TrackOneDropdownField
import com.dev.trackone.core.ui.component.TrackOneTextField
import com.dev.trackone.core.ui.theme.Dimens
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
        if (formState.saveSuccess) {
            onSaveSuccess()
        }
    }

    val categories = if (formState.isIncome) {
        ExpenseCategory.getIncomeCategories()
    } else {
        ExpenseCategory.getExpenseCategories()
    }

    val categoryNames = categories.map { it.displayName }
    val selectedCategoryName = formState.category?.displayName ?: ""

    val paymentModeNames = PaymentMode.entries.map { it.displayName }
    val selectedPaymentModeName = formState.paymentMode?.displayName ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpacingLg)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
    ) {

        
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = !formState.isIncome,
                onClick = { onTypeChange(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Expense")
            }
            SegmentedButton(
                selected = formState.isIncome,
                onClick = { onTypeChange(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Income")
            }
        }
        
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

        TrackOneTextField(
            value = formState.title,
            onValueChange = onTitleChange,
            label = "Title *",
            placeholder = "e.g., Groceries, Salary",
            leadingIcon = Icons.Default.Title,
            isError = formState.titleError != null,
            errorMessage = formState.titleError,
        )



        // Mode of Payment dropdown
        TrackOneDropdownField(
            selectedValue = selectedPaymentModeName,
            options = paymentModeNames,
            onOptionSelected = { name ->
                val mode = PaymentMode.entries.find { it.displayName == name }
                mode?.let { onPaymentModeChange(it) }
            },
            label = "Mode of Payment *",
        )

        if (formState.paymentModeError != null) {
            Text(
                text = formState.paymentModeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = Dimens.SpacingMd)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
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
                modifier = Modifier.padding(top = Dimens.SpacingSm),
            ) {
                Text("Pick Date")
            }
        }

        TrackOneTextField(
            value = formState.notes,
            onValueChange = onNotesChange,
            label = "Notes (Optional)",
            placeholder = "Add notes here...",
            leadingIcon = Icons.Default.Description,
            singleLine = false,
            maxLines = 3,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingLg))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isSaving,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = if (formState.isSaving) "Saving..." else if (formState.isEditing) "Update" else "Save",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        
        Spacer(modifier = Modifier.height(100.dp))
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
