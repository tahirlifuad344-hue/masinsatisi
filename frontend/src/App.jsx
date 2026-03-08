import { useEffect, useMemo, useState } from 'react';
import { apiRequest, API_BASE } from './api.js';

const fuelTypes = ['GASOLINE', 'DIESEL', 'HYBRID', 'ELECTRIC', 'LPG'];
const sortOptions = [
  { value: 'DATE_DESC', label: 'Yeni tarix' },
  { value: 'DATE_ASC', label: 'Kohne tarix' },
  { value: 'PRICE_ASC', label: 'Ucuzdan bahaya' },
  { value: 'PRICE_DESC', label: 'Bahadan ucuza' },
];

const emptyListingForm = {
  brand: '',
  model: '',
  year: '',
  engineSize: '',
  color: '',
  fuelType: 'GASOLINE',
  price: '',
};

const emptyRegisterForm = {
  fullName: '',
  phone: '',
  email: '',
  password: '',
};

const emptyLoginForm = {
  email: '',
  password: '',
};

function toListingPayload(form) {
  return {
    brand: form.brand.trim(),
    model: form.model.trim(),
    year: Number(form.year),
    engineSize: Number(form.engineSize),
    color: form.color.trim(),
    fuelType: form.fuelType,
    price: Number(form.price),
  };
}

