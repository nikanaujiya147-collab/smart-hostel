# Haven — Smart Hostel Management Frontend

A React + Vite frontend for the Smart Hostel Management System backend supplied with this project. It uses the backend REST API and the simplified `X-User-Id` authentication contract, including the added student-registration and Razorpay TEST-payment endpoints.

## Requirements

- Node.js 18 or newer (Node.js 20+ recommended)
- npm
- The Spring Boot backend running at `http://localhost:8080`

## Run in VS Code (Windows)

1. Extract this frontend folder alongside `smart-hostel-backend`.
2. Open the `smart-hostel-frontend` folder in VS Code.
3. Open **Terminal → New Terminal**. PowerShell is fine.
4. Install frontend packages:

   ```powershell
   npm install
   ```

5. Start the frontend:

   ```powershell
   npm run dev
   ```

6. Open the local address printed by Vite, normally `http://localhost:5173`.
7. Make sure the backend is running in a second terminal:

   ```powershell
   cd ..\smart-hostel-backend
   mvn spring-boot:run
   ```

The Vite development server proxies `/api` requests to `http://localhost:8080`, so the browser frontend and backend work together without changing backend CORS settings.

## Demo logins

All demo accounts use password `pass`:

| Username | Role |
|---|---|
| `warden1` | Warden |
| `student1` | Student |
| `student2` | Student |
| `staff1` | Maintenance staff |
| `acc1` | Accountant |

Choose a demo role on the login screen to fill the username automatically. The app then calls `POST /api/auth/login`, stores the returned `userId` for the browser session, and sends it as the `X-User-Id` header on authenticated requests.

## Connected modules

- **Rooms:** room inventory and active allocations; Warden can allocate or change a room; Student can view their own allocation.
- **Complaints:** Student can submit and track complaints; Warden can assign complaints to maintenance staff; Maintenance staff can start and resolve assigned complaints.
- **Visitors:** Warden can register, verify and check out visitors; Student can view their visitor records.
- **Fees:** Accountant can create fee records and record payments; Student can view personal fee balances. Payment receipts can be viewed and printed.
- **Notices:** Warden can publish notices; all logged-in roles can view active notices.
- **Mess:** Warden can publish a daily menu; Student can submit feedback and save meal preferences.
- **People and notifications:** Warden can view the student and maintenance staff directories; complaint notifications are displayed for logged-in users.

## Important project note

The backend's authentication is intentionally simplified for the OOM demonstration: the app sends the user ID in `X-User-Id`. This is not production-grade authentication. Do not expose this demo backend publicly or use real personal data/passwords.

## Build for production

```powershell
npm run build
```

The static site is written to `dist/`. The included `/api` proxy is for local development only; a deployed frontend needs an API base URL and appropriate backend CORS/deployment configuration.


## Added capabilities: student registration, welcome email and Razorpay TEST payments

### Environment variables (backend)

Set these in the shell before starting Spring Boot. Leave the mail variables unset to disable welcome-email delivery; leave Razorpay keys unset to disable online payments. No credentials are stored in source code.

| Variable | Purpose | Default / example |
|---|---|---|
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port with STARTTLS | `587` |
| `MAIL_USERNAME` | Gmail address used to send mail | your Gmail address; unset disables email |
| `MAIL_PASSWORD` | Gmail app password | supplied through environment only |
| `MAIL_FROM` | Sender address | defaults to `MAIL_USERNAME` |
| `RAZORPAY_KEY_ID` | Razorpay TEST public key | `rzp_test_...` |
| `RAZORPAY_KEY_SECRET` | Razorpay TEST secret (server only) | supplied through environment only |

Example (macOS/Linux):

```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-address@gmail.com
export MAIL_PASSWORD='your-16-character-app-password'
export MAIL_FROM=your-address@gmail.com
export RAZORPAY_KEY_ID=rzp_test_your_key_id
export RAZORPAY_KEY_SECRET=your_test_key_secret
```

### Gmail app password

1. Enable 2-Step Verification on the Google Account.
2. Open Google Account security settings and create an **App password** for the mail sender.
3. Put the generated app password in `MAIL_PASSWORD`; do not use your normal Gmail password or commit the value. Google may hide the App passwords option for some account types or security configurations.

### Razorpay TEST setup

1. Create/sign in to a Razorpay account and open the Dashboard.
2. Switch to **Test Mode**, then open Account & Settings / API Keys and generate test keys.
3. Set the test Key ID and Key Secret as the two environment variables above. The secret stays on the backend.
4. The student selects **Pay online** for a pending fee. Razorpay Checkout opens in test mode; successful payment is verified server-side before a local fee payment and receipt are recorded.

Use the current official Razorpay test credentials shown in the Razorpay documentation/dashboard for your account. Common test-mode card values include card number `4111 1111 1111 1111`, any future expiry, any CVV, and any cardholder name; use the test UPI flow / `success@razorpay` when offered by the Checkout test simulator. Test instruments can change, so verify the current values in Razorpay's official Test Mode documentation before a demonstration. Do not use real cards or real money.

### New workflows

- `POST /api/auth/register` creates student-only accounts. Registration remains successful if SMTP is unavailable; email errors are logged. If `MAIL_USERNAME` is unset, the app logs `Email not configured`.
- `POST /api/fees/{feeId}/online/order` creates a Razorpay order using the server-side pending amount (or a validated partial amount).
- `POST /api/fees/{feeId}/online/verify` verifies the HMAC signature and records the payment through the existing fee service. Repeated payment IDs are not recorded twice.
- Existing accountant manual payments and receipt endpoints remain available.
