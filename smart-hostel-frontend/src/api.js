const configuredApiUrl = (import.meta.env.VITE_API_BASE_URL || '').trim().replace(/\/$/, '');
const API_ROOT = configuredApiUrl
  ? (configuredApiUrl.endsWith('/api') ? configuredApiUrl : `${configuredApiUrl}/api`)
  : '/api';

export async function api(path, options = {}, user = null) {
  const headers = { ...(options.headers || {}) };
  if (options.body !== undefined) headers['Content-Type'] = 'application/json';
  if (user?.userId != null) headers['X-User-Id'] = String(user.userId);

  let response;
  try {
    response = await fetch(`${API_ROOT}${path}`, {
      ...options,
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new Error('Cannot reach the backend. Check that the backend service is running and the frontend API URL is configured correctly.');
  }

  const text = await response.text();
  let payload = null;
  if (text) {
    try { payload = JSON.parse(text); } catch { payload = text; }
  }
  if (!response.ok) {
    const message = typeof payload === 'string'
      ? payload
      : payload?.message || payload?.error || `Request failed (${response.status})`;
    throw new Error(message);
  }
  return payload;
}

export const get = (path, user) => api(path, {}, user);
export const post = (path, body, user) => api(path, { method: 'POST', body }, user);
export const put = (path, body, user) => api(path, { method: 'PUT', body }, user);
