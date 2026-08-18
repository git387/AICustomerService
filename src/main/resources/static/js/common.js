/**
 * 用户端公共脚本：登录态、购物车、下单、客服
 */
async function api(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    credentials: 'same-origin',
    ...options
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || (data.code && data.code !== 200)) {
    throw new Error(data.message || '请求失败');
  }
  return data;
}

let toastTimer;
function toast(message, options = {}) {
  let el = document.getElementById('app-toast');
  if (!el) {
    el = document.createElement('div');
    el.id = 'app-toast';
    el.setAttribute('role', 'status');
    document.body.appendChild(el);
  }
  el.className = 'app-toast ' + (options.type || 'success');
  el.replaceChildren();
  const text = document.createElement('span');
  text.textContent = message;
  el.appendChild(text);
  if (options.actionHref) {
    const link = document.createElement('a');
    link.href = options.actionHref;
    link.textContent = options.actionText || '查看';
    el.appendChild(link);
  }
  requestAnimationFrame(() => el.classList.add('show'));
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), options.duration || 2600);
}

function setCartBadge(count) {
  const badge = document.getElementById('cart-badge');
  if (!badge) return;
  if (count > 0) {
    badge.hidden = false;
    badge.textContent = count > 99 ? '99+' : String(count);
  } else {
    badge.hidden = true;
  }
}

async function refreshCartBadge() {
  try {
    const res = await api('/api/cart');
    const count = (res.data || []).reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
    setCartBadge(count);
  } catch (e) {
    setCartBadge(0);
  }
}

async function loginSubmit(event) {
  event.preventDefault();
  const form = event.target;
  try {
    await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: form.username.value,
        password: form.password.value
      })
    });
    const raw = form.redirect ? form.redirect.value : '/';
    const target = raw && raw.startsWith('/') ? raw : '/';
    window.location.href = target;
  } catch (e) {
    toast(e.message, { type: 'error' });
  }
}

async function registerSubmit(event) {
  event.preventDefault();
  const form = event.target;
  try {
    await api('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        username: form.username.value,
        password: form.password.value,
        nickname: form.nickname.value
      })
    });
    toast('注册成功，请登录');
    window.location.href = '/login';
  } catch (e) {
    toast(e.message, { type: 'error' });
  }
}

async function logout() {
  await api('/api/auth/logout', { method: 'POST' });
  window.location.href = '/login';
}

async function addToCart(productId, quantity = 1, btn) {
  try {
    if (btn) btn.disabled = true;
    await api('/api/cart', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity })
    });
    refreshCartBadge();
    toast('已加入购物车', { actionHref: '/cart', actionText: '去看看' });
    if (btn) {
      const oldText = btn.textContent;
      btn.textContent = '已加购';
      btn.classList.add('added');
      setTimeout(() => {
        btn.textContent = oldText;
        btn.classList.remove('added');
        btn.disabled = false;
      }, 1600);
    }
  } catch (e) {
    if (btn) btn.disabled = false;
    if (e.message.includes('未登录') || e.message.includes('请先登录')) {
      window.location.href = '/login';
    } else {
      toast(e.message, { type: 'error' });
    }
  }
}

window.api = api;
window.toast = toast;
window.addToCart = addToCart;
window.logout = logout;
window.loginSubmit = loginSubmit;
window.registerSubmit = registerSubmit;
document.addEventListener('DOMContentLoaded', () => {
  if (document.body.classList.contains('is-login')) {
    refreshCartBadge();
  }
});
