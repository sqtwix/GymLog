import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { LogOut, Calendar as CalendarIcon, Plus } from 'lucide-react';
import Calendar from './calendar/Calendar';
import TrainModal from './TrainModal';
import TrainDetails from './TrainDetails';
import { trainAPI } from '../services/trainService';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [selectedDate, setSelectedDate] = useState(() => {
    // Устанавливаем начальную дату как сегодня, но без времени
    const today = new Date();
    return new Date(today.getFullYear(), today.getMonth(), today.getDate());
  });
  const [trains, setTrains] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  // Загружаем тренировки при входе в систему
  useEffect(() => {
    if (user?.id) {
      loadTrains();
    }
  }, [user?.id]);

  const loadTrains = async () => {
    if (!user?.id) return;
    
    setLoading(true);
    const result = await trainAPI.getTrains(user.id);
    
    if (result.success) {
      console.log('Загруженные тренировки:', result.data);
      console.log('Количество тренировок:', result.data.length);
      
      // Показываем даты всех тренировок
      result.data.forEach((train, index) => {
        console.log(`Тренировка ${index + 1}:`, {
          date: train.date,
          type: train.type,
          description: train.description
        });
      });
      
      setTrains(result.data);
    } else {
      console.error('Ошибка загрузки тренировок:', result.error);
    }
    
    setLoading(false);
  };

  const handleDateSelect = (date) => {
    // Нормализуем дату к началу дня
    const normalizedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    console.log('Dashboard - handleDateSelect вызван с датой:', date);
    console.log('Dashboard - нормализованная дата:', normalizedDate);
    console.log('Dashboard - текущие тренировки:', trains);
    setSelectedDate(normalizedDate);
    
    // Проверяем, что состояние обновилось
    setTimeout(() => {
      console.log('Dashboard - selectedDate после обновления:', selectedDate);
    }, 100);
  };

  const handleAddTrain = () => {
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handleTrainAdded = () => {
    // Перезагружаем список тренировок после добавления новой
    loadTrains();
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
              trains={trains}
            />
          </div>

          <div className="selected-date-info">
            <TrainDetails 
              selectedDate={selectedDate}
              trains={trains}
              key={`${selectedDate?.getTime()}-${trains.length}`} // Принудительное обновление
            />
            
            <div className="date-actions">
              <button 
                className="btn btn-primary"
                onClick={handleAddTrain}
              >
                <Plus size={16} />
                Добавить тренировку
              </button>
            </div>
          </div>
        </div>

        <TrainModal
          isOpen={isModalOpen}
          onClose={handleModalClose}
          selectedDate={selectedDate}
          onTrainAdded={handleTrainAdded}
        />
      </div>
    </div>
  );
};

export default Dashboard;
