# 🚗 Car Rental System
### Spring Boot + JWT + Razorpay + Email + Concurrency Handling

---

## 📦 Tech Stack
- **Java 17** + **Spring Boot 3.2**
- **Spring Security** + **JWT** (Authentication)
- **Spring Data JPA** + **MySQL** (Database)
- **Razorpay SDK** (Payment Gateway)
- **JavaMail** (Email Notifications)
- **Lombok** (Boilerplate reduction)
- **@Version** + **@Lock** (Concurrency control)

---

## ⚙️ Setup - Step by Step

### Step 1: MySQL
```sql
CREATE DATABASE car_rental_db;
```

### Step 2: Update application.properties
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.mail.username=yourgmail@gmail.com
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
# Gmail App Password: myaccount.google.com → Security → 2FA → App Passwords

razorpay.key.id=rzp_test_XXXXXXXX
razorpay.key.secret=XXXXXXXXXXXXXXXX
# Razorpay keys: dashboard.razorpay.com → Settings → API Keys → Test Mode
```

### Step 3: Run in IntelliJ
1. File → Open → select `car-rental` folder
2. Wait for Maven to download dependencies (~2 min)
3. Right-click `CarRentalApplication.java` → Run
4. API live at: **http://localhost:8080**

---

## 📡 API Endpoints (Postman Guide)

### 🔐 Auth (No token needed)
```
POST /api/auth/register
Body: {
  "name": "John Doe",
  "email": "john@gmail.com",
  "password": "123456",
  "phone": "9999999999",
  "address": "Pune, Maharashtra",
  "licenseNumber": "MH1234567890"
}

POST /api/auth/login
Body: {
  "email": "john@gmail.com",
  "password": "123456"
}
```
> Copy `token` from response. Add to all requests:
> **Header:** `Authorization: Bearer <your_token>`

---

### 🚗 Cars (Public)
```
GET  /api/cars/available
GET  /api/cars/available-by-date?startDate=2026-05-01&endDate=2026-05-05
GET  /api/cars/search?keyword=Toyota
GET  /api/cars/category?category=SUV
GET  /api/cars/{id}
```

### 🚗 Cars (Admin only)
```
GET    /api/cars/all
POST   /api/cars/add
PUT    /api/cars/update/{id}
DELETE /api/cars/delete/{id}
PATCH  /api/cars/status/{id}?status=MAINTENANCE
```

**Add Car Body:**
```json
{
  "brand": "Toyota",
  "model": "Innova Crysta",
  "color": "White",
  "licensePlate": "MH12AB1234",
  "year": 2022,
  "seatingCapacity": 7,
  "pricePerDay": 2500.00,
  "fuelType": "Diesel",
  "transmission": "Manual",
  "category": "SUV",
  "imageUrl": "https://example.com/innova.jpg",
  "mileage": 14.5
}
```

---

### 📅 Bookings
```
POST /api/bookings/create       → Create booking (payment pending)
GET  /api/bookings/my           → My bookings
GET  /api/bookings/{id}         → Booking details (own only)
PUT  /api/bookings/cancel/{id}  → Cancel booking
GET  /api/bookings/all          → All bookings (Admin)
PUT  /api/bookings/complete/{id}→ Mark completed (Admin)
```

**Create Booking Body:**
```json
{
  "carId": 1,
  "startDate": "2026-05-01",
  "endDate": "2026-05-05"
}
```

---

### 💳 Payments (2-step Razorpay flow)
```
POST /api/payments/create-order/{bookingId}  → Get Razorpay order
POST /api/payments/verify                    → Verify & confirm payment
```

**Verify Payment Body:**
```json
{
  "razorpayOrderId": "order_XXXXXXXX",
  "razorpayPaymentId": "pay_XXXXXXXX",
  "razorpaySignature": "abc123..."
}
```

---

### 📊 Admin Dashboard
```
GET /api/admin/dashboard
```
**Response:**
```json
{
  "totalCars": 10,
  "availableCars": 6,
  "bookedCars": 3,
  "totalRevenue": 125000.0,
  "totalBookings": 45,
  "confirmedBookings": 38
}
```

---

### 🔒 Make Admin User
After registering, run in MySQL:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@gmail.com';
```

---

## 🏗️ Project Architecture
```
com.carrental
├── config/          → Security configuration
├── controller/      → REST endpoints (Auth, Car, Booking, Payment, Admin)
├── dto/
│   ├── request/     → Input DTOs (RegisterRequest, BookingRequest...)
│   └── response/    → Output DTOs (ApiResponse, BookingResponse...)
├── entity/          → JPA entities (User, Car, Booking)
├── exception/       → Custom exceptions + GlobalExceptionHandler
├── repository/      → JPA repositories with custom queries
├── security/        → JWT filter + UserDetailsService
└── service/         → Business logic (Auth, Car, Booking, Payment, Email, Admin)
```

---

## 💡 Interview Q&A

### Q: How does Razorpay payment work in your project?
**Answer (3 steps):**
1. User creates booking → `POST /api/bookings/create` → gets bookingId
2. Frontend calls `POST /api/payments/create-order/{bookingId}` → we create Razorpay order → return orderId + keyId
3. Frontend opens Razorpay popup with orderId → user pays → Razorpay returns 3 fields (orderId, paymentId, signature)
4. Frontend calls `POST /api/payments/verify` → we verify HMAC-SHA256 signature → confirm booking → send email

### Q: How do you verify Razorpay payment is genuine?
**Answer:** Using HMAC-SHA256 signature verification. Razorpay generates:
`HMAC_SHA256(orderId + "|" + paymentId, secretKey)`. We compute the same hash on our side and compare. If they match, payment is genuine and not tampered with.

### Q: How do you handle concurrent bookings?
**Answer:** Three layers:
1. `@Lock(PESSIMISTIC_WRITE)` → DB row lock, only 1 transaction at a time
2. `@Version` on Car entity → optimistic locking, detects version conflicts
3. `@Transactional` → atomic operation, car status and booking saved together

### Q: What is GlobalExceptionHandler?
**Answer:** A `@RestControllerAdvice` class that catches all exceptions centrally instead of try-catch in every controller. It maps each exception type to an appropriate HTTP status code and returns a standard `ApiResponse` format.

### Q: What is soft delete?
**Answer:** Instead of deleting from DB, we mark the car as `DELETED` status. Real-world apps never hard delete data because you may need it for audit trails, reports, or legal reasons.

### Q: Why is email sent asynchronously?
**Answer:** Using `@Async` so the email sends in a background thread. The user gets the API response instantly without waiting for the SMTP server. Email failure also doesn't affect the booking flow.

### Q: What is ApiResponse wrapper?
**Answer:** A standard response format `{success, message, data, timestamp}` for all APIs. This makes the frontend integration consistent and is industry standard practice.

### Q: What is GST and how is it calculated?
**Answer:** 18% Goods and Services Tax on the base rental amount. `finalAmount = baseAmount + (baseAmount × 0.18)`
