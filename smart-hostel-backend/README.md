# Smart Hostel Management System (OOM Project, Java + Spring Boot)

Backend with 6 modules (Rooms, Complaints, Visitors, Fees, Notices, Mess) and 4 roles
(Student, Warden, Maintenance Staff, Accountant). Database: H2 (no installation needed).
Demo UI: Swagger UI.

## 1. Requirements
- JDK 17 or newer (`java -version`)
- Maven 3.8+ (`mvn -version`). IntelliJ IDEA / VS Code (Java extensions) can also open `pom.xml` and download Maven for you.

## 2. Run
```
cd smart-hostel-backend
mvn spring-boot:run
```
Then open:
- Swagger UI (demo): http://localhost:8080/swagger-ui.html
- H2 database console: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:file:./data/hosteldb`, user `sa`, empty password)

Run tests: `mvn test`. To reset all data: stop the app and delete the `data/` folder.

## 3. Demo accounts (created automatically, password `pass`)
| Username | Role | Use in Swagger |
|---|---|---|
| student1 | Student | first call `POST /api/auth/login` to get `userId` |
| student2 | Student | |
| warden1 | Warden | |
| staff1 | Maintenance staff | |
| acc1 | Accountant | |

**Simplified authentication:** login returns `userId`. Every other API takes it in the
`X-User-Id` header (Swagger shows a text box for it). The server checks that the user's class
(Student, Warden, ...) is allowed for that API, otherwise HTTP 403.
Typical ids on a fresh database: student1=1, student2=2, warden1=3, staff1=4, acc1=5.

## 4. Demo script (about 5 minutes)
1. `POST /api/auth/login` with `{"username":"warden1","password":"pass"}` shows role + dashboard (polymorphism).
2. **Rooms:** Warden `GET /api/rooms/available`, then `POST /api/rooms/allocate` `{"studentId":1,"roomId":1}`.
   Allocate students 1 and 2 to room 1 (capacity 2), then try a third: HTTP 409 `RoomFullException`.
   `PUT /api/rooms/change` `{"studentId":1,"newRoomId":3}` moves a student. Student: `GET /api/rooms/my`.
3. **Complaints (State + Strategy + Observer):**
   - Student (id 1): `POST /api/complaints` `{"category":"ELECTRICAL","description":"Sparks from socket","strategy":"KEYWORD"}` gives priority CRITICAL.
   - Warden: `PUT /api/complaints/1/assign` `{"staffId":4}`.
   - Staff (id 4): try `PUT /api/complaints/1/resolve` first: 409 invalid state transition. Then `/start`, then `/resolve`.
   - Student: `GET /api/notifications` shows messages created by the Observer. See `[AUDIT]` lines in the console (second observer).
4. **Visitors:** Warden `POST /api/visitors`, `PUT /api/visitors/1/verify`, `PUT /api/visitors/1/checkout`. Student: `GET /api/visitors/my`.
5. **Fees:** Accountant `POST /api/fees/1/payments` `{"amount":10000,"method":"UPI"}`: status becomes PARTIAL. Pay the rest: PAID.
   `GET /api/fees/payments/1/receipt`. Overpaying returns 409. Student: `GET /api/fees/my`.
6. **Notices:** Warden `POST /api/notices` with an expiry date; everyone `GET /api/notices` shows only active notices.
7. **Mess:** `GET /api/mess/menu`, student `POST /api/mess/feedback`, `PUT /api/mess/preferences`.

## 5. Where each OOM concept is implemented
| Concept | Where |
|---|---|
| Classes, objects, constructors | every class in `model/`; constructors validate input |
| Encapsulation | private fields everywhere; `Room.assignOccupant()` enforces capacity; `User` has no password getter; `FeeRecord` status is derived, never set directly |
| Abstraction | abstract `User`; interfaces `RoomService`, `ComplaintService`, `ComplaintState`, `PriorityStrategy`, `ComplaintObserver` |
| Inheritance | `Student`, `Warden`, `MaintenanceStaff`, `Accountant` extend `User`; `RoomFullException` and `InvalidStateTransitionException` extend `BusinessRuleException`; concrete states extend `AbstractComplaintState` |
| Polymorphism | `getRole()` / `getDashboardTitle()` per user class; `PriorityStrategy`, `ComplaintObserver` and `ComplaintState` implementations used through their interface |
| Association | `Complaint`-`Student`, `Complaint`-`MaintenanceStaff`, `FeeRecord`-`Student`, `Visitor`-`Student`, `RoomAllocation` between `Student` and `Room` |
| Aggregation | `Room` / `Student` (via `RoomAllocation`): a student exists independently of any room |
| Composition | `Hostel` owns `Room` (created only via `addRoom`); `Complaint` owns `StatusHistory`; `FeeRecord` owns `Payment` (all cascade + orphanRemoval) |
| Interfaces vs abstract classes | interfaces for contracts (services, strategy, observer, state); abstract classes `User` and `AbstractComplaintState` for shared code |
| Exception handling | `exception/` package + `GlobalExceptionHandler` mapping to 400/403/404/409 JSON |
| Modularity | packages: `model`, `repository`, `service`, `controller`, `pattern`, `exception`, `config` |

## 6. Design patterns (all on the Complaint workflow)
- **State** (`pattern/state`): `Complaint` delegates `assign/startWork/resolve` to `SubmittedState`, `AssignedState`,
  `InProgressState`, `ResolvedState`. Illegal transitions throw `InvalidStateTransitionException`.
  Flow: SUBMITTED -> ASSIGNED -> IN_PROGRESS -> RESOLVED (re-assign allowed in ASSIGNED).
- **Strategy** (`pattern/strategy`): `CategoryPriorityStrategy` and `KeywordPriorityStrategy` implement `PriorityStrategy`;
  chosen at submit time by name. New strategies need no change to existing code (Open/Closed).
- **Observer** (`pattern/observer`): after each status change `ComplaintServiceImpl` notifies every `ComplaintObserver`:
  `StudentNotificationObserver` (saves a notification) and `AuditLogObserver` (console log). Spring injects all observers.

## 7. SOLID
- **S**ingle Responsibility: controllers handle HTTP, services hold workflows, entities hold their own rules, repositories handle storage.
- **O**pen/Closed: add a strategy, observer or state without modifying the service.
- **L**iskov: any `User` subclass can be used where `User` is expected (`AuthService.require`, login).
- **I**nterface Segregation: small interfaces (`PriorityStrategy`, `ComplaintObserver`, `ComplaintState`).
- **D**ependency Inversion: controllers use `RoomService` / `ComplaintService` interfaces; Spring injects implementations.

## 8. Known simplifications (mention in viva / future scope)
- Login is intentionally simple (password check + `X-User-Id` header). Future scope: BCrypt + JWT + Spring Security.
- H2 file database for easy demo; switching to MySQL needs only the datasource properties.
- No frontend; Swagger UI is the client. A React UI can call the same REST APIs.

## 9. API summary
| Area | Endpoints |
|---|---|
| Auth/users | `POST /api/auth/login`, `GET /api/users/students`, `GET /api/users/staff` |
| Rooms | `GET /api/rooms`, `/available`, `/my`, `/allocations`; `POST /api/rooms/allocate`; `PUT /api/rooms/change` |
| Complaints | `POST /api/complaints`; `GET /api/complaints`, `/my`, `/assigned`, `/{id}`; `PUT /api/complaints/{id}/assign`, `/start`, `/resolve`; `GET /api/notifications` |
| Visitors | `POST /api/visitors`; `PUT /{id}/checkout`, `/{id}/verify`; `GET /api/visitors`, `/my` |
| Fees | `POST /api/fees`; `POST /api/fees/{id}/payments`; `GET /api/fees`, `/my`, `/student/{id}`, `/payments/{id}/receipt` |
| Notices | `POST /api/notices`; `GET /api/notices`, `/all` |
| Mess | `POST /api/mess/menu`; `GET /api/mess/menu?date=`; `POST /api/mess/feedback`; `GET /api/mess/feedback`; `PUT /api/mess/preferences`; `GET /api/mess/preferences/my` |


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
