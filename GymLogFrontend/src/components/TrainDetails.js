import React, { useEffect, useState, useMemo } from 'react';
import { Clock, FileText, Calendar as CalendarIcon, Trash2 } from 'lucide-react';
import './TrainDetails.css';

// Получает dateKey из тренировки (используется для сравнения)
// Тренировки уже нормализованы в trainService с локальным dateKey
const getTrainDateKey = (train) => {
  if (!train) return null;
  // Используем dateKey если есть (уже нормализован с локальными компонентами)
  if (train.dateKey) return train.dateKey;
  if (!train.date) return null;
  // Если dateKey нет, вычисляем из date используя локальные методы
  const parsed = new Date(train.date);
  if (Number.isNaN(parsed.getTime())) return null;
  const year = parsed.getFullYear();
  const month = String(parsed.getMonth() + 1).padStart(2, '0');
  const day = String(parsed.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const TrainDetails = ({ selectedDate, trains = [], onDeleteDay, deleteState = {} }) => {
  const [dayTrains, setDayTrains] = useState([]);

  // Создаем безопасную дату из selectedDate используя useMemo для оптимизации
  const safeSelectedDate = useMemo(() => {
    if (!selectedDate) return null;
    
    // Если это Date объект, нормализуем его (убираем время)
    if (selectedDate instanceof Date) {
      const normalized = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate());
      return Number.isNaN(normalized.getTime()) ? null : normalized;
    }
    
    // Если это строка или другой тип, пытаемся преобразовать
    const parsed = new Date(selectedDate);
    if (Number.isNaN(parsed.getTime())) return null;
    
    // Нормализуем - убираем время
    return new Date(parsed.getFullYear(), parsed.getMonth(), parsed.getDate());
  }, [selectedDate]);
  
  // Формируем ключ для выбранной даты используя useMemo
  // ВАЖНО: Используем ЛОКАЛЬНЫЕ компоненты года, месяца, дня для создания ключа
  const selectedKey = useMemo(() => {
    if (!safeSelectedDate) return null;
    
    const year = safeSelectedDate.getFullYear();
    const month = safeSelectedDate.getMonth();
    const day = safeSelectedDate.getDate();
    const key = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    
    console.log('TrainDetails - Формирование selectedKey:', {
      selectedDate: selectedDate?.toString(),
      safeSelectedDate: {
        date: safeSelectedDate.toString(),
        year,
        month,
        day,
        iso: safeSelectedDate.toISOString()
      },
      selectedKey: key
    });
    
    return key;
  }, [safeSelectedDate, selectedDate]);

  useEffect(() => {
    console.log('TrainDetails - useEffect вызван:', {
      selectedDate: selectedDate?.toString(),
      safeSelectedDate: safeSelectedDate ? {
        date: safeSelectedDate.toString(),
        year: safeSelectedDate.getFullYear(),
        month: safeSelectedDate.getMonth(),
        day: safeSelectedDate.getDate(),
        iso: safeSelectedDate.toISOString()
      } : null,
      selectedKey,
      trainsCount: trains?.length || 0
    });
    
    if (!selectedKey) {
      console.log('TrainDetails - Нет selectedKey, очищаем тренировки');
      setDayTrains([]);
      return;
    }
    
    if (!trains || trains.length === 0) {
      console.log('TrainDetails - Нет тренировок для фильтрации');
      setDayTrains([]);
      return;
    }
    
    console.log('TrainDetails - Фильтрация тренировок:', {
      selectedKey,
      selectedDate: safeSelectedDate ? {
        local: safeSelectedDate.toLocaleDateString('ru-RU'),
        iso: safeSelectedDate.toISOString(),
        year: safeSelectedDate.getFullYear(),
        month: safeSelectedDate.getMonth(),
        day: safeSelectedDate.getDate()
      } : null,
      trainsCount: trains.length,
      trains: trains.map(t => {
        const trainDate = t.date ? new Date(t.date) : null;
        const trainKey = getTrainDateKey(t);
        return {
          id: t.id,
          type: t.type,
          dateKey: trainKey,
          date: trainDate ? {
            iso: trainDate.toISOString(),
            local: trainDate.toLocaleDateString('ru-RU'),
            localYear: trainDate.getFullYear(),
            localMonth: trainDate.getMonth(),
            localDay: trainDate.getDate()
          } : null
        };
      })
    });
    
    const filtered = trains.filter(train => {
      const trainKey = getTrainDateKey(train);
      const matches = trainKey === selectedKey;
      
      console.log('TrainDetails - Сравнение:', {
        trainId: train.id,
        trainType: train.type,
        trainKey,
        selectedKey,
        match: matches,
        trainDate: train.date ? new Date(train.date).toISOString() : null
      });
      
      return matches;
    });
    
    console.log('TrainDetails - Отфильтрованные тренировки:', {
      count: filtered.length,
      trains: filtered.map(t => ({ id: t.id, type: t.type }))
    });
    setDayTrains(filtered);
  }, [selectedKey, trains, safeSelectedDate]);

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
        {onDeleteDay && dayTrains.length > 0 && (
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