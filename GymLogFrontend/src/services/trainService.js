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

// Обработка 401 - истекший токен
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Токен истек - очищаем localStorage и перенаправляем на логин
      console.warn('Токен истек, выполняю logout');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      delete axios.defaults.headers.common['Authorization'];
      // Перенаправляем на страницу логина
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Нормализация данных тренировки
const normalizeTrain = (train) => {
  if (!train) return null;
  
  // Получаем дату из ответа (может быть строка или Date)
  const rawDate = train.date ?? train.Date ?? null;
  let normalizedDate = null;
  let dateKey = null;
  
  if (rawDate) {
    // Парсим дату - бэкенд возвращает в UTC
    const dateObj = new Date(rawDate);
    if (!Number.isNaN(dateObj.getTime())) {
      // Нормализуем дату - берем только год, месяц, день (игнорируем время и часовой пояс)
      // Это важно, так как бэкенд возвращает UTC, а нам нужно сравнение по дате без времени
      normalizedDate = dateObj;
      dateKey = formatDateKey(dateObj);
    }
  }
  
  return {
    id: train.id ?? train.Id ?? null,
    type: train.type ?? train.Type ?? '',
    description: train.description ?? train.Description ?? '',
    duration: train.duration ?? train.Duration ?? 0,
    date: normalizedDate,
    dateKey: dateKey
  };
};

const formatDateKey = (date) => {
  if (!date) return null;
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return null;
  // Важно: используем UTC методы для консистентности с бэкендом
  // Но для сравнения дат нам нужна локальная дата (год, месяц, день)
  // Поэтому используем getFullYear, getMonth, getDate (локальные методы)
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const trainAPI = {
  getTrains: async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('getTrains: нет токена в localStorage');
      return { success: false, error: 'Не авторизован' };
    }

    try {
      // Бэкенд берет userId из JWT токена, не нужно передавать в URL
      const res = await api.get('/trains');
      const trains = Array.isArray(res.data) 
        ? res.data.map(normalizeTrain).filter(t => t && t.date && t.dateKey)
        : [];
      
      console.log('getTrains - нормализованные тренировки:', trains.map(t => ({
        id: t.id,
        type: t.type,
        date: t.date,
        dateKey: t.dateKey
      })));
      
      return { success: true, data: trains };
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

  deleteTrain: async (date) => {
    const token = localStorage.getItem('token');
    if (!token) return { success: false, error: 'Не авторизован' };
    try {
      // Форматируем дату для бэкенда
      let dateStr;
      if (typeof date === 'string') {
        dateStr = date.split('T')[0];
      } else if (date instanceof Date) {
        dateStr = date.toISOString().split('T')[0];
      } else {
        dateStr = new Date(date).toISOString().split('T')[0];
      }
      const res = await api.delete(`/trains/delete/${dateStr}`);
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
