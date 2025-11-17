import axios from 'axios';

const TRAINS_ENDPOINT = '/api/trains';

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

const formatDateKey = (dateObj) => {
  if (!dateObj) return null;
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const day = String(dateObj.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const normalizeTrain = (train) => {
  if (!train) {
    return null;
  }

  const type = train.type ?? train.Type ?? '';
  const description = train.description ?? train.Description ?? '';
  const durationRaw = train.duration ?? train.Duration ?? 0;
  const duration = Number.isNaN(Number(durationRaw)) ? 0 : Number(durationRaw);
  const rawDate = train.date ?? train.Date ?? null;
  const parsedDate = rawDate ? new Date(rawDate) : null;

  const dateKey = parsedDate ? formatDateKey(parsedDate) : null;

  return {
    id: train.id ?? train.Id ?? null,
    userId: train.userId ?? train.UserId ?? null,
    type,
    description,
    duration,
    date: parsedDate ? parsedDate.toISOString() : null,
    dateKey
  };
};

const formatDateForApi = (date) => {
  if (!date) return null;
  if (typeof date === 'string') {
    return new Date(date).toISOString();
  }
  return date.toISOString();
};

// API для работы с тренировками
export const trainAPI = {
  // Добавить новую тренировку
  async addTrain(trainData) {
    try {
      const payload = {
        type: trainData.type,
        description: trainData.description,
        duration: Number(trainData.duration),
        date: formatDateForApi(trainData.date),
        userId: trainData.userId
      };

      const response = await axios.post(`${TRAINS_ENDPOINT}/train`, payload);
      return { success: true, data: normalizeTrain(response.data) };
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
      const response = await axios.get(`${TRAINS_ENDPOINT}/trains/${userId}`);
      const trains = Array.isArray(response.data)
        ? response.data
            .map(normalizeTrain)
            .filter(train => Boolean(train?.date))
            .sort((a, b) => new Date(a.date) - new Date(b.date))
        : [];
      return { success: true, data: trains };
    } catch (error) {
      console.error('Error getting trains:', error);
      return { 
        success: false, 
        error: error.response?.data || 'Ошибка при получении тренировок' 
      };
    }
  },

  // Удалить тренировки по дате
  async deleteTrain(userId, date) {
    try {
      const formattedDate = typeof date === 'string'
        ? date.split('T')[0]
        : formatDateForApi(date)?.split('T')[0];

      if (!formattedDate) {
        throw new Error('Не удалось определить дату для удаления');
      }

      const response = await axios.delete(
        `${TRAINS_ENDPOINT}/delete/${userId}/${encodeURIComponent(formattedDate)}`
      );
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Error deleting train:', error);
      return { 
        success: false, 
        error: error.response?.data || 'Ошибка при удалении тренировки' 
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

