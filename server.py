import os
import re
import uuid
import datetime
from flask import Flask, request, jsonify, send_from_directory

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STATIC_DIR = os.path.join(BASE_DIR, "src", "main", "resources", "static")
if not os.path.exists(STATIC_DIR):
    STATIC_DIR = BASE_DIR

app = Flask(__name__, static_folder=STATIC_DIR, static_url_path="")
app.secret_key = os.getenv("SECRET_KEY", "indusxcart-secret-2026")

# In-Memory Product Catalog
PRODUCTS = [
    {
        "id": "PROD-AUDIO-01",
        "name": "Sony WH-1000XM5 Wireless Headphones",
        "description": "Industry-leading noise cancellation with dual processors, 30-hour battery life, and ultra-comfortable lightweight design.",
        "price": 349.99,
        "category": "AUDIO",
        "categoryDisplay": "Audio & Headphones",
        "stockQuantity": 24,
        "rating": 4.8,
        "imageUrl": "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
        "tags": ["audio", "headphones", "bluetooth", "noise-cancelling", "sony"]
    },
    {
        "id": "PROD-AUDIO-02",
        "name": "Bose QuietComfort 45 Headphones",
        "description": "Iconic quiet, comfort, and sound. Deep, clear audio with active acoustic noise cancellation.",
        "price": 279.00,
        "category": "AUDIO",
        "categoryDisplay": "Audio & Headphones",
        "stockQuantity": 18,
        "rating": 4.7,
        "imageUrl": "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&q=80",
        "tags": ["audio", "headphones", "bose", "wireless", "travel"]
    },
    {
        "id": "PROD-AUDIO-03",
        "name": "Apple AirPods Pro (2nd Gen)",
        "description": "Next-level active noise cancellation, adaptive transparency, and personalized spatial audio.",
        "price": 239.00,
        "category": "AUDIO",
        "categoryDisplay": "Audio & Headphones",
        "stockQuantity": 45,
        "rating": 4.9,
        "imageUrl": "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&q=80",
        "tags": ["audio", "earbuds", "apple", "wireless", "airpods"]
    },
    {
        "id": "PROD-ELEC-01",
        "name": "Apple MacBook Air 15-inch M3",
        "description": "Strikingly thin design with the lightning-fast Apple M3 chip, up to 18 hours of battery, and stunning Liquid Retina display.",
        "price": 1249.00,
        "category": "ELECTRONICS",
        "categoryDisplay": "Electronics",
        "stockQuantity": 12,
        "rating": 4.9,
        "imageUrl": "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80",
        "tags": ["laptop", "macbook", "apple", "computer", "ultrabook", "m3"]
    },
    {
        "id": "PROD-ELEC-02",
        "name": "Dell UltraSharp 27\" 4K USB-C Hub Monitor",
        "description": "Brilliant IPS Black technology with 4K resolution, 98% DCI-P3 color gamut, and 90W USB-C power delivery.",
        "price": 499.50,
        "category": "ELECTRONICS",
        "categoryDisplay": "Electronics",
        "stockQuantity": 15,
        "rating": 4.6,
        "imageUrl": "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80",
        "tags": ["monitor", "display", "4k", "dell", "screen", "office"]
    },
    {
        "id": "PROD-ELEC-03",
        "name": "Kindle Paperwhite 16GB",
        "description": "6.8-inch glare-free display, adjustable warm light, waterproof design, and weeks of battery life.",
        "price": 139.99,
        "category": "ELECTRONICS",
        "categoryDisplay": "Electronics",
        "stockQuantity": 30,
        "rating": 4.7,
        "imageUrl": "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&q=80",
        "tags": ["reader", "kindle", "books", "amazon", "e-reader"]
    },
    {
        "id": "PROD-WEAR-01",
        "name": "Samsung Galaxy Watch 6 Classic",
        "description": "Rotating bezel, sapphire crystal glass, advanced sleep coaching, and comprehensive fitness tracking.",
        "price": 329.00,
        "category": "WEARABLES",
        "categoryDisplay": "Wearables & Smartwatches",
        "stockQuantity": 20,
        "rating": 4.6,
        "imageUrl": "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
        "tags": ["watch", "smartwatch", "samsung", "fitness", "wearable"]
    },
    {
        "id": "PROD-WEAR-02",
        "name": "Apple Watch Series 9 GPS 45mm",
        "description": "Powerful S9 SiP chip, magical double tap gesture, brighter Always-On display, and advanced health sensors.",
        "price": 399.00,
        "category": "WEARABLES",
        "categoryDisplay": "Wearables & Smartwatches",
        "stockQuantity": 25,
        "rating": 4.8,
        "imageUrl": "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500&q=80",
        "tags": ["watch", "smartwatch", "apple", "fitness", "wearable"]
    },
    {
        "id": "PROD-OFF-01",
        "name": "Logitech MX Master 3S Wireless Mouse",
        "description": "Quiet clicks, 8K DPI any-surface sensor, MagSpeed electromagnetic scrolling, and ergonomic thumb rest.",
        "price": 99.99,
        "category": "HOME_OFFICE",
        "categoryDisplay": "Home Office",
        "stockQuantity": 50,
        "rating": 4.9,
        "imageUrl": "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&q=80",
        "tags": ["mouse", "logitech", "bluetooth", "office", "ergonomic", "mx"]
    },
    {
        "id": "PROD-OFF-02",
        "name": "Keychron K2 Pro Mechanical Keyboard",
        "description": "QMK/VIA wireless custom mechanical keyboard with hot-swappable switches and RGB backlighting.",
        "price": 119.00,
        "category": "HOME_OFFICE",
        "categoryDisplay": "Home Office",
        "stockQuantity": 22,
        "rating": 4.7,
        "imageUrl": "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
        "tags": ["keyboard", "mechanical", "keychron", "wireless", "gaming", "office"]
    },
    {
        "id": "PROD-OFF-03",
        "name": "Rode NT-USB Mini Studio Microphone",
        "description": "Compact studio-quality USB microphone with directional cardioid pickup, built-in pop filter, and zero-latency monitoring.",
        "price": 99.00,
        "category": "HOME_OFFICE",
        "categoryDisplay": "Home Office",
        "stockQuantity": 16,
        "rating": 4.8,
        "imageUrl": "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80",
        "tags": ["microphone", "rode", "usb", "streaming", "podcast", "office"]
    },
    {
        "id": "PROD-ACC-01",
        "name": "Anker 737 Power Bank 24,000mAh (GaNPrime)",
        "description": "Ultra-powerful two-way charging with 140W max output and smart digital display showing power metrics.",
        "price": 109.99,
        "category": "ACCESSORIES",
        "categoryDisplay": "Accessories",
        "stockQuantity": 35,
        "rating": 4.8,
        "imageUrl": "https://images.unsplash.com/photo-1609592424369-07440b8a4f3a?w=500&q=80",
        "tags": ["powerbank", "anker", "charger", "battery", "usb-c"]
    },
    {
        "id": "PROD-GAME-01",
        "name": "Sony PlayStation DualSense Wireless Controller",
        "description": "Immersive haptic feedback, dynamic adaptive triggers, and built-in microphone in an iconic ergonomic design.",
        "price": 69.99,
        "category": "GAMING",
        "categoryDisplay": "Gaming",
        "stockQuantity": 40,
        "rating": 4.8,
        "imageUrl": "https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?w=500&q=80",
        "tags": ["gaming", "playstation", "controller", "sony", "dualsense"]
    },
    {
        "id": "PROD-GAME-02",
        "name": "Razer BlackShark V2 Pro Gaming Headset",
        "description": "Wireless esports headset with Razer TriForce Titanium 50mm drivers and HyperClear supercardioid mic.",
        "price": 179.99,
        "category": "GAMING",
        "categoryDisplay": "Gaming",
        "stockQuantity": 14,
        "rating": 4.6,
        "imageUrl": "https://images.unsplash.com/photo-1599669454699-248893623440?w=500&q=80",
        "tags": ["gaming", "headset", "razer", "audio", "esports"]
    }
]