function formatMoney(value) {
  if (value === null || value === undefined) return '-';
  const number = Number(value);
  if (Number.isNaN(number)) return String(value);
  return new Intl.NumberFormat('az-AZ', { style: 'currency', currency: 'AZN' }).format(number);
}

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('az-AZ', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

export default function App() {
  const [authMode, setAuthMode] = useState('login');
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm);
  const [loginForm, setLoginForm] = useState(emptyLoginForm);
  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem('auth');
    return stored ? JSON.parse(stored) : null;
  });

  const token = auth?.token || '';

  const [filters, setFilters] = useState({
    brand: '',
    model: '',
    yearFrom: '',
    yearTo: '',
    engineFrom: '',
    engineTo: '',
    color: '',
    fuelType: '',
    priceFrom: '',
    priceTo: '',
    sort: 'DATE_DESC',
    size: 12,
  });

  const [listings, setListings] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0,
  });
  const [selected, setSelected] = useState(null);
  const [createForm, setCreateForm] = useState(emptyListingForm);
  const [createImages, setCreateImages] = useState([]);
  const [editForm, setEditForm] = useState(null);
  const [editImages, setEditImages] = useState([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isOwner = useMemo(() => {
    if (!auth?.userId || !selected?.user?.id) return false;
    return Number(auth.userId) === Number(selected.user.id);
  }, [auth, selected]);

  async function loadListings(page = 0) {
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams();
      if (filters.brand) params.set('brand', filters.brand);
      if (filters.model) params.set('model', filters.model);
      if (filters.yearFrom) params.set('yearFrom', filters.yearFrom);
      if (filters.yearTo) params.set('yearTo', filters.yearTo);
      if (filters.engineFrom) params.set('engineFrom', filters.engineFrom);
      if (filters.engineTo) params.set('engineTo', filters.engineTo);
      if (filters.color) params.set('color', filters.color);
      if (filters.fuelType) params.set('fuelType', filters.fuelType);
      if (filters.priceFrom) params.set('priceFrom', filters.priceFrom);
      if (filters.priceTo) params.set('priceTo', filters.priceTo);
      params.set('sort', filters.sort);
      params.set('page', String(page));
      params.set('size', String(filters.size));

      const data = await apiRequest(`/api/listings?${params.toString()}`);
      setListings(data.content || []);
      setPageInfo({
        page: data.page,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
      });
      if (data.content && data.content.length > 0) {
        setSelected((prev) => prev || data.content[0]);
      }
    } catch (err) {
      setError(err.message || 'Elanlar yuklenmedi');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadListings(0);
  }, []);

  function saveAuth(nextAuth) {
    setAuth(nextAuth);
    if (nextAuth) {
      localStorage.setItem('auth', JSON.stringify(nextAuth));
    } else {
      localStorage.removeItem('auth');
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setStatus('Qeydiyyat gedir...');
    setError('');
    try {
      const data = await apiRequest('/api/auth/register', {
        method: 'POST',
        body: registerForm,
      });
      saveAuth(data);
      setRegisterForm(emptyRegisterForm);
      setStatus('Qeydiyyat ugurludur');
    } catch (err) {
      setError(err.message || 'Qeydiyyat alinmadi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    setStatus('Daxil olunur...');
    setError('');
    try {
      const data = await apiRequest('/api/auth/login', {
        method: 'POST',
        body: loginForm,
      });
      saveAuth(data);
      setLoginForm(emptyLoginForm);
      setStatus('Daxil oldunuz');
    } catch (err) {
      setError(err.message || 'Login alinmadi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  function handleLogout() {
    saveAuth(null);
    setStatus('Cixis edildi');
    setTimeout(() => setStatus(''), 3000);
  }

  async function handleCreateListing(event) {
    event.preventDefault();
    setStatus('Elan yaradilir...');
    setError('');
    try {
      const payload = toListingPayload(createForm);
      const formData = new FormData();
      formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      createImages.forEach((file) => formData.append('images', file));

      const created = await apiRequest('/api/listings', {
        method: 'POST',
        body: formData,
      }, token);

      setCreateForm(emptyListingForm);
      setCreateImages([]);
      setSelected(created);
      await loadListings(0);
      setStatus('Elan yaradildi');
    } catch (err) {
      setError(err.message || 'Elan yaradilmadi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  async function handleUpdateListing(event) {
    event.preventDefault();
    if (!selected) return;
    setStatus('Elan yenilenir...');
    setError('');
    try {
      const payload = toListingPayload(editForm);
      const updated = await apiRequest(`/api/listings/${selected.id}`, {
        method: 'PUT',
        body: payload,
      }, token);
      setSelected(updated);
      await loadListings(pageInfo.page);
      setStatus('Elan yenilendi');
    } catch (err) {
      setError(err.message || 'Yenileme alinmadi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  async function handleDeleteListing(listingId) {
    if (!listingId) return;
    setStatus('Elan silinir...');
    setError('');
    try {
      await apiRequest(`/api/listings/${listingId}`, {
        method: 'DELETE',
      }, token);
      setSelected(null);
      await loadListings(0);
      setStatus('Elan silindi');
    } catch (err) {
      setError(err.message || 'Silinmedi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  async function handleAddImages(event) {
    event.preventDefault();
    if (!selected || editImages.length === 0) return;
    setStatus('Sekiller yuklenir...');
    setError('');
    try {
      const formData = new FormData();
      editImages.forEach((file) => formData.append('images', file));
      const updated = await apiRequest(`/api/listings/${selected.id}/images`, {
        method: 'POST',
        body: formData,
      }, token);
      setSelected(updated);
      setEditImages([]);
      await loadListings(pageInfo.page);
      setStatus('Sekiller elave edildi');
    } catch (err) {
      setError(err.message || 'Sekiller elave edilmedi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  async function handleDeleteImage(imageId) {
    if (!imageId) return;
    setStatus('Sekil silinir...');
    setError('');
    try {
      await apiRequest(`/api/images/${imageId}`, { method: 'DELETE' }, token);
      const nextImages = (selected.images || []).filter((img) => img.id !== imageId);
      setSelected({ ...selected, images: nextImages });
      setStatus('Sekil silindi');
    } catch (err) {
      setError(err.message || 'Sekil silinmedi');
    } finally {
      setTimeout(() => setStatus(''), 3000);
    }
  }

  function handleSelect(listing) {
    setSelected(listing);
    setEditForm({
      brand: listing.brand,
      model: listing.model,
      year: listing.year,
      engineSize: listing.engineSize,
      color: listing.color,
      fuelType: listing.fuelType,
      price: listing.price,
    });
  }

  const heroStats = useMemo(() => {
    return [
      { label: 'Elanlar', value: pageInfo.totalElements },
      { label: 'Sifarisler', value: '24/7' },
      { label: 'Tehlukesiz', value: 'JWT' },
    ];
  }, [pageInfo.totalElements]);

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="logo-dot" />
          <div>
            <h1>MasinSatisi</h1>
            <p>Suretli al-sat platformasi</p>
          </div>
        </div>
        <div className="top-actions">
          {auth ? (
            <div className="user-chip">
              <div>
                <strong>{auth.fullName}</strong>
                <span>{auth.email}</span>
              </div>
              <button className="btn ghost" onClick={handleLogout}>Cixis</button>
            </div>
          ) : (
            <div className="auth-toggle">
              <button
                className={authMode === 'login' ? 'btn primary' : 'btn ghost'}
                onClick={() => setAuthMode('login')}
              >
                Daxil ol
              </button>
              <button
                className={authMode === 'register' ? 'btn primary' : 'btn ghost'}
                onClick={() => setAuthMode('register')}
              >
                Qeydiyyat
              </button>
            </div>
          )}
        </div>
      </header>

      <section className="hero">
        <div className="hero-text">
          <h2>Yeni elanlar, real qiymetler, etibarli saticilar.</h2>
          <p>Masinlari tap, filtrle, bir klikle elan ver. Backend hazirdir, frontend ise senin ucundur.</p>
          <div className="hero-actions">
            <button
              className="btn primary"
              onClick={() => {
                const target = auth ? 'create-form' : 'auth-panel';
                document.getElementById(target)?.scrollIntoView({ behavior: 'smooth' });
              }}
            >
              Yeni elan yarat
            </button>
            <button className="btn ghost" onClick={() => loadListings(0)}>
              Elanlari yenile
            </button>
          </div>
          {status && <div className="status ok">{status}</div>}
          {error && <div className="status error">{error}</div>}
        </div>
        <div className="hero-card">
          <h3>Platforma gostericileri</h3>
          <div className="hero-stats">
            {heroStats.map((stat) => (
              <div key={stat.label} className="stat">
                <span>{stat.label}</span>
                <strong>{stat.value}</strong>
              </div>
            ))}
          </div>
          <p className="small">API: {API_BASE}</p>
        </div>
      </section>

      <main className="layout">
        <aside className="panel">
          <h3>Filtrler</h3>
          <form
            onSubmit={(event) => {
              event.preventDefault();
              loadListings(0);
            }}
            className="form-grid"
          >
            <label>
              Marka
              <input value={filters.brand} onChange={(e) => setFilters({ ...filters, brand: e.target.value })} />
            </label>
            <label>
              Model
              <input value={filters.model} onChange={(e) => setFilters({ ...filters, model: e.target.value })} />
            </label>
            <div className="inline">
              <label>
                Il from
                <input type="number" value={filters.yearFrom} onChange={(e) => setFilters({ ...filters, yearFrom: e.target.value })} />
              </label>
              <label>
                Il to
                <input type="number" value={filters.yearTo} onChange={(e) => setFilters({ ...filters, yearTo: e.target.value })} />
              </label>
            </div>
            <div className="inline">
              <label>
                Mator from
                <input type="number" step="0.1" value={filters.engineFrom} onChange={(e) => setFilters({ ...filters, engineFrom: e.target.value })} />
              </label>
              <label>
                Mator to
                <input type="number" step="0.1" value={filters.engineTo} onChange={(e) => setFilters({ ...filters, engineTo: e.target.value })} />
              </label>
            </div>
            <label>
              Reng
              <input value={filters.color} onChange={(e) => setFilters({ ...filters, color: e.target.value })} />
            </label>
            <label>
              Yanacaq
              <select value={filters.fuelType} onChange={(e) => setFilters({ ...filters, fuelType: e.target.value })}>
                <option value="">Hamisi</option>
                {fuelTypes.map((fuel) => (
                  <option key={fuel} value={fuel}>{fuel}</option>
                ))}
              </select>
            </label>
            <div className="inline">
              <label>
                Qiymet from
                <input type="number" step="0.01" value={filters.priceFrom} onChange={(e) => setFilters({ ...filters, priceFrom: e.target.value })} />
              </label>
              <label>
                Qiymet to
                <input type="number" step="0.01" value={filters.priceTo} onChange={(e) => setFilters({ ...filters, priceTo: e.target.value })} />
              </label>
            </div>
            <label>
              Siralama
              <select value={filters.sort} onChange={(e) => setFilters({ ...filters, sort: e.target.value })}>
                {sortOptions.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            </label>
            <label>
              Sehife olcusu
              <input type="number" min="1" max="100" value={filters.size} onChange={(e) => setFilters({ ...filters, size: e.target.value })} />
            </label>
            <button className="btn primary" type="submit">Axtar</button>
          </form>

          <div className="divider" />

          {!auth && (
            <div id="auth-panel" className="panel-section">
              <h3>{authMode === 'login' ? 'Daxil ol' : 'Qeydiyyat'}</h3>
              {authMode === 'login' ? (
                <form onSubmit={handleLogin} className="form-grid">
                  <label>
                    Email
                    <input type="email" value={loginForm.email} onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })} required />
                  </label>
                  <label>
                    Sifre
                    <input type="password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} required />
                  </label>
                  <button className="btn primary" type="submit">Daxil ol</button>
                </form>
              ) : (
                <form onSubmit={handleRegister} className="form-grid">
                  <label>
                    Ad soyad
                    <input value={registerForm.fullName} onChange={(e) => setRegisterForm({ ...registerForm, fullName: e.target.value })} required />
                  </label>
                  <label>
                    Telefon
                    <input value={registerForm.phone} onChange={(e) => setRegisterForm({ ...registerForm, phone: e.target.value })} required />
                  </label>
                  <label>
                    Email
                    <input type="email" value={registerForm.email} onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })} required />
                  </label>
                  <label>
                    Sifre
                    <input type="password" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} required />
                  </label>
                  <button className="btn primary" type="submit">Qeydiyyat</button>
                </form>
              )}
              <div className="status">Elan yaratmaq ucun daxil olmalisan.</div>
            </div>
          )}

          {auth && (
            <div id="create-form" className="panel-section">
              <h3>Yeni elan</h3>
              <form onSubmit={handleCreateListing} className="form-grid">
                <label>
                  Marka
                  <input value={createForm.brand} onChange={(e) => setCreateForm({ ...createForm, brand: e.target.value })} required />
                </label>
                <label>
                  Model
                  <input value={createForm.model} onChange={(e) => setCreateForm({ ...createForm, model: e.target.value })} required />
                </label>
                <label>
                  Il
                  <input type="number" value={createForm.year} onChange={(e) => setCreateForm({ ...createForm, year: e.target.value })} required />
                </label>
                <label>
                  Motor (L)
                  <input type="number" step="0.1" value={createForm.engineSize} onChange={(e) => setCreateForm({ ...createForm, engineSize: e.target.value })} required />
                </label>
                <label>
                  Reng
                  <input value={createForm.color} onChange={(e) => setCreateForm({ ...createForm, color: e.target.value })} required />
                </label>
                <label>
                  Yanacaq
                  <select value={createForm.fuelType} onChange={(e) => setCreateForm({ ...createForm, fuelType: e.target.value })}>
                    {fuelTypes.map((fuel) => (
                      <option key={fuel} value={fuel}>{fuel}</option>
                    ))}
                  </select>
                </label>
                <label>
                  Qiymet
                  <input type="number" step="0.01" value={createForm.price} onChange={(e) => setCreateForm({ ...createForm, price: e.target.value })} required />
                </label>
                <label>
                  Sekiller
                  <input type="file" accept="image/*" multiple onChange={(e) => setCreateImages(Array.from(e.target.files || []))} required />
                </label>
                <button className="btn primary" type="submit">Elani gonder</button>
              </form>
            </div>
          )}
        </aside>

        <section className="content">
          <div className="content-header">
            <div>
              <h3>Elanlar</h3>
              <p className="small">Cemi: {pageInfo.totalElements}</p>
            </div>
            <div className="pager">
              <button className="btn ghost" disabled={pageInfo.page === 0 || loading} onClick={() => loadListings(pageInfo.page - 1)}>Onceki</button>
              <span>{pageInfo.page + 1} / {pageInfo.totalPages || 1}</span>
              <button className="btn ghost" disabled={pageInfo.page + 1 >= pageInfo.totalPages || loading} onClick={() => loadListings(pageInfo.page + 1)}>Sonraki</button>
            </div>
          </div>
          {loading && <div className="status">Yuklenir...</div>}
          {!loading && listings.length === 0 && <div className="empty">Hec bir elan tapilmadi.</div>}
          <div className="grid">
            {listings.map((listing, index) => (
              <button
                key={listing.id}
                className={`card ${selected?.id === listing.id ? 'active' : ''}`}
                style={{ '--i': index }}
                onClick={() => handleSelect(listing)}
              >
                <div className="card-media">
                  {listing.images && listing.images.length > 0 ? (
                    <img src={`${API_BASE}${listing.images[0].url}`} alt={`${listing.brand} ${listing.model}`} />
                  ) : (
                    <div className="placeholder">Sekil yoxdur</div>
                  )}
                </div>
                <div className="card-body">
                  <div className="card-title">
                    <h4>{listing.brand} {listing.model}</h4>
                    <span className="badge">{listing.fuelType}</span>
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

        <aside className="panel detail">
          <h3>Detallar</h3>
          {!selected && <div className="empty">Elan secin.</div>}
          {selected && (
            <div className="detail-body">
              <div className="detail-gallery">
                {(selected.images || []).length === 0 && <div className="placeholder">Sekil yoxdur</div>}
                {(selected.images || []).map((img) => (
                  <div key={img.id} className="thumb">
                    <img src={`${API_BASE}${img.url}`} alt="" />
                    {isOwner && (
                      <button className="chip" onClick={() => handleDeleteImage(img.id)}>Sil</button>
                    )}
                  </div>
                ))}
              </div>
              <div className="detail-info">
                <h4>{selected.brand} {selected.model}</h4>
                <p className="small">{formatDate(selected.createdAt)}</p>
                <div className="detail-grid">
                  <span>Il</span><strong>{selected.year}</strong>
                  <span>Motor</span><strong>{selected.engineSize} L</strong>
                  <span>Reng</span><strong>{selected.color}</strong>
                  <span>Yanacaq</span><strong>{selected.fuelType}</strong>
                  <span>Qiymet</span><strong>{formatMoney(selected.price)}</strong>
                </div>
                <div className="seller">
                  <div>
                    <strong>{selected.user?.fullName}</strong>
                    <span>{selected.user?.phone}</span>
                  </div>
                </div>
                {isOwner && (
                  <div className="owner-actions">
                    <button className="btn danger" onClick={() => handleDeleteListing(selected.id)}>Elani sil</button>
                  </div>
                )}
              </div>

              {isOwner && editForm && (
                <div className="panel-section">
                  <h4>Elani yenile</h4>
                  <form onSubmit={handleUpdateListing} className="form-grid">
                    <label>
                      Marka
                      <input value={editForm.brand} onChange={(e) => setEditForm({ ...editForm, brand: e.target.value })} required />
                    </label>
                    <label>
                      Model
                      <input value={editForm.model} onChange={(e) => setEditForm({ ...editForm, model: e.target.value })} required />
                    </label>
                    <label>
                      Il
                      <input type="number" value={editForm.year} onChange={(e) => setEditForm({ ...editForm, year: e.target.value })} required />
                    </label>
                    <label>
                      Motor
                      <input type="number" step="0.1" value={editForm.engineSize} onChange={(e) => setEditForm({ ...editForm, engineSize: e.target.value })} required />
                    </label>
                    <label>
                      Reng
                      <input value={editForm.color} onChange={(e) => setEditForm({ ...editForm, color: e.target.value })} required />
                    </label>
                    <label>
                      Yanacaq
                      <select value={editForm.fuelType} onChange={(e) => setEditForm({ ...editForm, fuelType: e.target.value })}>
                        {fuelTypes.map((fuel) => (
                          <option key={fuel} value={fuel}>{fuel}</option>
                        ))}
                      </select>
                    </label>
                    <label>
                      Qiymet
                      <input type="number" step="0.01" value={editForm.price} onChange={(e) => setEditForm({ ...editForm, price: e.target.value })} required />
                    </label>
                    <button className="btn primary" type="submit">Yenile</button>
                  </form>
                </div>
              )}

              {isOwner && (
                <div className="panel-section">
                  <h4>Sekil elave et</h4>
                  <form onSubmit={handleAddImages} className="form-grid">
                    <label>
                      Sekiller
                      <input type="file" accept="image/*" multiple onChange={(e) => setEditImages(Array.from(e.target.files || []))} />
                    </label>
                    <button className="btn ghost" type="submit">Elave et</button>
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
