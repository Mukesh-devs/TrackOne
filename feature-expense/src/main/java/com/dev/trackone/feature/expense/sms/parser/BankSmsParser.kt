package com.dev.trackone.feature.expense.sms.parser

import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import java.math.BigDecimal

/** What a bank SMS is reduced to. Nothing else from the message is kept. */
data class ParsedTransaction(
    val name: String,
    val amountPaise: Long,
    val type: TransactionType,
    /** The moment the SMS was received — never a date read from the body. */
    val occurredAt: Long,
)

/**
 * Turns a bank SMS into a [ParsedTransaction], entirely on the device.
 *
 * Plain Kotlin and regex: no Android imports, no network, no ML. SMS text is
 * untrusted input, so the body is length-capped before any regex runs, every
 * pattern is bounded (no nested quantifiers), and all of them are compiled
 * once here rather than per message.
 */
object BankSmsParser {

    /** Longer bodies are not bank alerts; they are dropped before any regex runs. */
    const val MAX_BODY_LENGTH = 500

    private const val MAX_NAME_LENGTH = 60

    /** How far before an amount to look for "Bal", "Avl", "Limit"… */
    private const val BALANCE_LOOKBEHIND = 15

    /** ₹ 99,999,999,999 — anything longer is not a real alert. */
    private const val MAX_INTEGER_DIGITS = 11

    private val IGNORE_CASE = setOf(RegexOption.IGNORE_CASE)

    // ---------------------------------------------------------------- classification

    /** Money went out. "sent by" is excluded: that is someone paying the user. */
    private val DEBIT_KEYWORDS = Regex(
        """\b(?:debited|spent|paid|withdrawn|withdrawal|purchased?|sent(?!\s+by\b)|transferred\s+to)\b""",
        IGNORE_CASE,
    )

    /** Money came in. */
    private val CREDIT_KEYWORDS = Regex("""\b(?:credited|received|deposited|refund(?:ed)?)\b""", IGNORE_CASE)

    /** A refund is money coming back even when the text also names the original purchase. */
    private val REFUND = Regex("""\brefund(?:ed)?\b""", IGNORE_CASE)

    /**
     * Card and UPI alerts such as "Txn Rs.40.00 On HDFC Bank Card … At …" carry
     * no other keyword. Only consulted when no debit or credit keyword is present.
     */
    private val WEAK_DEBIT_KEYWORDS = Regex("""\btxn\b""", IGNORE_CASE)

    /** OTPs, future or requested debits, failures, reversals and promotions. */
    private val EXCLUDED = Regex(
        """\botp\b|do\s+not\s+share|will\s+be\s+(?:debited|credited)|request|mandate|""" +
            """\bfailed\b|\bdeclined\b|\brevers(?:ed|al)\b|http|\boffer|\bapply\b""",
        IGNORE_CASE,
    )

    // ------------------------------------------------------------------------ amount

    /** "Rs.250.00", "Rs 1,499.00", "INR22.8", "₹ 500". */
    private val AMOUNT = Regex("""(?<![a-z])(?:rs\.?|inr|₹)\s{0,2}(\d[\d,]*(?:\.\d{1,2})?)""", IGNORE_CASE)

    /** An amount right after one of these is a balance or limit, not the transaction. */
    private val BALANCE_HINT = Regex(
        """\b(?:avl|avbl|available|bal|balance|limit|lmt|outstanding)|(?:avl|cl|cur)bal""",
        IGNORE_CASE,
    )

    // -------------------------------------------------------------------------- name

    /** The fraud-warning tail ("Not you? Call … to 7308…") never names the payee. */
    private val NAME_CUTOFF = Regex("""\bnot\s+(?:you|u)\b""", IGNORE_CASE)

