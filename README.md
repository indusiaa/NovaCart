# IndusXCart: AI & Voice Powered E-Commerce Shopping Cart System

An enterprise-ready, zero-dependency Java 27 E-Commerce Shopping Cart System featuring product catalog selection, shopping cart lifecycle management, transactional multi-step checkout, and integrated **AI Chatbot** and **Voice Assistant** capabilities.

---

## 🚀 Key Features

1. **Product Selection & Catalog Management**:
   - Categorized browsing (`ELECTRONICS`, `AUDIO`, `WEARABLES`, `ACCESSORIES`, `HOME_OFFICE`, `GAMING`).
   - Tokenized keyword search, price range filtering, and stock availability monitoring.
   - Real-time stock status badges (`In Stock`, `Low Stock`).

2. **Shopping Cart Management**:
   - Session-isolated carts with thread-safe item additions, quantity modifications, and removals.
   - Dynamic calculations: Subtotal, progressive Free Shipping progress bar (unlocked at $50+), 8% estimated tax, and final order total.
   - Promotional coupon vouchers (`WELCOME10` for 10% off, `SAVE20` for $20 off orders > $100, `FREESHIP` for free shipping, `TECH50` for $50 off orders > $300).

3. **Multi-Step Checkout Functionality**:
   - Customer details & shipping address collection.
   - Multi-provider payment simulation: Credit/Debit Card, Instant UPI, PayPal, and Cash on Delivery.
   - Strict transactional integrity: pre-checkout stock verification, simulated payment gateway processing, automatic inventory decrement, and unique order receipt generation with tracking IDs (`ORD-...`, `TRK-...`).

4. **Conversational AI Assistant (Chatbot)**:
   - Natural Language Understanding (NLU) engine extracting conversational intents (`SEARCH_PRODUCTS`, `ADD_TO_CART`, `VIEW_CART`, `REMOVE_FROM_CART`, `APPLY_COUPON`, `CHECKOUT`, `RECOMMEND_PRODUCTS`, `TRACK_ORDER`, `FAQ_SUPPORT`).
   - Interactive chat drawer with quick suggestion chips and product cards embedded directly in chat messages.

5. **Voice Assistant**:
   - Hands-free voice shopping using the Web Speech API (`SpeechRecognition` / `webkitSpeechRecognition`).
   - Acoustic waveform visualizer with listening animations.
   - Spoken human voice response generated via browser `SpeechSynthesis` (`speechSynthesis.speak`), normalized for speech cadence.
   - Voice command shortcuts ("Add Sony headphones to my cart", "Show keyboards", "What's in my cart?", "Proceed to checkout").

---

## 🏛️ High-Level System Architecture

```text
[Client Layer]
   │── Web Storefront UI (HTML5, Modern CSS, Vanilla JS)
   │── AI Chatbot Drawer (Conversational Widget)
   └── Voice Assistant (Web Speech API STT/TTS & Animated Orb)
         │
[REST API & Dispatch Layer] (com.sun.net.httpserver.HttpServer)
   │── /api/products         (ProductController)
   │── /api/cart             (CartController)
   │── /api/checkout         (CheckoutController)
   │── /api/ai/chat          (AiAssistantController)
   │── /api/ai/voice         (VoiceAssistantController)
   └── /                     (StaticContentController)
         │
[Service & Domain Layer]
   │── CatalogService        (Product catalog & stock validation)
   │── CartService           (Cart calculations, mutations & promotions)
   │── CheckoutService       (Order transaction orchestrator)
   │── DiscountService       (Coupon evaluation & rules)
   └── PaymentGateway        (Simulated multi-channel payment processor)
         │
[AI & Voice Intelligence Layer]
   │── NlpProcessor          (Intent classifier & slot extractor)
   │── AiRecommendationEngine (Cross-category affinity recommender)
   │── AiAssistantService    (Conversational dispatch & UI actions)
   └── VoiceAssistantService (Speech normalization & audio synthesis)
         │
[Repository & Persistence Layer]
   │── ProductRepository     (Thread-safe inventory store)
   │── CartRepository        (Session-keyed cart store)
   │── OrderRepository       (Order history & tracking store)
   └── CouponRepository      (Promotional code store)
```

---

## 🛠️ Quick Start & Running

### Requirements
- **Java SE 21 or higher** (Tested on Java 27 SE).
- No external Maven/Gradle downloads are required to run; standard Java libraries are used.

### Run with PowerShell
```powershell
cd "C:\Users\Lenovo\.gemini\antigravity\scratch\ecommerce-shopping-cart"
.\run.ps1
```

### Run with Command Prompt (cmd)
```cmd
run.bat
```

### Run Integration Test Suite
```powershell
$files = (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
javac -d bin -encoding UTF-8 $files
java -cp bin com.ecommerce.SystemIntegrationTest
```

Once started, open your browser at:
👉 **`http://localhost:8080/`**

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/products` | Retrieve catalog with optional `?category=...&query=...&minPrice=...&maxPrice=...` |
| `GET` | `/api/products/{id}` | Retrieve single product details |
| `GET` | `/api/categories` | Retrieve all product categories |
| `GET` | `/api/cart` | Retrieve current user session cart |
| `POST` | `/api/cart/items` | Add item: `{"productId": "PROD-AUDIO-01", "quantity": 1}` |
| `PUT` | `/api/cart/items` | Update quantity: `{"productId": "PROD-AUDIO-01", "quantity": 3}` |
| `DELETE`| `/api/cart/items?productId=...` | Remove item from cart |
| `POST` | `/api/cart/coupon` | Apply promo code: `{"code": "SAVE20"}` |
| `DELETE`| `/api/cart/coupon` | Remove applied coupon |
| `POST` | `/api/cart/clear` | Empty cart |
| `POST` | `/api/checkout` | Process checkout and create order |
| `GET` | `/api/orders` | Retrieve orders list (or filter by `?email=...`) |
| `GET` | `/api/orders/{id}` | Retrieve order details & tracking info |
| `POST` | `/api/ai/chat` | Send conversational chat query: `{"message": "..."}` |
| `POST` | `/api/ai/voice` | Send voice speech transcript: `{"transcript": "..."}` |

---

## 🎟️ Active Demo Promotional Coupons

- `WELCOME10`: 10% discount on orders over $20.00.
- `SAVE20`: $20.00 instant discount on orders over $100.00.
- `FREESHIP`: Free standard shipping on any order.
- `TECH50`: $50.00 discount on orders over $300.00.
- `GAMER15`: 15% discount on gaming gear / orders over $75.00.

---

## 🗣️ Voice Commands & Chatbot Queries to Try

- *"Show noise cancelling headphones"*
- *"Add Sony headphones to my cart"*
- *"Add two Bose headphones"*
- *"What is in my cart?"*
- *"Apply coupon SAVE20"*
- *"Recommend something for gaming"*
- *"What is your return policy?"*
- *"Proceed to checkout"*

