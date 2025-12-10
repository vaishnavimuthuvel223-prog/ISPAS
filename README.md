# ISP Automation System (ISPAS)

A complete Internet Service Provider automation platform with customer registration, usage tracking, billing, payments, and PDF invoice generation.

## Features

✅ **Customer Management**
- Self-service registration with email verification
- Customer profiles with phone and email
- Support ticket system for issue tracking

✅ **Service Plans**
- Pre-configured plans: Basic ($199), Standard ($299), Premium ($499)
- Monthly fee + per-MB usage charges
- Easy plan switching

✅ **Usage Tracking**
- Log data usage by device name and timestamp
- View historical usage records
- Usage charges calculated automatically

✅ **Billing & Invoicing**
- Automatic bill calculation (plan fee + usage charges)
- Professional PDF invoices generated on-demand
- Download bill PDFs directly from the website

✅ **Payment Processing**
- Stripe Checkout integration (test mode ready)
- Demo mode for testing without real credentials
- Payment confirmation emails with PDF bills attached

✅ **Email Notifications**
- Welcome email on customer registration
- Bill and payment receipt emails with PDF attachments
- SMTP-based (Gmail recommended)
- Demo mode logs emails if credentials not configured

✅ **Modern Web UI**
- Clean, responsive dashboard
- Real-time customer list
- Easy-to-use forms for all operations
- Mobile-friendly design

## Tech Stack

- **Backend:** Java 17, Spark Web Framework
- **Database:** SQLite (embedded)
- **PDF Generation:** iText 5
- **Payments:** Stripe API (test mode)
- **Email:** JavaMail (SMTP/Gmail)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Build:** Maven

## Quick Start

### 1. Build the Project

```bash
cd /workspaces/ISPAS
mvn clean package
```

### 2. Run the Server

```bash
java -cp target/ispas-0.1.0-jar-with-dependencies.jar com.ispas.web.WebServer
```

Server starts on `http://localhost:4567`

### 3. Access the Web UI

Open `http://localhost:4567` in your browser and:
1. Register a customer
2. Assign a plan
3. Log usage data
4. Generate a bill
5. Download PDF or proceed to payment

## Demo Flow (No Credentials Required)

The system works **out-of-the-box in demo mode**:

- **Emails:** Logged to console (set `ISPAS_EMAIL_PASSWORD` to send real emails)
- **Payments:** Demo sessions created (set `STRIPE_SECRET_KEY` for real Stripe integration)
- **PDFs:** Generated and downloadable immediately

## Enable Real Features

### Email Notifications (Gmail)

```bash
# Get an App Password from https://myaccount.google.com/apppasswords
export ISPAS_SENDER_EMAIL="your-email@gmail.com"
export ISPAS_EMAIL_PASSWORD="your-16-char-app-password"

# Restart the server
java -cp target/ispas-0.1.0-jar-with-dependencies.jar com.ispas.web.WebServer
```

Now emails will be sent to customer inboxes with PDF invoices attached!

### Stripe Payments (Test Mode)

```bash
# Get test keys from https://dashboard.stripe.com/test/apikeys
export STRIPE_SECRET_KEY="sk_test_YOUR_TEST_KEY"

# Restart the server
java -cp target/ispas-0.1.0-jar-with-dependencies.jar com.ispas.web.WebServer
```

Now customers can pay with Stripe Checkout (no real charges in test mode).

## API Endpoints

### Customer Management
- `POST /api/customers` - Register new customer
- `GET /api/customers` - List all customers

### Plans
- `GET /api/plans` - List available plans

### Plan Assignment
- `POST /api/assign` - Assign plan to customer

### Usage Tracking
- `POST /api/usage` - Log usage for customer
- `GET /api/usage/:id` - Get usage history for customer

### Billing
- `GET /api/bill/:id` - Calculate bill for customer
- `GET /api/bill/pdf/:id` - Download PDF invoice
- `POST /api/payment` - Process payment (generates PDF + sends email)

### Support Tickets
- `POST /api/tickets` - Create support ticket

### Email Testing
- `POST /api/email/test` - Send test email (demo mode if no credentials)

## File Structure

```
src/main/java/com/ispas/
├── Main.java                      # CLI demo (legacy)
├── db/Database.java              # SQLite connection & schema
├── model/                        # Data models
│   ├── Customer.java
│   ├── Plan.java
│   ├── UsageRecord.java
│   └── Ticket.java
├── dao/                          # Data access objects
│   ├── CustomerDao.java
│   ├── PlanDao.java
│   ├── UsageDao.java
│   └── TicketDao.java
├── service/                      # Business logic
│   ├── ISPService.java          # Core ISP operations
│   ├── EmailService.java        # Email with PDF attachments
│   ├── BillPdfService.java      # PDF invoice generation
│   └── StripePaymentService.java # Stripe integration
└── web/WebServer.java            # Spark REST API & web server

src/main/resources/public/
├── index.html                    # Web UI
└── app.js                        # Frontend logic

data/ispas.db                     # SQLite database (auto-created)
```

## Database Schema

- **plans**: Plan name, monthly fee, rate per MB
- **customers**: Name, email, phone, assigned plan
- **usage_records**: Customer ID, device name, date/time, MB used
- **tickets**: Customer ID, issue title/description, status, created date

## Running Tests

Create a test customer end-to-end:

```bash
# 1. Register
curl -X POST http://localhost:4567/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@example.com","phone":"5551234567"}'

# 2. Assign plan (planId=1 is Basic)
curl -X POST http://localhost:4567/api/assign \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"planId":1}'

# 3. Log usage
curl -X POST http://localhost:4567/api/usage \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"deviceName":"iPhone","mbUsed":250}'

# 4. Generate bill
curl http://localhost:4567/api/bill/1

# 5. Download PDF
curl http://localhost:4567/api/bill/pdf/1 > invoice.pdf

# 6. Process payment (sends email with PDF)
curl -X POST http://localhost:4567/api/payment \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"amount":212.5}'
```

## Notes

- Database file `data/ispas.db` is created automatically on first run
- Remove the database file to reset to seeded plans (Basic, Standard, Premium)
- All email operations are asynchronous (non-blocking)
- PDFs are generated on-demand and never cached (always current)
- Stripe test mode doesn't charge real cards; use test card `4242 4242 4242 4242`

## Future Enhancements

- User authentication & session management
- Real Stripe webhook handling for payment confirmation
- Admin dashboard for viewing analytics
- Scheduled billing (automatic monthly invoicing)
- Service suspension for unpaid bills
- SMS notifications
- Advanced usage analytics & forecasts
- Mobile app (React Native)

## License

MIT
