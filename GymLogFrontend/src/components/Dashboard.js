import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { LogOut, Calendar as CalendarIcon, Plus, RefreshCw, AlertCircle } from 'lucide-react';
import Calendar from './calendar/Calendar';
import TrainModal from './TrainModal';
import TrainDetails from './TrainDetails';
import { trainAPI } from '../services/trainService';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    return new Date(today.getFullYear(), today.getMonth(), today.getDate());
  });
  const [trains, setTrains] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [deleteState, setDeleteState] = useState({
    loading: false,
    error: '',
    success: ''
  });

  useEffect(() => {
    if (user?.id) {
      loadTrains();
    }
  }, [user?.id]);

  useEffect(() => {
    setDeleteState(prev => ({
      ...prev,
      error: '',
      success: ''
    }));
  }, [selectedDate]);

  const loadTrains = async () => {
    if (!user?.id) return;

    setLoading(true);
    setErrorMessage('');
    const result = await trainAPI.getTrains(user.id);

    if (result.success) {
      setTrains(result.data);
    } else {
      setErrorMessage(result.error || 'Не удалось загрузить тренировки');
    }

    setLoading(false);
  };

  const handleDateSelect = (date) => {
    const normalizedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    setSelectedDate(normalizedDate);
  };

  const handleAddTrain = () => setIsModalOpen(true);

  const handleModalClose = () => setIsModalOpen(false);

  const handleTrainAdded = async () => {
    await loadTrains();
  };

  const handleDeleteTrain = async () => {
    if (!user?.id || !selectedDate) return;

    const confirmed = window.confirm('Удалить все тренировки на выбранный день?');
    if (!confirmed) return;

    setDeleteState({ loading: true, error: '', success: '' });
    const dateParam = selectedDate.toISOString().split('T')[0];
    const result = await trainAPI.deleteTrain(user.id, dateParam);

    if (result.success) {
      const deletedCount = result.data?.deleted ?? result.data?.Deleted ?? 0;
      setDeleteState({
        loading: false,
        error: '',
        success: deletedCount
          ? `Удалено тренировок: ${deletedCount}`
          : 'Тренировок на этот день больше нет'
      });
      await loadTrains();
    } else {
      setDeleteState({
        loading: false,
        error: result.error || 'Не удалось удалить тренировку',
        success: ''
      });
    }
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

            {errorMessage && (
              <div className="calendar-error">
                <div className="calendar-error-message">
                  <AlertCircle size={18} />
                  <span>{errorMessage}</span>
                </div>
                <button className="btn btn-secondary small-btn" onClick={loadTrains} disabled={loading}>
                  <RefreshCw size={16} />
                  Повторить
                </button>
              </div>
            )}
            
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
              onDeleteDay={handleDeleteTrain}
              deleteState={deleteState}
            />
            
            <div className="date-actions">
              <button 
                className="btn btn-primary"
                onClick={handleAddTrain}
              >
                <Plus size={16} />
                Добавить тренировку
              </button>
              <button 
                className="btn btn-info"
                onClick={loadTrains}
                disabled={loading}
              >
                <RefreshCw size={16} />
                {loading ? 'Обновление...' : 'Обновить календарь'}
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