    // `.` never crosses a line break, so a capture stays on its own line.
    private val TO = Regex("""\b(?:trf\s+)?to\s+(?:vpa\s+)?(.{1,80})""", IGNORE_CASE)
    private val AT = Regex("""\bat\s+(.{1,80})""", IGNORE_CASE)
    private val PAYEE_CREDITED = Regex(""";\s*([^;\n]{1,60}?)\s+credited\b""", IGNORE_CASE)
    private val VPA = Regex("""\bvpa\s+(.{1,80})""", IGNORE_CASE)
    private val UPI_INFO = Regex("""\binfo:?\s*upi/([^\n]{1,100})""", IGNORE_CASE)
    private val FROM = Regex("""\b(?:received\s+)?from\s+(?:vpa\s+)?(.{1,80})""", IGNORE_CASE)
    private val BY = Regex("""\bby\s+(?:vpa\s+)?(.{1,80})""", IGNORE_CASE)

    /** Salary or interest credits: "towards SALARY", "for Interest". Never "for INR 170". */
    private val PURPOSE = Regex("""\b(?:towards|for)\s+(?!(?:rs\.?|inr|₹)\s{0,2}\d)(.{1,60})""", IGNORE_CASE)

    private val DEBIT_NAME_PATTERNS = listOf(TO, AT, PAYEE_CREDITED, VPA, UPI_INFO)
    private val CREDIT_NAME_PATTERNS = listOf(FROM, BY, VPA, UPI_INFO, PURPOSE)

    /** Where a captured name ends. Applied to " " + capture, so a leading stop word counts too. */
    private val NAME_STOP = Regex(
        """\s(?:on|ref|upi|thru|through|via|bal|avl|avlbal|not|ending|dt|date|value|info|imps|neft|""" +
            """rtgs|txn|is|was|has|credited|debited|towards|for|from|by|at|to|a/c|ac|acct|account|vpa|using)\b""" +
            """|[()\[\]{},;:|#*@!?₹]|\.(?=\s|$)|\s\d[\d\-/:.,]*(?=\s|$)|\s(?:rs\.?|inr)\s{0,2}\d""",
        IGNORE_CASE,
    )

    /** "a/c XX1234", "card ending 0824" — a masked account, not a person or merchant. */
    private val ACCOUNT_REFERENCE = Regex(
        """^\s*(?:your\s+)?(?:a/?c|acct|account|card)\b|^\s*(?:rs\.?|inr|₹)\s{0,2}\d""",
        IGNORE_CASE,
    )

    private val LEADING_YOUR = Regex("""^your\s+""", IGNORE_CASE)
    private val WHITESPACE = Regex("""\s+""")

    /** Codes that appear between the slashes of "Info: UPI/P2A/…/NAME". */
    private val UPI_INFO_CODES = setOf("p2a", "p2m", "p2p", "dr", "cr", "upi", "collect", "pay")

    private val FILLER_NAMES = setOf("upi", "vpa", "imps", "neft", "rtgs", "you", "your", "self", "card", "account")

    // ------------------------------------------------------------------------ public

    /**
     * Cheap pre-check for the SMS receiver: true when [body] is short enough and
     * mentions money moving. Says nothing about the sender — gate that first.
     */
    fun looksLikeTransaction(body: String): Boolean =
        body.length <= MAX_BODY_LENGTH &&
            (DEBIT_KEYWORDS.containsMatchIn(body) ||
                CREDIT_KEYWORDS.containsMatchIn(body) ||
                WEAK_DEBIT_KEYWORDS.containsMatchIn(body))

    /**
     * Returns the transaction in a bank credit or debit SMS, or null for
     * anything else: non-bank senders, OTPs, balance alerts, failed or reversed
     * payments, mandates, requests and promotions.
     */
    fun parse(sender: String, body: String, receivedAtMillis: Long): ParsedTransaction? {
        val brand = BankSenderRegistry.bankBrand(sender) ?: return null
        if (body.length > MAX_BODY_LENGTH) return null

        val text = sanitize(body)
        if (EXCLUDED.containsMatchIn(text)) return null
        val type = classify(text) ?: return null
        val amountPaise = extractAmountPaise(text) ?: return null
        val name = extractName(text, type) ?: BankSenderRegistry.displayName(brand)

        return ParsedTransaction(
            name = name,
            amountPaise = amountPaise,
            type = type,
            occurredAt = receivedAtMillis,
        )
    }

