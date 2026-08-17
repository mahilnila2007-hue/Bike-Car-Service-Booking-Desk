# NEKA — Smart Garage Service Booking & Vehicle Progress Management System

> **Production-style** full-stack Java + Firebase application for local two-wheeler and car garages.

---

## 🚀 Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security |
| Database | Firebase Firestore (NoSQL) |
| Authentication | Firebase Authentication + Admin SDK (JWT) |
| Real-Time | Server-Sent Events (SSE) |
| Frontend | HTML5, Vanilla CSS, Vanilla JS, Bootstrap 5 |
| API Docs | Springdoc OpenAPI 3 (Swagger UI) |
| Build | Maven |

---

## 📋 Prerequisites

1. **JDK 21** installed
2. **Maven 3.9+** installed
3. A **Firebase project** with Firestore and Authentication enabled
4. Your Firebase **Service Account JSON** key
5. Your Firebase **Web App config** (for frontend)

---

## ⚙️ Setup Instructions

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create or open your project
3. Enable **Firestore Database** (Start in **Test mode** for development)
4. Enable **Authentication → Email/Password**
5. Go to **Project Settings → Service accounts → Generate new private key**
6. Download the JSON and save it as:
   ```
   src/main/resources/firebase-service-account.json
   ```

### 2. Backend Configuration

Edit `src/main/resources/application.properties`:
```properties
firebase.service-account.path=firebase-service-account.json
```

### 3. Frontend Firebase Config

Edit `src/main/resources/static/js/firebase-init.js`:
```javascript
const firebaseConfig = {
  apiKey: "YOUR_WEB_API_KEY",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project-id",
  storageBucket: "your-project.appspot.com",
  messagingSenderId: "123456789",
  appId: "1:123456789:web:abcdef"
};
```
Find these values in Firebase Console → Project Settings → Your apps → Web app.

### 4. Build & Run

```bash
cd "c:\Users\MAHIL RAM\OneDrive\Desktop\NEKA"
mvn spring-boot:run
```

The app starts on **http://localhost:8080**

---

## 🗺️ Application Flow

```
Customer → Register/Login → Add Vehicle → Book Service
                                              ↓
Staff → Confirm Booking → Receive Vehicle → Create Service Job
                                              ↓
                    Auto Bay Allocation (Firestore Transaction)
                                              ↓
                    Auto Mechanic Assignment (by specialization)
                                              ↓
              Start Service → Update Progress → SSE Push to Customer
                                              ↓
                         Quality Check → Ready for Delivery
                                              ↓
                    Generate Bill (Labour + Parts + GST 18%)
                                              ↓
                             Mark Bill PAID → Completed
```

---

## 🔑 User Roles

| Role | Access |
|---|---|
| **CUSTOMER** | Register, add vehicles, book services, live tracking, view bills |
| **STAFF** | All bookings, service board, bay/mechanic management, billing |
| **ADMIN** | All of the above + user management, reports, seed data |

**Creating Admin/Staff accounts:**
1. Register via the app (creates CUSTOMER)
2. Use the Admin API to update role:
   ```
   PATCH /api/users/{uid}/role
   Body: {"role": "ADMIN"} or {"role": "STAFF"}
   ```
   Or call via Swagger UI at `http://localhost:8080/swagger-ui.html`

---

## 🌐 Frontend Pages

| Page | Role | Path |
|---|---|---|
| Login | All | `/login.html` |
| Register | Customer | `/register.html` |
| Customer Dashboard | CUSTOMER | `/customer-dashboard.html` |
| My Vehicles | CUSTOMER | `/customer-vehicles.html` |
| Book Service | CUSTOMER | `/customer-book-service.html` |
| Live Tracking (SSE) | CUSTOMER | `/customer-service-tracking.html` |
| Service History | CUSTOMER | `/customer-history.html` |
| Bills & Payments | CUSTOMER | `/customer-bills.html` |
| Staff Dashboard | STAFF/ADMIN | `/staff-dashboard.html` |
| Bookings Management | STAFF/ADMIN | `/staff-bookings.html` |
| Service Board | STAFF/ADMIN | `/staff-service-board.html` |
| Bay Management | STAFF/ADMIN | `/staff-bays.html` |
| Mechanic Management | STAFF/ADMIN | `/staff-mechanics.html` |
| Customer List | STAFF/ADMIN | `/staff-customers.html` |
| Billing | STAFF/ADMIN | `/staff-billing.html` |
| Admin Dashboard | ADMIN | `/admin-dashboard.html` |
| User Management | ADMIN | `/admin-users.html` |
| Reports & Analytics | ADMIN | `/admin-reports.html` |
| Settings | ADMIN | `/admin-settings.html` |