COUPONS = {
    "WELCOME10": {"code": "WELCOME10", "type": "PERCENTAGE", "value": 10.0, "minimumOrderAmount": 20.0},
    "SAVE20": {"code": "SAVE20", "type": "FIXED_AMOUNT", "value": 20.0, "minimumOrderAmount": 100.0},
    "FREESHIP": {"code": "FREESHIP", "type": "FREE_SHIPPING", "value": 5.99, "minimumOrderAmount": 0.0},
    "TECH50": {"code": "TECH50", "type": "FIXED_AMOUNT", "value": 50.0, "minimumOrderAmount": 300.0},
    "GAMER15": {"code": "GAMER15", "type": "PERCENTAGE", "value": 15.0, "minimumOrderAmount": 75.0}
}

CARTS = {}
ORDERS = {}

def get_session_id():
    return request.headers.get("X-Session-ID", "guest-session")

def get_or_create_cart(session_id):
    if session_id not in CARTS:
        CARTS[session_id] = {
            "cartId": f"CART-{uuid.uuid4().hex[:8].upper()}",
            "sessionId": session_id,
            "items": [],
            "appliedCoupon": None,
            "subtotal": 0.0,
            "discountAmount": 0.0,
            "tax": 0.0,
            "shippingFee": 0.0,
            "total": 0.0,
            "totalItemCount": 0
        }
    return CARTS[session_id]

