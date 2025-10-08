import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { Mail, Lock, User, Calendar, Eye, EyeOff } from 'lucide-react';
import './Auth.css';

const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    gender: '',
    birthday: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Валидация
    if (formData.password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      setLoading(false);
      return;
    }

    if (!formData.name || formData.name.length < 2) {
      setError('Имя должно содержать минимум 2 символа');
      setLoading(false);
      return;
    }

    const result = await register(formData);
    
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
    
    setLoading(false);
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-header">
          <h1 className="auth-title">GymLog</h1>
          <p className="auth-subtitle">Создайте новый аккаунт</p>
        </div>

        <div className="card">
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label className="form-label">
                <User size={16} />
                Имя
              </label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="form-input"
                placeholder="Введите ваше имя"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                <Mail size={16} />
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="form-input"
                placeholder="Введите ваш email"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                <Lock size={16} />
                Пароль
              </label>
              <div className="password-input">
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="form-input"
                  placeholder="Введите пароль (минимум 6 символов)"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Пол
              </label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleChange}
                className="form-input"
              >
                <option value="">Выберите пол</option>
                <option value="Мужской">Мужской</option>
                <option value="Женский">Женский</option>
                <option value="Другой">Другой</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">
                <Calendar size={16} />
                Дата рождения
              </label>
              <input
                type="date"
                name="birthday"
                value={formData.birthday}
                onChange={handleChange}
                className="form-input"
                required
              />
            </div>

            {error && (
              <div className="form-error">
                {typeof error === 'string' ? error : 'Произошла ошибка'}
              </div>
            )}

            <button
              type="submit"
              className="btn btn-primary auth-submit"
              disabled={loading}
            >
              {loading ? 'Регистрация...' : 'Зарегистрироваться'}
            </button>
          </form>

          <div className="auth-link">
            <p>Уже есть аккаунт? <Link to="/login">Войти</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
