(function () {
  const STORAGE_KEY = "kkmall_mvp_state_v1";
  const ADMIN_PHONE = "13900000000";
  const MOCK_CODE = "123456";

  const seed = {
    currentUserId: null,
    categories: [
      { id: 1, name: "服饰", sortOrder: 1, enabled: true },
      { id: 2, name: "数码", sortOrder: 2, enabled: true },
      { id: 3, name: "家居", sortOrder: 3, enabled: true },
    ],
    users: [
      { id: 1, phone: ADMIN_PHONE, nickname: "管理员", role: "ADMIN" },
    ],
    products: [
      {
        id: 1,
        categoryId: 1,
        title: "舒适纯棉基础款 T 恤",
        description: "柔软棉感，适合日常通勤与周末出行。版型利落，单穿或内搭都清爽。",
        images: ["T"],
        status: "ON_SALE",
      },
      {
        id: 2,
        categoryId: 1,
        title: "轻量防泼水通勤双肩包",
        description: "多隔层收纳，轻量耐磨，能装下 14 英寸电脑。",
        images: ["包"],
        status: "ON_SALE",
      },
      {
        id: 3,
        categoryId: 2,
        title: "蓝牙降噪耳机 Pro",
        description: "舒适佩戴，长续航，通勤和办公都能保持沉浸。",
        images: ["耳"],
        status: "ON_SALE",
      },
      {
        id: 4,
        categoryId: 2,
        title: "桌面无线快充支架",
        description: "立式观看，随放随充，适合办公桌和床头柜。",
        images: ["充"],
        status: "ON_SALE",
      },
      {
        id: 5,
        categoryId: 3,
        title: "北欧风陶瓷马克杯",
        description: "温润釉面，容量适中，咖啡、茶饮都合适。",
        images: ["杯"],
        status: "ON_SALE",
      },
      {
        id: 6,
        categoryId: 3,
        title: "可折叠收纳整理箱",
        description: "稳固叠放，透明可视，换季收纳更省心。",
        images: ["箱"],
        status: "ON_SALE",
      },
    ],
    skus: [
      { id: 101, productId: 1, specName: "颜色", specValue: "白色", price: 9900, stock: 20 },
      { id: 102, productId: 1, specName: "颜色", specValue: "黑色", price: 9900, stock: 12 },
      { id: 201, productId: 2, specName: "颜色", specValue: "曜石黑", price: 15900, stock: 9 },
      { id: 202, productId: 2, specName: "颜色", specValue: "雾灰", price: 15900, stock: 6 },
      { id: 301, productId: 3, specName: "颜色", specValue: "云白", price: 29900, stock: 8 },
      { id: 302, productId: 3, specName: "颜色", specValue: "夜黑", price: 29900, stock: 5 },
      { id: 401, productId: 4, specName: "颜色", specValue: "银灰", price: 8900, stock: 14 },
      { id: 501, productId: 5, specName: "容量", specValue: "350ml", price: 6900, stock: 25 },
      { id: 601, productId: 6, specName: "尺寸", specValue: "中号", price: 7900, stock: 18 },
    ],
    cartItems: [],
    addresses: [],
    orders: [],
    orderItems: [],
    shipments: [],
  };

  let state = loadState();
  let selectedSkuId = null;
  let selectedQty = 1;
  let toastTimer = null;

  function loadState() {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return structuredClone(seed);
    try {
      return { ...structuredClone(seed), ...JSON.parse(raw) };
    } catch {
      return structuredClone(seed);
    }
  }

  function saveState() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }

  function nextId(items) {
    return items.length ? Math.max(...items.map((item) => Number(item.id))) + 1 : 1;
  }

  function $(selector) {
    return document.querySelector(selector);
  }

  function money(cents) {
    return `¥${(cents / 100).toFixed(2)}`;
  }

  function currentUser() {
    return state.users.find((user) => user.id === state.currentUserId) || null;
  }

  function isAdmin() {
    const user = currentUser();
    return Boolean(user && user.role === "ADMIN");
  }

  function productById(id) {
    return state.products.find((product) => product.id === Number(id));
  }

  function skuById(id) {
    return state.skus.find((sku) => sku.id === Number(id));
  }

  function skusForProduct(productId) {
    return state.skus.filter((sku) => sku.productId === Number(productId));
  }

  function categoryName(id) {
    return state.categories.find((category) => category.id === Number(id))?.name || "-";
  }

  function minPrice(productId) {
    const prices = skusForProduct(productId).map((sku) => sku.price);
    return prices.length ? Math.min(...prices) : 0;
  }

  function totalStock(productId) {
    return skusForProduct(productId).reduce((sum, sku) => sum + sku.stock, 0);
  }

  function shippingFee(productAmount) {
    return productAmount >= 9900 ? 0 : 1000;
  }

  function statusText(status) {
    return {
      PENDING_PAYMENT: "待支付",
      PAID_PENDING_SHIPMENT: "已支付/待发货",
      SHIPPED: "已发货",
      COMPLETED: "已完成",
      CANCELLED: "已取消",
    }[status] || status;
  }

  function statusClass(status) {
    return {
      PENDING_PAYMENT: "pending",
      PAID_PENDING_SHIPMENT: "paid",
      SHIPPED: "shipped",
      COMPLETED: "done",
      CANCELLED: "cancelled",
    }[status] || "";
  }

  function showToast(message) {
    clearTimeout(toastTimer);
    const existing = $(".toast");
    if (existing) existing.remove();
    document.body.insertAdjacentHTML("beforeend", `<div class="toast">${message}</div>`);
    toastTimer = setTimeout(() => $(".toast")?.remove(), 2400);
  }

  function navigate(hash) {
    window.location.hash = hash;
  }

  function route() {
    const hash = window.location.hash || "#/";
    const [path, query = ""] = hash.slice(1).split("?");
    const params = Object.fromEntries(new URLSearchParams(query).entries());

    if (path.startsWith("/admin")) return renderAdmin(path, params);

    const user = currentUser();
    const protectedPaths = ["/cart", "/checkout", "/orders", "/pay"];
    if (protectedPaths.some((protectedPath) => path === protectedPath || path.startsWith(`${protectedPath}/`)) && !user) {
      navigate(`#/login?redirect=${encodeURIComponent(hash)}`);
      return;
    }

    renderShell(path, params);
  }

  function renderShell(path, params) {
    const user = currentUser();
    const cartCount = user ? state.cartItems.filter((item) => item.userId === user.id && !item.deleted).reduce((sum, item) => sum + item.quantity, 0) : 0;
    $("#app").innerHTML = `
      <div class="app-shell">
        <div class="promo-strip" id="promoStrip">
          <div class="promo-strip-inner">
            <span class="promo-emoji">🎁</span>
            <strong>百亿加补节</strong>
            <span>叠券 7.7 折起 · 抢购最后 2 天</span>
            <button class="promo-strip-btn" data-link="#/products">立即抢购</button>
          </div>
          <button class="promo-close" id="promoClose" aria-label="关闭促销活动">×</button>
        </div>
        <header class="topbar">
          <div class="topbar-inner">
            <a class="logo" href="#/"><span class="logo-mark">K</span><span>KKMall</span></a>
            <form class="search" id="searchForm">
              <input name="keyword" value="${params.keyword || ""}" placeholder="搜索商品、分类、好物" />
              <button type="submit">搜索</button>
            </form>
            <div class="nav-actions">
              <button class="plain-btn" data-link="#/cart">购物车 ${cartCount ? `(${cartCount})` : ""}</button>
              <button class="plain-btn" data-link="#/orders">我的订单</button>
              ${
                user
                  ? `<button class="ghost-btn" id="logoutBtn">${user.nickname}</button>`
                  : `<button class="ghost-btn" data-link="#/login">登录</button>`
              }
              <button class="primary-btn" data-link="#/admin/products">后台</button>
            </div>
          </div>
        </header>
        <main id="page"></main>
        <nav class="mobile-nav">
          <button data-link="#/" class="${path === "/" ? "active" : ""}">首页</button>
          <button data-link="#/products" class="${path === "/products" ? "active" : ""}">分类</button>
          <button data-link="#/cart" class="${path === "/cart" ? "active" : ""}">购物车</button>
          <button data-link="#/orders" class="${path === "/orders" ? "active" : ""}">订单</button>
        </nav>
      </div>
    `;

    $("#searchForm").addEventListener("submit", (event) => {
      event.preventDefault();
      const keyword = new FormData(event.currentTarget).get("keyword").trim();
      navigate(`#/products${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ""}`);
    });
    $("#promoClose")?.addEventListener("click", () => {
      $("#promoStrip")?.remove();
    });
    $("#logoutBtn")?.addEventListener("click", () => {
      state.currentUserId = null;
      saveState();
      showToast("已退出登录");
      navigate("#/");
    });
    bindLinks();

    if (path === "/") renderHome();
    else if (path === "/products") renderProducts(params);
    else if (path.startsWith("/products/")) renderProductDetail(path.split("/")[2]);
    else if (path === "/login") renderLogin(params);
    else if (path === "/cart") renderCart();
    else if (path === "/checkout") renderCheckout();
    else if (path.startsWith("/pay/")) renderPay(path.split("/")[2]);
    else if (path === "/orders") renderOrders(params);
    else if (path.startsWith("/orders/")) renderOrderDetail(path.split("/")[2]);
    else renderHome();
  }

  function bindLinks(root = document) {
    root.querySelectorAll("[data-link]").forEach((node) => {
      node.addEventListener("click", () => navigate(node.dataset.link));
    });
  }

  function productCard(product) {
    return `
      <article class="product-card" data-link="#/products/${product.id}">
        <div class="product-image">${product.images?.[0] || "货"}</div>
        <div class="product-body">
          <p class="product-title">${product.title}</p>
          <div class="price"><small>¥</small>${(minPrice(product.id) / 100).toFixed(2)}</div>
          <div class="muted">${totalStock(product.id) <= 8 ? "库存紧张" : "现货速发"}</div>
        </div>
      </article>
    `;
  }

  function activeProducts() {
    return state.products.filter((product) => product.status === "ON_SALE" && !product.deleted);
  }

  function renderHome() {
    const products = activeProducts();
    $("#page").innerHTML = `
      <div class="container">
        <section class="hero">
          <div class="hero-main">
            <h1>今日精选好物，满 99 元包邮</h1>
            <p>从日常服饰到桌面数码，先把可靠、清楚、好买的体验做好。</p>
            <button class="secondary-btn" data-link="#/products">立即逛逛</button>
          </div>
          <div class="hero-side">
            <div class="promo-tile"><strong>模拟支付闭环</strong><span class="muted">下单、支付、发货、订单查询都能体验。</span></div>
            <div class="promo-tile"><strong>商家手动发货</strong><span class="muted">后台录入物流公司和单号。</span></div>
          </div>
        </section>

        <section class="section">
          <div class="section-head">
            <h2 class="section-title">分类入口</h2>
          </div>
          <div class="tabs">
            ${state.categories.map((category) => `<button class="tab" data-link="#/products?categoryId=${category.id}">${category.name}</button>`).join("")}
          </div>
        </section>

        <section class="section">
          <div class="section-head">
            <h2 class="section-title">精选商品</h2>
            <button class="ghost-btn" data-link="#/products">查看全部</button>
          </div>
          <div class="product-grid">${products.slice(0, 8).map(productCard).join("")}</div>
        </section>
      </div>
    `;
    bindLinks($("#page"));
  }

  function renderProducts(params) {
    const categoryId = params.categoryId ? Number(params.categoryId) : null;
    const keyword = params.keyword?.trim() || "";
    const products = activeProducts().filter((product) => {
      const matchCategory = !categoryId || product.categoryId === categoryId;
      const matchKeyword = !keyword || product.title.includes(keyword);
      return matchCategory && matchKeyword;
    });
    $("#page").innerHTML = `
      <div class="container">
        <div class="section-head">
          <h1 class="section-title">${keyword ? `搜索：${keyword}` : categoryId ? categoryName(categoryId) : "全部商品"}</h1>
        </div>
        <div class="tabs">
          <button class="tab ${!categoryId ? "active" : ""}" data-link="#/products">全部</button>
          ${state.categories.map((category) => `<button class="tab ${category.id === categoryId ? "active" : ""}" data-link="#/products?categoryId=${category.id}">${category.name}</button>`).join("")}
        </div>
        <section class="section">
          ${
            products.length
              ? `<div class="product-grid">${products.map(productCard).join("")}</div>`
              : `<div class="card empty">暂无商品<br><button class="primary-btn" data-link="#/">回首页看看</button></div>`
          }
        </section>
      </div>
    `;
    bindLinks($("#page"));
  }

  function renderProductDetail(productId) {
    const product = productById(productId);
    if (!product || product.status !== "ON_SALE") {
      $("#page").innerHTML = `<div class="container"><div class="card empty">商品不存在或已下架</div></div>`;
      return;
    }
    const skus = skusForProduct(product.id);
    selectedSkuId = selectedSkuId && skus.some((sku) => sku.id === selectedSkuId) ? selectedSkuId : skus[0]?.id;
    selectedQty = 1;
    const selectedSku = skuById(selectedSkuId);
    $("#page").innerHTML = `
      <div class="container">
        <div class="detail-grid">
          <div class="card product-image" style="font-size:96px">${product.images?.[0] || "货"}</div>
          <div class="card detail-info">
            <h1>${product.title}</h1>
            <div class="price">${money(selectedSku?.price || 0)}</div>
            <p class="muted">${product.description}</p>
            <h3>选择规格</h3>
            <div class="sku-list">
              ${skus.map((sku) => `<button class="sku-btn ${sku.id === selectedSkuId ? "active" : ""}" data-sku="${sku.id}" ${sku.stock <= 0 ? "disabled" : ""}>${sku.specName}：${sku.specValue}</button>`).join("")}
            </div>
            <p class="muted">库存：${selectedSku?.stock || 0}</p>
            <h3>购买数量</h3>
            <div class="qty">
              <button id="minusQty">-</button>
              <span id="qtyText">${selectedQty}</span>
              <button id="plusQty">+</button>
            </div>
            <div class="action-row">
              <button class="primary-btn" id="addCartBtn" ${!selectedSku || selectedSku.stock <= 0 ? "disabled" : ""}>加入购物车</button>
              <button class="secondary-btn" data-link="#/cart">去购物车</button>
            </div>
          </div>
        </div>
      </div>
    `;
    $("#page").querySelectorAll("[data-sku]").forEach((node) => {
      node.addEventListener("click", () => {
        selectedSkuId = Number(node.dataset.sku);
        renderProductDetail(product.id);
      });
    });
    $("#minusQty").addEventListener("click", () => {
      selectedQty = Math.max(1, selectedQty - 1);
      $("#qtyText").textContent = selectedQty;
    });
    $("#plusQty").addEventListener("click", () => {
      const sku = skuById(selectedSkuId);
      selectedQty = Math.min(sku.stock, selectedQty + 1);
      $("#qtyText").textContent = selectedQty;
    });
    $("#addCartBtn").addEventListener("click", () => addToCart(product.id, selectedSkuId, selectedQty));
    bindLinks($("#page"));
  }

  function requireUser() {
    const user = currentUser();
    if (!user) {
      navigate(`#/login?redirect=${encodeURIComponent(window.location.hash || "#/")}`);
      return null;
    }
    return user;
  }

  function addToCart(productId, skuId, quantity) {
    const user = requireUser();
    if (!user) return;
    const sku = skuById(skuId);
    if (!sku || sku.stock <= 0) return showToast("库存不足");
    const existing = state.cartItems.find((item) => item.userId === user.id && item.skuId === skuId && !item.deleted);
    const nextQty = (existing?.quantity || 0) + quantity;
    if (nextQty > sku.stock) return showToast("数量超过库存");
    if (existing) existing.quantity = nextQty;
    else state.cartItems.push({ id: nextId(state.cartItems), userId: user.id, productId, skuId, quantity, deleted: false });
    saveState();
    showToast("已加入购物车");
    route();
  }

  function renderLogin(params) {
    $("#page").innerHTML = `
      <div class="container">
        <div class="card" style="max-width:420px;margin:40px auto;padding:24px">
          <div class="logo" style="margin-bottom:18px"><span class="logo-mark">K</span><span>KKMall 登录</span></div>
          <form id="loginForm" class="form-grid">
            <label class="field full">手机号<input name="phone" placeholder="用户手机号，管理员 ${ADMIN_PHONE}" required /></label>
            <label class="field full">验证码<input name="code" placeholder="模拟验证码 123456" required /></label>
            <button class="primary-btn field full" type="submit">登录</button>
          </form>
        </div>
      </div>
    `;
    $("#loginForm").addEventListener("submit", (event) => {
      event.preventDefault();
      const form = new FormData(event.currentTarget);
      const phone = form.get("phone").trim();
      const code = form.get("code").trim();
      if (!/^1\d{10}$/.test(phone)) return showToast("请输入 11 位手机号");
      if (code !== MOCK_CODE) return showToast("验证码错误");
      let user = state.users.find((item) => item.phone === phone);
      if (!user) {
        user = { id: nextId(state.users), phone, nickname: `用户${phone.slice(-4)}`, role: "CUSTOMER" };
        state.users.push(user);
      }
      state.currentUserId = user.id;
      saveState();
      showToast("登录成功");
      navigate(params.redirect || (user.role === "ADMIN" ? "#/admin/products" : "#/"));
    });
  }

  function cartRows() {
    const user = currentUser();
    if (!user) return [];
    return state.cartItems.filter((item) => item.userId === user.id && !item.deleted).map((item) => {
      const product = productById(item.productId);
      const sku = skuById(item.skuId);
      const settleable = Boolean(product && sku && product.status === "ON_SALE" && sku.stock >= item.quantity);
      let reason = null;
      if (!product || product.status !== "ON_SALE") reason = "商品已下架";
      else if (!sku || sku.stock < item.quantity) reason = "库存不足";
      return { ...item, product, sku, settleable, reason, subtotal: sku ? sku.price * item.quantity : 0 };
    });
  }

  function renderCart() {
    const rows = cartRows();
    const settleableRows = rows.filter((row) => row.settleable);
    const productAmount = settleableRows.reduce((sum, row) => sum + row.subtotal, 0);
    $("#page").innerHTML = `
      <div class="container">
        <div class="section-head"><h1 class="section-title">购物车</h1></div>
        ${
          rows.length
            ? `<div class="cart-layout">
                <div class="card">
                  ${rows.map(cartItemTemplate).join("")}
                </div>
                <aside class="card summary">
                  <h2>结算</h2>
                  <div class="summary-row"><span>可结算商品</span><strong>${settleableRows.length} 件</strong></div>
                  <div class="summary-row"><span>商品金额</span><strong>${money(productAmount)}</strong></div>
                  <button class="primary-btn" style="width:100%" id="checkoutBtn" ${!settleableRows.length ? "disabled" : ""}>去结算</button>
                </aside>
              </div>`
            : `<div class="card empty">购物车还是空的<br><button class="primary-btn" data-link="#/products">去逛逛</button></div>`
        }
      </div>
    `;
    $("#page").querySelectorAll("[data-remove-cart]").forEach((node) => node.addEventListener("click", () => {
      const item = state.cartItems.find((cartItem) => cartItem.id === Number(node.dataset.removeCart));
      if (item) item.deleted = true;
      saveState();
      renderCart();
    }));
    $("#page").querySelectorAll("[data-cart-qty]").forEach((node) => node.addEventListener("click", () => {
      const [id, delta] = node.dataset.cartQty.split(":").map(Number);
      const item = state.cartItems.find((cartItem) => cartItem.id === id);
      const sku = skuById(item.skuId);
      item.quantity = Math.max(1, Math.min(sku.stock, item.quantity + delta));
      saveState();
      renderCart();
    }));
    $("#checkoutBtn")?.addEventListener("click", () => navigate("#/checkout"));
    bindLinks($("#page"));
  }

  function cartItemTemplate(row) {
    return `
      <div class="cart-item">
        <div class="thumb">${row.product?.images?.[0] || "货"}</div>
        <div>
          <strong>${row.product?.title || "商品不存在"}</strong>
          <div class="muted">${row.sku ? `${row.sku.specName}：${row.sku.specValue}` : "-"}</div>
          ${row.settleable ? "" : `<div class="status pending">${row.reason}</div>`}
        </div>
        <div>
          <div class="price">${money(row.subtotal)}</div>
          <div class="qty" style="margin-top:8px">
            <button data-cart-qty="${row.id}:-1">-</button>
            <span>${row.quantity}</span>
            <button data-cart-qty="${row.id}:1">+</button>
          </div>
          <button class="mini-btn" style="margin-top:8px" data-remove-cart="${row.id}">删除</button>
        </div>
      </div>
    `;
  }

  function renderCheckout() {
    const user = requireUser();
    if (!user) return;
    const rows = cartRows().filter((row) => row.settleable);
    if (!rows.length) {
      navigate("#/cart");
      showToast("没有可结算商品");
      return;
    }
    const lastAddress = state.addresses.find((address) => address.userId === user.id && address.isDefault) || state.addresses.find((address) => address.userId === user.id);
    const productAmount = rows.reduce((sum, row) => sum + row.subtotal, 0);
    const fee = shippingFee(productAmount);
    $("#page").innerHTML = `
      <div class="container">
        <div class="section-head"><h1 class="section-title">确认订单</h1></div>
        <div class="checkout-layout">
          <div>
            <form class="card form-grid" id="addressForm" style="padding:18px;margin-bottom:18px">
              <h2 class="field full">收货地址</h2>
              <label class="field">收货人<input name="receiverName" value="${lastAddress?.receiverName || ""}" required /></label>
              <label class="field">手机号<input name="receiverPhone" value="${lastAddress?.receiverPhone || user.phone}" required /></label>
              <label class="field full">省市区<input name="region" value="${lastAddress?.region || ""}" required /></label>
              <label class="field full">详细地址<input name="detail" value="${lastAddress?.detail || ""}" required /></label>
            </form>
            <div class="card">${rows.map(cartItemTemplate).join("")}</div>
          </div>
          <aside class="card summary">
            <h2>金额明细</h2>
            <div class="summary-row"><span>商品金额</span><strong>${money(productAmount)}</strong></div>
            <div class="summary-row"><span>运费</span><strong>${money(fee)}</strong></div>
            <div class="free-tip">${fee ? `还差 ${money(9900 - productAmount)} 包邮` : "已包邮"}</div>
            <div class="summary-row"><span>应付金额</span><strong class="price">${money(productAmount + fee)}</strong></div>
            <button class="primary-btn" style="width:100%" id="submitOrderBtn">提交订单</button>
          </aside>
        </div>
      </div>
    `;
    $("#page").querySelectorAll("[data-cart-qty], [data-remove-cart]").forEach((node) => node.disabled = true);
    $("#submitOrderBtn").addEventListener("click", () => createOrder(rows));
  }

  function createOrder(rows) {
    const user = requireUser();
    if (!user) return;
    const form = new FormData($("#addressForm"));
    const address = {
      id: nextId(state.addresses),
      userId: user.id,
      receiverName: form.get("receiverName").trim(),
      receiverPhone: form.get("receiverPhone").trim(),
      region: form.get("region").trim(),
      detail: form.get("detail").trim(),
      isDefault: true,
    };
    if (!address.receiverName || !address.receiverPhone || !address.region || !address.detail) return showToast("请填写完整地址");
    state.addresses.forEach((item) => {
      if (item.userId === user.id) item.isDefault = false;
    });
    state.addresses.push(address);

    const freshRows = rows.map((row) => {
      const product = productById(row.productId);
      const sku = skuById(row.skuId);
      if (!product || product.status !== "ON_SALE" || !sku || sku.stock < row.quantity) return null;
      return { ...row, product, sku, subtotal: sku.price * row.quantity };
    });
    if (freshRows.some((row) => !row)) return showToast("商品状态变化，请返回购物车确认");
    const productAmount = freshRows.reduce((sum, row) => sum + row.subtotal, 0);
    const fee = shippingFee(productAmount);
    const order = {
      id: nextId(state.orders),
      orderNo: `${new Date().toISOString().replace(/\D/g, "").slice(0, 14)}${String(Math.floor(Math.random() * 10000)).padStart(4, "0")}`,
      userId: user.id,
      productAmount,
      shippingFee: fee,
      payableAmount: productAmount + fee,
      status: "PENDING_PAYMENT",
      addressSnapshot: { ...address },
      createdAt: new Date().toISOString(),
      paidAt: null,
      shippedAt: null,
    };
    state.orders.push(order);
    freshRows.forEach((row) => {
      state.orderItems.push({
        id: nextId(state.orderItems),
        orderId: order.id,
        productId: row.productId,
        skuId: row.skuId,
        titleSnapshot: row.product.title,
        imageSnapshot: row.product.images?.[0],
        skuSnapshot: `${row.sku.specName}：${row.sku.specValue}`,
        unitPrice: row.sku.price,
        quantity: row.quantity,
        subtotal: row.subtotal,
      });
      const cartItem = state.cartItems.find((item) => item.id === row.id);
      if (cartItem) cartItem.deleted = true;
    });
    saveState();
    navigate(`#/pay/${order.id}`);
  }

  function renderPay(orderId) {
    const user = requireUser();
    if (!user) return;
    const order = state.orders.find((item) => item.id === Number(orderId) && item.userId === user.id);
    if (!order) return $("#page").innerHTML = `<div class="container"><div class="card empty">订单不存在</div></div>`;
    $("#page").innerHTML = `
      <div class="container">
        <div class="card" style="max-width:480px;margin:40px auto;padding:24px;text-align:center">
          <h1>模拟支付</h1>
          <p class="muted">订单号：${order.orderNo}</p>
          <div class="price" style="font-size:32px">${money(order.payableAmount)}</div>
          <p>支付方式：MVP 模拟支付</p>
          <button class="primary-btn" id="mockPayBtn" ${order.status !== "PENDING_PAYMENT" ? "disabled" : ""}>确认模拟支付</button>
          <button class="plain-btn" data-link="#/orders/${order.id}">查看订单</button>
        </div>
      </div>
    `;
    $("#mockPayBtn")?.addEventListener("click", () => mockPay(order.id));
    bindLinks($("#page"));
  }

  function mockPay(orderId) {
    const order = state.orders.find((item) => item.id === Number(orderId));
    if (!order) return showToast("订单不存在");
    if (order.status !== "PENDING_PAYMENT") {
      navigate(`#/orders/${order.id}`);
      return;
    }
    const items = state.orderItems.filter((item) => item.orderId === order.id);
    for (const item of items) {
      const sku = skuById(item.skuId);
      if (!sku || sku.stock < item.quantity) return showToast("库存不足，支付失败");
    }
    items.forEach((item) => {
      const sku = skuById(item.skuId);
      sku.stock -= item.quantity;
    });
    order.status = "PAID_PENDING_SHIPMENT";
    order.paidAt = new Date().toISOString();
    saveState();
    showToast("支付成功");
    navigate(`#/orders/${order.id}`);
  }

  function renderOrders(params) {
    const user = requireUser();
    if (!user) return;
    const status = params.status || "";
    const orders = state.orders.filter((order) => order.userId === user.id && (!status || order.status === status)).sort((a, b) => b.id - a.id);
    $("#page").innerHTML = `
      <div class="container">
        <div class="section-head"><h1 class="section-title">我的订单</h1></div>
        <div class="tabs">
          ${["", "PENDING_PAYMENT", "PAID_PENDING_SHIPMENT", "SHIPPED", "COMPLETED", "CANCELLED"].map((item) => `<button class="tab ${status === item ? "active" : ""}" data-link="#/orders${item ? `?status=${item}` : ""}">${item ? statusText(item) : "全部"}</button>`).join("")}
        </div>
        <section class="section card">
          ${orders.length ? orders.map(orderCard).join("") : `<div class="empty">还没有订单<br><button class="primary-btn" data-link="#/products">去首页看看</button></div>`}
        </section>
      </div>
    `;
    bindLinks($("#page"));
  }

  function orderCard(order) {
    const items = state.orderItems.filter((item) => item.orderId === order.id);
    return `
      <div class="order-card">
        <div class="thumb">${items[0]?.imageSnapshot || "单"}</div>
        <div>
          <strong>订单号：${order.orderNo}</strong>
          <div class="muted">${items.map((item) => item.titleSnapshot).join("、")}</div>
          <div class="muted">${new Date(order.createdAt).toLocaleString()}</div>
        </div>
        <div>
          <span class="status ${statusClass(order.status)}">${statusText(order.status)}</span>
          <div class="price">${money(order.payableAmount)}</div>
          <button class="mini-btn red" data-link="#/orders/${order.id}">查看详情</button>
        </div>
      </div>
    `;
  }

  function renderOrderDetail(orderId) {
    const user = requireUser();
    if (!user) return;
    const order = state.orders.find((item) => item.id === Number(orderId) && item.userId === user.id);
    if (!order) return $("#page").innerHTML = `<div class="container"><div class="card empty">订单不存在</div></div>`;
    renderOrderDetailShared(order, false);
  }

  function renderOrderDetailShared(order, adminMode) {
    const items = state.orderItems.filter((item) => item.orderId === order.id);
    const shipment = state.shipments.find((item) => item.orderId === order.id);
    const address = order.addressSnapshot;
    $("#page, .admin-content").innerHTML = `
      <div class="${adminMode ? "" : "container"}">
        <div class="section-head">
          <h1 class="section-title">订单详情</h1>
          <span class="status ${statusClass(order.status)}">${statusText(order.status)}</span>
        </div>
        <div class="checkout-layout">
          <div>
            <div class="card" style="padding:18px;margin-bottom:18px">
              <h2>订单信息</h2>
              <p>订单号：${order.orderNo}</p>
              <p>创建时间：${new Date(order.createdAt).toLocaleString()}</p>
              ${order.paidAt ? `<p>支付时间：${new Date(order.paidAt).toLocaleString()}</p>` : ""}
              ${order.shippedAt ? `<p>发货时间：${new Date(order.shippedAt).toLocaleString()}</p>` : ""}
            </div>
            <div class="card" style="padding:18px;margin-bottom:18px">
              <h2>收货地址</h2>
              <p>${address.receiverName} ${address.receiverPhone}</p>
              <p>${address.region} ${address.detail}</p>
            </div>
            <div class="card">
              ${items.map((item) => `
                <div class="cart-item">
                  <div class="thumb">${item.imageSnapshot || "货"}</div>
                  <div><strong>${item.titleSnapshot}</strong><div class="muted">${item.skuSnapshot}</div></div>
                  <div><div class="price">${money(item.subtotal)}</div><div class="muted">x ${item.quantity}</div></div>
                </div>
              `).join("")}
            </div>
          </div>
          <aside class="card summary">
            <h2>金额明细</h2>
            <div class="summary-row"><span>商品金额</span><strong>${money(order.productAmount)}</strong></div>
            <div class="summary-row"><span>运费</span><strong>${money(order.shippingFee)}</strong></div>
            <div class="summary-row"><span>应付金额</span><strong class="price">${money(order.payableAmount)}</strong></div>
            ${
              shipment
                ? `<hr><h2>物流信息</h2><p>物流公司：${shipment.logisticsCompany}</p><p>物流单号：${shipment.trackingNo}</p>`
                : order.status === "PENDING_PAYMENT" && !adminMode
                  ? `<button class="primary-btn" style="width:100%" data-link="#/pay/${order.id}">去支付</button>`
                  : `<p class="muted">暂无物流信息</p>`
            }
            ${adminMode && order.status === "PAID_PENDING_SHIPMENT" ? shipmentForm(order.id) : ""}
          </aside>
        </div>
      </div>
    `;
    bindLinks(document);
    $("#shipmentForm")?.addEventListener("submit", (event) => {
      event.preventDefault();
      const form = new FormData(event.currentTarget);
      shipOrder(order.id, form.get("logisticsCompany").trim(), form.get("trackingNo").trim());
    });
  }

  function shipmentForm(orderId) {
    return `
      <hr>
      <h2>发货</h2>
      <form id="shipmentForm" class="form-grid">
        <label class="field full">物流公司<input name="logisticsCompany" placeholder="如：顺丰速运" required /></label>
        <label class="field full">物流单号<input name="trackingNo" placeholder="输入物流单号" required /></label>
        <button class="primary-btn field full" type="submit">确认发货</button>
      </form>
    `;
  }

  function renderAdmin(path, params) {
    const user = currentUser();
    if (path !== "/admin/login" && (!user || user.role !== "ADMIN")) {
      navigate("#/admin/login");
      return;
    }
    $("#app").innerHTML = `
      <div class="admin-shell">
        <aside class="admin-side">
          <a class="logo" href="#/admin/products"><span class="logo-mark">K</span><span>管理后台</span></a>
          <a class="side-link ${path.startsWith("/admin/products") ? "active" : ""}" href="#/admin/products">商品管理</a>
          <a class="side-link ${path.startsWith("/admin/orders") ? "active" : ""}" href="#/admin/orders">订单管理</a>
          <a class="side-link" href="#/">返回商城</a>
        </aside>
        <main class="admin-main">
          <div class="admin-top">
            <strong>KKMall 管理后台</strong>
            <div>${user ? user.nickname : "未登录"} <button class="mini-btn" id="adminLogout">退出</button></div>
          </div>
          <div class="admin-content"></div>
        </main>
      </div>
    `;
    $("#adminLogout")?.addEventListener("click", () => {
      state.currentUserId = null;
      saveState();
      navigate("#/admin/login");
    });

    if (path === "/admin/login") renderAdminLogin();
    else if (path === "/admin/products") renderAdminProducts(params);
    else if (path === "/admin/products/new") renderAdminProductEdit();
    else if (path.startsWith("/admin/products/")) renderAdminProductEdit(path.split("/")[3]);
    else if (path === "/admin/orders") renderAdminOrders(params);
    else if (path.startsWith("/admin/orders/")) renderAdminOrderDetail(path.split("/")[3]);
    else renderAdminProducts(params);
  }

  function renderAdminLogin() {
    $(".admin-content").innerHTML = `
      <div class="card" style="max-width:420px;margin:40px auto;padding:24px">
        <h1>后台登录</h1>
        <form id="adminLoginForm" class="form-grid">
          <label class="field full">管理员手机号<input name="phone" value="${ADMIN_PHONE}" required /></label>
          <label class="field full">验证码<input name="code" value="${MOCK_CODE}" required /></label>
          <button class="primary-btn field full" type="submit">登录</button>
        </form>
      </div>
    `;
    $("#adminLoginForm").addEventListener("submit", (event) => {
      event.preventDefault();
      const form = new FormData(event.currentTarget);
      const phone = form.get("phone").trim();
      const code = form.get("code").trim();
      const user = state.users.find((item) => item.phone === phone && item.role === "ADMIN");
      if (!user || code !== MOCK_CODE) return showToast("管理员账号或验证码错误");
      state.currentUserId = user.id;
      saveState();
      navigate("#/admin/products");
    });
  }

  function renderAdminProducts(params) {
    const status = params.status || "";
    const products = state.products.filter((product) => !status || product.status === status);
    $(".admin-content").innerHTML = `
      <div class="toolbar">
        <h1 class="section-title">商品管理</h1>
        <button class="primary-btn" data-link="#/admin/products/new">新增商品</button>
      </div>
      <div class="tabs" style="margin-bottom:14px">
        ${["", "ON_SALE", "OFF_SALE", "DRAFT"].map((item) => `<button class="tab ${status === item ? "active" : ""}" data-link="#/admin/products${item ? `?status=${item}` : ""}">${item || "全部"}</button>`).join("")}
      </div>
      <div class="card">
        <table class="table">
          <thead><tr><th>商品</th><th>分类</th><th>最低价</th><th>库存</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            ${products.map((product) => `
              <tr>
                <td><strong>${product.title}</strong></td>
                <td>${categoryName(product.categoryId)}</td>
                <td>${money(minPrice(product.id))}</td>
                <td>${totalStock(product.id)}</td>
                <td><span class="status ${product.status === "ON_SALE" ? "shipped" : "cancelled"}">${product.status}</span></td>
                <td class="table-actions">
                  <button class="mini-btn" data-link="#/admin/products/${product.id}/edit">编辑</button>
                  <button class="mini-btn red" data-product-status="${product.id}:${product.status === "ON_SALE" ? "OFF_SALE" : "ON_SALE"}">${product.status === "ON_SALE" ? "下架" : "上架"}</button>
                </td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
    bindLinks($(".admin-content"));
    $(".admin-content").querySelectorAll("[data-product-status]").forEach((node) => {
      node.addEventListener("click", () => {
        const [id, nextStatus] = node.dataset.productStatus.split(":");
        const product = productById(id);
        product.status = nextStatus;
        saveState();
        renderAdminProducts(params);
      });
    });
  }

  function renderAdminProductEdit(productId) {
    const product = productId ? productById(productId) : null;
    const skus = product ? skusForProduct(product.id) : [];
    $(".admin-content").innerHTML = `
      <div class="toolbar"><h1 class="section-title">${product ? "编辑商品" : "新增商品"}</h1></div>
      <form id="productForm" class="card form-grid" style="padding:18px">
        <label class="field">商品标题<input name="title" value="${product?.title || ""}" required /></label>
        <label class="field">分类
          <select name="categoryId">${state.categories.map((category) => `<option value="${category.id}" ${product?.categoryId === category.id ? "selected" : ""}>${category.name}</option>`).join("")}</select>
        </label>
        <label class="field full">商品描述<textarea name="description" rows="4">${product?.description || ""}</textarea></label>
        <label class="field">图片占位字<input name="image" value="${product?.images?.[0] || "新"}" maxlength="2" /></label>
        <label class="field">状态
          <select name="status">
            ${["DRAFT", "ON_SALE", "OFF_SALE"].map((status) => `<option value="${status}" ${product?.status === status ? "selected" : ""}>${status}</option>`).join("")}
          </select>
        </label>
        <div class="field full">
          <h2>SKU 信息</h2>
          <div id="skuEditor">
            ${(skus.length ? skus : [{ specName: "规格", specValue: "默认", price: 9900, stock: 10 }]).map(skuEditorRow).join("")}
          </div>
          <button type="button" class="secondary-btn" id="addSkuRow">新增 SKU</button>
        </div>
        <div class="field full action-row">
          <button class="primary-btn" type="submit">保存</button>
          <button class="plain-btn" type="button" data-link="#/admin/products">返回</button>
        </div>
      </form>
    `;
    $("#addSkuRow").addEventListener("click", () => {
      $("#skuEditor").insertAdjacentHTML("beforeend", skuEditorRow({ specName: "规格", specValue: "新规格", price: 9900, stock: 10 }));
    });
    $("#productForm").addEventListener("submit", (event) => {
      event.preventDefault();
      saveProduct(product?.id, new FormData(event.currentTarget));
    });
    bindLinks($(".admin-content"));
  }

  function skuEditorRow(sku) {
    return `
      <div class="form-grid" style="margin-bottom:10px">
        <input name="skuId" value="${sku.id || ""}" type="hidden" />
        <label class="field">规格名<input name="specName" value="${sku.specName}" required /></label>
        <label class="field">规格值<input name="specValue" value="${sku.specValue}" required /></label>
        <label class="field">价格（分）<input name="price" type="number" min="0" value="${sku.price}" required /></label>
        <label class="field">库存<input name="stock" type="number" min="0" value="${sku.stock}" required /></label>
      </div>
    `;
  }

  function saveProduct(productId, form) {
    let product = productId ? productById(productId) : null;
    if (!product) {
      product = { id: nextId(state.products) };
      state.products.push(product);
    }
    product.title = form.get("title").trim();
    product.categoryId = Number(form.get("categoryId"));
    product.description = form.get("description").trim();
    product.images = [form.get("image").trim() || "新"];
    product.status = form.get("status");
    const skuIds = form.getAll("skuId");
    const names = form.getAll("specName");
    const values = form.getAll("specValue");
    const prices = form.getAll("price");
    const stocks = form.getAll("stock");
    for (let i = 0; i < names.length; i += 1) {
      const id = Number(skuIds[i]);
      let sku = id ? skuById(id) : null;
      if (!sku) {
        sku = { id: nextId(state.skus), productId: product.id };
        state.skus.push(sku);
      }
      sku.productId = product.id;
      sku.specName = names[i].trim();
      sku.specValue = values[i].trim();
      sku.price = Number(prices[i]);
      sku.stock = Number(stocks[i]);
    }
    saveState();
    showToast("商品已保存");
    navigate("#/admin/products");
  }

  function renderAdminOrders(params) {
    const status = params.status || "";
    const orders = state.orders.filter((order) => !status || order.status === status).sort((a, b) => b.id - a.id);
    $(".admin-content").innerHTML = `
      <div class="toolbar"><h1 class="section-title">订单管理</h1></div>
      <div class="tabs" style="margin-bottom:14px">
        ${["", "PENDING_PAYMENT", "PAID_PENDING_SHIPMENT", "SHIPPED", "COMPLETED", "CANCELLED"].map((item) => `<button class="tab ${status === item ? "active" : ""}" data-link="#/admin/orders${item ? `?status=${item}` : ""}">${item ? statusText(item) : "全部"}</button>`).join("")}
      </div>
      <div class="card">
        <table class="table">
          <thead><tr><th>订单号</th><th>用户</th><th>金额</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
          <tbody>
            ${orders.map((order) => {
              const user = state.users.find((item) => item.id === order.userId);
              return `<tr>
                <td>${order.orderNo}</td>
                <td>${user?.phone || "-"}</td>
                <td>${money(order.payableAmount)}</td>
                <td><span class="status ${statusClass(order.status)}">${statusText(order.status)}</span></td>
                <td>${new Date(order.createdAt).toLocaleString()}</td>
                <td><button class="mini-btn red" data-link="#/admin/orders/${order.id}">查看详情</button></td>
              </tr>`;
            }).join("")}
          </tbody>
        </table>
      </div>
    `;
    bindLinks($(".admin-content"));
  }

  function renderAdminOrderDetail(orderId) {
    const order = state.orders.find((item) => item.id === Number(orderId));
    if (!order) return $(".admin-content").innerHTML = `<div class="card empty">订单不存在</div>`;
    renderOrderDetailShared(order, true);
  }

  function shipOrder(orderId, logisticsCompany, trackingNo) {
    if (!logisticsCompany || !trackingNo) return showToast("请填写物流信息");
    const order = state.orders.find((item) => item.id === Number(orderId));
    if (!order || order.status !== "PAID_PENDING_SHIPMENT") return showToast("订单状态不允许发货");
    if (state.shipments.some((item) => item.orderId === order.id)) return showToast("订单已发货");
    const now = new Date().toISOString();
    state.shipments.push({ id: nextId(state.shipments), orderId: order.id, logisticsCompany, trackingNo, shippedAt: now });
    order.status = "SHIPPED";
    order.shippedAt = now;
    saveState();
    showToast("发货成功");
    renderAdminOrderDetail(order.id);
  }

  window.addEventListener("hashchange", route);
  route();
}());