def recalculate_cart(cart):
    subtotal = sum(item["price"] * item["quantity"] for item in cart["items"])
    total_count = sum(item["quantity"] for item in cart["items"])
    
    subtotal = round(subtotal, 2)
    cart["subtotal"] = subtotal
    cart["totalItemCount"] = total_count

    if not cart["items"]:
        cart["shippingFee"] = 0.0
        cart["discountAmount"] = 0.0
        cart["tax"] = 0.0
        cart["total"] = 0.0
        return cart

    coupon = cart.get("appliedCoupon")
    discount = 0.0
    shipping = 0.0 if subtotal >= 50.0 else 5.99

    if coupon:
        if coupon["type"] == "PERCENTAGE":
            discount = round(subtotal * (coupon["value"] / 100.0), 2)
        elif coupon["type"] == "FIXED_AMOUNT":
            discount = min(coupon["value"], subtotal)
        elif coupon["type"] == "FREE_SHIPPING":
            discount = shipping
            shipping = 0.0

    taxable = max(0.0, subtotal - discount)
    tax = round(taxable * 0.08, 2)
    total = round(taxable + tax + shipping, 2)

    cart["discountAmount"] = discount
    cart["shippingFee"] = shipping
    cart["tax"] = tax
    cart["total"] = total
    return cart

# Serve Frontend
@app.route("/")
def index():
    for f in [app.static_folder, BASE_DIR]:
        if os.path.exists(os.path.join(f, "index.html")):
            return send_from_directory(f, "index.html")
    return "<h1>IndusXCart System Online</h1>"

@app.route("/<path:path>")
def static_proxy(path):
    clean_path = path.split("?")[0]
    for f in [app.static_folder, BASE_DIR]:
        target = os.path.join(f, clean_path)
        if os.path.exists(target) and not os.path.isdir(target):
            return send_from_directory(f, clean_path)
    return index()

# Catalog Endpoints
@app.route("/api/products", methods=["GET"])
def get_products():
    category = request.args.get("category")
    query = request.args.get("query", "").strip().lower()
    min_price = request.args.get("minPrice", type=float)
    max_price = request.args.get("maxPrice", type=float)

    filtered = PRODUCTS
    if category and category != "ALL":
        filtered = [p for p in filtered if p["category"] == category]
    if min_price is not None:
        filtered = [p for p in filtered if p["price"] >= min_price]
    if max_price is not None:
        filtered = [p for p in filtered if p["price"] <= max_price]
    if query:
        tokens = query.split()
        filtered = [
            p for p in filtered
            if all(
                t in p["name"].lower() or 
                t in p["description"].lower() or 
                any(t in tag.lower() for tag in p["tags"])
                for t in tokens
            )
        ]
    return jsonify(filtered)

