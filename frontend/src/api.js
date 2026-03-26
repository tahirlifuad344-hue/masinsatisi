const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081';

async function parseError(res) {
  const text = await res.text();
  if (!text) return 'Request failed';
  try {
    const data = JSON.parse(text);
    if (data && data.message) return data.message;
    return text;
  } catch {
    return text;
  }
}

export async function apiRequest(path, { method = 'GET', body, headers } = {}, token) {
  const config = {
    method,
    headers: {
      ...(headers || {}),
    },
  };

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (body instanceof FormData) {
    config.body = body;
  } else if (body !== undefined) {
    config.headers['Content-Type'] = 'application/json';
    config.body = JSON.stringify(body);
  }

  const res = await fetch(`${API_BASE}${path}`, config);
  if (!res.ok) {
    const message = await parseError(res);
    throw new Error(message);
  }

  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return res.json();
  }
  return null;
}

export { API_BASE };
