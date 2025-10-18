import React, { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon } from 'lucide-react';
import { format, startOfMonth, endOfMonth, startOfWeek, endOfWeek, addDays, isSameMonth, isSameDay, addMonths, subMonths } from 'date-fns';
import { ru } from 'date-fns/locale';
import './Calendar.css';

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
      
      // Проверяем, есть ли тренировка в этот день
      const dayTrains = trains.filter(train => {
        if (!train.date) return false;
        
        // Пробуем разные способы парсинга даты
        let trainDate;
        if (typeof train.date === 'string') {
          trainDate = new Date(train.date);
        } else {
          trainDate = new Date(train.date);
        }
        
        const dayDate = new Date(day);
        
        // Проверяем валидность дат
        if (isNaN(trainDate.getTime()) || isNaN(dayDate.getTime())) {
          return false;
        }
        
        // Нормализуем даты к началу дня для точного сравнения
        const trainDateOnly = new Date(trainDate.getFullYear(), trainDate.getMonth(), trainDate.getDate());
        const dayDateOnly = new Date(dayDate.getFullYear(), dayDate.getMonth(), dayDate.getDate());
        
        return trainDateOnly.getTime() === dayDateOnly.getTime();
      });
      const hasTrain = dayTrains.length > 0;
      
      // Отладочная информация для 3 ноября
      if (day.getDate() === 3 && day.getMonth() === 10) { // 3 ноября (месяц 10 = ноябрь)
        console.log('3 ноября - отладка:', {
          day: day.toDateString(),
          isCurrentMonth,
          isSelected,
          isToday,
          hasTrain,
          selectedDate: selectedDate?.toDateString(),
          today: new Date().toDateString()
        });
      }

      days.push(
        <div
          key={day.toString()}
          className={`calendar-day ${!isCurrentMonth ? 'other-month' : ''} ${isSelected ? 'selected' : ''} ${isToday ? 'today' : ''} ${hasTrain ? 'has-train' : ''}`}
          onClick={() => {
            console.log('Calendar - клик по дню:', day.toDateString());
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
