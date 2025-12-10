package com.ispas.service;

import com.ispas.dao.*;
import com.ispas.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ISPService {
    private final CustomerDao customerDao = new CustomerDao();
    private final PlanDao planDao = new PlanDao();
    private final UsageDao usageDao = new UsageDao();
    private final TicketDao ticketDao = new TicketDao();

    public Plan createPlan(String name, double monthlyFee, double ratePerMb) throws SQLException {
        Plan p = new Plan(name, monthlyFee, ratePerMb);
        return planDao.create(p);
    }

    public List<Plan> listPlans() throws SQLException { return planDao.listAll(); }

    public Customer registerCustomer(String name, String email, String phone) throws SQLException {
        Customer c = new Customer(name, email, phone);
        Customer created = customerDao.create(c);
        // Send welcome email asynchronously
        new Thread(() -> EmailService.sendRegistrationEmail(email, name, created.getId())).start();
        return created;
    }

    public List<Customer> listCustomers() throws SQLException { return customerDao.listAll(); }

    public void assignPlan(int customerId, int planId) throws SQLException { customerDao.assignPlan(customerId, planId); }

    public UsageRecord logUsage(int customerId, String deviceName, double mbUsed) throws SQLException {
        String dt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        UsageRecord u = new UsageRecord(customerId, deviceName, dt, mbUsed);
        return usageDao.create(u);
    }

    public List<UsageRecord> getUsageForCustomer(int customerId) throws SQLException { return usageDao.listByCustomer(customerId); }

    public Ticket raiseTicket(int customerId, String title, String description) throws SQLException {
        String dt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Ticket t = new Ticket(customerId, title, description, "OPEN", dt);
        return ticketDao.create(t);
    }

    public List<Ticket> getTicketsForCustomer(int customerId) throws SQLException { return ticketDao.listByCustomer(customerId); }

    public double generateBillForCustomer(int customerId) throws SQLException {
        Customer c = customerDao.findById(customerId);
        if (c == null) return 0.0;
        Plan p = null;
        if (c.getPlanId() != null) p = planDao.findById(c.getPlanId());
        double planFee = p != null ? p.getMonthlyFee() : 0.0;
        double rate = p != null ? p.getRatePerMb() : 0.0;
        List<UsageRecord> usage = usageDao.listByCustomer(customerId);
        double totalMb = usage.stream().mapToDouble(UsageRecord::getMbUsed).sum();
        return planFee + (totalMb * rate);
    }
}
