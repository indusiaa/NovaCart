package com.ecommerce.service;

import com.ecommerce.model.PaymentMethod;
import java.util.UUID;

/**
 * Multi-provider simulated payment processing gateway.
 */
public class PaymentGateway {

    public static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;

        public PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }

    public PaymentResult processPayment(PaymentMethod method, double amount, String paymentDetails) {
        if (amount <= 0.0) {
            return new PaymentResult(false, null, "Payment amount must be greater than zero.");
        }

        String txnId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        switch (method) {
            case CREDIT_CARD:
                if (paymentDetails != null && paymentDetails.contains("DECLINE")) {
                    return new PaymentResult(false, null, "Transaction declined by card issuer.");
                }
                return new PaymentResult(true, txnId, "Credit card payment of $" + String.format("%.2f", amount) + " approved.");

            case UPI:
                return new PaymentResult(true, txnId, "UPI payment verified successfully.");

            case PAYPAL:
                return new PaymentResult(true, txnId, "PayPal checkout authorized.");

            case CASH_ON_DELIVERY:
                return new PaymentResult(true, "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), 
                        "Order registered with Cash on Delivery.");

            default:
                return new PaymentResult(true, txnId, "Payment processed.");
        }
    }
}
