import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { LogOut, Calendar as CalendarIcon, Plus } from 'lucide-react';
import Calendar from './calendar/Calendar';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [selectedDate, setSelectedDate] = useState(new Date());

  const handleDateSelect = (date) => {
    setSelectedDate(date);
    console.log('Selected date:', date);
    // Здесь будет логика для работы с выбранной датой
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <div>
            <h1 className="dashboard-title">
              <CalendarIcon size={32} />
              Планировщик тренировок
            </h1>
            <p className="dashboard-subtitle">
              Добро пожаловать, {user?.username || 'Пользователь'}!
            </p>
          </div>
          <div className="user-info">
            <span className="user-name">{user?.username}</span>
            <button onClick={handleLogout} className="logout-btn">
              <LogOut size={16} />
              Выйти
            </button>
          </div>
        </div>

        <div className="dashboard-content">
          <div className="calendar-section">
            <div className="section-header">
              <h2>Календарь тренировок</h2>
              <p>Выберите день для планирования тренировки</p>
            </div>
            
            <Calendar 
              selectedDate={selectedDate}
              onDateSelect={handleDateSelect}
            />
          </div>

          <div className="selected-date-info">
            <div className="card">
              <h3>Выбранная дата</h3>
              <p className="selected-date">
                {selectedDate.toLocaleDateString('ru-RU', {
                  weekday: 'long',
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })}
              </p>
              <div className="date-actions">
                <button className="btn btn-primary">
                  <Plus size={16} />
                  Добавить тренировку
                </button>
                <p className="action-hint">
                  Функционал добавления тренировок будет доступен после разработки соответствующих API
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
