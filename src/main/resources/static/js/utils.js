/**
 * utils.js — Shared utilities: toasts, loaders, status badges, formatters
 */

// ── Toast Notifications ──
const Toast = (() => {
  let container = null;

  function getContainer() {
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container-custom';
      document.body.appendChild(container);
    }
    return container;
  }

  function show(message, type = 'info', duration = 4000) {
    const icons = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' };
    const c = getContainer();
    const toast = document.createElement('div');
    toast.className = `toast-custom ${type}`;
    toast.innerHTML = `
      <span style="font-size:1.1rem;font-weight:700">${icons[type] || 'ℹ'}</span>
      <span style="font-size:0.875rem;color:#f1f5f9;flex:1">${message}</span>
      <span onclick="this.closest('.toast-custom').remove()" style="cursor:pointer;color:#475569;font-size:1.1rem">×</span>
    `;
    c.appendChild(toast);
    setTimeout(() => {
      toast.classList.add('removing');
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  return {
    success: (msg, d) => show(msg, 'success', d),
    error:   (msg, d) => show(msg, 'error', d),
    warning: (msg, d) => show(msg, 'warning', d),
    info:    (msg, d) => show(msg, 'info', d),
  };
})();

// ── Loader ──
const Loader = (() => {
  let overlay = null;

  function show() {
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.className = 'loader-overlay';
      overlay.innerHTML = '<div class="loader-spinner"></div>';
      document.body.appendChild(overlay);
    }
    overlay.style.display = 'flex';
  }

  function hide() {
    if (overlay) overlay.style.display = 'none';
  }

  return { show, hide };
})();

// ── Status Badge ──
function getStatusBadge(status) {
  const map = {
    'PENDING':           ['badge-pending',     'Pending'],
    'CONFIRMED':         ['badge-confirmed',   'Confirmed'],
    'VEHICLE_RECEIVED':  ['badge-received',    'Received'],
    'BOOKED':            ['badge-pending',     'Booked'],
    'BAY_ALLOCATED':     ['badge-received',    'Bay Allocated'],
    'SERVICE_STARTED':   ['badge-progress',    'Started'],
    'SERVICE_IN_PROGRESS':['badge-progress',   'In Progress'],
    'WAITING_FOR_PARTS': ['badge-waiting',     'Waiting Parts'],
    'QUALITY_CHECK':     ['badge-quality',     'Quality Check'],
    'READY_FOR_DELIVERY':['badge-ready',       'Ready'],
    'COMPLETED':         ['badge-completed',   'Completed'],
    'CANCELLED':         ['badge-cancelled',   'Cancelled'],
    'AVAILABLE':         ['badge-available',   'Available'],
    'OCCUPIED':          ['badge-occupied',    'Occupied'],
    'MAINTENANCE':       ['badge-maintenance', 'Maintenance'],
    'BUSY':              ['badge-occupied',    'Busy'],
    'ON_LEAVE':          ['badge-maintenance', 'On Leave'],
    'PAID':              ['badge-completed',   'Paid'],
  };
  const [cls, label] = map[status] || ['badge-pending', status];
  return `<span class="badge-status ${cls}">${label}</span>`;
}

// ── Status Timeline Config ──
const STATUS_STEPS = [
  { key: 'BOOKED',             icon: '📋', label: 'Booking Confirmed'   },
  { key: 'VEHICLE_RECEIVED',   icon: '🚗', label: 'Vehicle Received'    },
  { key: 'BAY_ALLOCATED',      icon: '🏢', label: 'Bay Allocated'       },
  { key: 'SERVICE_STARTED',    icon: '🔧', label: 'Service Started'     },
  { key: 'SERVICE_IN_PROGRESS',icon: '⚙️', label: 'Service In Progress' },
  { key: 'WAITING_FOR_PARTS',  icon: '⏳', label: 'Waiting for Parts'  },
  { key: 'QUALITY_CHECK',      icon: '✅', label: 'Quality Check'       },
  { key: 'READY_FOR_DELIVERY', icon: '🎉', label: 'Ready for Delivery'  },
  { key: 'COMPLETED',          icon: '🏁', label: 'Completed'           },
];

function buildTimeline(currentStatus, history) {
  const statusOrder = STATUS_STEPS.map(s => s.key);
  const currentIdx = statusOrder.indexOf(currentStatus);

  // Build a map of history records by status
  const historyMap = {};
  if (history) {
    history.forEach(h => { historyMap[h.newStatus] = h; });
  }

  let html = '<div class="timeline">';
  STATUS_STEPS.forEach((step, idx) => {
    let dotClass, lineClass;
    if (idx < currentIdx) { dotClass = 'done'; lineClass = 'done'; }
    else if (idx === currentIdx) { dotClass = 'current'; lineClass = 'current'; }
    else { dotClass = 'pending'; lineClass = ''; }

    const hist = historyMap[step.key];
    const timeStr = hist ? formatTime(hist.changedAt) : '';
    const remark = hist && hist.remarks ? `<div class="timeline-step-remark">${hist.remarks}</div>` : '';

    const doneIcon = '✓';
    const displayIcon = dotClass === 'done' ? doneIcon : step.icon;

    html += `
      <div class="timeline-item">
        <div class="timeline-indicator">
          <div class="timeline-dot ${dotClass}">${displayIcon}</div>
          ${idx < STATUS_STEPS.length - 1 ? `<div class="timeline-line ${lineClass}"></div>` : ''}
        </div>
        <div class="timeline-content">
          <div class="timeline-step-title ${dotClass}">${step.label}</div>
          ${timeStr ? `<div class="timeline-step-time">${timeStr}</div>` : ''}
          ${remark}
        </div>
      </div>`;
  });
  html += '</div>';
  return html;
}

// ── Formatters ──
function formatDate(str) {
  if (!str) return '—';
  try { return new Date(str).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' }); }
  catch { return str; }
}

function formatDateTime(str) {
  if (!str) return '—';
  try { return new Date(str).toLocaleString('en-IN', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit', hour12:true }); }
  catch { return str; }
}

function formatTime(str) {
  if (!str) return '—';
  try { return new Date(str).toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit', hour12:true }); }
  catch { return str; }
}

function formatCurrency(amount) {
  if (amount === null || amount === undefined) return '₹0.00';
  return '₹' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// ── Sidebar User Setup ──
function setupSidebarUser() {
  const profile = GarageAuth.getStoredUser();
  if (!profile) return;
  const nameEl = document.getElementById('sidebar-user-name');
  const roleEl = document.getElementById('sidebar-user-role');
  const avatarEl = document.getElementById('sidebar-user-avatar');
  if (nameEl) nameEl.textContent = profile.name || profile.email;
  if (roleEl) roleEl.textContent = profile.role;
  if (avatarEl) avatarEl.textContent = (profile.name || profile.email || '?')[0].toUpperCase();
}

// ── Logout ──
function setupLogoutBtn() {
  const btn = document.getElementById('btn-logout');
  if (btn) btn.addEventListener('click', () => GarageAuth.logout());
}

// ── Confirmation dialog ──
function confirm(message, title = 'Confirm Action') {
  return new Promise(resolve => {
    const modal = document.getElementById('confirmModal');
    if (!modal) { resolve(window.confirm(message)); return; }
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmMessage').textContent = message;
    const yes = document.getElementById('confirmYes');
    const bsModal = new bootstrap.Modal(modal);
    const onYes = () => { resolve(true); bsModal.hide(); yes.removeEventListener('click', onYes); };
    yes.addEventListener('click', onYes);
    modal.addEventListener('hidden.bs.modal', () => resolve(false), { once: true });
    bsModal.show();
  });
}

window.Toast = Toast;
window.Loader = Loader;
window.getStatusBadge = getStatusBadge;
window.STATUS_STEPS = STATUS_STEPS;
window.buildTimeline = buildTimeline;
window.formatDate = formatDate;
window.formatDateTime = formatDateTime;
window.formatTime = formatTime;
window.formatCurrency = formatCurrency;
window.setupSidebarUser = setupSidebarUser;
window.setupLogoutBtn = setupLogoutBtn;
window.garageConfirm = confirm;