@app.route("/api/categories", methods=["GET"])
def get_categories():
    cats = [
        {"name": "ELECTRONICS", "displayName": "Electronics"},
        {"name": "AUDIO", "displayName": "Audio & Headphones"},
        {"name": "WEARABLES", "displayName": "Wearables & Smartwatches"},
        {"name": "HOME_OFFICE", "displayName": "Home Office"},
        {"name": "GAMING", "displayName": "Gaming"},
        {"name": "ACCESSORIES", "displayName": "Accessories"}
    ]
    return jsonify(cats)

# Cart Endpoints
@app.route("/api/cart", methods=["GET"])
def get_cart():
    cart = get_or_create_cart(get_session_id())
    return jsonify(recalculate_cart(cart))

@app.route("/api/cart/items", methods=["POST"])
def add_to_cart():
    session_id = get_session_id()
    data = request.get_json() or {}
    product_id = data.get("productId")
    quantity = int(data.get("quantity", 1))

    product = next((p for p in PRODUCTS if p["id"] == product_id), None)
    if not product:
        return jsonify({"message": "Product not found"}), 404

    cart = get_or_create_cart(session_id)
    existing = next((i for i in cart["items"] if i["productId"] == product_id), None)
    if existing:
        existing["quantity"] += quantity
        existing["subtotal"] = round(existing["price"] * existing["quantity"], 2)
    else:
        cart["items"].append({
            "productId": product["id"],
            "productName": product["name"],
            "price": product["price"],
            "imageUrl": product["imageUrl"],
            "quantity": quantity,
            "subtotal": round(product["price"] * quantity, 2),
            "stockQuantity": product["stockQuantity"]
        })

    return jsonify(recalculate_cart(cart))

@app.route("/api/cart/items", methods=["PUT"])
def update_cart_item():
    session_id = get_session_id()
    data = request.get_json() or {}
    product_id = data.get("productId")
    quantity = int(data.get("quantity", 1))

    cart = get_or_create_cart(session_id)
    if quantity <= 0:
        cart["items"] = [i for i in cart["items"] if i["productId"] != product_id]
    else:
        item = next((i for i in cart["items"] if i["productId"] == product_id), None)
        if item:
            item["quantity"] = quantity
            item["subtotal"] = round(item["price"] * quantity, 2)

    return jsonify(recalculate_cart(cart))

@app.route("/api/cart/items", methods=["DELETE"])
def remove_cart_item():
    session_id = get_session_id()
    product_id = request.args.get("productId") or (request.get_json() or {}).get("productId")
    cart = get_or_create_cart(session_id)
    cart["items"] = [i for i in cart["items"] if i["productId"] != product_id]
    return jsonify(recalculate_cart(cart))

@app.route("/api/cart/coupon", methods=["POST"])
def apply_coupon():
    session_id = get_session_id()
    data = request.get_json() or {}
    code = (data.get("code") or "").strip().upper()

    coupon = COUPONS.get(code)
    if not coupon:
        return jsonify({"message": f"Invalid coupon code: '{code}'"}), 400

    cart = get_or_create_cart(session_id)
    recalculate_cart(cart)
    if cart["subtotal"] < coupon["minimumOrderAmount"]:
        return jsonify({"message": f"Coupon requires minimum order of ${coupon['minimumOrderAmount']:.2f}"}), 400

    cart["appliedCoupon"] = coupon
    return jsonify(recalculate_cart(cart))

@app.route("/api/cart/coupon", methods=["DELETE"])
def remove_coupon():
    session_id = get_session_id()
    cart = get_or_create_cart(session_id)
    cart["appliedCoupon"] = None
    return jsonify(recalculate_cart(cart))

@app.route("/api/cart/clear", methods=["POST"])
def clear_cart():
    session_id = get_session_id()
    cart = get_or_create_cart(session_id)
    cart["items"] = []
    cart["appliedCoupon"] = None
    return jsonify(recalculate_cart(cart))