---

## 📡 REST API Summary

| Endpoint | Method | Description |
|---|---|---|
| `/api/auth/profile` | POST/GET/PUT | Firebase profile sync |
| `/api/vehicles` | GET/POST | Vehicle CRUD |
| `/api/bookings` | GET/POST | Booking management |
| `/api/bookings/{id}/confirm` | POST | Confirm booking |
| `/api/bookings/{id}/receive` | POST | Mark vehicle received |
| `/api/service-jobs` | GET/POST | Create & list service jobs |
| `/api/service-jobs/{id}/allocate-bay` | POST | Auto bay allocation |
| `/api/service-jobs/{id}/assign-mechanic` | POST | Auto mechanic assignment |
| `/api/service-jobs/{id}/start` | POST | Start service + ETA calc |
| `/api/service-jobs/{id}/status` | PATCH | Update status + SSE push |
| `/api/service-jobs/{id}/history` | GET | Status history audit trail |
| `/api/bays` | GET/POST | Bay management |
| `/api/mechanics` | GET/POST | Mechanic management |
| `/api/parts` | GET/POST | Parts inventory |
| `/api/bills` | GET/POST | Bill generation |
| `/api/bills/{id}/pay` | POST | Mark paid |
| `/api/reports/dashboard` | GET | Dashboard stats |
| `/api/sse/subscribe` | GET (SSE) | Real-time status stream |
| `/api/seed` | POST | Seed demo data |

**Full interactive docs:** `http://localhost:8080/swagger-ui.html`

---

## 🏢 Bay Allocation Algorithm

1. Get all bays → filter AVAILABLE (exclude MAINTENANCE)
2. Prefer exact vehicle-type match (TWO_WHEELER or FOUR_WHEELER)
3. Fallback to UNIVERSAL bays
4. Select lowest-numbered suitable bay
5. **Firestore transaction** atomically sets bay OCCUPIED + assigns to job
6. Prevents race conditions when two requests hit simultaneously

---

## 👨‍🔧 Mechanic Assignment Algorithm

1. Filter AVAILABLE mechanics
2. Match specialization to vehicle type
3. Among matches, prefer higher experience
4. Fallback to GENERAL specialization
5. Last resort: any available mechanic
6. Set mechanic status to BUSY; released on COMPLETED

---

## 💰 Billing Formula

```
partsCost = Σ(partPrice × quantity)
subtotal  = labourCost + partsCost
taxAmount = subtotal × 18%
total     = subtotal + taxAmount − discount
```

---

## 🌱 Demo Data Setup

1. First create an admin user (register then update role)
2. Login as admin → visit Admin Dashboard
3. Click **"🌱 Seed Demo Data"** button
4. Seeds: 6 bays, 5 mechanics, 10 service types, 10 parts

---

## 📂 Project Structure

```
src/main/java/com/garage/management/
├── GarageManagementApplication.java
├── config/          (Firebase, Security, CORS, Swagger)
├── controller/      (Auth, Vehicle, Booking, ServiceJob, Bay,
│                     Mechanic, Bill, Part, ServiceType,
│                     Report, Sse, Seed)
├── exception/       (Custom exceptions + GlobalExceptionHandler)
├── model/           (11 domain models)
├── repository/      (FirestoreRepository + 10 domain repos)
├── security/        (FirebaseAuthFilter, UserRole, UserDetails)
├── service/         (Business logic services)
└── util/            (DateTimeUtil, IdGenerator)

src/main/resources/
├── application.properties
├── firebase-service-account.json  ← YOU PROVIDE THIS
└── static/
    ├── css/ (style.css, tracking.css)
    ├── js/  (firebase-init.js, auth.js, api.js, utils.js)
    └── *.html  (All frontend pages)
```

---

## 🔒 Security Notes

- Firebase `firebase-service-account.json` is in `.gitignore` — **never commit it**
- Firebase Web config in `firebase-init.js` is **public** (safe) — it's the client SDK config
- All backend API calls require a valid Firebase ID token in `Authorization: Bearer <token>`
- Role checks happen at both Spring Security level (`@PreAuthorize`) and service level

---

## 📞 Contact & Support

Built for NEKA Garage Management System. For issues, check Swagger UI at `/swagger-ui.html` for endpoint details.
#   B i k e - C a r - S e r v i c e - B o o k i n g - D e s k  
 #   B i k e - C a r - S e r v i c e - B o o k i n g - D e s k  
 