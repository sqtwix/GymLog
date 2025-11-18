import React, { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon } from 'lucide-react';
import { format, startOfMonth, endOfMonth, startOfWeek, endOfWeek, addDays, isSameMonth, isSameDay, addMonths, subMonths } from 'date-fns';
import { ru } from 'date-fns/locale';
import './Calendar.css';

const formatDateKey = (date) => {
  if (!date) return null;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getTrainDateKey = (train) => {
  if (!train) return null;
  if (train.dateKey) return train.dateKey;
  if (!train.date) return null;
  const parsed = new Date(train.date);
  if (Number.isNaN(parsed.getTime())) return null;
  return formatDateKey(parsed);
};

const Calendar = ({ selectedDate, onDateSelect, trains = [] }) => {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [monthStart, setMonthStart] = useState(startOfMonth(currentMonth));
  const [monthEnd, setMonthEnd] = useState(endOfMonth(monthStart));
  const [startDate, setStartDate] = useState(startOfWeek(monthStart, { weekStartsOn: 1 }));
  const [endDate, setEndDate] = useState(endOfWeek(monthEnd, { weekStartsOn: 1 }));

  useEffect(() => {
    const newMonthStart = startOfMonth(currentMonth);
    const newMonthEnd = endOfMonth(newMonthStart);
    setMonthStart(newMonthStart);
    setMonthEnd(newMonthEnd);
    setStartDate(startOfWeek(newMonthStart, { weekStartsOn: 1 }));
    setEndDate(endOfWeek(newMonthEnd, { weekStartsOn: 1 }));
  }, [currentMonth]);

  const nextMonth = () => {
    setCurrentMonth(addMonths(currentMonth, 1));
  };

  const prevMonth = () => {
    setCurrentMonth(subMonths(currentMonth, 1));
  };

  const goToToday = () => {
    setCurrentMonth(new Date());
  };

  const renderDays = () => {
    const days = [];
    let day = startDate;

    while (day <= endDate) {
      const isCurrentMonth = isSameMonth(day, currentMonth);
      const isSelected = isSameDay(day, selectedDate);
      const isToday = isSameDay(day, new Date());
      
      const dayKey = formatDateKey(day);
      const dayTrains = trains.filter(train => getTrainDateKey(train) === dayKey);
      const hasTrain = dayTrains.length > 0;

      days.push(
        <div
          key={day.toString()}
          className={`calendar-day ${!isCurrentMonth ? 'other-month' : ''} ${isSelected ? 'selected' : ''} ${isToday ? 'today' : ''} ${hasTrain ? 'has-train' : ''}`}
          onClick={() => {
            onDateSelect(day);
          }}
          title={hasTrain ? `Тренировок: ${dayTrains.length}` : ''}
        >
          <span className="day-number">{format(day, 'd')}</span>
          {isToday && <div className="today-indicator" />}
          {hasTrain && <div className="train-indicator" />}
        </div>
      );
      day = addDays(day, 1);
    }

    return days;
  };

  const weekDays = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  return (
    <div className="calendar-container">
      <div className="calendar-header">
        <div className="calendar-title">
          <CalendarIcon size={24} />
          <h3>{format(currentMonth, 'MMMM yyyy', { locale: ru })}</h3>
        </div>
        <div className="calendar-controls">
          <button onClick={prevMonth} className="calendar-btn">
            <ChevronLeft size={20} />
          </button>
          <button onClick={goToToday} className="calendar-btn today-btn">
            Сегодня
          </button>
          <button onClick={nextMonth} className="calendar-btn">
            <ChevronRight size={20} />
          </button>
        </div>
      </div>

      <div className="calendar-grid">
        <div className="calendar-weekdays">
          {weekDays.map((day) => (
            <div key={day} className="weekday">
              {day}
            </div>
          ))}
        </div>
        <div className="calendar-days">
          {renderDays()}
        </div>
      </div>
    </div>
  );
};

export default Calendar;