# Checkout Endpoint
@app.route("/api/checkout", methods=["POST"])
def checkout():
    session_id = get_session_id()
    data = request.get_json() or {}
    cart = get_or_create_cart(session_id)
    recalculate_cart(cart)

    if not cart["items"]:
        return jsonify({"message": "Cannot checkout an empty shopping cart"}), 400

    order_id = f"ORD-{uuid.uuid4().hex[:6].upper()}"
    tracking = f"TRK-{uuid.uuid4().hex[:10].upper()}"
    txn_id = f"TXN-{int(datetime.datetime.now().timestamp())}-{uuid.uuid4().hex[:4].upper()}"

    order = {
        "orderId": order_id,
        "customerName": data.get("customerName", "Valued Customer"),
        "customerEmail": data.get("customerEmail", ""),
        "shippingAddress": data.get("shippingAddress", ""),
        "paymentMethod": data.get("paymentMethod", "CREDIT_CARD"),
        "transactionId": txn_id,
        "trackingNumber": tracking,
        "subtotal": cart["subtotal"],
        "discountAmount": cart["discountAmount"],
        "tax": cart["tax"],
        "shippingFee": cart["shippingFee"],
        "totalAmount": cart["total"],
        "items": list(cart["items"]),
        "status": "Order Confirmed",
        "createdAt": datetime.datetime.now().isoformat()
    }

    ORDERS[order_id] = order
    # Clear cart
    cart["items"] = []
    cart["appliedCoupon"] = None
    recalculate_cart(cart)

    return jsonify(order), 201

@app.route("/api/orders", methods=["GET"])
def get_orders():
    return jsonify(list(ORDERS.values()))

