import React, { useEffect } from 'react';
import { Clock, Dumbbell, FileText, Calendar as CalendarIcon } from 'lucide-react';
import './TrainDetails.css';

const TrainDetails = ({ selectedDate, trains = [] }) => {
  
  // Отслеживаем изменения selectedDate и trains
  useEffect(() => {
    console.log('TrainDetails - useEffect сработал:', {
      selectedDate: selectedDate?.toDateString(),
      trainsCount: trains.length,
      trains: trains
    });
  }, [selectedDate, trains]);
  // Фильтруем тренировки для выбранной даты
  const dayTrains = trains.filter(train => {
    if (!train.date || !selectedDate) return false;
    
    // Пробуем разные способы парсинга даты
    let trainDate;
    if (typeof train.date === 'string') {
      trainDate = new Date(train.date);
    } else {
      trainDate = new Date(train.date);
    }
    
    const selected = new Date(selectedDate);
    
    // Проверяем валидность дат
    if (isNaN(trainDate.getTime()) || isNaN(selected.getTime())) {
      console.log('Невалидная дата:', { trainDate: train.date, selectedDate });
      return false;
    }
    
    // Нормализуем даты к началу дня для точного сравнения
    const trainDateOnly = new Date(trainDate.getFullYear(), trainDate.getMonth(), trainDate.getDate());
    const selectedDateOnly = new Date(selected.getFullYear(), selected.getMonth(), selected.getDate());
    
    const isMatch = trainDateOnly.getTime() === selectedDateOnly.getTime();
    
    // Отладочная информация для ВСЕХ тренировок
    console.log('Проверка тренировки:', {
      trainDate: trainDate.toDateString(),
      selectedDate: selected.toDateString(),
      trainDateOnly: trainDateOnly.toDateString(),
      selectedDateOnly: selectedDateOnly.toDateString(),
      isMatch,
      train: train
    });
    
    return isMatch;
  });
  
  // Отладочная информация
  console.log('TrainDetails - selectedDate:', selectedDate?.toDateString(), 'dayTrains:', dayTrains.length);

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

  return (
    <div className="train-details">
      <div className="trains-header">
        <h3>Тренировки на {selectedDate.toLocaleDateString('ru-RU', {
          weekday: 'long',
          day: 'numeric',
          month: 'long'
        })}</h3>
        <span className="trains-count">{dayTrains.length} тренировок</span>
      </div>

      <div className="trains-list">
        {dayTrains.map((train, index) => (
          <div key={index} className="train-card">
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
