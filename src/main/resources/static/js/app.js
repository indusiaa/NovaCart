/**
 * IndusXCart Client Application Logic
 * Integrates Product Catalog, Cart Drawer, Checkout, AI Chatbot, and Voice Assistant.
 * Features Seamless Dual-Mode: Connects to Java 27 Backend, with Instant Local Fallback
 * if opened directly via file:// or if server is starting up.
 */

// Generate or retrieve persistent customer session ID
const SESSION_ID = (() => {
    let sid = localStorage.getItem('indusxcart_session_id');
    if (!sid) {
        sid = 'session-' + Math.random().toString(36).substring(2, 10);
        localStorage.setItem('indusxcart_session_id', sid);
    }
    return sid;
})();

// Detect API base URL
const API_BASE = (window.location.protocol.startsWith('http')) 
    ? window.location.origin 
    : 'http://localhost:8080';

let isBackendAvailable = false;

// Common Headers for JSON REST requests
function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'X-Session-ID': SESSION_ID
    };
}

// ============================================================================
// TOAST NOTIFICATIONS
// ============================================================================
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = type === 'success' ? '✅' : '⚠️';
    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ============================================================================
// SEED CATALOG DATA (Guarantees Instant Display in Any Mode)
// ============================================================================
const SEED_PRODUCTS = [
    {
        id: "PROD-AUDIO-01",
        name: "Sony WH-1000XM5 Wireless Headphones",
        description: "Industry-leading noise cancellation with dual processors, 30-hour battery life, and ultra-comfortable lightweight design.",
        price: 349.99,
        category: "AUDIO",
        categoryDisplay: "Audio & Headphones",
        stockQuantity: 24,
        rating: 4.8,
        imageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
        tags: ["audio", "headphones", "bluetooth", "noise-cancelling", "sony"]
    },
    {
        id: "PROD-AUDIO-02",
        name: "Bose QuietComfort 45 Headphones",
        description: "Iconic quiet, comfort, and sound. Deep, clear audio with active acoustic noise cancellation.",
        price: 279.00,
        category: "AUDIO",
        categoryDisplay: "Audio & Headphones",
        stockQuantity: 18,
        rating: 4.7,
        imageUrl: "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&q=80",
        tags: ["audio", "headphones", "bose", "wireless", "travel"]
    },
    {
        id: "PROD-AUDIO-03",
        name: "Apple AirPods Pro (2nd Gen)",
        description: "Next-level active noise cancellation, adaptive transparency, and personalized spatial audio.",
        price: 239.00,
        category: "AUDIO",
        categoryDisplay: "Audio & Headphones",
        stockQuantity: 45,
        rating: 4.9,
        imageUrl: "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&q=80",
        tags: ["audio", "earbuds", "apple", "wireless", "airpods"]
    },
    {
        id: "PROD-ELEC-01",
        name: "Apple MacBook Air 15-inch M3",
        description: "Strikingly thin design with the lightning-fast Apple M3 chip, up to 18 hours of battery, and stunning Liquid Retina display.",
        price: 1249.00,
        category: "ELECTRONICS",
        categoryDisplay: "Electronics",
        stockQuantity: 12,
        rating: 4.9,
        imageUrl: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&q=80",
        tags: ["laptop", "macbook", "apple", "computer", "ultrabook", "m3"]
    },
    {
        id: "PROD-ELEC-02",
        name: "Dell UltraSharp 27\" 4K USB-C Hub Monitor",
        description: "Brilliant IPS Black technology with 4K resolution, 98% DCI-P3 color gamut, and 90W USB-C power delivery.",
        price: 499.50,
        category: "ELECTRONICS",
        categoryDisplay: "Electronics",
        stockQuantity: 15,
        rating: 4.6,
        imageUrl: "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80",
        tags: ["monitor", "display", "4k", "dell", "screen", "office"]
    },
    {
        id: "PROD-ELEC-03",
        name: "Kindle Paperwhite 16GB",
        description: "6.8-inch glare-free display, adjustable warm light, waterproof design, and weeks of battery life.",
        price: 139.99,
        category: "ELECTRONICS",
        categoryDisplay: "Electronics",
        stockQuantity: 30,
        rating: 4.7,
        imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&q=80",
        tags: ["reader", "kindle", "books", "amazon", "e-reader"]
    },
    {
        id: "PROD-WEAR-01",
        name: "Samsung Galaxy Watch 6 Classic",
        description: "Rotating bezel, sapphire crystal glass, advanced sleep coaching, and comprehensive fitness tracking.",
        price: 329.00,
        category: "WEARABLES",
        categoryDisplay: "Wearables & Smartwatches",
        stockQuantity: 20,
        rating: 4.6,
        imageUrl: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
        tags: ["watch", "smartwatch", "samsung", "fitness", "wearable"]
    },
    {
        id: "PROD-WEAR-02",
        name: "Apple Watch Series 9 GPS 45mm",
        description: "Powerful S9 SiP chip, magical double tap gesture, brighter Always-On display, and advanced health sensors.",
        price: 399.00,
        category: "WEARABLES",
        categoryDisplay: "Wearables & Smartwatches",
        stockQuantity: 25,
        rating: 4.8,
        imageUrl: "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=500&q=80",
        tags: ["watch", "smartwatch", "apple", "fitness", "wearable"]
    },
    {
        id: "PROD-OFF-01",
        name: "Logitech MX Master 3S Wireless Mouse",
        description: "Quiet clicks, 8K DPI any-surface sensor, MagSpeed electromagnetic scrolling, and ergonomic thumb rest.",
        price: 99.99,
        category: "HOME_OFFICE",
        categoryDisplay: "Home Office",
        stockQuantity: 50,
        rating: 4.9,
        imageUrl: "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&q=80",
        tags: ["mouse", "logitech", "bluetooth", "office", "ergonomic", "mx"]
    },
    {
        id: "PROD-OFF-02",
        name: "Keychron K2 Pro Mechanical Keyboard",
        description: "QMK/VIA wireless custom mechanical keyboard with hot-swappable switches and RGB backlighting.",
        price: 119.00,
        category: "HOME_OFFICE",
        categoryDisplay: "Home Office",
        stockQuantity: 22,
        rating: 4.7,
        imageUrl: "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
        tags: ["keyboard", "mechanical", "keychron", "wireless", "gaming", "office"]
    },
    {
        id: "PROD-OFF-03",
        name: "Rode NT-USB Mini Studio Microphone",
        description: "Compact studio-quality USB microphone with directional cardioid pickup, built-in pop filter, and zero-latency monitoring.",
        price: 99.00,
        category: "HOME_OFFICE",
        categoryDisplay: "Home Office",
        stockQuantity: 16,
        rating: 4.8,
        imageUrl: "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80",
        tags: ["microphone", "rode", "usb", "streaming", "podcast", "office"]
    },
    {
        id: "PROD-ACC-01",
        name: "Anker 737 Power Bank 24,000mAh (GaNPrime)",
        description: "Ultra-powerful two-way charging with 140W max output and smart digital display showing power metrics.",
        price: 109.99,
        category: "ACCESSORIES",
        categoryDisplay: "Accessories",
        stockQuantity: 35,
        rating: 4.8,
        imageUrl: "https://images.unsplash.com/photo-1609592424369-07440b8a4f3a?w=500&q=80",
        tags: ["powerbank", "anker", "charger", "battery", "usb-c"]
    },
    {
        id: "PROD-GAME-01",
        name: "Sony PlayStation DualSense Wireless Controller",
        description: "Immersive haptic feedback, dynamic adaptive triggers, and built-in microphone in an iconic ergonomic design.",
        price: 69.99,
        category: "GAMING",
        categoryDisplay: "Gaming",
        stockQuantity: 40,
        rating: 4.8,
        imageUrl: "https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?w=500&q=80",
        tags: ["gaming", "playstation", "controller", "sony", "dualsense"]
    },
    {
        id: "PROD-GAME-02",
        name: "Razer BlackShark V2 Pro Gaming Headset",
        description: "Wireless esports headset with Razer TriForce Titanium 50mm drivers and HyperClear supercardioid mic.",
        price: 179.99,
        category: "GAMING",
        categoryDisplay: "Gaming",
        stockQuantity: 14,
        rating: 4.6,
        imageUrl: "https://images.unsplash.com/photo-1599669454699-248893623440?w=500&q=80",
        tags: ["gaming", "headset", "razer", "audio", "esports"]
    }
];

