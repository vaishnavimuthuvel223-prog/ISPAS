package com.ispas.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ispas.dao.CustomerDao;
import com.ispas.dao.PlanDao;
import com.ispas.dao.UsageDao;
import com.ispas.model.Customer;
import com.ispas.model.Plan;
import com.ispas.model.UsageRecord;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generate professional PDF bills for customers using iText5.
 */
public class BillPdfService {
    private static final CustomerDao customerDao = new CustomerDao();
    private static final PlanDao planDao = new PlanDao();
    private static final UsageDao usageDao = new UsageDao();

    /**
     * Generate a PDF bill for a customer and return as byte array.
     * @param customerId Customer ID
     * @param billAmount Total bill amount in dollars
     * @return PDF bytes or null on error
     */
    public static byte[] generateBillPdf(int customerId, double billAmount) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Get customer and plan info
            Customer customer = customerDao.findById(customerId);
            if (customer == null) {
                System.err.println("Customer not found: " + customerId);
                return null;
            }

            Plan plan = customer.getPlanId() != null ? planDao.findById(customer.getPlanId()) : null;
            List<UsageRecord> usage = usageDao.listByCustomer(customerId);

            // Header
            Paragraph title = new Paragraph("ISP Automation System - Bill Invoice");
            title.setAlignment(Element.ALIGN_CENTER);
            title.getFont().setSize(18);
            title.getFont().setStyle(Font.BOLD);
            document.add(title);

            document.add(new Paragraph(" "));

            Paragraph dateP = new Paragraph("Invoice Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            dateP.setAlignment(Element.ALIGN_RIGHT);
            dateP.getFont().setSize(10);
            document.add(dateP);

            document.add(new Paragraph(" "));

            // Customer info
            Paragraph customerInfo = new Paragraph();
            customerInfo.add("Bill To:\n");
            customerInfo.add("Name: " + customer.getName() + "\n");
            customerInfo.add("Email: " + customer.getEmail() + "\n");
            customerInfo.add("Phone: " + customer.getPhone() + "\n");
            customerInfo.add("Customer ID: " + customerId + "\n");
            document.add(customerInfo);

            document.add(new Paragraph(" "));

            // Plan and charges
            Paragraph planSection = new Paragraph("Service Plan & Charges");
            planSection.getFont().setStyle(Font.BOLD);
            planSection.getFont().setSize(12);
            document.add(planSection);

            // Charges table
            PdfPTable chargesTable = new PdfPTable(2);
            chargesTable.setWidthPercentage(100);
            chargesTable.addCell("Description");
            chargesTable.addCell("Amount");

            double planFee = plan != null ? plan.getMonthlyFee() : 0.0;
            chargesTable.addCell("Monthly Plan Fee (" + (plan != null ? plan.getName() : "None") + ")");
            chargesTable.addCell("$" + String.format("%.2f", planFee));

            double totalUsage = usage.stream().mapToDouble(u -> u.getMbUsed()).sum();
            double usageRate = plan != null ? plan.getRatePerMb() : 0.0;
            double usageCharge = totalUsage * usageRate;

            chargesTable.addCell("Data Usage (" + String.format("%.1f", totalUsage) + " MB @ $" + usageRate + "/MB)");
            chargesTable.addCell("$" + String.format("%.2f", usageCharge));

            chargesTable.addCell("Total Amount Due");
            chargesTable.addCell("$" + String.format("%.2f", billAmount));

            document.add(chargesTable);
            document.add(new Paragraph(" "));

            // Usage breakdown
            if (!usage.isEmpty()) {
                Paragraph usageSection = new Paragraph("Recent Usage Details");
                usageSection.getFont().setStyle(Font.BOLD);
                usageSection.getFont().setSize(12);
                document.add(usageSection);

                PdfPTable usageTable = new PdfPTable(3);
                usageTable.setWidthPercentage(100);
                usageTable.addCell("Device");
                usageTable.addCell("Date & Time");
                usageTable.addCell("MB Used");

                // Show last 10 usage records
                usage.stream().skip(Math.max(0, usage.size() - 10)).forEach(u -> {
                    usageTable.addCell(u.getDeviceName());
                    usageTable.addCell(u.getDateTime());
                    usageTable.addCell(String.format("%.1f", u.getMbUsed()));
                });

                document.add(usageTable);
                document.add(new Paragraph(" "));
            }

            // Footer
            Paragraph footer = new Paragraph(
                    "Thank you for using ISP Automation System!\n" +
                    "Please make payment by the due date to avoid service suspension.\n" +
                    "For support, contact: support@ispas.local");
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.getFont().setSize(9);
            footer.getFont().setStyle(Font.ITALIC);
            document.add(footer);

            document.close();
            byte[] pdfBytes = baos.toByteArray();
            System.out.println("Bill PDF generated for customer " + customerId + " (" + pdfBytes.length + " bytes)");
            return pdfBytes;
        } catch (SQLException e) {
            System.err.println("Error generating bill PDF: " + e.getMessage());
            return null;
        } catch (DocumentException e) {
            System.err.println("Error creating PDF document: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error creating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
