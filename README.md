# EcolimSAC - Sistema de Gestión de Residuos

EcolimSAC es una aplicación móvil Android diseñada para facilitar el registro, monitoreo y análisis de la recolección de residuos en la empresa. Permite a los operarios registrar recolecciones detalladas y a los administradores visualizar el historial y estadísticas en tiempo real.

## 📱 Características Principales

### 1. Autenticación y Gestión de Usuarios
- **Inicio de Sesión**: Acceso seguro mediante correo electrónico y contraseña.
- **Registro**: Creación de nuevas cuentas usuario con nombre, apellido y credenciales.
- **Sesión Persistente**: Verificación automática de sesión activa al iniciar la app.
- **Roles de Usuario**: Diferenciación entre usuarios estándar y administradores (visualización diferenciada en el historial).

### 2. Panel Principal (Dashboard)
Acceso rápido a las funcionalidades principales de la aplicación:
- **Estadísticas**
- **Nueva Recolección**
- **Historial**
- **Mis Datos**
- **Cierre de Sesión**

### 3. Nueva Recolección
Formulario intuitivo para el registro de residuos recolectados:
- **Tipo de Residuo**: Categorización en Aprovechable, No Aprovechable, Orgánico y Peligroso.
- **Clasificación Detallada**: Sub-categorías dinámicas basadas en el tipo seleccionado (ej. Papel, Vidrio, Pilas, Restos de comida).
- **Peso**: Registro del peso en Kilogramos (kg).
- **Área de Origen**: Selección del lugar de recolección (Planta Principal, Comedor, Almacén, Oficinas).

### 4. Historial de Recolecciones
Listado cronológico de las actividades de recolección:
- **Vista de Operario**: Visualiza solo sus propios registros.
- **Vista de Administrador**: Acceso al registro global de todas las recolecciones.
- **Detalles**: Muestra fecha, tipo, peso y clasificación de cada registro.

### 5. Estadísticas y Reportes
Herramientas de análisis visual de datos:
- **Gráficos**: Visualización de la distribución de residuos mediante gráficos de pastel (PieChart).
- **Filtros**:
  - Por rango de fechas (Desde/Hasta).
  - Por tipo de residuo (Todos o específico).
- **Indicadores Clave**: Visualización del Total de Kilos recolectados y Total de Operaciones.
- **Exportación**: Capacidad de generar reportes (PDF).

### 6. Perfil de Usuario (Mis Datos)
Gestión de la información personal:
- Visualización de datos de la cuenta (UID, Correo, Rol/Nivel de Acceso).
- Edición de Nombre y Apellido.
- Identificador de usuario único.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Java
- **Plataforma**: Android SDK (Min SDK: 24, Target SDK: 34)
- **Backend (Firebase)**:
  - **Authentication**: Gestión de identidad.
  - **Firestore Database**: Base de datos NoSQL en tiempo real para usuarios y registros de recolección.
- **Librerías Externas**:
  - [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart): Para la generación de gráficos estadísticos.
  - **AndroidX & Material Design Components**: Para una interfaz de usuario moderna y responsiva.

## 📂 Estructura del Proyecto

El código fuente se encuentra organizado en paquetes funcionales bajo `com.ecollimsac.ecolim`:

- **`menu/`**: Contiene la lógica de las funcionalidades principales.
  - `estadistica/`: Lógica de gráficos y reportes.
  - `historial/`: Adaptadores y vistas para el listado de registros.
  - `nuevarecoleccion/`: Formulario de ingreso de datos.
  - `misdatos/`: Gestión de perfil.
- **`model/`**: Modelos de datos (POJOs), como `RecoleccionRegistro`.
- **`view/`**: Componentes de vista personalizados.
- **Actividades Principales**:
  - `MainActivity`: Login.
  - `RegistroActivity`: Registro de usuarios.
  - `DahsboardActivity`: Menú principal.
  - `SplahsActivity`: Pantalla de carga inicial.

## 🚀 Primeros Pasos

1.  Clonar el repositorio.
2.  Abrir el proyecto en Android Studio.
3.  Asegurarse de tener el archivo `google-services.json` configurado correctamente en la carpeta `app/` para la conexión con Firebase.
4.  Sincronizar el proyecto con Gradle.
5.  Ejecutar en un dispositivo o emulador Android.

---
© 2024 EcolimSAC. Todos los derechos reservados.

