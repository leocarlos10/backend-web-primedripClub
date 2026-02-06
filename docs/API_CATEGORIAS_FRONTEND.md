# API de Categorías - Guía para Frontend

Esta guía proporciona toda la información necesaria para consumir los endpoints de categorías desde el frontend.

## 📋 Tabla de Contenidos
- [Base URL](#base-url)
- [Estructura de Respuesta](#estructura-de-respuesta)
- [Autenticación](#autenticación)
- [Endpoints Disponibles](#endpoints-disponibles)
- [Ejemplos de Consumo](#ejemplos-de-consumo)

## Base URL

```
http://localhost:8080/v1/categorias
```

## Estructura de Respuesta

Todas las respuestas de la API siguen esta estructura:

```typescript
interface Response<T> {
  responseCode: number;
  success: boolean;
  message: string;
  data: T;
}
```

### Estructura de Categoría

```typescript
interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string;
}

interface CategoriaRequest {
  nombre: string;        // Obligatorio, máximo 100 caracteres
  descripcion?: string;  // Opcional, máximo 500 caracteres
}
```

## Autenticación

- **Endpoints públicos**: Obtener todas las categorías, Obtener por ID
- **Endpoints protegidos** (requieren rol ADMIN): Crear, Actualizar, Eliminar

Para endpoints protegidos, incluir el token JWT en el header:
```javascript
headers: {
  'Authorization': `Bearer ${token}`
}
```

## Endpoints Disponibles

### 1. Obtener Todas las Categorías

**Endpoint**: `GET /v1/categorias`

**Permisos**: Público

**Respuesta exitosa** (200):
```json
{
  "responseCode": 200,
  "success": true,
  "message": "Categorías obtenidas exitosamente",
  "data": [
    {
      "id": 1,
      "nombre": "Camisetas",
      "descripcion": "Camisetas de diseño exclusivo"
    },
    {
      "id": 2,
      "nombre": "Pantalones",
      "descripcion": "Pantalones y jeans"
    }
  ]
}
```

---

### 2. Obtener Categoría por ID

**Endpoint**: `GET /v1/categorias/{id}`

**Permisos**: Público

**Parámetros**:
- `id` (path): ID de la categoría

**Respuesta exitosa** (200):
```json
{
  "responseCode": 200,
  "success": true,
  "message": "Categoría obtenida exitosamente",
  "data": {
    "id": 1,
    "nombre": "Camisetas",
    "descripcion": "Camisetas de diseño exclusivo"
  }
}
```

**Respuesta error** (404):
```json
{
  "responseCode": 404,
  "success": false,
  "message": "Categoría no encontrada",
  "data": null
}
```

---

### 3. Crear Categoría

**Endpoint**: `POST /v1/categorias`

**Permisos**: Solo ADMIN

**Headers requeridos**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body de la petición**:
```json
{
  "nombre": "Accesorios",
  "descripcion": "Accesorios y complementos"
}
```

**Respuesta exitosa** (201):
```json
{
  "responseCode": 201,
  "success": true,
  "message": "Categoría creada exitosamente",
  "data": {
    "id": 3,
    "nombre": "Accesorios",
    "descripcion": "Accesorios y complementos"
  }
}
```

**Respuesta error - Validación** (400):
```json
{
  "responseCode": 400,
  "success": false,
  "message": "El nombre es obligatorio",
  "data": null
}
```

**Respuesta error - No autorizado** (403):
```json
{
  "responseCode": 403,
  "success": false,
  "message": "Acceso denegado",
  "data": null
}
```

---

### 4. Actualizar Categoría

**Endpoint**: `PUT /v1/categorias/{id}`

**Permisos**: Solo ADMIN

**Headers requeridos**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Parámetros**:
- `id` (path): ID de la categoría a actualizar

**Body de la petición**:
```json
{
  "nombre": "Accesorios Premium",
  "descripcion": "Accesorios y complementos de alta calidad"
}
```

**Respuesta exitosa** (200):
```json
{
  "responseCode": 200,
  "success": true,
  "message": "Categoría actualizada exitosamente",
  "data": {
    "id": 3,
    "nombre": "Accesorios Premium",
    "descripcion": "Accesorios y complementos de alta calidad"
  }
}
```

---

### 5. Eliminar Categoría

**Endpoint**: `DELETE /v1/categorias/{id}`

**Permisos**: Solo ADMIN

**Headers requeridos**:
```
Authorization: Bearer {token}
```

**Parámetros**:
- `id` (path): ID de la categoría a eliminar

**Respuesta exitosa** (204):
```json
{
  "responseCode": 204,
  "success": true,
  "message": "Categoría eliminada exitosamente",
  "data": null
}
```

---

## Ejemplos de Consumo

### Con Fetch API (JavaScript Vanilla)

#### 1. Obtener todas las categorías

```javascript
async function obtenerCategorias() {
  try {
    const response = await fetch('http://localhost:8080/v1/categorias');
    const data = await response.json();
    
    if (data.success) {
      console.log('Categorías:', data.data);
      return data.data;
    } else {
      console.error('Error:', data.message);
    }
  } catch (error) {
    console.error('Error de red:', error);
  }
}
```

#### 2. Obtener categoría por ID

```javascript
async function obtenerCategoriaPorId(id) {
  try {
    const response = await fetch(`http://localhost:8080/v1/categorias/${id}`);
    const data = await response.json();
    
    if (data.success) {
      console.log('Categoría:', data.data);
      return data.data;
    } else {
      console.error('Error:', data.message);
    }
  } catch (error) {
    console.error('Error de red:', error);
  }
}
```

#### 3. Crear categoría (requiere autenticación)

```javascript
async function crearCategoria(token, categoriaData) {
  try {
    const response = await fetch('http://localhost:8080/v1/categorias', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(categoriaData)
    });
    
    const data = await response.json();
    
    if (data.success) {
      console.log('Categoría creada:', data.data);
      return data.data;
    } else {
      console.error('Error:', data.message);
    }
  } catch (error) {
    console.error('Error de red:', error);
  }
}

// Uso
const nuevaCategoria = {
  nombre: "Zapatos",
  descripcion: "Calzado deportivo y casual"
};

crearCategoria('tu-token-jwt-aqui', nuevaCategoria);
```

#### 4. Actualizar categoría (requiere autenticación)

```javascript
async function actualizarCategoria(token, id, categoriaData) {
  try {
    const response = await fetch(`http://localhost:8080/v1/categorias/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(categoriaData)
    });
    
    const data = await response.json();
    
    if (data.success) {
      console.log('Categoría actualizada:', data.data);
      return data.data;
    } else {
      console.error('Error:', data.message);
    }
  } catch (error) {
    console.error('Error de red:', error);
  }
}

// Uso
const categoriaActualizada = {
  nombre: "Zapatos Premium",
  descripcion: "Calzado deportivo y casual de alta gama"
};

actualizarCategoria('tu-token-jwt-aqui', 1, categoriaActualizada);
```

#### 5. Eliminar categoría (requiere autenticación)

```javascript
async function eliminarCategoria(token, id) {
  try {
    const response = await fetch(`http://localhost:8080/v1/categorias/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const data = await response.json();
    
    if (data.success) {
      console.log('Categoría eliminada exitosamente');
      return true;
    } else {
      console.error('Error:', data.message);
      return false;
    }
  } catch (error) {
    console.error('Error de red:', error);
    return false;
  }
}

// Uso
eliminarCategoria('tu-token-jwt-aqui', 1);
```

---

### Con Axios

#### Configuración inicial

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/v1';

// Crear instancia de axios
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para manejar respuestas
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response) {
      console.error('Error:', error.response.data.message);
    }
    return Promise.reject(error);
  }
);
```

#### Ejemplos con Axios

```javascript
// 1. Obtener todas las categorías
async function obtenerCategorias() {
  try {
    const response = await api.get('/categorias');
    return response.data;
  } catch (error) {
    console.error('Error al obtener categorías:', error);
  }
}

// 2. Obtener categoría por ID
async function obtenerCategoriaPorId(id) {
  try {
    const response = await api.get(`/categorias/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error al obtener categoría:', error);
  }
}

// 3. Crear categoría
async function crearCategoria(categoriaData) {
  try {
    const response = await api.post('/categorias', categoriaData);
    return response.data;
  } catch (error) {
    console.error('Error al crear categoría:', error);
  }
}

// 4. Actualizar categoría
async function actualizarCategoria(id, categoriaData) {
  try {
    const response = await api.put(`/categorias/${id}`, categoriaData);
    return response.data;
  } catch (error) {
    console.error('Error al actualizar categoría:', error);
  }
}

// 5. Eliminar categoría
async function eliminarCategoria(id) {
  try {
    const response = await api.delete(`/categorias/${id}`);
    return response.success;
  } catch (error) {
    console.error('Error al eliminar categoría:', error);
  }
}
```

---

### Con React y Hooks

```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8080/v1/categorias';

// Hook personalizado para categorías
function useCategorias() {
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    cargarCategorias();
  }, []);

  const cargarCategorias = async () => {
    setLoading(true);
    try {
      const response = await axios.get(API_URL);
      if (response.data.success) {
        setCategorias(response.data.data);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const crearCategoria = async (categoriaData) => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.post(API_URL, categoriaData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.data.success) {
        setCategorias([...categorias, response.data.data]);
        return response.data.data;
      }
    } catch (err) {
      setError(err.message);
    }
  };

  const actualizarCategoria = async (id, categoriaData) => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.put(`${API_URL}/${id}`, categoriaData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.data.success) {
        setCategorias(
          categorias.map(cat => 
            cat.id === id ? response.data.data : cat
          )
        );
        return response.data.data;
      }
    } catch (err) {
      setError(err.message);
    }
  };

  const eliminarCategoria = async (id) => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.delete(`${API_URL}/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.data.success) {
        setCategorias(categorias.filter(cat => cat.id !== id));
        return true;
      }
    } catch (err) {
      setError(err.message);
      return false;
    }
  };

  return {
    categorias,
    loading,
    error,
    cargarCategorias,
    crearCategoria,
    actualizarCategoria,
    eliminarCategoria
  };
}

// Componente de ejemplo
function CategoriasComponent() {
  const {
    categorias,
    loading,
    error,
    crearCategoria,
    eliminarCategoria
  } = useCategorias();

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Categorías</h2>
      <ul>
        {categorias.map(categoria => (
          <li key={categoria.id}>
            {categoria.nombre} - {categoria.descripcion}
            <button onClick={() => eliminarCategoria(categoria.id)}>
              Eliminar
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

---

## 🔒 Manejo de Errores Comunes

### Error 401 - No Autenticado
```json
{
  "responseCode": 401,
  "success": false,
  "message": "Token no válido o expirado",
  "data": null
}
```
**Solución**: Verificar que el token JWT sea válido y no haya expirado.

### Error 403 - No Autorizado
```json
{
  "responseCode": 403,
  "success": false,
  "message": "No tienes permisos para realizar esta acción",
  "data": null
}
```
**Solución**: Verificar que el usuario tenga rol ADMIN.

### Error 404 - No Encontrado
```json
{
  "responseCode": 404,
  "success": false,
  "message": "Categoría no encontrada",
  "data": null
}
```
**Solución**: Verificar que el ID de la categoría exista.

### Error 400 - Validación
```json
{
  "responseCode": 400,
  "success": false,
  "message": "El nombre es obligatorio",
  "data": null
}
```
**Solución**: Verificar que los datos enviados cumplan las validaciones:
- `nombre`: Obligatorio, máximo 100 caracteres
- `descripcion`: Opcional, máximo 500 caracteres

---

## 📝 Notas Importantes

1. **CORS**: Asegúrate de que el backend tenga configurado CORS para permitir peticiones desde tu dominio frontend.

2. **Almacenamiento del Token**: Guarda el token JWT de manera segura (localStorage, sessionStorage o cookies httpOnly).

3. **Manejo de Errores**: Implementa un manejo robusto de errores para mejorar la experiencia del usuario.

4. **Loading States**: Muestra estados de carga mientras se realizan las peticiones.

5. **Validación en Frontend**: Valida los datos antes de enviarlos al backend para mejorar la experiencia del usuario.

---

## 🔗 Referencias

- [Documentación de CORS](./CORS_FRONTEND_EXAMPLES.md)
- [Guía de Implementación Backend](./GUIA_IMPLEMENTACION_BACKEND.md)
