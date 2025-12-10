package com.ispas;

import com.ispas.service.ISPService;
import com.ispas.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {
        ISPService svc = new ISPService();
        seedPlansIfEmpty(svc);
        System.out.println("ISP Automation System (CLI demo)");
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                printMenu();
                String cmd = sc.nextLine().trim();
            try {
                if (cmd.equals("1")) {
                    System.out.print("Name: "); String name = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Phone: "); String phone = sc.nextLine();
                    var c = svc.registerCustomer(name,email,phone);
                    System.out.println("Registered customer id="+c.getId());
                } else if (cmd.equals("2")) {
                    var plans = svc.listPlans();
                    System.out.println("Available plans:");
                    for (Plan p: plans) System.out.println(p.getId()+": "+p.getName()+" - $"+p.getMonthlyFee()+" + $"+p.getRatePerMb()+"/MB");
                } else if (cmd.equals("3")) {
                    System.out.print("Customer id: "); int cid = Integer.parseInt(sc.nextLine());
                    System.out.print("Plan id: "); int pid = Integer.parseInt(sc.nextLine());
                    svc.assignPlan(cid,pid);
                    System.out.println("Assigned plan.");
                } else if (cmd.equals("4")) {
                    System.out.print("Customer id: "); int cid = Integer.parseInt(sc.nextLine());
                    System.out.print("Device name: "); String dev = sc.nextLine();
                    System.out.print("MB used: "); double mb = Double.parseDouble(sc.nextLine());
                    svc.logUsage(cid, dev, mb);
                    System.out.println("Logged usage.");
                } else if (cmd.equals("5")) {
                    System.out.print("Customer id: "); int cid = Integer.parseInt(sc.nextLine());
                    System.out.print("Title: "); String title = sc.nextLine();
                    System.out.print("Description: "); String desc = sc.nextLine();
                    svc.raiseTicket(cid, title, desc);
                    System.out.println("Ticket created.");
                } else if (cmd.equals("6")) {
                    System.out.print("Customer id: "); int cid = Integer.parseInt(sc.nextLine());
                    double bill = svc.generateBillForCustomer(cid);
                    System.out.println("Current bill: $"+String.format("%.2f", bill));
                } else if (cmd.equals("7")) {
                    System.out.println("Customers:");
                    List<com.ispas.model.Customer> customers = svc.listCustomers();
                    for (Customer c: customers) System.out.println(c.getId()+": "+c.getName()+" (plan="+c.getPlanId()+")");
                } else if (cmd.equals("0")) {
                    System.out.println("Bye"); break;
                } else {
                    System.out.println("Unknown option");
                }
            } catch (Exception e) {
                System.out.println("Error: "+e.getMessage());
            }
            }
        }
    }

    private static void seedPlansIfEmpty(ISPService svc) throws SQLException {
        var plans = svc.listPlans();
        if (plans.isEmpty()) {
            svc.createPlan("Basic", 10.0, 0.01);
            svc.createPlan("Standard", 25.0, 0.008);
            svc.createPlan("Premium", 50.0, 0.005);
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1) Register customer");
        System.out.println("2) List plans");
        System.out.println("3) Assign plan to customer");
        System.out.println("4) Log usage (hotspot/device)");
        System.out.println("5) Raise ticket");
        System.out.println("6) Generate bill for customer");
        System.out.println("7) List customers");
        System.out.println("0) Exit");
        System.out.print("> ");
    }
}
