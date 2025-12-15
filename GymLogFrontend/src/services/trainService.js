// src/services/trainService.js
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

// ЕДИНСТВЕННЫЙ ИСТОЧНИК ПРАВДЫ — только этот интерцептор
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const trainAPI = {
  getTrains: async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('getTrains: нет токена в localStorage');
      return { success: false, error: 'Не авторизован' };
    }

    try {
      const res = await api.get('/trains');
      return { success: true, data: res.data || [] };
    } catch (err) {
      console.error('getTrains error', {
        status: err.response?.status,
        data: err.response?.data,
        message: err.message
      });
      // Явно различаем неавторизованность
      if (err.response?.status === 401) {
        return { success: false, error: 'Не авторизован' };
      }
      return { success: false, error: err.response?.data || err.message || 'Ошибка' };
    }
  },

  addTrain: async (trainData) => {
    const token = localStorage.getItem('token');
    if (!token) {
      return { success: false, error: 'Не авторизован' };
    }

    try {
      const res = await api.post('/trains/train', {
        type: trainData.type,
        description: trainData.description || '',
        duration: Number(trainData.duration),
        date: new Date(trainData.date).toISOString()
      });
      return { success: true, data: res.data };
    } catch (err) {
      console.error('addTrain error', { status: err.response?.status, data: err.response?.data, message: err.message });
      return { success: false, error: err.response?.data?.message || err.response?.data || 'Ошибка' };
    }
  },

  deleteTrain: async (userId, date) => {
    const token = localStorage.getItem('token');
    if (!token) return { success: false, error: 'Не авторизован' };
    try {
      const d = typeof date === 'string' ? date.split('T')[0] : date;
      const res = await api.delete(`/trains/delete/${d}`);
      return { success: true, data: res.data };
    } catch (err) {
      console.error('deleteTrain error', { status: err.response?.status, data: err.response?.data, message: err.message });
      return { success: false, error: err.response?.data || err.message };
    }
  }
};

export const TRAIN_TYPES = ['Тренажерный зал', 'Бег', 'Плавание', 'Йога', 'Фитнес', 'Боевое искусство'];
export const GYM_DESCRIPTIONS = ['Грудь', 'Спина', 'Ноги', 'Плечи', 'Руки', 'Пресс', 'Кардио'];
export const validateTrainData = (d) => {
  const e = {};
  if (!d.type) e.type = 'Выберите тип';
  if (!d.description?.trim()) e.description = 'Укажите описание';
  if (!d.duration || d.duration <= 0) e.duration = 'Укажите время';
  if (!d.date) e.date = 'Выберите дату';
  return { isValid: Object.keys(e).length === 0, errors: e };
};