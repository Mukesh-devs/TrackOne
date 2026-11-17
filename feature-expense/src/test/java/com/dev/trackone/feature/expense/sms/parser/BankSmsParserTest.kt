package com.dev.trackone.feature.expense.sms.parser

import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every message here is invented. Formats follow real bank alerts, but all
 * account numbers, references, names and phone numbers are made up.
 */
class BankSmsParserTest {

    private val now = 1_763_400_000_000L

    private fun parse(sender: String, body: String) = BankSmsParser.parse(sender, body, now)

    private fun parsed(sender: String, body: String): ParsedTransaction {
        val txn = parse(sender, body)
        assertNotNull("expected a transaction for: $body", txn)
        return txn!!
    }

    // ------------------------------------------------------------ debit formats

    @Test
    fun upiDebitToPerson() {
        val txn = parsed(
            "VM-HDFCBK-T",
            "Rs.250.00 debited from a/c XX1234 on 17-11-26 to RAMESH KUMAR (UPI Ref 123456789012). " +
                "Not you? Call 1800000000",
        )
        assertEquals(25_000L, txn.amountPaise)
        assertEquals("Ramesh Kumar", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals(now, txn.occurredAt)
    }

    @Test
    fun debitAtMerchant() {
        val txn = parsed("AX-SBIINB-S", "Your a/c no. XXXX1234 is debited by Rs 1,499.00 at AMAZON PAY INDIA")
        assertEquals(149_900L, txn.amountPaise)
        assertEquals("Amazon Pay India", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
    }

    @Test
    fun debitWherePayeeIsCreditedStaysDebit() {
        val txn = parsed(
            "VM-ICICIB-T",
            "ICICI Bank Acct XX280 debited for Rs 598.10 on 17-Nov-26; Dominos Pizza credited. UPI:000000000000",
        )
        assertEquals(59_810L, txn.amountPaise)
        assertEquals("Dominos Pizza", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
    }

    @Test
    fun pnbDebitIgnoresTrailingBalance() {
        val txn = parsed(
            "BZ-PNBSMS-S",
            "A/c X0298 debited INR 80.00 Dt 17-11-26 21:25:48 to VARISH thru UPI:000000000000.Bal INR 165.00",
        )
        assertEquals(8_000L, txn.amountPaise)
        assertEquals("Varish", txn.name)
    }

    @Test
    fun pnbDebitWithFraudWarningTail() {
        val txn = parsed(
            "BZ-PNBSMS-S",
            "A/c X1111 debited INR 179.19 Dt 16-11-26 20:35:24 to Zomato thru UPI:000000000000.Bal INR 0.57 " +
                "Not u?Fwd this SMS to 0000000000 to block UPI.-PNB",
        )
        assertEquals(17_919L, txn.amountPaise)
        assertEquals("Zomato", txn.name)
    }

    @Test
    fun cardSpend() {
        val txn = parsed("VM-HDFCBK-T", "Rs 3,200 spent on HDFC Bank Card x4321 at SWIGGY on 17-11-26")
        assertEquals(320_000L, txn.amountPaise)
        assertEquals("Swiggy", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
    }

    @Test
    fun cardSpendWithPaddedMerchantAndBalance() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "Spent Rs.100 From HDFC Bank Card x0000 At ..SRI GANESH STORES_ On 2026-11-17:10:04:36 " +
                "Bal Rs.46.83 Not You? Call 18000000000/SMS BLOCK DC 0000 to 0000000000",
        )
        assertEquals(10_000L, txn.amountPaise)
        assertEquals("Sri Ganesh Stores", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
    }

    @Test
    fun cardSpendWhereWarningSaysTo() {
        val txn = parsed(
            "JD-HDFCBK-S",
            "Spent Rs.68 On HDFC Bank Card 0000 At Swiggy On 2026-10-03:22:20:28.Not You? " +
                "To Block+Reissue Call 18000000000/SMS BLOCK CC 0000 to 0000000000",
        )
        assertEquals(6_800L, txn.amountPaise)
        assertEquals("Swiggy", txn.name)
    }

    @Test
    fun multiLineSentToVpa() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "Sent Rs.85.00\nFrom HDFC Bank A/C *0000\nTo ravikumar@okbank\nOn 31/10/26\nRef 000000000000\n" +
                "Not You?\nCall 18000000000/SMS BLOCK UPI to 0000000000",
        )
        assertEquals(8_500L, txn.amountPaise)
        assertEquals("Ravikumar", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
    }

    @Test
    fun multiLineTxnWithMaskedVpaFallsBackToBank() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "Txn Rs.40.00\nOn HDFC Bank Card 0000\nAt *ab1c@pty \nby UPI 0000000000000\nOn 11-11\n" +
                "Not You?\nCall 18000000000/SMS BLOCK CC 0000 to 0000000000",
        )
        assertEquals(4_000L, txn.amountPaise)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals("HDFC Bank", txn.name)
    }

