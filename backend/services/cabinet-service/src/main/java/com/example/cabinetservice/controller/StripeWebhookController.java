package com.example.cabinetservice.controller;

import com.example.cabinetservice.service.CabinetService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final CabinetService cabinetService;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        if (endpointSecret == null || endpointSecret.isEmpty()) {
            log.error("Stripe webhook secret is not configured.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook secret not configured");
        }

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe signature.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error parsing Stripe event: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Handle the event
        if ("payment_intent.succeeded".equals(event.getType())) {
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if (stripeObject instanceof PaymentIntent) {
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                handlePaymentSucceeded(paymentIntent);
            }
        } else {
            log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Received");
    }

    private void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        log.info("Payment succeeded: {}", paymentIntent.getId());

        String abonnementIdStr = paymentIntent.getMetadata().get("abonnementId");

        if (abonnementIdStr != null) {
            try {
                Long abonnementId = Long.parseLong(abonnementIdStr);
                Double amount = paymentIntent.getAmount() / 100.0; // Stripe amount is in cents
                cabinetService.handleSuccessfulPayment(abonnementId, amount, paymentIntent.getId());
            } catch (NumberFormatException e) {
                log.error("Invalid abonnementId in metadata: {}", abonnementIdStr);
            }
        } else {
            log.warn("No abonnementId found in payment metadata.");
        }
    }
}
