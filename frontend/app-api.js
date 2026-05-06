(function () {
  const API_BASE = (window.KKMALL_API_BASE || "").replace(/\/$/, "");
  const TOKEN_KEY = "kkmall_api_token";
  const USER_KEY = "kkmall_api_user";
  const ADMIN_TOKEN_KEY = "kkmall_admin_token";
  const MOCK_CODE = "123456";

  const state = { categories: [], cart: { items: [], productAmount: 0 } };
  let selectedSkuId = null;
  let toastTimer = null;

  const $ = (selector) => document.querySelector(selector);
  const money = (cents) => `¥${(Number(cents || 0) / 100).toFixed(2)}`;
  const currentUser = () => {
    try { return JSON.parse(localStorage.getItem(USER_KEY) || "null"); } catch { return null; }
  };
  const token = () => localStorage.getItem(TOKEN_KEY);
  const adminToken = () => localStorage.getItem(ADMIN_TOKEN_KEY);

  async function api(path, options = {}) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    const authToken = options.admin ? adminToken() : token();
    if (authToken) headers.Authorization = `Bearer ${authToken}`;
    const response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
      body: options.body && typeof options.body !== "string" ? JSON.stringify(options.body) : options.body,
    });
    const payload = await response.json().catch(() => ({}));
    if (!response.ok || payload.code !== 0) {
      const error = new Error(payload.message || payload.code || `HTTP_${response.status}`);
      error.code = payload.code;
      error.status = response.status;
      error.admin = Boolean(options.admin);
      throw error;
    }
    return payload.data;
  }

  function handleApiError(error, fallbackRoute) {
    if (error.admin && (error.code === "AUTH_REQUIRED" || error.code === "AUTH_FORBIDDEN")) {
      localStorage.removeItem(ADMIN_TOKEN_KEY);
      toast("后台登录已失效，请重新登录");
      if ((window.location.hash || "#/").startsWith("#/admin")) {
        setTimeout(route, 0);
      } else {
        navigate("#/admin");
      }
      return true;
    }
    if (error.code === "AUTH_REQUIRED" || error.message === "AUTH_REQUIRED") {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      toast("请先登录");
      navigate(fallbackRoute || `#/login?redirect=${encodeURIComponent(window.location.hash || "#/")}`);
      return true;
    }
    return false;
  }

  function runAction(action) {
    action().catch((error) => {
      if (!handleApiError(error)) toast(error.message || "操作失败");
    });
  }

  function statusText(status) {
    return {
      PENDING_PAYMENT: "待支付",
      PAID_PENDING_SHIPMENT: "已支付/待发货",
      SHIPPED: "已发货",
      COMPLETED: "已完成",
      CANCELLED: "已取消",
      ON_SALE: "上架",
      OFF_SALE: "下架",
      DRAFT: "草稿",
    }[status] || status || "-";
  }

  function imageMark(value) {
    return `<div class="thumb product-thumb">${value || "K"}</div>`;
  }

  function toast(message) {
    clearTimeout(toastTimer);
    $(".toast")?.remove();
    document.body.insertAdjacentHTML("beforeend", `<div class="toast">${message}</div>`);
    toastTimer = setTimeout(() => $(".toast")?.remove(), 2200);
  }

  function navigate(hash) {
    window.location.hash = hash;
  }

  async function refreshCart() {
    if (!token()) {
      state.cart = { items: [], productAmount: 0 };
      return;
    }
    state.cart = await api("/api/v1/cart");
  }

  async function loadCategories() {
    state.categories = await api("/api/v1/categories");
  }

  async function ensureLogin() {
    if (token()) return;
    navigate(`#/login?redirect=${encodeURIComponent(window.location.hash || "#/")}`);
    throw new Error("AUTH_REQUIRED");
  }

  function shell(content, params = {}) {
    const user = currentUser();
    const cartCount = (state.cart.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
    $("#app").innerHTML = `
      <div class="app-shell">
        <div class="promo-strip" id="promoStrip">
          <div class="promo-strip-inner">
            <span class="promo-emoji">礼</span>
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
            <nav class="nav-actions">
              <a class="plain-btn" href="#/cart">购物车${cartCount ? `(${cartCount})` : ""}</a>
              <a class="plain-btn" href="#/orders">我的订单</a>
              ${user ? `<button class="ghost-btn" id="logoutBtn">${user.nickname || user.phone}</button>` : `<a class="ghost-btn" href="#/login">登录</a>`}
              <a class="primary-btn" href="#/admin">后台</a>
            </nav>
          </div>
        </header>
        <main class="container">${content}</main>
        <nav class="mobile-tabs">
          <a href="#/">首页</a><a href="#/products">分类</a><a href="#/cart">购物车</a><a href="#/orders">订单</a>
        </nav>
      </div>`;

    $("#promoClose")?.addEventListener("click", () => $("#promoStrip")?.remove());
    $("[data-link]")?.addEventListener("click", (event) => navigate(event.currentTarget.dataset.link));
    $("#logoutBtn")?.addEventListener("click", () => {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      toast("已退出登录");
      route();
    });
    $("#searchForm")?.addEventListener("submit", (event) => {
      event.preventDefault();
      const keyword = new FormData(event.currentTarget).get("keyword") || "";
      navigate(`#/products?keyword=${encodeURIComponent(keyword)}`);
    });
  }

  async function products(params = {}) {
    const query = new URLSearchParams();
    if (params.categoryId) query.set("categoryId", params.categoryId);
    if (params.keyword) query.set("keyword", params.keyword);
    const data = await api(`/api/v1/products?${query.toString()}`);
    return data.items || [];
  }

  function productGrid(items) {
    return `<section class="product-grid">
      ${items.map((item) => `
        <a class="product-card" href="#/product?id=${item.id}">
          <div class="product-image">${item.coverImage || "K"}</div>
          <div class="product-body">
            <div class="product-title">${item.title}</div>
            <div class="product-meta"><span class="price">${money(item.minPrice)}</span><span class="muted">库存实时校验</span></div>
          </div>
        </a>`).join("") || `<div class="empty card">暂无商品</div>`}
    </section>`;
  }

  async function renderHome(params) {
    const items = await products(params);
    shell(`
      <section class="hero">
        <div class="hero-main">
          <h1>今日精选好物，满 99 元包邮</h1>
          <p>从日常服饰到桌面数码，先把可靠、清楚、好买的体验做好。</p>
          <a class="secondary-btn hero-cta" href="#/products">立即逛逛</a>
        </div>
        <div class="hero-side">
          <div class="promo-tile"><strong>模拟支付闭环</strong><span>下单、支付、发货、订单查询都能体验。</span></div>
          <div class="promo-tile"><strong>商家手动发货</strong><span>后台录入物流公司和单号。</span></div>
        </div>
      </section>
      <section class="section">
        <div class="section-head"><h2 class="section-title">分类入口</h2><span class="muted">单商家自营 MVP</span></div>
        <div class="category-grid">${state.categories.map((item) => `<a class="tab" href="#/products?categoryId=${item.id}">${item.name}</a>`).join("")}</div>
      </section>
      <section class="section">
        <div class="section-head"><h2 class="section-title">推荐商品</h2><a class="ghost-btn" href="#/products">查看全部</a></div>
        ${productGrid(items)}
      </section>`, params);
  }

  async function renderProducts(params) {
    const items = await products(params);
    shell(`
      <section class="section">
        <div class="section-head"><h1 class="section-title">商品列表</h1><span class="muted">${params.keyword ? `搜索：${params.keyword}` : "全部在售商品"}</span></div>
        <div class="filters">
          <a class="tab ${!params.categoryId ? "active" : ""}" href="#/products">全部</a>
          ${state.categories.map((item) => `<a class="tab ${String(params.categoryId) === String(item.id) ? "active" : ""}" href="#/products?categoryId=${item.id}">${item.name}</a>`).join("")}
        </div>
      </section>
      ${productGrid(items)}`, params);
  }

  async function renderProduct(params) {
    const product = await api(`/api/v1/products/${params.id}`);
    if (!product.skus.some((sku) => Number(sku.id) === Number(selectedSkuId))) {
      selectedSkuId = product.skus[0]?.id;
    }
    shell(`
      <section class="detail-grid">
        <div class="card product-image detail-image">${product.images?.[0] || "K"}</div>
        <div class="card detail-info">
          <h1>${product.title}</h1>
          <p class="muted">${product.description || ""}</p>
          <div class="sku-list">
            ${product.skus.map((sku) => `<button class="sku-btn ${Number(selectedSkuId) === Number(sku.id) ? "active" : ""}" data-sku="${sku.id}">${sku.specName}：${sku.specValue} · ${money(sku.price)} · 库存 ${sku.stock}</button>`).join("")}
          </div>
          <div class="field qty-field"><label>数量</label><input id="qtyInput" type="number" min="1" value="1" /></div>
          <div class="action-row"><button class="primary-btn" id="addCartBtn">加入购物车</button><a class="secondary-btn" href="#/cart">查看购物车</a></div>
        </div>
      </section>`, params);
    document.querySelectorAll(".sku-btn").forEach((button) => button.addEventListener("click", () => {
      selectedSkuId = Number(button.dataset.sku);
      renderProduct(params);
    }));
    $("#addCartBtn").addEventListener("click", () => runAction(async () => {
      await ensureLogin();
      await api("/api/v1/cart/items", { method: "POST", body: { skuId: Number(selectedSkuId), quantity: Number($("#qtyInput").value || 1) } });
      toast("已加入购物车");
      navigate("#/cart");
    }));
  }

  async function renderCart(params) {
    await ensureLogin();
    await refreshCart();
    shell(`
      <section class="cart-layout">
        <div class="card">
          ${(state.cart.items || []).map((item) => `
            <div class="cart-item">
              ${imageMark(item.image)}
              <div><strong>${item.title}</strong><p class="muted">${item.specText}</p><span class="price">${money(item.price)}</span></div>
              <div class="cart-actions"><input class="qty-input" data-id="${item.id}" type="number" min="1" max="${item.stock}" value="${item.quantity}" /><button class="ghost-btn remove-cart" data-id="${item.id}">删除</button></div>
            </div>`).join("") || `<div class="empty">购物车还是空的</div>`}
        </div>
        <aside class="card summary">
          <h2>结算</h2>
          <div class="summary-row"><span>商品金额</span><strong>${money(state.cart.productAmount)}</strong></div>
          <div class="free-tip">满 99 元包邮，未满收 10 元运费。</div>
          <a class="primary-btn wide-btn" href="#/checkout">去结算</a>
        </aside>
      </section>`, params);
    document.querySelectorAll(".qty-input").forEach((input) => input.addEventListener("change", () => runAction(async () => {
      await api(`/api/v1/cart/items/${input.dataset.id}`, { method: "PATCH", body: { quantity: Number(input.value || 1) } });
      route();
    })));
    document.querySelectorAll(".remove-cart").forEach((button) => button.addEventListener("click", () => runAction(async () => {
      await api(`/api/v1/cart/items/${button.dataset.id}`, { method: "DELETE" });
      route();
    })));
  }

  async function renderCheckout(params) {
    await ensureLogin();
    const [addresses] = await Promise.all([api("/api/v1/addresses"), refreshCart()]);
    const shippingFee = state.cart.productAmount >= 9900 ? 0 : 1000;
    shell(`
      <section class="checkout-layout">
        <div class="card detail-info">
          <h2>收货地址</h2>
          <div class="field"><select id="addressSelect">${addresses.map((item) => `<option value="${item.id}">${item.receiverName} ${item.receiverPhone} ${item.region}${item.detail}</option>`).join("")}</select></div>
          <div class="form-grid address-form">
            <div class="field"><input id="receiverName" placeholder="收货人" /></div>
            <div class="field"><input id="receiverPhone" placeholder="手机号" /></div>
            <div class="field"><input id="region" placeholder="省市区" /></div>
            <div class="field"><input id="detail" placeholder="详细地址" /></div>
            <button class="secondary-btn" id="saveAddressBtn">保存地址</button>
          </div>
        </div>
        <aside class="card summary">
          <h2>订单金额</h2>
          <div class="summary-row"><span>商品金额</span><strong>${money(state.cart.productAmount)}</strong></div>
          <div class="summary-row"><span>运费</span><strong>${money(shippingFee)}</strong></div>
          <div class="summary-row"><span>应付</span><strong class="price">${money(state.cart.productAmount + shippingFee)}</strong></div>
          <button class="primary-btn wide-btn" id="createOrderBtn">提交订单</button>
        </aside>
      </section>`, params);
    $("#saveAddressBtn").addEventListener("click", () => runAction(async () => {
      await api("/api/v1/addresses", {
        method: "POST",
        body: {
          receiverName: $("#receiverName").value,
          receiverPhone: $("#receiverPhone").value,
          region: $("#region").value,
          detail: $("#detail").value,
          isDefault: true,
        },
      });
      toast("地址已保存");
      route();
    }));
    $("#createOrderBtn").addEventListener("click", () => runAction(async () => {
      const addressId = Number($("#addressSelect").value);
      if (!addressId) return toast("请先保存收货地址");
      if (!state.cart.items.length) return toast("购物车为空，先去挑选商品");
      const order = await api("/api/v1/orders", { method: "POST", body: { addressId, cartItemIds: state.cart.items.map((item) => item.id) } });
      navigate(`#/pay?id=${order.id}`);
    }));
  }

  async function renderPay(params) {
    await ensureLogin();
    const order = await api(`/api/v1/orders/${params.id}`);
    shell(`
      <section class="card detail-info narrow-panel">
        <h1>模拟支付</h1>
        <p>订单号：${order.orderNo}</p>
        <p>状态：${statusText(order.status)}</p>
        <p class="price">应付金额：${money(order.payableAmount)}</p>
        <button class="primary-btn wide-btn" id="payBtn">确认模拟支付</button>
      </section>`, params);
    $("#payBtn").addEventListener("click", () => runAction(async () => {
      await api("/api/v1/payments/mock", { method: "POST", body: { orderId: Number(params.id) } });
      toast("支付成功");
      navigate(`#/orders/${params.id}`);
    }));
  }

  async function renderOrders(params) {
    await ensureLogin();
    const data = await api("/api/v1/orders");
    const orders = data.items || [];
    shell(`
      <section class="card">
        ${orders.map((order) => `
          <a class="order-card" href="#/orders/${order.id}">
            <div><strong>${order.orderNo}</strong><p class="muted">${order.createdAt || ""}</p></div>
            <span class="status ${order.status === "SHIPPED" ? "shipped" : "paid"}">${statusText(order.status)}</span>
            <strong>${money(order.payableAmount)}</strong>
          </a>`).join("") || `<div class="empty">暂无订单</div>`}
      </section>`, params);
  }

  async function renderOrderDetail(params) {
    await ensureLogin();
    const order = await api(`/api/v1/orders/${params.id}`);
    shell(`
      <section class="card detail-info">
        <h1>订单详情</h1>
        <p>订单号：${order.orderNo}</p>
        <p>状态：${statusText(order.status)}</p>
        <p>物流：${order.shipment ? `${order.shipment.logisticsCompany} ${order.shipment.trackingNo}` : "暂无物流"}</p>
        <p class="price">应付金额：${money(order.payableAmount)}</p>
      </section>
      <section class="card">
        ${order.items.map((item) => `<div class="cart-item">${imageMark(item.imageSnapshot)}<div><strong>${item.titleSnapshot}</strong><p class="muted">${item.skuSnapshot}</p></div><strong>${money(item.subtotal)}</strong></div>`).join("")}
      </section>`, params);
  }

  async function renderLogin(params) {
    shell(`
      <section class="card detail-info narrow-panel">
        <h1>手机号登录</h1>
        <div class="field"><input id="phoneInput" value="13800000000" placeholder="手机号" /></div>
        <div class="field"><input id="codeInput" value="${MOCK_CODE}" placeholder="模拟验证码" /></div>
        <button class="primary-btn wide-btn" id="loginBtn">登录</button>
      </section>`, params);
    $("#loginBtn").addEventListener("click", () => runAction(async () => {
      const phone = $("#phoneInput").value.trim();
      await api("/api/v1/auth/mock-code", { method: "POST", body: { phone } });
      const data = await api("/api/v1/auth/login", { method: "POST", body: { phone, code: $("#codeInput").value.trim() } });
      localStorage.setItem(TOKEN_KEY, data.token);
      localStorage.setItem(USER_KEY, JSON.stringify(data.user));
      navigate(params.redirect || "#/");
    }));
  }

  async function renderAdmin(params) {
    if (!adminToken()) return renderAdminLogin(params);
    const [productsData, ordersData] = await Promise.all([
      api("/api/v1/admin/products", { admin: true }),
      api("/api/v1/admin/orders", { admin: true }),
    ]);
    shell(`
      <section class="section-head"><h1 class="section-title">管理后台</h1><button class="ghost-btn" id="adminLogoutBtn">退出后台</button></section>
      <section class="admin-layout">
        <div class="card detail-info">
          <h2>新增商品</h2>
          <div class="field"><input id="pTitle" placeholder="商品标题" /></div>
          <div class="field"><input id="pPrice" type="number" placeholder="价格，单位元" /></div>
          <div class="field"><input id="pStock" type="number" placeholder="库存" /></div>
          <div class="field"><select id="pCategory">${state.categories.map((item) => `<option value="${item.id}">${item.name}</option>`).join("")}</select></div>
          <button class="primary-btn wide-btn" id="createProductBtn">创建并上架</button>
        </div>
        <div class="card detail-info">
          <h2>商品管理</h2>
          ${productsData.map((item) => `<div class="admin-row"><span>${item.title}</span><span>${statusText(item.status)}</span><button class="ghost-btn product-status" data-id="${item.id}" data-status="${item.status === "ON_SALE" ? "OFF_SALE" : "ON_SALE"}">${item.status === "ON_SALE" ? "下架" : "上架"}</button></div>`).join("")}
        </div>
      </section>
      <section class="card detail-info section">
        <h2>订单发货</h2>
        ${(ordersData.items || []).map((order) => `<div class="admin-row"><span>${order.orderNo}</span><span>${statusText(order.status)}</span><strong>${money(order.payableAmount)}</strong>${order.status === "PAID_PENDING_SHIPMENT" ? `<button class="primary-btn ship-btn" data-id="${order.id}">发货</button>` : ""}</div>`).join("") || `<div class="empty">暂无订单</div>`}
      </section>`, params);
    $("#adminLogoutBtn").addEventListener("click", () => {
      localStorage.removeItem(ADMIN_TOKEN_KEY);
      route();
    });
    $("#createProductBtn").addEventListener("click", () => runAction(async () => {
      const title = $("#pTitle").value.trim();
      const price = Math.round(Number($("#pPrice").value || 0) * 100);
      const stock = Number($("#pStock").value || 0);
      if (!title) return toast("请填写商品标题");
      if (!price || price <= 0) return toast("请填写有效价格");
      await api("/api/v1/admin/products", {
        method: "POST",
        admin: true,
        body: {
          categoryId: Number($("#pCategory").value),
          title,
          description: "后台创建的商品",
          images: [title.slice(0, 1) || "K"],
          status: "ON_SALE",
          skus: [{ specName: "规格", specValue: "默认", price, stock }],
        },
      });
      toast("商品已创建");
      route();
    }));
    document.querySelectorAll(".product-status").forEach((button) => button.addEventListener("click", () => runAction(async () => {
      await api(`/api/v1/admin/products/${button.dataset.id}/status`, { method: "PATCH", admin: true, body: { status: button.dataset.status } });
      route();
    })));
    document.querySelectorAll(".ship-btn").forEach((button) => button.addEventListener("click", () => runAction(async () => {
      await api(`/api/v1/admin/orders/${button.dataset.id}/shipment`, { method: "POST", admin: true, body: { logisticsCompany: "顺丰速运", trackingNo: `KK${Date.now()}` } });
      toast("已录入物流");
      route();
    })));
  }

  async function renderAdminLogin(params) {
    shell(`
      <section class="card detail-info narrow-panel">
        <h1>后台登录</h1>
        <div class="field"><input id="adminPhoneInput" value="13900000000" placeholder="管理员手机号" /></div>
        <div class="field"><input id="adminCodeInput" value="${MOCK_CODE}" placeholder="模拟验证码" /></div>
        <button class="primary-btn wide-btn" id="adminLoginBtn">进入后台</button>
      </section>`, params);
    $("#adminLoginBtn").addEventListener("click", () => runAction(async () => {
      const data = await api("/api/v1/admin/auth/login", { method: "POST", body: { phone: $("#adminPhoneInput").value.trim(), code: $("#adminCodeInput").value.trim() } });
      localStorage.setItem(ADMIN_TOKEN_KEY, data.token);
      route();
    }));
  }

  function parseHash() {
    const hash = window.location.hash || "#/";
    const [path, query = ""] = hash.slice(1).split("?");
    return { path: path || "/", params: Object.fromEntries(new URLSearchParams(query).entries()) };
  }

  async function route() {
    try {
      await Promise.all([loadCategories(), refreshCart().catch(() => {})]);
      const { path, params } = parseHash();
      if (path === "/") return renderHome(params);
      if (path === "/products") return renderProducts(params);
      if (path === "/product") return renderProduct(params);
      if (path === "/cart") return renderCart(params);
      if (path === "/checkout") return renderCheckout(params);
      if (path === "/pay") return renderPay(params);
      if (path === "/orders") return renderOrders(params);
      if (path.startsWith("/orders/")) return renderOrderDetail({ id: path.split("/")[2] });
      if (path === "/login") return renderLogin(params);
      if (path.startsWith("/admin")) return renderAdmin(params);
      return renderHome(params);
    } catch (error) {
      if (error.admin && (error.code === "AUTH_REQUIRED" || error.code === "AUTH_FORBIDDEN")) {
        localStorage.removeItem(ADMIN_TOKEN_KEY);
        return renderAdminLogin({});
      }
      if (handleApiError(error)) return;
      if (error.message === "AUTH_REQUIRED") return;
      shell(`<section class="card detail-info narrow-panel"><h1>页面加载失败</h1><p>${error.message}</p><button class="primary-btn wide-btn" onclick="location.reload()">重试</button></section>`);
    }
  }

  window.addEventListener("hashchange", route);
  route();
})();
