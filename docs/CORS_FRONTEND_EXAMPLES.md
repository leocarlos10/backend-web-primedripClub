# Ejemplos de uso desde el Frontend (React + TypeScript)

## ✅ Configuración CORS Completada en el Backend

Ya configuramos CORS en Spring Boot, ahora puedes hacer peticiones desde tu frontend sin problemas.

---

## 📝 Ejemplos de uso en React

### 1. Usando Fetch API

```typescript
// Login con Fetch
const login = async (email: string, password: string) => {
  try {
    const response = await fetch('http://localhost:8080/v1/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Si usas cookies
      body: JSON.stringify({
        email,
        password
      })
    });

    if (!response.ok) {
      throw new Error('Error en el login');
    }

    const data = await response.json();
    console.log('Token:', data.data.token);
    
    // Guardar token en localStorage
    localStorage.setItem('token', data.data.token);
    
    return data;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
```

### 2. Usando Axios (Recomendado)

#### Instalar Axios
```bash
npm install axios
```

#### Crear un servicio API (src/services/api.ts)
```typescript
import axios from 'axios';

// Crear instancia de axios con configuración base
const api = axios.create({
  baseURL: 'http://localhost:8080', // Tu backend
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Si usas cookies
});

// Interceptor para agregar el token a todas las peticiones
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas y errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

#### Servicios de autenticación (src/services/authService.ts)
```typescript
import api from './api';

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
  telefono?: string;
}

interface LoginResponse {
  responseCode: number;
  success: boolean;
  message: string;
  data: {
    token: string;
    usuario: {
      id: number;
      nombre: string;
      email: string;
      roles: string[];
    };
  };
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/v1/auth/login', credentials);
    
    // Guardar token
    if (response.data.success && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.data.usuario));
    }
    
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/v1/auth/register', userData);
    
    // Guardar token
    if (response.data.success && response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.data.usuario));
    }
    
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  },
};
```

### 3. Componente de Login (src/components/Login.tsx)
```typescript
import React, { useState } from 'react';
import { authService } from '../services/authService';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authService.login({ email, password });
      
      if (response.success) {
        console.log('Login exitoso:', response.data.usuario);
        // Redirigir al dashboard o home
        window.location.href = '/dashboard';
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al iniciar sesión');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2>Iniciar Sesión</h2>
      
      {error && <div className="error-message">{error}</div>}
      
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email">Email:</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        
        <div>
          <label htmlFor="password">Contraseña:</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Cargando...' : 'Iniciar Sesión'}
        </button>
      </form>
    </div>
  );
};

export default Login;
```

### 4. Hook personalizado para autenticación (src/hooks/useAuth.ts)
```typescript
import { useState, useEffect } from 'react';
import { authService } from '../services/authService';

interface User {
  id: number;
  nombre: string;
  email: string;
  roles: string[];
}

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay un usuario autenticado
    const token = authService.getToken();
    const userData = localStorage.getItem('user');
    
    if (token && userData) {
      setUser(JSON.parse(userData));
      setIsAuthenticated(true);
    }
    
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const response = await authService.login({ email, password });
    if (response.success) {
      setUser(response.data.usuario);
      setIsAuthenticated(true);
    }
    return response;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  return {
    user,
    isAuthenticated,
    loading,
    login,
    logout,
  };
};
```

---

## ⚙️ Variables de Entorno

### En React (archivo .env)
```env
REACT_APP_API_URL=http://localhost:8080
```

### Usar en el código:
```typescript
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  // ...
});
```

---

## 🔒 Notas Importantes

1. **En desarrollo**: Usa `http://localhost:8080` (puerto de tu Spring Boot)
2. **En producción**: Actualiza los `allowedOrigins` en `CorsConfig.java` con tu dominio real
3. **Token JWT**: Guárdalo en `localStorage` o `sessionStorage`
4. **Credenciales**: Si usas cookies, asegúrate de incluir `withCredentials: true` o `credentials: 'include'`

---

## 🚀 Para Producción

### En CorsConfig.java, actualiza:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://tu-frontend.com",
    "https://www.tu-frontend.com"
));
```

### O usa variables de entorno en Spring Boot:
```properties
# application.properties
cors.allowed.origins=https://tu-frontend.com,https://www.tu-frontend.com
```

```java
// CorsConfig.java
@Value("${cors.allowed.origins}")
private String[] allowedOrigins;

configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
```
