import React, { useEffect, useState } from 'react';
import { Clock, FileText, Calendar as CalendarIcon, Trash2 } from 'lucide-react';
import './TrainDetails.css';

const formatDateKey = (date) => {
  if (!date) return null;
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getTrainDateKey = (train) => {
  if (!train?.date) return null;
  return formatDateKey(train.date);
};

const TrainDetails = ({ selectedDate, trains = [], onDeleteDay, deleteState = {} }) => {
  const [dayTrains, setDayTrains] = useState([]);

  const safeSelectedDate = selectedDate ? new Date(selectedDate) : null;
  const selectedKey = safeSelectedDate ? formatDateKey(safeSelectedDate) : null;

  useEffect(() => {
    if (!selectedKey || !trains.length) {
      setDayTrains([]);
      return;
    }
    const filtered = trains.filter(train => getTrainDateKey(train) === selectedKey);
    setDayTrains(filtered);
  }, [selectedKey, trains]);

  const formatTime = (minutes) => {
    if (!minutes) return '0м';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return h > 0 ? `${h}ч ${m}м` : `${m}м`;
  };

  if (!safeSelectedDate) {
    return (
      <div className="train-details">
        <div className="no-trains">
          <CalendarIcon size={48} className="no-trains-icon" />
          <h3>Выберите день</h3>
          <p>Нажмите на дату в календаре</p>
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
          <p>На этот день ничего не запланировано</p>
        </div>
      </div>
    );
  }

  const { loading: deleteLoading = false, error: deleteError, success: deleteSuccess } = deleteState;
  const totalDuration = dayTrains.reduce((sum, t) => sum + (t.duration || 0), 0);

  return (
    <div className="train-details">
      <div className="trains-header">
        <div>
          <h3>
            Тренировки на {safeSelectedDate.toLocaleDateString('ru-RU', {
              weekday: 'long',
              day: 'numeric',
              month: 'long'
            })}
          </h3>
          <span className="trains-count">{dayTrains.length} шт.</span>
        </div>
        {onDeleteDay && (
          <div className="trains-actions">
            {deleteSuccess && <span className="train-status success">{deleteSuccess}</span>}
            {deleteError && <span className="train-status error">{deleteError}</span>}
            <button className="btn btn-danger delete-day-btn" onClick={onDeleteDay} disabled={deleteLoading}>
              <Trash2 size={16} />
              {deleteLoading ? 'Удаление...' : 'Удалить день'}
            </button>
          </div>
        )}
      </div>

      <div className="trains-summary">
        <div className="summary-item">
          <span className="summary-label">Всего</span>
          <strong>{dayTrains.length}</strong>
        </div>
        <div className="summary-item">
          <span className="summary-label">Длительность</span>
          <strong>{formatTime(totalDuration)}</strong>
        </div>
      </div>

      <div className="trains-list">
        {dayTrains.map((train, i) => (
          <div key={train.id || i} className="train-card">
            <div className="train-type">
              <span className="train-type-name">{train.type || 'Тренировка'}</span>
            </div>
            <div className="train-duration">
              <Clock size={16} />
              <span>{formatTime(train.duration)}</span>
            </div>
            {train.description && (
              <div className="train-description">
                <FileText size={16} />
                <span>{train.description}</span>
              </div>
            )}
            <div className="train-time">
              <CalendarIcon size={16} />
              <span>{new Date(train.date).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default TrainDetails;