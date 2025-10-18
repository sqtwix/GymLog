import axios from 'axios';

// Константы для типов тренировок
export const TRAIN_TYPES = [
  'Тренажерный зал',
  'Бег',
  'Плавание',
  'Йога',
  'Фитнес',
  'Боевое искусство'
];

// Константы для описания тренировок в тренажерном зале
export const GYM_DESCRIPTIONS = [
  'Грудь',
  'Спина',
  'Ноги',
  'Плечи',
  'Руки',
  'Пресс',
  'Кардио'
];

// API для работы с тренировками
export const trainAPI = {
  // Добавить новую тренировку
  async addTrain(trainData) {
    try {
      const response = await axios.post('/api/user/train', trainData);
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Error adding train:', error);
      return { 
        success: false, 
        error: error.response?.data || 'Ошибка при добавлении тренировки' 
      };
    }
  },

  // Получить все тренировки пользователя
  async getTrains(userId) {
    try {
      const response = await axios.get(`/api/user/trains/${userId}`);
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Error getting trains:', error);
      return { 
        success: false, 
        error: error.response?.data || 'Ошибка при получении тренировок' 
      };
    }
  }
};

// Валидация данных тренировки
export const validateTrainData = (trainData) => {
  const errors = {};

  if (!trainData.type || trainData.type.trim() === '') {
    errors.type = 'Выберите тип тренировки';
  }

  if (!trainData.description || trainData.description.trim() === '') {
    errors.description = 'Заполните описание тренировки';
  }

  if (!trainData.duration || trainData.duration <= 0) {
    errors.duration = 'Введите корректное время тренировки (больше 0 минут)';
  }

  if (trainData.duration > 300) {
    errors.duration = 'Время тренировки не может превышать 300 минут';
  }

  if (!trainData.date) {
    errors.date = 'Выберите дату тренировки';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};

