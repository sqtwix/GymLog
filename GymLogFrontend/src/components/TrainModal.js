import React, { useState, useEffect } from 'react';
import { X, Clock, Dumbbell, FileText, Calendar } from 'lucide-react';
import { TRAIN_TYPES, GYM_DESCRIPTIONS, trainAPI, validateTrainData } from '../services/trainService';
import './TrainModal.css';

const TrainModal = ({ isOpen, onClose, selectedDate, onTrainAdded }) => {
  const [formData, setFormData] = useState({
    type: '',
    description: '',
    duration: '',
    date: selectedDate ? new Date(selectedDate).toISOString().split('T')[0] : ''
  });
  const [selectedGymDescriptions, setSelectedGymDescriptions] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showGymDescriptions, setShowGymDescriptions] = useState(false);

  useEffect(() => {
    if (formData.type === 'Тренажерный зал') {
      setShowGymDescriptions(true);
    } else {
      setShowGymDescriptions(false);
      setFormData(prev => ({ ...prev, description: '' }));
    }
  }, [formData.type]);

  useEffect(() => {
    if (formData.type === 'Тренажерный зал') {
      setFormData(prev => ({ ...prev, description: selectedGymDescriptions.join(', ') }));
    }
  }, [selectedGymDescriptions]);

  const handleGymDescriptionSelect = (desc) => {
    setSelectedGymDescriptions(prev =>
      prev.includes(desc) ? prev.filter(d => d !== desc) : [...prev, desc]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    const trainDate = new Date(formData.date);
    if (isNaN(trainDate)) {
      setErrors({ date: 'Неверная дата' });
      setLoading(false);
      return;
    }

    const trainData = {
      type: formData.type,
      description: formData.type === 'Тренажерный зал' ? selectedGymDescriptions.join(', ') : formData.description,
      duration: parseInt(formData.duration),
      date: trainDate.toISOString()
      // userId УДАЛЁН — бэкенд берёт из JWT!
    };

    if (formData.type === 'Тренажерный зал' && selectedGymDescriptions.length === 0) {
      setErrors({ description: 'Выберите хотя бы одну группу мышц' });
      setLoading(false);
      return;
    }

    const validation = validateTrainData(trainData);
    if (!validation.isValid) {
      setErrors(validation.errors);
      setLoading(false);
      return;
    }

    const result = await trainAPI.addTrain(trainData);

    if (result.success) {
      onTrainAdded?.();
      onClose();
    } else {
      setErrors({ general: result.error || 'Ошибка добавления тренировки' });
    }

    setLoading(false);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Добавить тренировку</h2>
          <button className="close-btn" onClick={onClose}><X size={24} /></button>
        </div>

        <form onSubmit={handleSubmit} className="train-form">
          {errors.general && <div className="form-error general-error">{errors.general}</div>}

          {/* Тип тренировки */}
          <div className="form-group">
            <label><Dumbbell size={16} /> Тип тренировки</label>
            <select name="type" value={formData.type} onChange={e => setFormData(prev => ({ ...prev, type: e.target.value }))} required>
              <option value="">Выберите тип</option>
              {TRAIN_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>

          {/* Описание */}
          <div className="form-group">
            <label><FileText size={16} /> Описание</label>
            {showGymDescriptions ? (
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
                {errors.description && <div className="form-error">{errors.description}</div>}
              </div>
            ) : (
              <input
                type="text"
                value={formData.description}
                onChange={e => setFormData(prev => ({ ...prev, description: e.target.value }))}
                placeholder="Опишите тренировку"
                required
              />
            )}
          </div>

          {/* Длительность */}
          <div className="form-group">
            <label><Clock size={16} /> Длительность (мин)</label>
            <input
              type="number"
              value={formData.duration}
              onChange={e => setFormData(prev => ({ ...prev, duration: e.target.value }))}
              min="1"
              max="300"
              required
            />
          </div>

          {/* Дата */}
          <div className="form-group">
            <label><Calendar size={16} /> Дата</label>
            <input
              type="date"
              value={formData.date}
              onChange={e => setFormData(prev => ({ ...prev, date: e.target.value }))}
              min={new Date().toISOString().split('T')[0]}
              required
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Отмена
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Добавление...' : 'Добавить'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TrainModal;