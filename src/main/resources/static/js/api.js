/**
 * api.js - REST API client with automatic Firebase token injection
 */

const API = (() => {
  const BASE = '';

  async function getHeaders() {
    const token = await GarageAuth.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
  }

  async function request(method, path, body) {
    const headers = await getHeaders();
    const opts = { method, headers };
    if (body !== undefined) opts.body = JSON.stringify(body);

    const resp = await fetch(BASE + path, opts);
    if (resp.status === 204) return null;

    const data = await resp.json().catch(() => ({}));
    if (!resp.ok) {
      const msg = data.message || data.error || `HTTP ${resp.status}`;
      throw new Error(msg);
    }
    return data;
  }

  return {
    get:    (path)        => request('GET',    path),
    post:   (path, body)  => request('POST',   path, body),
    put:    (path, body)  => request('PUT',    path, body),
    patch:  (path, body)  => request('PATCH',  path, body),
    delete: (path)        => request('DELETE', path),

    // Auth
    createProfile: (body)       => request('POST', '/api/auth/profile', body),
    getProfile:    ()           => request('GET',  '/api/auth/profile'),
    updateProfile: (body)       => request('PUT',  '/api/auth/profile', body),

    // Users (admin)
    getAllUsers:    ()           => request('GET',  '/api/users'),
    updateUserRole:(uid, role)  => request('PATCH', `/api/users/${uid}/role`, { role }),

    // Vehicles
    addVehicle:     (body)      => request('POST', '/api/vehicles', body),
    getVehicles:    (params)    => request('GET',  '/api/vehicles' + (params || '')),
    getVehicle:     (id)        => request('GET',  `/api/vehicles/${id}`),
    updateVehicle:  (id, body)  => request('PUT',  `/api/vehicles/${id}`, body),
    deleteVehicle:  (id)        => request('DELETE', `/api/vehicles/${id}`),

    // Bookings
    createBooking:  (body)      => request('POST', '/api/bookings', body),
    getBookings:    (params)    => request('GET',  '/api/bookings' + (params || '')),
    getBooking:     (id)        => request('GET',  `/api/bookings/${id}`),
    confirmBooking: (id)        => request('POST', `/api/bookings/${id}/confirm`),
    receiveVehicle: (id)        => request('POST', `/api/bookings/${id}/receive`),
    cancelBooking:  (id)        => request('POST', `/api/bookings/${id}/cancel`),

    // Service Jobs
    createServiceJob: (bookingId)      => request('POST', '/api/service-jobs', { bookingId }),
    getServiceJobs:   (params)         => request('GET',  '/api/service-jobs' + (params || '')),
    getServiceJob:    (id)             => request('GET',  `/api/service-jobs/${id}`),
    getJobByBooking:  (bookingId)      => request('GET',  `/api/service-jobs/booking/${bookingId}`),
    allocateBay:      (id)             => request('POST', `/api/service-jobs/${id}/allocate-bay`),
    assignMechanic:   (id, mechanicId) => request('POST', `/api/service-jobs/${id}/assign-mechanic`, mechanicId ? { mechanicId } : {}),
    startService:     (id)             => request('POST', `/api/service-jobs/${id}/start`),
    updateStatus:     (id, status, remarks) => request('PATCH', `/api/service-jobs/${id}/status`, { status, remarks }),
    updateNotes:      (id, notes)      => request('PATCH', `/api/service-jobs/${id}/notes`, { notes }),
    updateEta:        (id, eta)        => request('PATCH', `/api/service-jobs/${id}/eta`, { eta }),
    getStatusHistory: (id)             => request('GET',  `/api/service-jobs/${id}/history`),

    // Bays
    getBays:          ()        => request('GET',  '/api/bays'),
    getAvailableBays: ()        => request('GET',  '/api/bays/available'),
    updateBayStatus:  (id, status) => request('PATCH', `/api/bays/${id}/status`, { status }),

    // Mechanics
    getMechanics:         ()    => request('GET',  '/api/mechanics'),
    getAvailableMechanics:()    => request('GET',  '/api/mechanics/available'),
    updateMechanicStatus: (id, status) => request('PATCH', `/api/mechanics/${id}/status`, { status }),

    // Service Types
    getServiceTypes: ()         => request('GET',  '/api/service-types'),

    // Parts
    getParts:        ()         => request('GET',  '/api/parts'),
    getPart:         (id)       => request('GET',  `/api/parts/${id}`),
    addPartToJob:    (jobId, partId, qty) => request('POST', `/api/bills/service-job/${jobId}/parts`, { partId, quantity: qty }),
    getPartsForJob:  (jobId)    => request('GET',  `/api/bills/service-job/${jobId}/parts`),

    // Bills
    generateBill:    (body)     => request('POST', '/api/bills', body),
    getBills:        ()         => request('GET',  '/api/bills'),
    getBill:         (id)       => request('GET',  `/api/bills/${id}`),
    getBillByJob:    (jobId)    => request('GET',  `/api/bills/service-job/${jobId}`),
    markBillPaid:    (id)       => request('POST', `/api/bills/${id}/pay`),

    // Reports
    getDashboard:    ()         => request('GET',  '/api/reports/dashboard'),
    getDailyReport:  (date)     => request('GET',  `/api/reports/daily${date ? '?date='+date : ''}`),
    getBayReport:    ()         => request('GET',  '/api/reports/bays'),
    getMechanicReport: ()       => request('GET',  '/api/reports/mechanics'),

    // Seed
    seedData:        ()         => request('POST', '/api/seed'),
  };
})();

window.API = API;
