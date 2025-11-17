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
  const [token, setToken] = useState(localStorage.getItem('token'));

  // Настройка axios для автоматического добавления токена
  useEffect(() => {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common['Authorization'];
    }
  }, [token]);

  // Проверка токена при загрузке приложения
  useEffect(() => {
    const checkAuth = async () => {
      if (token) {
        try {
          // Здесь можно добавить запрос для проверки токена
          // Пока просто устанавливаем пользователя из localStorage
          const savedUser = localStorage.getItem('user');
          if (savedUser) {
            setUser(normalizeUser(JSON.parse(savedUser)));
          }
        } catch (error) {
          console.error('Auth check failed:', error);
          logout();
        }
      }
      setLoading(false);
    };

    checkAuth();
  }, [token]);

  const login = async (email, password) => {
    try {
      const response = await axios.post('/api/user/login', {
        email,
        password
      });

      const responseData = response.data || {};
      const newToken = responseData.token || responseData.Token;
      const userData = responseData.user || responseData.User;

      if (!newToken || !userData) {
        throw new Error('Ответ сервера не содержит токен или данные пользователя');
      }

      const normalizedUser = normalizeUser(userData);

      setToken(newToken);
      setUser(normalizedUser);

      axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(normalizedUser));
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      const message = error.response?.data || error.message || 'Ошибка входа';
      return { 
        success: false, 
        error: message 
      };
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
      
      // После регистрации автоматически входим
      return await login(userData.email, userData.password);
    } catch (error) {
      console.error('Register error:', error);
      const message = error.response?.data || error.message || 'Ошибка регистрации';
      return { 
        success: false, 
        error: message 
      };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    delete axios.defaults.headers.common['Authorization'];
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
