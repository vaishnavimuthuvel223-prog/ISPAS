package com.ispas.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Stripe payment processor for ISP billing.
 * Requires STRIPE_SECRET_KEY environment variable.
 * Uses test mode keys by default (no real charges).
 */
public class StripePaymentService {

    public static void init() {
        String apiKey = System.getenv("STRIPE_SECRET_KEY");
        if (apiKey == null) {
            System.out.println("[DEMO] Stripe payment disabled - set STRIPE_SECRET_KEY environment variable");
            System.out.println("[DEMO] Get test keys from https://dashboard.stripe.com/test/apikeys");
            return;
        }
        Stripe.apiKey = apiKey;
        System.out.println("Stripe payment service initialized");
    }

    /**
     * Create a Stripe Checkout session for paying a bill.
     * Returns the session ID (client should redirect to session.url).
     * @param customerId ISP customer ID (for reference)
     * @param amountCents amount in cents (e.g., 20525 = $205.25)
     * @param customerEmail email of the customer
     * @return session ID or null if Stripe is not configured
     */
    public static String createCheckoutSession(int customerId, long amountCents, String customerEmail) {
        String apiKey = System.getenv("STRIPE_SECRET_KEY");
        if (apiKey == null) {
            System.out.println("[DEMO] Would create Stripe checkout for customer " + customerId + ", amount: $" + (amountCents / 100.0));
            return "demo-session-" + customerId;
        }

        try {
            Stripe.apiKey = apiKey;

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:4567?payment=success&customerId=" + customerId)
                    .setCancelUrl("http://localhost:4567?payment=cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amountCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("ISP Bill - Customer #" + customerId)
                                                                    .setDescription("Internet service provider bill")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .setCustomerEmail(customerEmail)
                    .putMetadata("customerId", String.valueOf(customerId))
                    .build();

            Session session = Session.create(params);
            System.out.println("Stripe Checkout session created: " + session.getId());
            return session.getId();
        } catch (StripeException e) {
            System.err.println("Stripe error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve session details (for verifying payment status).
     * @param sessionId Stripe session ID
     * @return Session object or null on error
     */
    public static Session getSession(String sessionId) {
        String apiKey = System.getenv("STRIPE_SECRET_KEY");
        if (apiKey == null) {
            System.out.println("[DEMO] Would retrieve session: " + sessionId);
            return null;
        }

        try {
            Stripe.apiKey = apiKey;
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            System.err.println("Stripe error retrieving session: " + e.getMessage());
            return null;
        }
    }
}
