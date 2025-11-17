import React, { useMemo } from 'react';
import { Clock, FileText, Calendar as CalendarIcon, Trash2 } from 'lucide-react';
import { isSameDay } from 'date-fns';
import './TrainDetails.css';

const TrainDetails = ({ selectedDate, trains = [], onDeleteDay, deleteState = {} }) => {
  const safeSelectedDate = selectedDate ? new Date(selectedDate) : null;

  const dayTrains = useMemo(() => {
    if (!safeSelectedDate) return [];
    return trains.filter(train => {
      if (!train?.date) return false;
      const trainDate = new Date(train.date);
      if (Number.isNaN(trainDate.getTime())) return false;
      return isSameDay(trainDate, safeSelectedDate);
    });
  }, [safeSelectedDate, trains]);

  const formatTime = (minutes) => {
    if (minutes < 60) {
      return `${minutes} мин`;
    }
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}ч ${mins}мин` : `${hours}ч`;
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'Тренажерный зал':
        return '🏋️';
      case 'Бег':
        return '🏃';
      case 'Плавание':
        return '🏊';
      case 'Йога':
        return '🧘';
      case 'Фитнес':
        return '💪';
      case 'Боевое искусство':
        return '🥋';
      default:
        return '🏃';
    }
  };

  if (!safeSelectedDate) {
    return (
      <div className="train-details">
        <div className="no-trains">
          <CalendarIcon size={48} className="no-trains-icon" />
          <h3>Выберите день</h3>
          <p>Нажмите на дату в календаре, чтобы увидеть план тренировки</p>
        </div>
      </div>
    );
  }

  if (dayTrains.length === 0) {
    return (
      <div className="train-details">
        <div className="no-trains">
          <CalendarIcon size={48} className="no-trains-icon" />
          <h3>Нет тренировок</h3>
          <p>На выбранную дату тренировок не запланировано</p>
        </div>
      </div>
    );
  }

  const { loading: deleteLoading, error: deleteError, success: deleteSuccess } = deleteState;
  const totalDuration = dayTrains.reduce((sum, train) => sum + (train.duration || 0), 0);

  return (
    <div className="train-details">
      <div className="trains-header">
        <div>
          <h3>Тренировки на {safeSelectedDate.toLocaleDateString('ru-RU', {
            weekday: 'long',
            day: 'numeric',
            month: 'long'
          })}</h3>
          <span className="trains-count">{dayTrains.length} тренировок</span>
        </div>
        {onDeleteDay && (
          <div className="trains-actions">
            {deleteSuccess && <span className="train-status success">{deleteSuccess}</span>}
            {deleteError && <span className="train-status error">{deleteError}</span>}
            <button
              className="btn btn-danger delete-day-btn"
              onClick={onDeleteDay}
              disabled={deleteLoading}
            >
              <Trash2 size={16} />
              {deleteLoading ? 'Удаление...' : 'Удалить день'}
            </button>
          </div>
        )}
      </div>

      <div className="trains-summary">
        <div className="summary-item">
          <span className="summary-label">Всего тренировок</span>
          <strong>{dayTrains.length}</strong>
        </div>
        <div className="summary-item">
          <span className="summary-label">Общая длительность</span>
          <strong>{formatTime(totalDuration)}</strong>
        </div>
      </div>

      <div className="trains-list">
        {dayTrains.map((train, index) => (
          <div key={`${train.id || index}-${train.date}`} className="train-card">
            <div className="train-header">
              <div className="train-type">
                <span className="train-icon">{getTypeIcon(train.type)}</span>
                <span className="train-type-name">{train.type}</span>
              </div>
              <div className="train-duration">
                <Clock size={16} />
                <span>{formatTime(train.duration)}</span>
              </div>
            </div>

            <div className="train-description">
              <FileText size={16} />
              <span>{train.description}</span>
            </div>

            <div className="train-time">
              <CalendarIcon size={16} />
              <span>
                {new Date(train.date).toLocaleTimeString('ru-RU', {
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default TrainDetails;
