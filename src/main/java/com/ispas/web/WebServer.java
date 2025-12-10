package com.ispas.web;

import com.google.gson.Gson;
import com.ispas.model.Customer;
import com.ispas.service.ISPService;
import spark.Spark;

import java.sql.SQLException;
import java.util.Map;

public class WebServer {
    public static void main(String[] args) throws SQLException {
        // Initialize Stripe payment service
        com.ispas.service.StripePaymentService.init();

        ISPService svc = new ISPService();
        // seed plans if empty
        if (svc.listPlans().isEmpty()) {
            svc.createPlan("Basic", 199.0, 0.05);
            svc.createPlan("Standard", 299.0, 0.03);
            svc.createPlan("Premium", 499.0, 0.01);
        }

        Gson gson = new Gson();
        Spark.port(4567);
        Spark.staticFiles.location("/public");

        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        Spark.get("/api/plans", (req, res) -> {
            res.type("application/json");
            return gson.toJson(svc.listPlans());
        });

        Spark.get("/api/customers", (req, res) -> {
            res.type("application/json");
            return gson.toJson(svc.listCustomers());
        });

        Spark.post("/api/customers", (req, res) -> {
            Customer in = gson.fromJson(req.body(), Customer.class);
            var created = svc.registerCustomer(in.getName(), in.getEmail(), in.getPhone());
            res.type("application/json");
            return gson.toJson(created);
        });

        Spark.post("/api/assign", (req, res) -> {
            Map<?,?> m = gson.fromJson(req.body(), Map.class);
            int cid = ((Number) m.get("customerId")).intValue();
            int pid = ((Number) m.get("planId")).intValue();
            svc.assignPlan(cid, pid);
            return gson.toJson(Map.of("ok", true));
        });

        Spark.post("/api/usage", (req, res) -> {
            Map<?,?> m = gson.fromJson(req.body(), Map.class);
            int cid = ((Number) m.get("customerId")).intValue();
            String device = (String) m.get("deviceName");
            double mb = ((Number) m.get("mbUsed")).doubleValue();
            svc.logUsage(cid, device, mb);
            return gson.toJson(Map.of("ok", true));
        });

        // Return usage records for a customer
        Spark.get("/api/usage/:id", (req, res) -> {
            int cid = Integer.parseInt(req.params(":" + "id"));
            res.type("application/json");
            return gson.toJson(svc.getUsageForCustomer(cid));
        });

        Spark.post("/api/tickets", (req, res) -> {
            Map<?,?> m = gson.fromJson(req.body(), Map.class);
            int cid = ((Number) m.get("customerId")).intValue();
            String title = (String) m.get("title");
            String desc = (String) m.get("description");
            svc.raiseTicket(cid, title, desc);
            return gson.toJson(Map.of("ok", true));
        });

        Spark.get("/api/bill/:id", (req, res) -> {
            int cid = Integer.parseInt(req.params(":id"));
            double bill = svc.generateBillForCustomer(cid);
            res.type("application/json");
            return gson.toJson(Map.of("bill", bill));
        });

        // Payment endpoint
        Spark.post("/api/payment", (req, res) -> {
            Map<?,?> m = gson.fromJson(req.body(), Map.class);
            int cid = ((Number) m.get("customerId")).intValue();
            double amount = ((Number) m.get("amount")).doubleValue();
            
            var customer = svc.listCustomers().stream().filter(c -> c.getId() == cid).findFirst().orElse(null);
            if (customer == null) {
                res.status(404);
                return gson.toJson(Map.of("error", "Customer not found"));
            }
            
            // Send bill email
            new Thread(() -> com.ispas.service.EmailService.sendBillEmail(customer.getEmail(), customer.getName(), amount, cid)).start();
            
            res.type("application/json");
            return gson.toJson(Map.of("status", "Payment processed", "amount", amount, "customerId", cid, "message", "Receipt sent to email"));
        });

        // Test email endpoint - POST { "email": "you@domain.com", "name": "Optional Name" }
        Spark.post("/api/email/test", (req, res) -> {
            Map<?,?> m = gson.fromJson(req.body(), Map.class);
            String email = (String) m.get("email");
            Object nameObj = m.get("name");
            String name = nameObj != null ? nameObj.toString() : "User";
            if (email == null || email.isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("error", "email required"));
            }
            // Send a simple test email (async)
            new Thread(() -> com.ispas.service.EmailService.sendRegistrationEmail(email, name, -1)).start();
            res.type("application/json");
            return gson.toJson(Map.of("status", "ok", "message", "Test email queued (demo mode if no password)"));
        });

        // Stripe checkout endpoint - POST { "customerId": 1 }
        Spark.post("/api/checkout", (req, res) -> {
            Map<?,?> m = gson.fromJson(req.body(), Map.class);
            int cid = ((Number) m.get("customerId")).intValue();
            
            var customer = svc.listCustomers().stream().filter(c -> c.getId() == cid).findFirst().orElse(null);
            if (customer == null) {
                res.status(404);
                return gson.toJson(Map.of("error", "Customer not found"));
            }
            
            double bill = svc.generateBillForCustomer(cid);
            long amountCents = (long) (bill * 100);
            
            String sessionId = com.ispas.service.StripePaymentService.createCheckoutSession(cid, amountCents, customer.getEmail());
            res.type("application/json");
            return gson.toJson(Map.of("sessionId", sessionId, "amount", bill));
        });

        Spark.awaitInitialization();
        System.out.println("ISP Automation Web Server started on http://localhost:4567");
    }
}