// ============================================================================
// MAIN APP & CATALOG MANAGER
// ============================================================================
const app = {
    products: [...SEED_PRODUCTS],
    selectedCategory: 'ALL',
    searchQuery: '',
    currentSort: 'featured',

    async init() {
        this.renderProducts();
        await this.checkBackendStatus();
        await this.loadProducts();
        await cartManager.loadCart();
    },

    async checkBackendStatus() {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 1200);
            const res = await fetch(`${API_BASE}/api/products`, { 
                headers: getHeaders(), 
                signal: controller.signal 
            });
            clearTimeout(timeoutId);
            if (res.ok) {
                isBackendAvailable = true;
                this.updateModeIndicator(true);
                return;
            }
        } catch (e) {
            // Backend offline, fallback active
        }
        isBackendAvailable = false;
        this.updateModeIndicator(false);
    },

    updateModeIndicator(isOnline) {
        let badge = document.getElementById('backendStatusBadge');
        if (!badge) {
            badge = document.createElement('span');
            badge.id = 'backendStatusBadge';
            badge.style.fontSize = '0.72rem';
            badge.style.fontWeight = '700';
            badge.style.padding = '3px 8px';
            badge.style.borderRadius = '9999px';
            badge.style.marginLeft = '8px';
            const navText = document.querySelector('.logo-text');
            if (navText) navText.appendChild(badge);
        }

        if (isOnline) {
            badge.textContent = '🟢 Server Online';
            badge.style.background = 'rgba(16, 185, 129, 0.2)';
            badge.style.color = '#34d399';
            badge.style.border = '1px solid rgba(16, 185, 129, 0.4)';
        } else {
            badge.textContent = '⚡ Instant Mode';
            badge.style.background = 'rgba(99, 102, 241, 0.2)';
            badge.style.color = '#a5b4fc';
            badge.style.border = '1px solid rgba(99, 102, 241, 0.4)';
        }
    },

    async loadProducts() {
        if (isBackendAvailable) {
            try {
                let url = `${API_BASE}/api/products`;
                const params = new URLSearchParams();
                if (this.selectedCategory !== 'ALL') params.append('category', this.selectedCategory);
                if (this.searchQuery.trim()) params.append('query', this.searchQuery.trim());
                if ([...params].length > 0) url += `?${params.toString()}`;

                const res = await fetch(url, { headers: getHeaders() });
                if (res.ok) {
                    this.products = await res.json();
                    this.renderProducts();
                    return;
                }
            } catch (err) {
                console.warn('Backend fetch failed, using local catalog:', err);
            }
        }

        // Local filtering
        let list = [...SEED_PRODUCTS];
        if (this.selectedCategory !== 'ALL') {
            list = list.filter(p => p.category === this.selectedCategory);
        }
        if (this.searchQuery.trim()) {
            const qTokens = this.searchQuery.toLowerCase().trim().split(/\s+/);
            list = list.filter(p => {
                const pText = (p.name + " " + p.description + " " + p.tags.join(" ")).toLowerCase();
                return qTokens.every(tok => pText.includes(tok));
            });
        }
        this.products = list;
        this.renderProducts();
    },

    renderProducts() {
        const grid = document.getElementById('productGrid');
        const countLabel = document.getElementById('productCountLabel');
        const searchClear = document.getElementById('clearSearchBtn');

        if (!grid) return;
        if (searchClear) {
            searchClear.style.display = this.searchQuery ? 'block' : 'none';
        }

        let sorted = [...this.products];
        if (this.currentSort === 'price-asc') {
            sorted.sort((a, b) => a.price - b.price);
        } else if (this.currentSort === 'price-desc') {
            sorted.sort((a, b) => b.price - a.price);
        } else if (this.currentSort === 'name') {
            sorted.sort((a, b) => a.name.localeCompare(b.name));
        } else {
            sorted.sort((a, b) => b.rating - a.rating);
        }

        if (countLabel) {
            countLabel.textContent = `Showing ${sorted.length} item${sorted.length === 1 ? '' : 's'}`;
        }

        if (sorted.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 50px 20px; color: var(--text-muted);">
                    <div style="font-size: 2.5rem; margin-bottom: 10px;">🔍</div>
                    <h3>No products found</h3>
                    <p>Try searching for different keywords or clear your filters.</p>
                    <button class="btn btn-secondary" style="margin-top: 14px;" onclick="app.resetFilters()">Clear Filters</button>
                </div>
            `;
            return;
        }

        grid.innerHTML = sorted.map(p => {
            const stockClass = p.stockQuantity <= 5 ? 'stock-low' : 'stock-in';
            const stockLabel = p.stockQuantity <= 5 ? `Low Stock (${p.stockQuantity})` : `In Stock (${p.stockQuantity})`;
            return `
                <div class="product-card" data-id="${p.id}">
                    <div class="product-img-box">
                        <img src="${p.imageUrl}" alt="${p.name}" loading="lazy">
                        <span class="product-category-tag">${p.categoryDisplay || p.category}</span>
                        <span class="product-stock-tag ${stockClass}">${stockLabel}</span>
                    </div>
                    <div class="product-body">
                        <h3 class="product-title">${p.name}</h3>
                        <div class="product-rating">
                            <span>★</span> <strong>${p.rating.toFixed(1)}</strong>
                            <span style="color: var(--text-muted); font-size: 0.75rem;">(verified)</span>
                        </div>
                        <p class="product-desc">${p.description}</p>
                        <div class="product-footer">
                            <span class="product-price">$${p.price.toFixed(2)}</span>
                            <button class="btn btn-add-cart" onclick="cartManager.addItem('${p.id}', 1)">
                                <span>🛒</span> Add
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    setCategory(cat) {
        this.selectedCategory = cat;
        document.querySelectorAll('.cat-pill').forEach(pill => {
            pill.classList.toggle('active', pill.dataset.category === cat);
        });
        const sectionTitle = document.getElementById('sectionTitle');
        if (sectionTitle) {
            sectionTitle.textContent = cat === 'ALL' ? 'Featured Products' : `${cat} Products`;
        }
        this.loadProducts();
    },

    handleSearch(val) {
        this.searchQuery = val;
        this.loadProducts();
    },

    clearSearch() {
        const input = document.getElementById('searchInput');
        if (input) input.value = '';
        this.searchQuery = '';
        this.loadProducts();
    },

    handleSort(sortVal) {
        this.currentSort = sortVal;
        this.renderProducts();
    },

    resetFilters() {
        const input = document.getElementById('searchInput');
        if (input) input.value = '';
        this.searchQuery = '';
        this.setCategory('ALL');
    }
};

// ============================================================================
// CART MANAGEMENT (Local Engine + Backend Sync)
// ============================================================================
const cartManager = {
    cart: { items: [], subtotal: 0, discountAmount: 0, tax: 0, shippingFee: 0, total: 0, totalItemCount: 0, appliedCoupon: null },

    async loadCart() {
        // Try backend first
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart`, { headers: getHeaders() });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    return;
                }
            } catch (err) {
                console.warn('Cart backend load failed, using local cart:', err);
            }
        }

        // Load local cart from localStorage
        const stored = localStorage.getItem('indusxcart_local_cart');
        if (stored) {
            try {
                this.cart = JSON.parse(stored);
            } catch (e) {}
        }
        this.recalculate();
        this.updateUi();
    },

    saveLocal() {
        localStorage.setItem('indusxcart_local_cart', JSON.stringify(this.cart));
    },

    recalculate() {
        let subtotal = 0;
        let count = 0;
        this.cart.items.forEach(item => {
            item.subtotal = Math.round(item.price * item.quantity * 100) / 100;
            subtotal += item.subtotal;
            count += item.quantity;
        });

        this.cart.subtotal = Math.round(subtotal * 100) / 100;
        this.cart.totalItemCount = count;

        // Shipping
        if (this.cart.items.length === 0) {
            this.cart.shippingFee = 0;
        } else if (this.cart.appliedCoupon && this.cart.appliedCoupon.type === 'FREE_SHIPPING') {
            this.cart.shippingFee = 0;
        } else {
            this.cart.shippingFee = this.cart.subtotal >= 50.0 ? 0 : 5.99;
        }

        // Discount
        let discount = 0;
        if (this.cart.appliedCoupon && this.cart.items.length > 0) {
            const c = this.cart.appliedCoupon;
            if (c.type === 'PERCENTAGE') {
                discount = Math.round((this.cart.subtotal * (c.value / 100)) * 100) / 100;
            } else if (c.type === 'FIXED_AMOUNT') {
                discount = Math.min(c.value, this.cart.subtotal);
            } else if (c.type === 'FREE_SHIPPING') {
                discount = 5.99;
            }
        }
        this.cart.discountAmount = discount;

        // Tax 8%
        const taxable = Math.max(0, this.cart.subtotal - discount);
        this.cart.tax = Math.round(taxable * 0.08 * 100) / 100;

        // Total
        if (this.cart.items.length === 0) {
            this.cart.total = 0;
        } else {
            this.cart.total = Math.round((taxable + this.cart.tax + this.cart.shippingFee) * 100) / 100;
        }

        this.saveLocal();
    },

    async addItem(productId, quantity = 1) {
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart/items`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ productId, quantity })
                });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    showToast(`Added to cart! Total: $${this.cart.total.toFixed(2)}`);
                    return;
                }
            } catch (err) {}
        }

        // Local engine
        const product = SEED_PRODUCTS.find(p => p.id === productId);
        if (!product) {
            showToast('Product not found', 'error');
            return;
        }

        let existing = this.cart.items.find(i => i.productId === productId);
        if (existing) {
            existing.quantity += quantity;
        } else {
            this.cart.items.push({
                productId: product.id,
                productName: product.name,
                price: product.price,
                imageUrl: product.imageUrl,
                quantity: quantity,
                subtotal: product.price * quantity,
                stockQuantity: product.stockQuantity
            });
        }

        this.recalculate();
        this.updateUi();
        showToast(`Added ${quantity}x ${product.name} to cart! Total: $${this.cart.total.toFixed(2)}`);
    },

    async updateQuantity(productId, quantity) {
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart/items`, {
                    method: 'PUT',
                    headers: getHeaders(),
                    body: JSON.stringify({ productId, quantity })
                });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    return;
                }
            } catch (err) {}
        }

        // Local
        if (quantity <= 0) {
            this.removeItem(productId);
            return;
        }
        const item = this.cart.items.find(i => i.productId === productId);
        if (item) {
            item.quantity = quantity;
            this.recalculate();
            this.updateUi();
        }
    },

    async removeItem(productId) {
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart/items?productId=${encodeURIComponent(productId)}`, {
                    method: 'DELETE',
                    headers: getHeaders()
                });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    showToast('Item removed from cart');
                    return;
                }
            } catch (err) {}
        }

        // Local
        this.cart.items = this.cart.items.filter(i => i.productId !== productId);
        this.recalculate();
        this.updateUi();
        showToast('Item removed from cart');
    },

    async applyCoupon() {
        const input = document.getElementById('couponInput');
        const code = input ? input.value.trim().toUpperCase() : '';
        if (!code) {
            showToast('Please enter a coupon code', 'error');
            return;
        }

        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart/coupon`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ code })
                });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    if (input) input.value = '';
                    showToast(`Coupon ${code} applied successfully!`);
                    return;
                }
            } catch (err) {}
        }

        // Local Coupon Engine
        const COUPONS = {
            'WELCOME10': { code: 'WELCOME10', type: 'PERCENTAGE', value: 10, min: 20 },
            'SAVE20': { code: 'SAVE20', type: 'FIXED_AMOUNT', value: 20, min: 100 },
            'FREESHIP': { code: 'FREESHIP', type: 'FREE_SHIPPING', value: 5.99, min: 0 },
            'TECH50': { code: 'TECH50', type: 'FIXED_AMOUNT', value: 50, min: 300 },
            'GAMER15': { code: 'GAMER15', type: 'PERCENTAGE', value: 15, min: 75 }
        };

        const coupon = COUPONS[code];
        if (!coupon) {
            showToast(`Invalid coupon code: '${code}'`, 'error');
            return;
        }

        if (this.cart.subtotal < coupon.min) {
            showToast(`Coupon ${code} requires minimum order of $${coupon.min.toFixed(2)}`, 'error');
            return;
        }

        this.cart.appliedCoupon = coupon;
        this.recalculate();
        this.updateUi();
        if (input) input.value = '';
        showToast(`Coupon ${code} applied! Discount: $${this.cart.discountAmount.toFixed(2)}`);
    },

    async removeCoupon() {
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart/coupon`, { method: 'DELETE', headers: getHeaders() });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    showToast('Coupon removed');
                    return;
                }
            } catch (err) {}
        }

        this.cart.appliedCoupon = null;
        this.recalculate();
        this.updateUi();
        showToast('Coupon removed');
    },

    async clearCart() {
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/cart/clear`, { method: 'POST', headers: getHeaders() });
                if (res.ok) {
                    this.cart = await res.json();
                    this.updateUi();
                    showToast('Shopping cart cleared');
                    return;
                }
            } catch (err) {}
        }

        this.cart.items = [];
        this.cart.appliedCoupon = null;
        this.recalculate();
        this.updateUi();
        showToast('Shopping cart cleared');
    },

    updateUi() {
        const headerBadge = document.getElementById('cartCountBadge');
        const headerSubtotal = document.getElementById('cartHeaderSubtotal');
        const drawerCount = document.getElementById('drawerCartCount');

        if (headerBadge) headerBadge.textContent = this.cart.totalItemCount || 0;
        if (headerSubtotal) headerSubtotal.textContent = `$${(this.cart.total || 0).toFixed(2)}`;
        if (drawerCount) drawerCount.textContent = `${this.cart.totalItemCount || 0} item${this.cart.totalItemCount === 1 ? '' : 's'}`;

        const freeShipThreshold = 50.0;
        const subtotal = this.cart.subtotal || 0;
        const shippingStatus = document.getElementById('shippingStatusText');
        const shippingFill = document.getElementById('shippingFill');

        if (shippingStatus && shippingFill) {
            if (subtotal >= freeShipThreshold || (this.cart.appliedCoupon && this.cart.appliedCoupon.type === 'FREE_SHIPPING')) {
                shippingStatus.innerHTML = '🎉 You unlocked <strong>FREE Shipping</strong>!';
                shippingFill.style.width = '100%';
            } else {
                const diff = (freeShipThreshold - subtotal).toFixed(2);
                shippingStatus.innerHTML = `Add <strong>$${diff}</strong> more for <strong>FREE shipping</strong>!`;
                const pct = Math.min(100, Math.round((subtotal / freeShipThreshold) * 100));
                shippingFill.style.width = `${pct}%`;
            }
        }

        const container = document.getElementById('cartItemsContainer');
        const footer = document.getElementById('cartDrawerFooter');

        if (!this.cart.items || this.cart.items.length === 0) {
            if (container) {
                container.innerHTML = `
                    <div class="empty-cart-state">
                        <div class="empty-cart-icon">🛒</div>
                        <h4>Your Cart is Empty</h4>
                        <p style="font-size: 0.85rem; margin-top: 6px;">Looks like you haven't added any gear yet.</p>
                        <button class="btn btn-secondary btn-sm" style="margin-top: 14px;" onclick="cartManager.closeDrawer()">Start Shopping</button>
                    </div>
                `;
            }
            if (footer) footer.style.opacity = '0.5';
        } else {
            if (footer) footer.style.opacity = '1';
            if (container) {
                container.innerHTML = this.cart.items.map(item => `
                    <div class="cart-item-card">
                        <img src="${item.imageUrl}" class="cart-item-img" alt="${item.productName}">
                        <div class="cart-item-details">
                            <div class="cart-item-top">
                                <span class="cart-item-name">${item.productName}</span>
                                <button class="btn-remove-item" onclick="cartManager.removeItem('${item.productId}')" title="Remove item">✕</button>
                            </div>
                            <div class="cart-item-bottom">
                                <div class="qty-control">
                                    <button class="qty-btn" onclick="cartManager.updateQuantity('${item.productId}', ${item.quantity - 1})">-</button>
                                    <span class="qty-display">${item.quantity}</span>
                                    <button class="qty-btn" onclick="cartManager.updateQuantity('${item.productId}', ${item.quantity + 1})">+</button>
                                </div>
                                <span class="cart-item-price">$${item.subtotal.toFixed(2)}</span>
                            </div>
                        </div>
                    </div>
                `).join('');
            }
        }

        const couponInputGroup = document.getElementById('couponInputGroup');
        const appliedTag = document.getElementById('appliedCouponTag');
        const couponName = document.getElementById('appliedCouponName');

        if (this.cart.appliedCoupon) {
            if (couponInputGroup) couponInputGroup.style.display = 'none';
            if (appliedTag) appliedTag.style.display = 'flex';
            if (couponName) couponName.textContent = `🏷️ ${this.cart.appliedCoupon.code} (-$${this.cart.discountAmount.toFixed(2)})`;
        } else {
            if (couponInputGroup) couponInputGroup.style.display = 'flex';
            if (appliedTag) appliedTag.style.display = 'none';
        }

        const summarySubtotal = document.getElementById('summarySubtotal');
        const discountRow = document.getElementById('discountRow');
        const summaryDiscount = document.getElementById('summaryDiscount');
        const summaryTax = document.getElementById('summaryTax');
        const summaryShipping = document.getElementById('summaryShipping');
        const summaryTotal = document.getElementById('summaryTotal');
        const checkoutModalTotal = document.getElementById('checkoutModalTotal');

        if (summarySubtotal) summarySubtotal.textContent = `$${(this.cart.subtotal || 0).toFixed(2)}`;
        if (discountRow) {
            discountRow.style.display = this.cart.discountAmount > 0 ? 'flex' : 'none';
            if (summaryDiscount) summaryDiscount.textContent = `-$${(this.cart.discountAmount || 0).toFixed(2)}`;
        }
        if (summaryTax) summaryTax.textContent = `$${(this.cart.tax || 0).toFixed(2)}`;
        if (summaryShipping) {
            summaryShipping.textContent = this.cart.shippingFee === 0 ? 'FREE' : `$${(this.cart.shippingFee || 0).toFixed(2)}`;
        }
        if (summaryTotal) summaryTotal.textContent = `$${(this.cart.total || 0).toFixed(2)}`;
        if (checkoutModalTotal) checkoutModalTotal.textContent = `$${(this.cart.total || 0).toFixed(2)}`;
    },

    openDrawer() {
        document.getElementById('cartDrawer').classList.add('open');
        document.getElementById('cartBackdrop').classList.add('open');
    },

    closeDrawer() {
        document.getElementById('cartDrawer').classList.remove('open');
        document.getElementById('cartBackdrop').classList.remove('open');
    }
};

// ============================================================================
// CHECKOUT MANAGER
// ============================================================================
const checkoutManager = {
    selectedPaymentMethod: 'CREDIT_CARD',

    openModal() {
        if (!cartManager.cart.items || cartManager.cart.items.length === 0) {
            showToast('Your cart is empty. Please add items before checking out.', 'error');
            return;
        }
        cartManager.closeDrawer();
        document.getElementById('checkoutBackdrop').classList.add('open');
        document.getElementById('checkoutBody').style.display = 'block';
        document.getElementById('orderReceiptView').style.display = 'none';
    },

    closeModal() {
        document.getElementById('checkoutBackdrop').classList.remove('open');
    },

    selectPayment(method) {
        this.selectedPaymentMethod = method;
        const cardFields = document.getElementById('paymentCardFields');
        const upiFields = document.getElementById('paymentUpiFields');
        if (cardFields) cardFields.style.display = method === 'CREDIT_CARD' ? 'block' : 'none';
        if (upiFields) upiFields.style.display = method === 'UPI' ? 'block' : 'none';
    },

    async submitOrder(event) {
        event.preventDefault();

        const customerName = document.getElementById('custName').value.trim();
        const customerEmail = document.getElementById('custEmail').value.trim();
        const customerPhone = document.getElementById('custPhone').value.trim();
        const shippingAddress = document.getElementById('shippingAddress').value.trim();

        let paymentDetails = '';
        if (this.selectedPaymentMethod === 'CREDIT_CARD') {
            paymentDetails = document.getElementById('cardNum').value.trim();
        } else if (this.selectedPaymentMethod === 'UPI') {
            paymentDetails = document.getElementById('upiId').value.trim();
        }

        const payload = {
            customerName,
            customerEmail,
            customerPhone,
            shippingAddress,
            paymentMethod: this.selectedPaymentMethod,
            paymentDetails
        };

        const submitBtn = document.getElementById('submitOrderBtn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Processing Payment... 🔒';
        }

        let orderData = null;

        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/checkout`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify(payload)
                });
                if (res.ok) {
                    orderData = await res.json();
                }
            } catch (err) {}
        }

        // Local fallback order generation
        if (!orderData) {
            orderData = {
                orderId: "ORD-" + Math.floor(100000 + Math.random() * 900000) + "-" + Math.random().toString(36).substring(2, 6).toUpperCase(),
                trackingNumber: "TRK-" + Math.random().toString(36).substring(2, 12).toUpperCase(),
                transactionId: "TXN-" + Date.now() + "-" + Math.random().toString(36).substring(2, 6).toUpperCase(),
                totalAmount: cartManager.cart.total,
                shippingAddress: shippingAddress
            };
            cartManager.clearCart();
        }

        // Show receipt
        document.getElementById('checkoutBody').style.display = 'none';
        const receipt = document.getElementById('orderReceiptView');
        receipt.style.display = 'block';

        document.getElementById('receiptOrderId').textContent = orderData.orderId;
        document.getElementById('receiptTracking').textContent = orderData.trackingNumber;
        document.getElementById('receiptTransaction').textContent = orderData.transactionId;
        document.getElementById('receiptTotal').textContent = `$${orderData.totalAmount.toFixed(2)}`;
        document.getElementById('receiptAddress').textContent = orderData.shippingAddress;

        await cartManager.loadCart();
        showToast(`Order ${orderData.orderId} confirmed!`);

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Place Order Now 🚀';
        }
    },

    finishAndShop() {
        this.closeModal();
    }
};

