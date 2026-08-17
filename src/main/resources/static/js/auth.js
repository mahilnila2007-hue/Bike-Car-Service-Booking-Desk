/**
 * auth.js - Firebase Authentication & Role-Based Routing
 */

const GarageAuth = (() => {
  const TOKEN_KEY = 'garage_token';
  const USER_KEY = 'garage_user';

  async function getToken() {
    const user = garageAuth.currentUser;
    if (!user) return null;
    return await user.getIdToken();
  }

  async function getTokenForce() {
    const user = garageAuth.currentUser;
    if (!user) return null;
    return await user.getIdToken(true);
  }

  function getStoredUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; }
  }

  function storeUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  function clearUser() {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
  }

  async function login(email, password) {
    const cred = await garageAuth.signInWithEmailAndPassword(email, password);
    const token = await cred.user.getIdToken();

    // Fetch profile from backend
    const resp = await fetch('/api/auth/profile', {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    });
    if (!resp.ok) throw new Error('Failed to load profile.');
    const profile = await resp.json();
    storeUser(profile);
    return { user: cred.user, profile, token };
  }

  async function register(email, password, name, phone) {
    const cred = await garageAuth.createUserWithEmailAndPassword(email, password);
    const token = await cred.user.getIdToken();

    // Create profile in backend
    const resp = await fetch('/api/auth/profile', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, phone, role: 'CUSTOMER' })
    });
    if (!resp.ok) throw new Error('Failed to create profile.');
    const profile = await resp.json();
    storeUser(profile);
    return { user: cred.user, profile, token };
  }

  async function logout() {
    clearUser();
    await garageAuth.signOut();
    window.location.href = '/login.html';
  }

  function redirectByRole(role) {
    const roleRoutes = {
      ADMIN: '/admin-dashboard.html',
      STAFF: '/staff-dashboard.html',
      CUSTOMER: '/customer-dashboard.html'
    };
    window.location.href = roleRoutes[role] || '/customer-dashboard.html';
  }

  function requireAuth(allowedRoles) {
    garageAuth.onAuthStateChanged(async (user) => {
      if (!user) {
        window.location.href = '/login.html';
        return;
      }
      const profile = getStoredUser();
      if (!profile) {
        // Re-fetch profile
        const token = await user.getIdToken();
        const resp = await fetch('/api/auth/profile', {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (resp.ok) {
          const p = await resp.json();
          storeUser(p);
          if (allowedRoles && !allowedRoles.includes(p.role)) {
            redirectByRole(p.role);
          }
        } else {
          await logout();
        }
        return;
      }
      if (allowedRoles && !allowedRoles.includes(profile.role)) {
        redirectByRole(profile.role);
      }
    });
  }

  return { getToken, getTokenForce, getStoredUser, login, register, logout, redirectByRole, requireAuth };
})();

window.GarageAuth = GarageAuth;
