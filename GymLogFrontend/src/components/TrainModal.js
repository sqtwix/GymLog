import React, { useState, useEffect, useMemo } from 'react';
import { X, Clock, Dumbbell, FileText, Calendar } from 'lucide-react';
import { TRAIN_TYPES, GYM_DESCRIPTIONS, trainAPI, validateTrainData } from '../services/trainService';
import './TrainModal.css';

const formatInputDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getTodayStart = () => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return today;
};

const getInitialDateString = (selectedDate) => {
  const todayStart = getTodayStart();
  const baseDate = selectedDate ? new Date(selectedDate) : new Date();
  if (baseDate < todayStart) {
    return formatInputDate(todayStart);
  }
  return formatInputDate(baseDate);
};

const TrainModal = ({ isOpen, onClose, selectedDate, onTrainAdded }) => {
  const todayStart = useMemo(() => getTodayStart(), []);
  const todayInputValue = formatInputDate(todayStart);

  const [formData, setFormData] = useState({
    type: '',
    description: '',
    duration: '',
    date: getInitialDateString(selectedDate)
  });
  const [selectedGymDescriptions, setSelectedGymDescriptions] = useState([]);
  
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showGymDescriptions, setShowGymDescriptions] = useState(false);

  // Обновляем дату при изменении selectedDate
  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      date: getInitialDateString(selectedDate)
    }));
  }, [selectedDate]);

  // Обновляем форму при изменении типа тренировки
  useEffect(() => {
    if (formData.type === 'Тренажерный зал') {
      setShowGymDescriptions(true);
      setFormData(prev => ({ ...prev, description: '' }));
      setSelectedGymDescriptions([]);
    } else {
      setShowGymDescriptions(false);
      setFormData(prev => ({ ...prev, description: '' }));
      setSelectedGymDescriptions([]);
    }
  }, [formData.type]);

  // Обновляем описание при изменении выбранных групп мышц
  useEffect(() => {
    if (formData.type === 'Тренажерный зал') {
      setFormData(prev => ({ 
        ...prev, 
        description: selectedGymDescriptions.join(', ') 
      }));
    }
  }, [selectedGymDescriptions, formData.type]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Очищаем ошибку при изменении поля
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleGymDescriptionSelect = (description) => {
    setSelectedGymDescriptions(prev => {
      if (prev.includes(description)) {
        // Убираем из выбранных
        return prev.filter(item => item !== description);
      } else {
        // Добавляем к выбранным
        return [...prev, description];
      }
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Получаем userId из localStorage
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
      setErrors({ general: 'Пользователь не авторизован' });
      return;
    }

    const trainDateObj = new Date(formData.date);
    trainDateObj.setHours(0, 0, 0, 0);

    if (trainDateObj < todayStart) {
      setErrors({ date: 'Нельзя планировать тренировки в прошлом' });
      return;
    }

    const trainData = {
      ...formData,
      duration: parseInt(formData.duration),
      date: trainDateObj,
      userId: user.id
    };

    // Дополнительная валидация для тренажерного зала
    if (formData.type === 'Тренажерный зал' && selectedGymDescriptions.length === 0) {
      setErrors({ description: 'Выберите хотя бы одну группу мышц' });
      return;
    }

    // Валидация
    const validation = validateTrainData(trainData);
    if (!validation.isValid) {
      setErrors(validation.errors);
      return;
    }

    setLoading(true);
    setErrors({});

    const result = await trainAPI.addTrain(trainData);
    
    if (result.success) {
      // Очищаем форму
      setFormData({
        type: '',
        description: '',
        duration: '',
        date: selectedDate ? selectedDate.toISOString().split('T')[0] : ''
      });
      setSelectedGymDescriptions([]);
      
      // Уведомляем родительский компонент
      if (onTrainAdded) {
        await onTrainAdded();
      }
      
      // Закрываем модальное окно
      onClose();
    } else {
      setErrors({ general: result.error });
    }
    
    setLoading(false);
  };

  const handleClose = () => {
    setFormData({
      type: '',
      description: '',
      duration: '',
      date: getInitialDateString(selectedDate)
    });
    setSelectedGymDescriptions([]);
    setErrors({});
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Добавить тренировку</h2>
          <button className="close-btn" onClick={handleClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="train-form">
          {errors.general && (
            <div className="form-error general-error">
              {errors.general}
            </div>
          )}

          <div className="form-group">
            <label className="form-label">
              <Dumbbell size={16} />
              Тип тренировки
            </label>
            <select
              name="type"
              value={formData.type}
              onChange={handleChange}
              className={`form-input ${errors.type ? 'error' : ''}`}
              required
            >
              <option value="">Выберите тип тренировки</option>
              {TRAIN_TYPES.map(type => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
            {errors.type && <div className="form-error">{errors.type}</div>}
          </div>

          <div className="form-group">
            <label className="form-label">
              <FileText size={16} />
              Описание тренировки
            </label>
            
            {showGymDescriptions ? (
              <div className="gym-descriptions">
                <p className="description-hint">Выберите группы мышц (можно несколько):</p>
                <div className="gym-options">
                  {GYM_DESCRIPTIONS.map(desc => (
                    <button
                      key={desc}
                      type="button"
                      className={`gym-option ${selectedGymDescriptions.includes(desc) ? 'selected' : ''}`}
                      onClick={() => handleGymDescriptionSelect(desc)}
                    >
                      {desc}
                    </button>
                  ))}
                </div>
                {errors.description && <div className="form-error">{errors.description}</div>}
              </div>
            ) : (
              <input
                type="text"
                name="description"
                value={formData.description}
                onChange={handleChange}
                className={`form-input ${errors.description ? 'error' : ''}`}
                placeholder="Опишите тренировку"
                required
              />
            )}
            {!showGymDescriptions && errors.description && (
              <div className="form-error">{errors.description}</div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">
              <Clock size={16} />
              Время тренировки (минуты)
            </label>
            <input
              type="number"
              name="duration"
              value={formData.duration}
              onChange={handleChange}
              className={`form-input ${errors.duration ? 'error' : ''}`}
              placeholder="Введите время в минутах"
              min="1"
              max="300"
              required
            />
            {errors.duration && <div className="form-error">{errors.duration}</div>}
          </div>

          <div className="form-group">
            <label className="form-label">
              <Calendar size={16} />
              Дата тренировки
            </label>
            <input
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              min={todayInputValue}
              className={`form-input ${errors.date ? 'error' : ''}`}
              required
            />
            {errors.date && <div className="form-error">{errors.date}</div>}
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleClose}
              disabled={loading}
            >
              Отмена
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Добавление...' : 'Добавить тренировку'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TrainModal;

