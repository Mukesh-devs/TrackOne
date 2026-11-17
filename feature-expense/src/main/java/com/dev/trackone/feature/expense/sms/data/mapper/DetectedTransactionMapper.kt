package com.dev.trackone.feature.expense.sms.data.mapper

import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionEntity
import com.dev.trackone.feature.expense.sms.domain.model.DetectedTransaction

fun DetectedTransactionEntity.toDomain(): DetectedTransaction = DetectedTransaction(
    id = id,
    name = name,
    amountPaise = amountPaise,
    type = type,
    occurredAt = occurredAt,
)
