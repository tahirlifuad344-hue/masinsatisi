import { useEffect, useMemo, useState } from 'react';
import { apiRequest, API_BASE } from './api.js';

const FUEL_LABELS = {
  GASOLINE: 'Benzin', DIESEL: 'Dizel',
  HYBRID: 'Hibrid', ELECTRIC: 'Elektrik', LPG: 'Qaz'
};
const fuelTypes = Object.keys(FUEL_LABELS);

const sortOptions = [
  { value: 'DATE_DESC', label: 'Ən yeni' },
  { value: 'DATE_ASC',  label: 'Ən köhnə' },
  { value: 'PRICE_ASC', label: 'Ucuzdan bahaya' },
  { value: 'PRICE_DESC', label: 'Bahadan ucuza' },
];

const emptyListingForm = {
  brand: '', model: '', year: '', engineSize: '',
  color: '', fuelType: 'GASOLINE', price: '',
};
const emptyRegisterForm = { fullName: '', phone: '', email: '', password: '' };
const emptyLoginForm = { email: '', password: '' };

function toListingPayload(form) {
  return {
    brand: form.brand.trim(), model: form.model.trim(),
    year: Number(form.year), engineSize: Number(form.engineSize),
    color: form.color.trim(), fuelType: form.fuelType,
    price: Number(form.price),
  };
}

function formatMoney(value) {
  if (value == null) return '-';
  return new Intl.NumberFormat('az-AZ', { style: 'currency', currency: 'AZN' }).format(Number(value));
}

function formatDate(value) {
  if (!value) return '-';
  const d = new Date(value);
  return isNaN(d) ? value : new Intl.DateTimeFormat('az-AZ', { dateStyle: 'medium' }).format(d);
}