    @Test
    fun transferToMaskedAccountFallsBackToBank() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "HDFC Bank:Rs. 154.00 debited from a/c *0000 on 13/11/26 to a/c **1111 (UPI Ref No. 000000000000). " +
                "Not you? Call on 18000000000 to report",
        )
        assertEquals(15_400L, txn.amountPaise)
        assertEquals("HDFC Bank", txn.name)
    }

    @Test
    fun debitWithNoPayeeAndBalanceWithoutSpace() {
        val txn = parsed(
            "AD-DBSBNK-S",
            "Dear Customer, Your account no ************0000 is debited with INR 480 on 22-05-2026. " +
                "Current Balance is INR22.8.",
        )
        assertEquals(48_000L, txn.amountPaise)
        assertEquals("DBS Bank", txn.name)
        assertEquals(TransactionType.DEBIT, txn.type)
    }

    @Test
    fun debitToVpaUsesLocalPart() {
        val txn = parsed("VM-AXISBK-S", "INR 120.00 debited from A/c no. XX1234 to VPA shopowner@okbank on 17-11-26")
        assertEquals("Shopowner", txn.name)
    }

    @Test
    fun debitWithUpiInfoSegments() {
        val txn = parsed(
            "VM-SBIUPI-S",
            "Your A/c XX1234 debited by Rs 75.00 on 17Nov26. Info: UPI/P2A/000000000000/MEERA NAIR/SBI",
        )
        assertEquals(7_500L, txn.amountPaise)
        assertEquals("Meera Nair", txn.name)
    }

    @Test
    fun balanceBeforeDebitAmountIsSkipped() {
        val txn = parsed(
            "VM-HDFCBK-T",
            "Avl Bal Rs 12,000.00. Rs 250.00 debited from a/c XX1234 to TEST SHOP",
        )
        assertEquals(25_000L, txn.amountPaise)
        assertEquals("Test Shop", txn.name)
    }

    @Test
    fun balanceHintDoesNotLeakIntoNextAmount() {
        val txn = parsed("VM-HDFCBK-T", "Bal: Rs 5.00 Rs 250 debited from a/c XX1234 to TEST SHOP")
        assertEquals(25_000L, txn.amountPaise)
    }

    // ----------------------------------------------------------- credit formats

    @Test
    fun upiCreditFromPerson() {
        val txn = parsed("VM-HDFCBK-T", "Rs 5000 credited to a/c XX1234 by UPI from ANITA SHARMA")
        assertEquals(500_000L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("Anita Sharma", txn.name)
    }

    @Test
    fun salaryCreditWithoutPerson() {
        val txn = parsed("VM-HDFCBK-T", "Rs 12,000 credited to a/c XX1234 towards SALARY")
        assertEquals(1_200_000L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("Salary", txn.name)
    }

    @Test
    fun refundIsCredit() {
        val txn = parsed("VM-HDFCBK-T", "Rs 299 refund credited to a/c XX1234 from AMAZON")
        assertEquals(29_900L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("Amazon", txn.name)
    }

    @Test
    fun refundMentioningPurchaseIsStillCredit() {
        val txn = parsed("VM-ICICIB-T", "Refund of Rs 499.00 for your purchase at MYNTRA has been credited to Acct XX280")
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals(49_900L, txn.amountPaise)
    }

    @Test
    fun pnbCreditByPerson() {
        val txn = parsed(
            "BZ-PNBSMS-S",
            "A/c X0000 credited for INR 170.00 on 16-11-26 20:34:42 by ravi thru UPI.AvlBal INR 179.76" +
                "(UPI:000000000000).-PNB",
        )
        assertEquals(17_000L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("Ravi", txn.name)
    }

    @Test
    fun creditFromMaskedVpaFallsBackToBank() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "Credit Alert!\nRs.600.00 credited to HDFC Bank A/c XX0000 on 15-11-26 from VPA *12-3@okbank (UPI 00000000)",
        )
        assertEquals(60_000L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("HDFC Bank", txn.name)
    }

    @Test
    fun cardPaymentReceivedIgnoresAvailableLimit() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "DEAR HDFCBANK CARDMEMBER, PAYMENT OF Rs. 2052.00 RECEIVED TOWARDS YOUR CREDIT CARD ENDING WITH 0000 " +
                "ON 26-10-2026.YOUR AVAILABLE LIMIT IS RS. 10223.95",
        )
        assertEquals(205_200L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("Credit Card", txn.name)
    }

    @Test
    fun cardPaymentCreditedWithoutPayer() {
        val txn = parsed(
            "VM-HDFCBK-S",
            "HDFC Bank Cardmember, Online Payment of Rs.4376 vide Ref# 000AbcdeFG was credited to your card " +
                "ending 0000 On 01/NOV/2026 value Date 01/NOV/2026",
        )
        assertEquals(437_600L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("HDFC Bank", txn.name)
    }

    @Test
    fun creditWithNoPayer() {
        val txn = parsed(
            "AD-DBSBNK-S",
            "Dear Customer, your DBS account no XXXXXXXXXXXX0000 is credited with INR 70.00 on 17-Sep-2026 " +
                "and is subject to clearance. Current Balance is INR 65.13.",
        )
        assertEquals(7_000L, txn.amountPaise)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("DBS Bank", txn.name)
    }

    // ------------------------------------------------------------- type logic

    @Test
    fun classifyPrefersDebitKeyword() {
        assertEquals(TransactionType.DEBIT, BankSmsParser.classify("debited for Rs 598; Dominos credited"))
        assertEquals(TransactionType.DEBIT, BankSmsParser.classify("Rs 50 paid to X"))
        assertEquals(TransactionType.DEBIT, BankSmsParser.classify("Rs 50 withdrawn at ATM"))
    }

    @Test
    fun classifyCreditKeywords() {
        assertEquals(TransactionType.CREDIT, BankSmsParser.classify("Rs 50 credited to a/c"))
        assertEquals(TransactionType.CREDIT, BankSmsParser.classify("Rs 50 received from X"))
        assertEquals(TransactionType.CREDIT, BankSmsParser.classify("Rs 50 deposited in a/c"))
        assertEquals(TransactionType.CREDIT, BankSmsParser.classify("Rs 50 sent by X, credited to your a/c"))
    }

    @Test
    fun classifyRefundWinsOverPurchase() {
        assertEquals(TransactionType.CREDIT, BankSmsParser.classify("Refund for purchase at X"))
    }

    @Test
    fun classifyWeakTxnOnlyWithoutOtherKeywords() {
        assertEquals(TransactionType.DEBIT, BankSmsParser.classify("Txn Rs.40 On Card 0000"))
        assertEquals(TransactionType.CREDIT, BankSmsParser.classify("Txn Rs.40 credited"))
        assertNull(BankSmsParser.classify("Your balance is Rs 40"))
    }

    // ------------------------------------------------------------- rejections

    @Test
    fun otpIsIgnored() {
        assertNull(parse("VM-HDFCBK-T", "123456 is your OTP. Do not share. Rs 500 payment to TEST SHOP"))
    }

    @Test
    fun promotionalHeaderIsIgnored() {
        assertNull(parse("VM-HDFCBK-P", "Rs.250.00 debited from a/c XX1234 to TEST SHOP"))
    }

    @Test
    fun governmentHeaderIsIgnored() {
        assertNull(parse("VM-HDFCBK-G", "Rs.250.00 debited from a/c XX1234 to TEST SHOP"))
    }

    @Test
    fun phoneNumberSenderIsIgnored() {
        assertNull(parse("+910000000000", "Rs.5000.00 debited from a/c XX1234 to TEST SHOP"))
        assertNull(parse("0000000000", "Rs.5000.00 debited from a/c XX1234 to TEST SHOP"))
    }

    @Test
    fun unknownBrandIsIgnored() {
        assertNull(parse("VM-RANDOM-T", "Rs.250.00 debited from a/c XX1234 to TEST SHOP"))
    }

    @Test
    fun failedAndDeclinedAreIgnored() {
        assertNull(parse("VM-HDFCBK-T", "Txn of Rs 500 failed / declined"))
        assertNull(parse("VM-HDFCBK-T", "Your payment of Rs 500 to TEST SHOP was declined"))
    }

    @Test
    fun reversalsMandatesAndRequestsAreIgnored() {
        assertNull(parse("VM-HDFCBK-T", "Rs 500 debited earlier has been reversed to a/c XX1234"))
        assertNull(parse("VM-HDFCBK-T", "Reversal of Rs 500 credited to a/c XX1234"))
        assertNull(parse("VM-HDFCBK-T", "Rs 500 will be debited from a/c XX1234 on 20-11-26 for mandate"))
        assertNull(parse("VM-HDFCBK-T", "TEST SHOP has requested Rs 500 from you. Pay via UPI app"))
    }

    @Test
    fun promotionsAreIgnored() {
        assertNull(parse("VM-HDFCBK-S", "Get Rs 500 credited as cashback! Apply now at https://example.com"))
    }

    @Test
    fun balanceOnlyAlertIsIgnored() {
        assertNull(parse("VM-HDFCBK-S", "Avl Bal in A/c XX1234 is Rs 5,000.00 as on 17-11-26"))
        assertNull(parse("VM-HDFCBK-S", "Your last txn: Avl Bal Rs 5,000.00"))
    }

    @Test
    fun zeroAmountIsIgnored() {
        assertNull(parse("VM-HDFCBK-T", "Rs 0.00 debited from a/c XX1234 to TEST SHOP"))
    }

    @Test
    fun longOrGarbageTextIsIgnoredQuickly() {
        val long = "Rs 500 debited ".repeat(100)
        val start = System.nanoTime()
        assertNull(parse("VM-HDFCBK-T", long))
        assertNull(parse("VM-HDFCBK-T", "debited ".repeat(60).take(499)))
        assertNull(parse("VM-HDFCBK-T", "‮\u0000₹₹₹ debited ¯\\_(ツ)_/¯ ;;;;;;;; credited"))
        assertNull(parse("VM-HDFCBK-T", ""))
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        assertTrue("parser took ${elapsedMs}ms", elapsedMs < 500)
    }

    @Test
    fun invisibleCharactersDoNotBreakParsing() {
        val txn = parsed("VM-HDFCBK-T", "Rs.250.00 debited from a/c XX1234 to TEST​ SHOP")
        assertEquals(25_000L, txn.amountPaise)
        assertEquals("Test Shop", txn.name)
    }

    @Test
    fun longNamesAreCapped() {
        val txn = parsed("VM-HDFCBK-T", "Rs 10 debited from a/c XX1234 to ${"ABCDEFGHIJ ".repeat(7)}")
        assertTrue(txn.name.length <= 60)
    }

    // --------------------------------------------------------------- receiver gate

    @Test
    fun looksLikeTransactionGate() {
        assertTrue(BankSmsParser.looksLikeTransaction("Rs 10 debited"))
        assertTrue(BankSmsParser.looksLikeTransaction("Rs 10 credited"))
        assertFalse(BankSmsParser.looksLikeTransaction("Your parcel is out for delivery"))
        assertFalse(BankSmsParser.looksLikeTransaction("debited ".repeat(70)))
    }

    @Test
    fun toPaiseIsExact() {
        assertEquals(149_900L, BankSmsParser.toPaise("1,499.00"))
        assertEquals(2_280L, BankSmsParser.toPaise("22.8"))
        assertEquals(1L, BankSmsParser.toPaise("0.01"))
        assertNull(BankSmsParser.toPaise("0"))
        assertNull(BankSmsParser.toPaise("123456789012345"))
    }
}
