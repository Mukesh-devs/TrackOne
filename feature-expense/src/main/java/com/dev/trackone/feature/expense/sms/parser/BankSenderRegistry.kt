package com.dev.trackone.feature.expense.sms.parser

/**
 * The only place that decides which SMS senders count as banks.
 *
 * Indian bank alerts arrive from DLT headers shaped `XX-BRAND-S`, e.g.
 * `VM-HDFCBK-T`: a two-letter operator/circle prefix, the registered brand,
 * and an optional category suffix. Only the BRAND part is matched here.
 *
 * THIS LIST IS A BEST-EFFORT STARTER. Verify it against the senders of real
 * bank alerts on your own phone and add any that are missing — one line per
 * brand, keyed by the exact header brand (upper case). The value is the name
 * shown when an SMS does not say who the money went to or came from.
 */
object BankSenderRegistry {

    private val BANKS: HashMap<String, String> = hashMapOf(
        "HDFCBK" to "HDFC Bank",
        "HDFCBN" to "HDFC Bank",
        "SBIINB" to "SBI",
        "SBIPSG" to "SBI",
        "SBIUPI" to "SBI",
        "SBICRD" to "SBI Card",
        "ICICIB" to "ICICI Bank",
        "ICICIT" to "ICICI Bank",
        "AXISBK" to "Axis Bank",
        "KOTAKB" to "Kotak Bank",
        "PNBSMS" to "PNB",
        "BOIIND" to "Bank of India",
        "CANBNK" to "Canara Bank",
        "UNIONB" to "Union Bank",
        "IDFCFB" to "IDFC First Bank",
        "YESBNK" to "Yes Bank",
        "INDUSB" to "IndusInd Bank",
        "FEDBNK" to "Federal Bank",
        "BOBTXN" to "Bank of Baroda",
        "BARODA" to "Bank of Baroda",
        "IDBIBK" to "IDBI Bank",
        "AUBANK" to "AU Bank",
        "RBLBNK" to "RBL Bank",
        "DBSBNK" to "DBS Bank",
    )

    /**
     * `XX-BRAND` with an optional `-T` (transactional), `-S` (service),
     * `-P` (promotional) or `-G` (government) suffix.
     */
    private val DLT_HEADER = Regex("^[A-Z]{2}-([A-Z0-9]{3,10})(?:-([STPG]))?$")

    /** Promotional and government headers never carry a bank transaction. */
    private val REJECTED_SUFFIXES = setOf("P", "G")

    /**
     * The bank brand behind [sender], or null if it is not a known bank's
     * transactional or service header.
     *
     * Phone numbers are rejected outright: real bank alerts come from
     * alphanumeric headers, and a number texting "Rs 5000 debited" is far more
     * likely to be a scam than a bank.
     */
    fun bankBrand(sender: String): String? {
        val normalized = sender.trim().uppercase()
        if (normalized.isEmpty() || normalized.startsWith("+") || normalized.all { it.isDigit() }) {
            return null
        }
        val match = DLT_HEADER.matchEntire(normalized) ?: return null
        val suffix = match.groupValues[2]
        if (suffix in REJECTED_SUFFIXES) return null
        val brand = match.groupValues[1]
        return brand.takeIf { it in BANKS }
    }

    fun isBankSender(sender: String): Boolean = bankBrand(sender) != null

    /** Human-readable bank name for a brand returned by [bankBrand]. */
    fun displayName(brand: String): String = BANKS[brand] ?: brand
}
