import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (e) => {
    if (e.response?.status === 401 && !window.location.pathname.startsWith('/login')) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(e);
  }
);

export const errMsg = (e) => {
  if (e.response?.status === 403 && !e.response?.data?.message) return 'Your role is not permitted to do this.';
  const d = e.response?.data;
  if (d?.fieldErrors) return Object.entries(d.fieldErrors).map(([k, v]) => `${k}: ${v}`).join('; ');
  return d?.message || e.message;
};

export const fmt = (d) => (d ? new Date(d).toLocaleString() : '-');

export default api;