export default function App() {
  const [authMode, setAuthMode] = useState('login');
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm);
  const [loginForm, setLoginForm] = useState(emptyLoginForm);
  const [auth, setAuth] = useState(() => {
    const s = localStorage.getItem('auth');
    return s ? JSON.parse(s) : null;
  });
  const token = auth?.token || '';

  const [filters, setFilters] = useState({
    brand: '', model: '', yearFrom: '', yearTo: '',
    engineFrom: '', engineTo: '', color: '', fuelType: '',
    priceFrom: '', priceTo: '', sort: 'DATE_DESC', size: 12,
  });
  const [listings, setListings] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 12, totalElements: 0, totalPages: 0 });
  const [selected, setSelected] = useState(null);
  const [createForm, setCreateForm] = useState(emptyListingForm);
  const [createImages, setCreateImages] = useState([]);
  const [editForm, setEditForm] = useState(null);
  const [editImages, setEditImages] = useState([]);
  const [status, setStatus] = useState('');
  const [statusType, setStatusType] = useState('ok');
  const [loading, setLoading] = useState(false);

  const isOwner = useMemo(() => {
    if (!auth?.userId || !selected?.user?.id) return false;
    return Number(auth.userId) === Number(selected.user.id);
  }, [auth, selected]);

  function showStatus(msg, type = 'ok') {
    setStatus(msg); setStatusType(type);
    setTimeout(() => setStatus(''), 3000);
  }

  async function loadListings(page = 0) {
    setLoading(true);
    try {
      const p = new URLSearchParams();
      if (filters.brand) p.set('brand', filters.brand);
      if (filters.model) p.set('model', filters.model);
      if (filters.yearFrom) p.set('yearFrom', filters.yearFrom);
      if (filters.yearTo) p.set('yearTo', filters.yearTo);
      if (filters.engineFrom) p.set('engineFrom', filters.engineFrom);
      if (filters.engineTo) p.set('engineTo', filters.engineTo);
      if (filters.color) p.set('color', filters.color);
      if (filters.fuelType) p.set('fuelType', filters.fuelType);
      if (filters.priceFrom) p.set('priceFrom', filters.priceFrom);
      if (filters.priceTo) p.set('priceTo', filters.priceTo);
      p.set('sort', filters.sort);
      p.set('page', String(page));
      p.set('size', String(filters.size));

      const data = await apiRequest(`/api/listings?${p.toString()}`);
      setListings(data.content || []);
      setPageInfo({ page: data.page, size: data.size, totalElements: data.totalElements, totalPages: data.totalPages });
      if (data.content?.length > 0) setSelected(prev => prev || data.content[0]);
    } catch (err) {
      showStatus(err.message || 'Elanlar yüklənmədi', 'error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadListings(0); }, []);

  function saveAuth(next) {
    setAuth(next);
    next ? localStorage.setItem('auth', JSON.stringify(next)) : localStorage.removeItem('auth');
  }

  async function handleRegister(e) {
    e.preventDefault();
    try {
      const data = await apiRequest('/api/auth/register', { method: 'POST', body: registerForm });
      saveAuth(data); setRegisterForm(emptyRegisterForm);
      showStatus('Qeydiyyat uğurlu oldu!');
    } catch (err) { showStatus(err.message || 'Qeydiyyat alınmadı', 'error'); }
  }

  async function handleLogin(e) {
    e.preventDefault();
    try {
      const data = await apiRequest('/api/auth/login', { method: 'POST', body: loginForm });
      saveAuth(data); setLoginForm(emptyLoginForm);
      showStatus('Xoş gəldiniz, ' + data.fullName + '!');
    } catch (err) { showStatus(err.message || 'Giriş alınmadı', 'error'); }
  }

  function handleLogout() {
    saveAuth(null); showStatus('Çıxış edildi');
  }

  async function handleCreateListing(e) {
    e.preventDefault();
    try {
      const fd = new FormData();
      fd.append('data', new Blob([JSON.stringify(toListingPayload(createForm))], { type: 'application/json' }));
      createImages.forEach(f => fd.append('images', f));
      const created = await apiRequest('/api/listings', { method: 'POST', body: fd }, token);
      setCreateForm(emptyListingForm); setCreateImages([]);
      setSelected(created); await loadListings(0);
      showStatus('Elan uğurla yaradıldı!');
    } catch (err) { showStatus(err.message || 'Elan yaradılmadı', 'error'); }
  }

  async function handleUpdateListing(e) {
    e.preventDefault();
    if (!selected) return;
    try {
      const updated = await apiRequest(`/api/listings/${selected.id}`, { method: 'PUT', body: toListingPayload(editForm) }, token);
      setSelected(updated); await loadListings(pageInfo.page);
      showStatus('Elan yeniləndi!');
    } catch (err) { showStatus(err.message || 'Yenilənmədi', 'error'); }
  }

  async function handleDeleteListing(id) {
    if (!id) return;
    try {
      await apiRequest(`/api/listings/${id}`, { method: 'DELETE' }, token);
      setSelected(null); await loadListings(0);
      showStatus('Elan silindi');
    } catch (err) { showStatus(err.message || 'Silinmədi', 'error'); }
  }

  async function handleAddImages(e) {
    e.preventDefault();
    if (!selected || editImages.length === 0) return;
    try {
      const fd = new FormData();
      editImages.forEach(f => fd.append('images', f));
      const updated = await apiRequest(`/api/listings/${selected.id}/images`, { method: 'POST', body: fd }, token);
      setSelected(updated); setEditImages([]); await loadListings(pageInfo.page);
      showStatus('Şəkillər əlavə edildi!');
    } catch (err) { showStatus(err.message || 'Şəkillər əlavə edilmədi', 'error'); }
  }

  async function handleDeleteImage(imageId) {
    try {
      await apiRequest(`/api/images/${imageId}`, { method: 'DELETE' }, token);
      setSelected({ ...selected, images: (selected.images || []).filter(i => i.id !== imageId) });
      showStatus('Şəkil silindi');
    } catch (err) { showStatus(err.message || 'Şəkil silinmədi', 'error'); }
  }

  function handleSelect(listing) {
    setSelected(listing);
    setEditForm({
      brand: listing.brand, model: listing.model, year: listing.year,
      engineSize: listing.engineSize, color: listing.color,
      fuelType: listing.fuelType, price: listing.price,
    });
  }

  return (
      <div className="app">

        {/* ── TOPBAR ── */}
        <header className="topbar">
          <div className="brand">
            <div className="logo-dot" />
            <div>
              <h1>MaşınSatışı</h1>
              <p>Sürətli al-sat platforması</p>
            </div>
          </div>
          <div className="top-actions">
            {auth ? (
                <div className="user-chip">
                  <div>
                    <strong>{auth.fullName}</strong>
                    <span>{auth.email}</span>
                  </div>
                  <button className="btn ghost" onClick={handleLogout}>Çıxış</button>
                </div>
            ) : (
                <div className="auth-toggle">
                  <button className={authMode === 'login' ? 'btn primary' : 'btn ghost'} onClick={() => setAuthMode('login')}>Daxil ol</button>
                  <button className={authMode === 'register' ? 'btn primary' : 'btn ghost'} onClick={() => setAuthMode('register')}>Qeydiyyat</button>
                </div>
            )}
          </div>
        </header>

        {/* ── HERO ── */}
        <section className="hero">
          <div className="hero-text">
            <h2>Yeni elanlar, real qiymətlər, etibarlı satıcılar.</h2>
            <p>Maşınları tap, filtrə et, bir klikdə elan ver.</p>
            <div className="hero-actions">
              <button className="btn primary" style={{ background: 'white', color: 'var(--blue)' }}
                      onClick={() => document.getElementById(auth ? 'create-form' : 'auth-panel')?.scrollIntoView({ behavior: 'smooth' })}>
                + Yeni elan
              </button>
              <button className="btn ghost" style={{ borderColor: 'rgba(255,255,255,.4)', color: 'white' }}
                      onClick={() => loadListings(0)}>
                Yenilə
              </button>
            </div>
            {status && <div className={`status ${statusType}`}>{status}</div>}
          </div>
          <div className="hero-card">
            <h3>Platforma göstəriciləri</h3>
            <div className="hero-stats">
              <div className="stat"><span>Elanlar</span><strong>{pageInfo.totalElements}</strong></div>
              <div className="stat"><span>Xidmət</span><strong>24/7</strong></div>
              <div className="stat"><span>Təhlükəsiz</span><strong>JWT</strong></div>
            </div>
          </div>
        </section>

        {/* ── MAIN LAYOUT ── */}
        <main className="layout">

          {/* SOL PANEL */}
          <aside className="panel">
            <h3>Filtrlər</h3>
            <form onSubmit={e => { e.preventDefault(); loadListings(0); }} className="form-grid">
              <label>Marka<input value={filters.brand} onChange={e => setFilters({ ...filters, brand: e.target.value })} placeholder="BMW, Toyota..." /></label>
              <label>Model<input value={filters.model} onChange={e => setFilters({ ...filters, model: e.target.value })} placeholder="X5, Camry..." /></label>
              <div className="inline">
                <label>İldən<input type="number" value={filters.yearFrom} onChange={e => setFilters({ ...filters, yearFrom: e.target.value })} placeholder="2010" /></label>
                <label>İlədək<input type="number" value={filters.yearTo} onChange={e => setFilters({ ...filters, yearTo: e.target.value })} placeholder="2024" /></label>
              </div>
              <label>Rəng<input value={filters.color} onChange={e => setFilters({ ...filters, color: e.target.value })} placeholder="Qara, Ağ..." /></label>
              <label>Yanacaq
                <select value={filters.fuelType} onChange={e => setFilters({ ...filters, fuelType: e.target.value })}>
                  <option value="">Hamısı</option>
                  {fuelTypes.map(f => <option key={f} value={f}>{FUEL_LABELS[f]}</option>)}
                </select>
              </label>
              <div className="inline">
                <label>Qiymət (min)<input type="number" value={filters.priceFrom} onChange={e => setFilters({ ...filters, priceFrom: e.target.value })} placeholder="0" /></label>
                <label>Qiymət (max)<input type="number" value={filters.priceTo} onChange={e => setFilters({ ...filters, priceTo: e.target.value })} placeholder="100000" /></label>
              </div>
              <label>Sıralama
                <select value={filters.sort} onChange={e => setFilters({ ...filters, sort: e.target.value })}>
                  {sortOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
              </label>
              <button className="btn primary" type="submit">Axtar</button>
            </form>

            <div className="divider" />

            {/* AUTH */}
            {!auth && (
                <div id="auth-panel" className="panel-section">
                  <h3>{authMode === 'login' ? 'Daxil ol' : 'Qeydiyyat'}</h3>
                  {authMode === 'login' ? (
                      <form onSubmit={handleLogin} className="form-grid">
                        <label>Email<input type="email" value={loginForm.email} onChange={e => setLoginForm({ ...loginForm, email: e.target.value })} required /></label>
                        <label>Şifrə<input type="password" value={loginForm.password} onChange={e => setLoginForm({ ...loginForm, password: e.target.value })} required /></label>
                        <button className="btn primary" type="submit">Daxil ol</button>
                      </form>
                  ) : (
                      <form onSubmit={handleRegister} className="form-grid">
                        <label>Ad soyad<input value={registerForm.fullName} onChange={e => setRegisterForm({ ...registerForm, fullName: e.target.value })} required /></label>
                        <label>Telefon<input value={registerForm.phone} onChange={e => setRegisterForm({ ...registerForm, phone: e.target.value })} required /></label>
                        <label>Email<input type="email" value={registerForm.email} onChange={e => setRegisterForm({ ...registerForm, email: e.target.value })} required /></label>
                        <label>Şifrə<input type="password" value={registerForm.password} onChange={e => setRegisterForm({ ...registerForm, password: e.target.value })} required /></label>
                        <button className="btn primary" type="submit">Qeydiyyat</button>
                      </form>
                  )}
                  <p className="status" style={{ background: 'var(--blue-light)', color: 'var(--blue)', marginTop: 8 }}>Elan yaratmaq üçün daxil olmalısınız.</p>
                </div>
            )}

            {/* YENİ ELAN */}
            {auth && (
                <div id="create-form" className="panel-section">
                  <h3>+ Yeni elan</h3>
                  <form onSubmit={handleCreateListing} className="form-grid">
                    <label>Marka<input value={createForm.brand} onChange={e => setCreateForm({ ...createForm, brand: e.target.value })} required /></label>
                    <label>Model<input value={createForm.model} onChange={e => setCreateForm({ ...createForm, model: e.target.value })} required /></label>
                    <div className="inline">
                      <label>İl<input type="number" value={createForm.year} onChange={e => setCreateForm({ ...createForm, year: e.target.value })} required /></label>
                      <label>Motor (L)<input type="number" step="0.1" value={createForm.engineSize} onChange={e => setCreateForm({ ...createForm, engineSize: e.target.value })} required /></label>
                    </div>
                    <label>Rəng<input value={createForm.color} onChange={e => setCreateForm({ ...createForm, color: e.target.value })} required /></label>
                    <label>Yanacaq
                      <select value={createForm.fuelType} onChange={e => setCreateForm({ ...createForm, fuelType: e.target.value })}>
                        {fuelTypes.map(f => <option key={f} value={f}>{FUEL_LABELS[f]}</option>)}
                      </select>
                    </label>
                    <label>Qiymət (AZN)<input type="number" value={createForm.price} onChange={e => setCreateForm({ ...createForm, price: e.target.value })} required /></label>
                    <label>Şəkillər<input type="file" accept="image/*" multiple onChange={e => setCreateImages(Array.from(e.target.files || []))} required /></label>
                    <button className="btn primary" type="submit">Elanı göndər</button>
                  </form>
                </div>
            )}
          </aside>

          {/* MƏRKƏZ — ELANLAR */}
          <section className="content">
            <div className="content-header">
              <div>
                <h3>Elanlar</h3>
                <p className="small">Cəmi: {pageInfo.totalElements} elan</p>
              </div>
              <div className="pager">
                <button className="btn ghost" disabled={pageInfo.page === 0 || loading} onClick={() => loadListings(pageInfo.page - 1)}>← Əvvəlki</button>
                <span>{pageInfo.page + 1} / {pageInfo.totalPages || 1}</span>
                <button className="btn ghost" disabled={pageInfo.page + 1 >= pageInfo.totalPages || loading} onClick={() => loadListings(pageInfo.page + 1)}>Növbəti →</button>
              </div>
            </div>

            {loading && <div className="empty">Yüklənir...</div>}
            {!loading && listings.length === 0 && <div className="empty">Heç bir elan tapılmadı.</div>}

            <div className="grid">
              {listings.map(listing => (
                  <button key={listing.id} className={`card ${selected?.id === listing.id ? 'active' : ''}`} onClick={() => handleSelect(listing)}>
                    <div className="card-media">
                      {listing.images?.length > 0
                          ? <img src={`${API_BASE}${listing.images[0].url}`} alt={`${listing.brand} ${listing.model}`} />
                          : <div className="placeholder">Şəkil yoxdur</div>}
                    </div>
                    <div className="card-body">
                      <div className="card-title">
                        <h4>{listing.brand} {listing.model}</h4>
                        <span className="badge">{FUEL_LABELS[listing.fuelType] || listing.fuelType}</span>
                      </div>
                      <div className="card-meta">
                        <span>{listing.year}</span>
                        <span>{listing.engineSize} L</span>
                        <span>{listing.color}</span>
                      </div>
                      <div className="card-footer">
                        <strong>{formatMoney(listing.price)}</strong>
                        <span>{listing.user?.fullName}</span>
                      </div>
                    </div>
                  </button>
              ))}
            </div>
          </section>

          {/* SAĞ PANEL — DETALLAR */}
          <aside className="panel detail">
            <h3>Detallar</h3>
            {!selected && <div className="empty">Elan seçin.</div>}
            {selected && (
                <div className="detail-body">
                  <div className="detail-gallery">
                    {(selected.images || []).length === 0 && <div className="placeholder" style={{ gridColumn: '1/-1' }}>Şəkil yoxdur</div>}
                    {(selected.images || []).map(img => (
                        <div key={img.id} className="thumb">
                          <img src={`${API_BASE}${img.url}`} alt="" />
                          {isOwner && <button className="chip" onClick={() => handleDeleteImage(img.id)}>Sil</button>}
                        </div>
                    ))}
                  </div>
                  <div className="detail-info">
                    <h4>{selected.brand} {selected.model}</h4>
                    <p className="small">{formatDate(selected.createdAt)}</p>
                    <div className="detail-grid">
                      <span>İl</span><strong>{selected.year}</strong>
                      <span>Motor</span><strong>{selected.engineSize} L</strong>
                      <span>Rəng</span><strong>{selected.color}</strong>
                      <span>Yanacaq</span><strong>{FUEL_LABELS[selected.fuelType] || selected.fuelType}</strong>
                      <span>Qiymət</span><strong>{formatMoney(selected.price)}</strong>
                    </div>
                    <div className="seller">
                      <div>
                        <strong>{selected.user?.fullName}</strong>
                        <span>{selected.user?.phone}</span>
                      </div>
                    </div>
                    {isOwner && (
                        <div className="owner-actions" style={{ marginTop: 10 }}>
                          <button className="btn danger" onClick={() => handleDeleteListing(selected.id)}>Elanı sil</button>
                        </div>
                    )}
                  </div>

                  {isOwner && editForm && (
                      <div className="panel-section">
                        <h4>Elanı yenilə</h4>
                        <form onSubmit={handleUpdateListing} className="form-grid">
                          <label>Marka<input value={editForm.brand} onChange={e => setEditForm({ ...editForm, brand: e.target.value })} required /></label>
                          <label>Model<input value={editForm.model} onChange={e => setEditForm({ ...editForm, model: e.target.value })} required /></label>
                          <div className="inline">
                            <label>İl<input type="number" value={editForm.year} onChange={e => setEditForm({ ...editForm, year: e.target.value })} required /></label>
                            <label>Motor<input type="number" step="0.1" value={editForm.engineSize} onChange={e => setEditForm({ ...editForm, engineSize: e.target.value })} required /></label>
                          </div>
                          <label>Rəng<input value={editForm.color} onChange={e => setEditForm({ ...editForm, color: e.target.value })} required /></label>
                          <label>Yanacaq
                            <select value={editForm.fuelType} onChange={e => setEditForm({ ...editForm, fuelType: e.target.value })}>
                              {fuelTypes.map(f => <option key={f} value={f}>{FUEL_LABELS[f]}</option>)}
                            </select>
                          </label>
                          <label>Qiymət<input type="number" value={editForm.price} onChange={e => setEditForm({ ...editForm, price: e.target.value })} required /></label>
                          <button className="btn primary" type="submit">Yenilə</button>
                        </form>
                      </div>
                  )}

                  {isOwner && (
                      <div className="panel-section">
                        <h4>Şəkil əlavə et</h4>
                        <form onSubmit={handleAddImages} className="form-grid">
                          <label>Şəkillər<input type="file" accept="image/*" multiple onChange={e => setEditImages(Array.from(e.target.files || []))} /></label>
                          <button className="btn ghost" type="submit">Əlavə et</button>
                        </form>
                      </div>
                  )}
                </div>
            )}
          </aside>
        </main>
      </div>
  );
}