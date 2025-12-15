import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

const normalizeUser = (rawUser) => {
  if (!rawUser) return null;
  return {
    id: rawUser.id ?? rawUser.Id ?? null,
    username: rawUser.username ?? rawUser.Username ?? '',
    email: rawUser.email ?? rawUser.Email ?? '',
    gender: rawUser.gender ?? rawUser.Gender ?? '',
    birthDay: rawUser.birthDay ?? rawUser.BirthDay ?? null
  };
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // КРИТИЧНО: сразу при загрузке приложения ставим токен в axios!
  const savedToken = localStorage.getItem('token');
  
  const [token, setToken] = useState(savedToken);

  // Восстанавливаем пользователя при загрузке
  useEffect(() => {
    const loadUser = () => {
      if (token) {
        const savedUser = localStorage.getItem('user');
        if (savedUser) {
          try {
            const parsed = JSON.parse(savedUser);
            setUser(normalizeUser(parsed));
          } catch (e) {
            console.error('Ошибка парсинга пользователя из localStorage');
          }
        }
      }
      setLoading(false);
    };
    loadUser();
  }, [token]);

  const login = async (email, password) => {
    try {
      const response = await axios.post('/api/user/login', { email, password });
      const { token: newToken, user: userData } = response.data;

      if (!newToken || !userData) {
        throw new Error('Сервер не вернул токен или пользователя');
      }

      const normalizedUser = normalizeUser(userData);

      // Сохраняем всё
      setToken(newToken);
      setUser(normalizedUser);
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(normalizedUser));

      console.log('Успешный вход! Пользователь:', normalizedUser.id);
      return { success: true };
    } catch (error) {
      console.error('Ошибка входа:', error);
      const message = error.response?.data || error.message || 'Ошибка входа';
      return { success: false, error: message };
    }
  };

  const register = async (userData) => {
    try {
      const payload = {
        name: userData.name,
        email: userData.email,
        password: userData.password,
        gender: userData.gender,
        birthDay: userData.birthday ? new Date(userData.birthday).toISOString() : null
      };

      await axios.post('/api/user/register', payload);
      return await login(userData.email, userData.password);
    } catch (error) {
      console.error('Ошибка регистрации:', error);
      const message = error.response?.data || error.message || 'Ошибка регистрации';
      return { success: false, error: message };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    delete axios.defaults.headers.common['Authorization'];
    console.log('Выход выполнен');
  };

  const value = {
    user,
    token,
    login,
    register,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};