// ============================================================================
// AI CHATBOT ASSISTANT (Dual-Mode: Backend + Local NLP)
// ============================================================================
const chatAssistant = {
    isOpen: false,

    toggle() {
        this.isOpen = !this.isOpen;
        const widget = document.getElementById('chatWidget');
        widget.classList.toggle('open', this.isOpen);
        if (this.isOpen) {
            setTimeout(() => {
                const input = document.getElementById('chatInputField');
                if (input) input.focus();
            }, 300);
        }
    },

    openWithPrompt(prompt) {
        if (!this.isOpen) this.toggle();
        this.sendPrompt(prompt);
    },

    async sendMessage(event) {
        event.preventDefault();
        const input = document.getElementById('chatInputField');
        const text = input ? input.value.trim() : '';
        if (!text) return;

        input.value = '';
        this.appendMessage('user', text);
        await this.queryAi(text);
    },

    sendPrompt(promptText) {
        this.appendMessage('user', promptText);
        this.queryAi(promptText);
    },

    async queryAi(queryText) {
        const typingId = this.appendTypingIndicator();

        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/ai/chat`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ message: queryText })
                });
                if (res.ok) {
                    this.removeTypingIndicator(typingId);
                    const data = await res.json();
                    this.appendMessage('bot', data.message, data.data, data.action);
                    this.updateSuggestions(data.suggestions);

                    if (data.action === 'CART_UPDATED') await cartManager.loadCart();
                    else if (data.action === 'OPEN_CHECKOUT') checkoutManager.openModal();
                    else if (data.action === 'VIEW_CART') cartManager.openDrawer();
                    return;
                }
            } catch (err) {}
        }

        // Local NLP Engine Fallback
        setTimeout(() => {
            this.removeTypingIndicator(typingId);
            const localResp = this.processLocalNlp(queryText);
            this.appendMessage('bot', localResp.message, localResp.data, localResp.action);
            this.updateSuggestions(localResp.suggestions);

            if (localResp.action === 'CART_UPDATED') {
                cartManager.updateUi();
            } else if (localResp.action === 'OPEN_CHECKOUT') {
                checkoutManager.openModal();
            } else if (localResp.action === 'VIEW_CART') {
                cartManager.openDrawer();
            }
        }, 400);
    },

    processLocalNlp(query) {
        const text = query.toLowerCase();

        // 1. Add to cart
        if (text.startsWith('add') || text.includes('put in cart') || text.includes('add to cart')) {
            const clean = text.replace(/^(add|put)\s+/, '').replace(/\s+(to|into|in)\s+(my\s+)?cart.*$/, '').trim();
            const matched = SEED_PRODUCTS.find(p => p.name.toLowerCase().includes(clean) || p.tags.some(t => clean.includes(t)));
            if (matched) {
                cartManager.addItem(matched.id, 1);
                return {
                    message: `Added **${matched.name}** to your cart ($${matched.price.toFixed(2)}). Your cart total is now **$${cartManager.cart.total.toFixed(2)}**.`,
                    action: 'CART_UPDATED',
                    suggestions: ['View Cart', 'Proceed to Checkout', 'Recommend accessories']
                };
            }
        }

        // 2. View Cart
        if (text.includes('cart') || text.includes('my items')) {
            if (cartManager.cart.items.length === 0) {
                return {
                    message: "Your cart is currently empty. Would you like some product recommendations?",
                    action: 'VIEW_CART',
                    suggestions: ['Show popular headphones', 'Browse Laptops', 'Show Gaming gear']
                };
            }
            return {
                message: `You have **${cartManager.cart.totalItemCount} items** in your cart. Subtotal: **$${cartManager.cart.subtotal.toFixed(2)}**, Total: **$${cartManager.cart.total.toFixed(2)}**.`,
                action: 'VIEW_CART',
                suggestions: ['Proceed to Checkout', 'Apply coupon WELCOME10', 'Empty Cart']
            };
        }

        // 3. Checkout
        if (text.includes('checkout') || text.includes('pay') || text.includes('place order')) {
            return {
                message: `You're ready to checkout **${cartManager.cart.totalItemCount} items** for a total of **$${cartManager.cart.total.toFixed(2)}**! Opening checkout window...`,
                action: 'OPEN_CHECKOUT',
                suggestions: ['Place Order', 'Edit Cart']
            };
        }

        // 4. Coupons
        if (text.includes('coupon') || text.includes('code') || text.includes('promo')) {
            const m = text.match(/(welcome10|save20|freeship|tech50|gamer15)/i);
            if (m) {
                const code = m[1].toUpperCase();
                document.getElementById('couponInput').value = code;
                cartManager.applyCoupon();
                return {
                    message: `Applied coupon **${code}**! Your new total is **$${cartManager.cart.total.toFixed(2)}**.`,
                    action: 'CART_UPDATED',
                    suggestions: ['Proceed to Checkout', 'View Cart']
                };
            }
            return {
                message: "Available coupons: **WELCOME10** (10% off), **SAVE20** ($20 off over $100), and **FREESHIP** (Free delivery).",
                action: 'NONE',
                suggestions: ['Apply WELCOME10', 'Apply SAVE20', 'Apply FREESHIP']
            };
        }

        // 5. Dynamic Product Search & Recommendations
        let cleanQuery = text;
        cleanQuery = cleanQuery.replace(/^(can you |please |could you |hey |hi |ok |assistant |tell me |help me )+/gi, '');
        cleanQuery = cleanQuery.replace(/^(search for|search|find me|find|show me|show|look for|look up|browse|get me|i want to see|i want|do you have|where is|where are|display)\s+/gi, '');
        cleanQuery = cleanQuery.replace(/\s+(dikhao|khojo|dhundo|batao|dekho|search karo|chahiye)$/gi, '');
        cleanQuery = cleanQuery.replace(/^(dikhao|khojo|dhundo|batao)\s+/gi, '');
        cleanQuery = cleanQuery.replace(/[?!.,;]/g, '').trim();

        const tokens = cleanQuery.split(/\s+/).filter(t => t.length > 1);
        let matches = [];
        if (tokens.length > 0) {
            matches = SEED_PRODUCTS.filter(p => {
                const pText = (p.name + " " + p.category + " " + p.description + " " + p.tags.join(" ")).toLowerCase();
                return tokens.some(tok => pText.includes(tok));
            });
        }

        if (matches.length > 0) {
            return {
                message: `Found **${matches.length}** product(s) matching '**${cleanQuery}**':`,
                action: 'SHOW_PRODUCTS',
                data: matches,
                suggestions: [matches[0].name.slice(0, 22), 'View Cart', 'Checkout']
            };
        } else if (cleanQuery.length > 1) {
            return {
                message: `No products found matching '**${cleanQuery}**'. Try searching for **headphones**, **MacBook**, or **keyboard**. Here are our top featured items:`,
                action: 'SHOW_RECOMMENDATIONS',
                data: SEED_PRODUCTS.slice(0, 3),
                suggestions: ['Show Headphones', 'Show MacBook', 'Browse Keyboards']
            };
        }

        return {
            message: `Welcome to **IndusXCart**! Here are our top recommendations for you:`,
            action: 'SHOW_RECOMMENDATIONS',
            data: SEED_PRODUCTS.slice(0, 3),
            suggestions: ['Search Headphones', 'Show MacBook', 'What is in my cart?']
        };
    },

    appendMessage(sender, text, data = null, action = null) {
        const container = document.getElementById('chatMessages');
        if (!container) return;

        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${sender}-bubble`;

        let formattedText = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                                .replace(/\n/g, '<br>');

        let extraHtml = '';
        if (data && Array.isArray(data) && (action === 'SHOW_PRODUCTS' || action === 'SHOW_RECOMMENDATIONS')) {
            extraHtml = `
                <div style="margin-top: 10px; display: flex; flex-direction: column; gap: 8px;">
                    ${data.slice(0, 3).map(p => `
                        <div class="chat-mini-card">
                            <div>
                                <strong style="font-size: 0.85rem; color: #ffffff;">${p.name}</strong>
                                <div style="font-size: 0.8rem; color: #34d399;">$${p.price.toFixed(2)}</div>
                            </div>
                            <button class="btn btn-add-cart btn-sm" onclick="cartManager.addItem('${p.id}', 1)" style="padding: 4px 10px; font-size: 0.75rem;">
                                Add
                            </button>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        bubble.innerHTML = `<div class="bubble-content">${formattedText}${extraHtml}</div>`;
        container.appendChild(bubble);
        container.scrollTop = container.scrollHeight;
    },

    appendTypingIndicator() {
        const container = document.getElementById('chatMessages');
        const id = 'typing-' + Date.now();
        const bubble = document.createElement('div');
        bubble.id = id;
        bubble.className = 'chat-bubble bot-bubble';
        bubble.innerHTML = '<div class="bubble-content" style="font-style: italic; color: var(--text-muted);">IndusX is thinking... ✨</div>';
        container.appendChild(bubble);
        container.scrollTop = container.scrollHeight;
        return id;
    },

    removeTypingIndicator(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    },

    updateSuggestions(suggestions) {
        const container = document.getElementById('chatSuggestions');
        if (!container || !suggestions || suggestions.length === 0) return;

        container.innerHTML = suggestions.map(s => `
            <button class="suggestion-chip" onclick="chatAssistant.sendPrompt('${s.replace(/'/g, "\\'")}')">${s}</button>
        `).join('');
    }
};

// ============================================================================
// VOICE ASSISTANT (Dual-Mode: Backend + Web Speech STT/TTS)
// ============================================================================
const voiceAssistant = {
    recognition: null,
    isListening: false,

    init() {
        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (SpeechRec) {
            this.recognition = new SpeechRec();
            this.recognition.continuous = false;
            this.recognition.interimResults = true;
            this.recognition.lang = 'en-US';

            this.recognition.onstart = () => {
                this.isListening = true;
                this.setUiListening(true);
            };

            this.recognition.onresult = (event) => {
                let interim = '';
                let final = '';
                for (let i = event.resultIndex; i < event.results.length; ++i) {
                    if (event.results[i].isFinal) {
                        final += event.results[i][0].transcript;
                    } else {
                        interim += event.results[i][0].transcript;
                    }
                }

                const transcriptEl = document.getElementById('voiceTranscript');
                if (transcriptEl) transcriptEl.textContent = `"${final || interim}"`;

                if (final) this.processTranscript(final);
            };

            this.recognition.onerror = (event) => {
                const statusEl = document.getElementById('voiceStatus');
                if (statusEl) statusEl.textContent = `Listening stopped. Click any voice test chip below!`;
                this.setUiListening(false);
            };

            this.recognition.onend = () => {
                this.isListening = false;
                this.setUiListening(false);
            };
        }
    },

    toggle() {
        const modal = document.getElementById('voiceModal');
        const isVisible = modal.style.display !== 'none';
        if (isVisible) this.stopListening();
        else this.startListening();
    },

    startListening() {
        const modal = document.getElementById('voiceModal');
        modal.style.display = 'flex';

        const transcriptEl = document.getElementById('voiceTranscript');
        if (transcriptEl) transcriptEl.textContent = '"Listening for your command..."';

        const respBox = document.getElementById('voiceResponseBox');
        if (respBox) respBox.style.display = 'none';

        if (this.recognition) {
            try { this.recognition.start(); } catch (e) {}
        } else {
            const statusEl = document.getElementById('voiceStatus');
            if (statusEl) statusEl.textContent = 'Click any voice command chip below to test!';
        }
    },

    stopListening() {
        if (this.recognition && this.isListening) this.recognition.stop();
        const modal = document.getElementById('voiceModal');
        modal.style.display = 'none';
        if ('speechSynthesis' in window) window.speechSynthesis.cancel();
    },

    setUiListening(listening) {
        const statusEl = document.getElementById('voiceStatus');
        const bars = document.getElementById('waveformBars');
        if (statusEl) statusEl.textContent = listening ? 'Listening... Speak now 🎙️' : 'Processing audio... ✨';
        if (bars) bars.style.opacity = listening ? '1' : '0.4';
    },

    simulateUtterance(command) {
        const modal = document.getElementById('voiceModal');
        modal.style.display = 'flex';

        const transcriptEl = document.getElementById('voiceTranscript');
        if (transcriptEl) transcriptEl.textContent = `"${command}"`;

        this.processTranscript(command);
    },

    async processTranscript(transcript) {
        const statusEl = document.getElementById('voiceStatus');
        if (statusEl) statusEl.textContent = 'IndusX Assistant analyzing voice command... ⚡';

        let spoken = "";
        let action = "";
        let cleanQuery = "";
        let searchMatches = [];

        // Try cloud backend first
        if (isBackendAvailable) {
            try {
                const res = await fetch(`${API_BASE}/api/ai/voice`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ transcript })
                });
                if (res.ok) {
                    const data = await res.json();
                    spoken = data.spokenResponse;
                    action = data.action;
                    cleanQuery = data.query || "";
                    searchMatches = data.data || [];
                }
            } catch (err) {
                console.warn("Backend voice API unavailable, using local voice intelligence:", err);
            }
        }

        // Local Voice Intelligence (runs instantly with dynamic search reading!)
        if (!spoken) {
            const raw = transcript.trim();
            const lower = raw.toLowerCase();

            // 1. ADD TO CART INTENT
            const isAdd = lower.includes('add') || lower.includes('buy') || lower.includes('daalo') || lower.includes('kharido') || lower.includes('cart mein');
            if (isAdd && (lower.includes('add') || lower.includes('buy') || lower.includes('daalo') || lower.includes('kharido'))) {
                let bestMatch = null;
                for (const p of SEED_PRODUCTS) {
                    const pName = p.name.toLowerCase();
                    if (lower.includes(pName) || p.tags.some(t => lower.includes(t.toLowerCase()))) {
                        bestMatch = p;
                        break;
                    }
                }
                if (!bestMatch) {
                    for (const p of SEED_PRODUCTS) {
                        const words = p.name.toLowerCase().split(/\s+/);
                        if (words.some(w => w.length > 3 && lower.includes(w))) {
                            bestMatch = p;
                            break;
                        }
                    }
                }

                if (bestMatch) {
                    const qtyMatch = lower.match(/\b([1-9]|one|two|three|four|five)\b/);
                    let qty = 1;
                    if (qtyMatch) {
                        const numWord = qtyMatch[1];
                        const map = { one: 1, two: 2, three: 3, four: 4, five: 5 };
                        qty = map[numWord] || parseInt(numWord, 10) || 1;
                    }
                    cartManager.addItem(bestMatch.id, qty);
                    spoken = `Added ${qty > 1 ? qty + ' ' : ''}${bestMatch.name} to your cart. Your new cart total is $${cartManager.cart.total.toFixed(2)}. Would you like to checkout?`;
                    action = "CART_UPDATED";
                } else {
                    spoken = `I couldn't identify which product you want to add. Please specify the name, like "Add Sony headphones to cart".`;
                    action = "NONE";
                }
            }
            // 2. VIEW CART INTENT
            else if (lower.includes('cart') && (lower.includes('what') || lower.includes('show') || lower.includes('view') || lower.includes('check') || lower.includes('kya') || lower.includes('dikhao') || lower.includes('in my') || lower.trim() === 'cart')) {
                if (cartManager.cart.items.length === 0) {
                    spoken = "Your shopping cart is currently empty. You can ask me to search for headphones, MacBook, or gaming gear!";
                } else {
                    const itemsDesc = cartManager.cart.items.slice(0, 2).map(i => `${i.quantity}x ${i.productName}`).join(' and ');
                    spoken = `You have ${cartManager.cart.totalItemCount} items in your cart including ${itemsDesc}. Your total is $${cartManager.cart.total.toFixed(2)}.`;
                }
                action = "VIEW_CART";
            }
            // 3. CHECKOUT INTENT
            else if (lower.includes('checkout') || lower.includes('place order') || lower.includes('pay') || lower.includes('order karo')) {
                if (cartManager.cart.items.length === 0) {
                    spoken = "Your cart is currently empty. Please add items before checking out.";
                    action = "NONE";
                } else {
                    spoken = `Ready for checkout! Opening secure payment for your ${cartManager.cart.totalItemCount} items totaling $${cartManager.cart.total.toFixed(2)}.`;
                    action = "OPEN_CHECKOUT";
                }
            }
            // 4. COUPON / DEALS INTENT
            else if (lower.includes('coupon') || lower.includes('discount') || lower.includes('offer') || lower.includes('deal') || lower.includes('promo') || lower.includes('save20') || lower.includes('welcome10')) {
                if (lower.includes('save20')) {
                    const input = document.getElementById('couponInput');
                    if (input) input.value = 'SAVE20';
                    cartManager.applyCoupon();
                    spoken = `Applied coupon SAVE20! You saved $${cartManager.cart.discountAmount.toFixed(2)}. Your new total is $${cartManager.cart.total.toFixed(2)}.`;
                    action = "CART_UPDATED";
                } else if (lower.includes('welcome10')) {
                    const input = document.getElementById('couponInput');
                    if (input) input.value = 'WELCOME10';
                    cartManager.applyCoupon();
                    spoken = `Applied coupon WELCOME10 for 10% off! Your new total is $${cartManager.cart.total.toFixed(2)}.`;
                    action = "CART_UPDATED";
                } else {
                    spoken = "We have two active coupons: WELCOME10 for 10% off, and SAVE20 for $20 off on orders over $100. Say 'Apply coupon SAVE20' to use it!";
                    action = "NONE";
                }
            }
            // 5. CLEAR CART
            else if (lower.includes('clear cart') || lower.includes('empty cart') || lower.includes('cart khali')) {
                cartManager.clearCart();
                spoken = "I have emptied your shopping cart.";
                action = "CART_UPDATED";
            }
            // 6. DYNAMIC VOICE SEARCH INTENT (Reads current search results aloud!)
            else {
                cleanQuery = lower;
                cleanQuery = cleanQuery.replace(/^(can you |please |could you |hey |hi |ok |assistant |tell me |help me )+/gi, '');
                cleanQuery = cleanQuery.replace(/^(search for|search|find me|find|show me|show|look for|look up|browse|get me|i want to see|i want|do you have|where is|where are|display)\s+/gi, '');
                cleanQuery = cleanQuery.replace(/\s+(dikhao|khojo|dhundo|batao|dekho|search karo|chahiye)$/gi, '');
                cleanQuery = cleanQuery.replace(/^(dikhao|khojo|dhundo|batao)\s+/gi, '');
                cleanQuery = cleanQuery.replace(/[?!.,;]/g, '').trim();

                const tokens = cleanQuery.split(/\s+/).filter(t => t.length > 1);
                let matches = [];
                if (tokens.length > 0) {
                    matches = SEED_PRODUCTS.filter(p => {
                        const pText = (p.name + " " + p.category + " " + p.description + " " + p.tags.join(" ")).toLowerCase();
                        return tokens.some(tok => pText.includes(tok));
                    });
                }

                if (matches.length > 0) {
                    searchMatches = matches;
                    if (matches.length === 1) {
                        const m = matches[0];
                        spoken = `I found 1 product for "${cleanQuery}": ${m.name} for $${m.price.toFixed(2)}. It has a rating of ${m.rating} stars. Showing it on your screen now!`;
                    } else if (matches.length === 2) {
                        spoken = `Found 2 products for "${cleanQuery}": ${matches[0].name} for $${matches[0].price.toFixed(2)}, and ${matches[1].name} for $${matches[1].price.toFixed(2)}. Displaying them on your screen!`;
                    } else {
                        const top2 = `${matches[0].name} for $${matches[0].price.toFixed(2)} and ${matches[1].name} for $${matches[1].price.toFixed(2)}`;
                        spoken = `I found ${matches.length} products for "${cleanQuery}". Top picks include ${top2}. Showing all results on your screen!`;
                    }
                    action = "SEARCH_RESULTS";
                } else if (cleanQuery.length > 1) {
                    spoken = `I searched for "${cleanQuery}", but could not find any matching products. You can search for headphones, MacBook, Keychron keyboard, or monitors.`;
                    action = "SEARCH_EMPTY";
                } else {
                    spoken = "I'm your IndusX Voice Assistant. Ask me to search headphones, show laptops, or say 'Add Sony headphones to cart'.";
                    action = "GREETING";
                }
            }
        }

        // Apply UI Updates based on Action
        if (action === 'SEARCH_RESULTS' || action === 'SEARCH') {
            const queryToUse = cleanQuery || transcript;
            const searchInput = document.getElementById('searchInput');
            if (searchInput) searchInput.value = queryToUse;
            app.handleSearch(queryToUse);
            const grid = document.getElementById('productGrid');
            if (grid) {
                setTimeout(() => {
                    grid.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }, 300);
            }
        } else if (action === 'SEARCH_EMPTY') {
            const queryToUse = cleanQuery || transcript;
            const searchInput = document.getElementById('searchInput');
            if (searchInput) searchInput.value = queryToUse;
            app.handleSearch(queryToUse);
        } else if (action === 'CART_UPDATED' || action === 'VIEW_CART') {
            cartManager.openDrawer();
        } else if (action === 'OPEN_CHECKOUT') {
            checkoutManager.openModal();
        }

        // Display assistant spoken text
        const respBox = document.getElementById('voiceResponseBox');
        const respText = document.getElementById('assistantSpeechText');
        if (respBox && respText) {
            respBox.style.display = 'block';
            respText.textContent = spoken;
        }

        if (statusEl) statusEl.textContent = 'Speaking response... 🔊';
        this.speak(spoken);
    },

    speak(text) {
        if (!('speechSynthesis' in window)) return;
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 1.05;
        window.speechSynthesis.speak(utterance);
    }
};

// ============================================================================
// BOOTSTRAP ON LOAD
// ============================================================================
window.addEventListener('DOMContentLoaded', () => {
    app.init();
    voiceAssistant.init();
});