    /**
     * Credit or debit, from the account holder's point of view.
     *
     * A debit keyword wins over a credit one: "debited for Rs 598; Dominos
     * Pizza credited" names the *payee* as credited, so the user still paid.
     * The one exception is a refund, which is money coming back even when the
     * text also mentions the original purchase.
     */
    fun classify(text: String): TransactionType? = when {
        REFUND.containsMatchIn(text) -> TransactionType.CREDIT
        DEBIT_KEYWORDS.containsMatchIn(text) -> TransactionType.DEBIT
        CREDIT_KEYWORDS.containsMatchIn(text) -> TransactionType.CREDIT
        WEAK_DEBIT_KEYWORDS.containsMatchIn(text) -> TransactionType.DEBIT
        else -> null
    }

    // ----------------------------------------------------------------------- helpers

    /** Drops invisible/control characters (keeping line breaks) and normalises spaces. */
    private fun sanitize(body: String): String {
        val out = StringBuilder(body.length)
        for (c in body) {
            when {
                c == '\n' || c == '\r' -> out.append('\n')
                Character.isSpaceChar(c) || c == '\t' -> out.append(' ')
                Character.isISOControl(c) -> Unit
                Character.getType(c) == Character.FORMAT.toInt() -> Unit
                else -> out.append(c)
            }
        }
        return out.toString()
    }

    /**
     * First amount that is not a balance or limit. The look-behind window never
     * reaches past the previous amount, so "Bal Rs 5.00 Rs 250 debited" still
     * finds the 250.
     */
    private fun extractAmountPaise(text: String): Long? {
        var previousEnd = 0
        for (match in AMOUNT.findAll(text)) {
            val start = match.range.first
            val before = text.substring(maxOf(previousEnd, start - BALANCE_LOOKBEHIND), start)
            previousEnd = match.range.last + 1
            if (BALANCE_HINT.containsMatchIn(before)) continue
            return toPaise(match.groupValues[1]) ?: continue
        }
        return null
    }

    /** "1,499.00" → 149900. Exact decimal arithmetic; zero and oversized values are rejected. */
    internal fun toPaise(raw: String): Long? {
        val digits = raw.replace(",", "")
        if (digits.substringBefore('.').length > MAX_INTEGER_DIGITS) return null
        return try {
            BigDecimal(digits).movePointRight(2).longValueExact().takeIf { it > 0 }
        } catch (e: NumberFormatException) {
            null
        } catch (e: ArithmeticException) {
            null
        }
    }

    private fun extractName(text: String, type: TransactionType): String? {
        val searchable = NAME_CUTOFF.find(text)?.let { text.substring(0, it.range.first) } ?: text
        val patterns = if (type == TransactionType.DEBIT) DEBIT_NAME_PATTERNS else CREDIT_NAME_PATTERNS
        for (pattern in patterns) {
            for (match in pattern.findAll(searchable)) {
                val raw = match.groupValues[1]
                val name = if (pattern === UPI_INFO) nameFromUpiInfo(raw) else cleanName(raw)
                if (name != null) return name
            }
        }
        return null
    }

    /** "P2A/123456789012/RAMESH KUMAR/HDFC" → "Ramesh Kumar". */
    private fun nameFromUpiInfo(raw: String): String? = raw.split('/')
        .asSequence()
        .map { it.trim() }
        .filter { it.lowercase() !in UPI_INFO_CODES }
        .firstNotNullOfOrNull { cleanName(it) }

    private fun cleanName(raw: String): String? {
        if (ACCOUNT_REFERENCE.containsMatchIn(raw)) return null
        val padded = " $raw"
        val end = NAME_STOP.find(padded)?.range?.first ?: padded.length
        val name = padded.substring(0, end)
            .trim { !it.isLetterOrDigit() }
            .replace(LEADING_YOUR, "")
            .replace(WHITESPACE, " ")
        if (name.count { it.isLetter() } < 2) return null
        if (name.lowercase() in FILLER_NAMES) return null
        return titleCase(name).take(MAX_NAME_LENGTH).trim()
    }

    private fun titleCase(value: String): String = value.split(' ').joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.titlecase() }
    }
}