# AI Assistant Chat Endpoint
@app.route("/api/ai/chat", methods=["POST"])
def ai_chat():
    session_id = get_session_id()
    data = request.get_json() or {}
    msg = (data.get("message") or data.get("query") or data.get("transcript") or data.get("text") or "").strip().lower()
    cart = get_or_create_cart(session_id)
    recalculate_cart(cart)

    # 1. Add to cart intent
    if msg.startswith("add") or "add to cart" in msg or "put in cart" in msg or "buy" in msg:
        clean = re.sub(r"^(add|put|buy|kharido|daalo)\s+", "", msg)
        clean = re.sub(r"\s+(to|into|in)\s+(my\s+)?cart.*$", "", clean).strip()
        clean = re.sub(r"\s+(cart\s+mein).*$", "", clean).strip()
        
        # Quantity extraction
        qty = 1
        qty_match = re.search(r"\b([1-9]|one|two|three|four|five)\b", clean)
        if qty_match:
            word_map = {"one": 1, "two": 2, "three": 3, "four": 4, "five": 5}
            matched_qty = qty_match.group(1)
            qty = word_map.get(matched_qty, int(matched_qty) if matched_qty.isdigit() else 1)
            clean = re.sub(r"\b([1-9]|one|two|three|four|five)\b", "", clean).strip()

        matched = next((p for p in PRODUCTS if clean in p["name"].lower() or any(clean in t for t in p["tags"])), None)
        if not matched and clean:
            tokens = [t for t in clean.split() if len(t) > 2]
            for p in PRODUCTS:
                p_text = (p["name"] + " " + p["category"] + " " + " ".join(p["tags"])).lower()
                if any(t in p_text for t in tokens):
                    matched = p
                    break

        if matched:
            cart["items"].append({
                "productId": matched["id"],
                "productName": matched["name"],
                "price": matched["price"],
                "imageUrl": matched["imageUrl"],
                "quantity": qty,
                "subtotal": matched["price"] * qty,
                "stockQuantity": matched["stockQuantity"]
            })
            recalculate_cart(cart)
            item_label = f"{qty}x {matched['name']}" if qty > 1 else matched['name']
            return jsonify({
                "message": f"Added **{item_label}** to your cart (${matched['price'] * qty:.2f}). Your new total is **${cart['total']:.2f}**.",
                "spokenText": f"Added {item_label} to your cart. Total is now ${cart['total']:.2f}. Would you like to checkout?",
                "action": "CART_UPDATED",
                "data": cart,
                "suggestions": ["View Cart", "Proceed to Checkout", "Recommend accessories"]
            })
        else:
            return jsonify({
                "message": f"Could not find a product matching '{clean}'. Please check the name or browse categories.",
                "spokenText": f"I couldn't identify the product '{clean}' to add. Try saying Add Sony Headphones.",
                "action": "NONE",
                "data": None,
                "suggestions": ["Add Sony headphones", "Add Keychron keyboard", "View Catalog"]
            })

    # 2. View Cart intent
    if "cart" in msg and any(k in msg for k in ["what", "show", "view", "check", "kya", "dikhao", "in my", "summary"]) or msg == "cart":
        if not cart["items"]:
            return jsonify({
                "message": "Your shopping cart is currently empty. Would you like some recommendations?",
                "spokenText": "Your shopping cart is currently empty. You can ask me to find headphones or MacBook!",
                "action": "VIEW_CART",
                "data": cart,
                "suggestions": ["Show Headphones", "Browse Laptops", "Gaming Gear"]
            })
        items_desc = ", ".join([f"{i['quantity']}x {i['productName']}" for i in cart["items"][:2]])
        return jsonify({
            "message": f"You have **{cart['totalItemCount']} items** in your cart. Items: {items_desc}. Subtotal: **${cart['subtotal']:.2f}**, Total: **${cart['total']:.2f}**.",
            "spokenText": f"You have {cart['totalItemCount']} items in your cart including {items_desc}. Your total is ${cart['total']:.2f}.",
            "action": "VIEW_CART",
            "data": cart,
            "suggestions": ["Proceed to Checkout", "Apply coupon SAVE20", "Clear Cart"]
        })

    # 3. Checkout intent
    if "checkout" in msg or "place order" in msg or "pay" in msg or "order karo" in msg:
        if not cart["items"]:
            return jsonify({
                "message": "Your cart is empty. Please add products before checking out.",
                "spokenText": "Your cart is empty. Please add items before checking out.",
                "action": "NONE",
                "data": cart,
                "suggestions": ["Browse Headphones", "Browse Laptops"]
            })
        return jsonify({
            "message": f"You're ready to checkout **{cart['totalItemCount']} items** for **${cart['total']:.2f}**. Opening secure checkout...",
            "spokenText": f"Opening secure checkout for your {cart['totalItemCount']} items totaling ${cart['total']:.2f}.",
            "action": "OPEN_CHECKOUT",
            "data": cart,
            "suggestions": ["Place Order", "Apply Coupon"]
        })

    # 4. Coupon intent
    if "coupon" in msg or "code" in msg or "save20" in msg or "welcome10" in msg or "discount" in msg or "offer" in msg:
        for code in COUPONS:
            if code.lower() in msg:
                cart["appliedCoupon"] = COUPONS[code]
                recalculate_cart(cart)
                return jsonify({
                    "message": f"Coupon **{code}** applied! You saved **${cart['discountAmount']:.2f}**. New total is **${cart['total']:.2f}**.",
                    "spokenText": f"Coupon {code} applied! You saved ${cart['discountAmount']:.2f}. Your new total is ${cart['total']:.2f}.",
                    "action": "CART_UPDATED",
                    "data": cart,
                    "suggestions": ["Proceed to Checkout", "View Cart"]
                })
        return jsonify({
            "message": "Available coupons: **WELCOME10** (10% off), **SAVE20** ($20 off over $100), and **FREESHIP** (Free delivery).",
            "spokenText": "We have coupons WELCOME10 for 10% off and SAVE20 for $20 off on orders over $100.",
            "action": "NONE",
            "data": None,
            "suggestions": ["Apply WELCOME10", "Apply SAVE20", "Apply FREESHIP"]
        })

    # 5. Clear Cart intent
    if "clear cart" in msg or "empty cart" in msg or "cart khali" in msg:
        cart["items"] = []
        cart["appliedCoupon"] = None
        recalculate_cart(cart)
        return jsonify({
            "message": "Your cart has been cleared.",
            "spokenText": "I have emptied your shopping cart.",
            "action": "CART_UPDATED",
            "data": cart,
            "suggestions": ["Show Headphones", "Browse Laptops"]
        })

    # 6. DYNAMIC VOICE & TEXT PRODUCT SEARCH INTENT
    clean = re.sub(r"^(can you |please |could you |hey |hi |ok |assistant |tell me |help me )+", "", msg)
    clean = re.sub(r"^(search for|search|find me|find|show me|show|look for|look up|browse|get me|i want to see|i want|do you have|where is|where are|display)\s+", "", clean)
    clean = re.sub(r"\s+(dikhao|khojo|dhundo|batao|dekho|search karo|chahiye)$", "", clean)
    clean = re.sub(r"^(dikhao|khojo|dhundo|batao)\s+", "", clean)
    clean = re.sub(r"[?!.,;]", "", clean).strip()

    tokens = [t for t in clean.split() if len(t) > 1]
    matches = []
    if tokens:
        for p in PRODUCTS:
            p_text = (p["name"] + " " + p["category"] + " " + p["description"] + " " + " ".join(p["tags"])).lower()
            if any(tok in p_text for tok in tokens):
                matches.append(p)

    if matches:
        if len(matches) == 1:
            m = matches[0]
            spoken = f"I found 1 product for '{clean}': {m['name']} for ${m['price']:.2f}. Rated {m['rating']} stars. Showing it on your screen now!"
        elif len(matches) == 2:
            spoken = f"Found 2 products for '{clean}': {matches[0]['name']} for ${matches[0]['price']:.2f}, and {matches[1]['name']} for ${matches[1]['price']:.2f}. Displaying them on your screen!"
        else:
            top2 = f"{matches[0]['name']} for ${matches[0]['price']:.2f} and {matches[1]['name']} for ${matches[1]['price']:.2f}"
            spoken = f"I found {len(matches)} products for '{clean}'. Top picks include {top2}. Showing all results on your screen!"

        return jsonify({
            "message": f"Found **{len(matches)} product(s)** for '**{clean}**':",
            "spokenText": spoken,
            "action": "SEARCH_RESULTS",
            "query": clean,
            "data": matches,
            "suggestions": [f"Add {matches[0]['name'][:25]}", "View Cart", "Clear Search"]
        })
    elif clean:
        return jsonify({
            "message": f"No products found matching '**{clean}**'. Try searching for **headphones**, **MacBook**, **Keychron keyboard**, or **Dell monitor**.",
            "spokenText": f"I searched for '{clean}', but could not find any matching products. You can search for headphones, MacBook, keyboard, or monitors.",
            "action": "SEARCH_EMPTY",
            "query": clean,
            "data": [],
            "suggestions": ["Show Headphones", "Show MacBook", "Browse Keyboards"]
        })

    # Default fallback
    recs = PRODUCTS[:3]
    return jsonify({
        "message": "Welcome to **IndusXCart**! How can I help you today?",
        "spokenText": "Welcome to IndusXCart! Ask me to search headphones, laptops, or add items to your cart.",
        "action": "SHOW_RECOMMENDATIONS",
        "data": recs,
        "suggestions": ["Search Headphones", "Show MacBook", "What is in my cart?"]
    })

# Voice Assistant Endpoint
@app.route("/api/ai/voice", methods=["POST"])
def ai_voice():
    session_id = get_session_id()
    data = request.get_json() or {}
    transcript = (data.get("transcript") or data.get("text") or data.get("message") or "").strip().lower()

    # Pass the data directly
    chat_resp = ai_chat().get_json()
    return jsonify({
        "recognizedText": transcript,
        "spokenResponse": chat_resp.get("spokenText") or "I processed your voice request.",
        "displayText": chat_resp.get("message"),
        "action": chat_resp.get("action", "NONE"),
        "query": chat_resp.get("query"),
        "data": chat_resp.get("data"),
        "speechRate": 1.05,
        "speechPitch": 1.0
    })

if __name__ == '__main__':
    port = int(os.getenv("PORT", 8080))
    host = os.getenv("HOST", "0.0.0.0")
    print(f"==================================================")
    print(f"🚀 IndusXCart E-Commerce System Online")
    print(f"🌐 Running at: http://{host}:{port}")
    print(f"==================================================")
    app.run(host=host, port=port, debug=False)

