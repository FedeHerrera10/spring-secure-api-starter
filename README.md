# 🚀 Proyecto Base para APIs – Java 17 + Spring Boot 3

## 📌 Objetivo

Este repositorio tiene como objetivo construir **un proyecto base reutilizable** para el desarrollo de APIs modernas en **Java 17 con Spring Boot 3**, incorporando desde el inicio una **arquitectura sólida**, **seguridad robusta**, **buenas prácticas** y **herramientas estándar de la industria**.

La idea es que este proyecto sirva como **plantilla (starter)** para futuros desarrollos, evitando repetir la misma configuración de seguridad, entidades base y estructura general cada vez que se inicia un nuevo proyecto.

----------

## 🧱 Stack Tecnológico

-   **Java 17**
    
-   **Spring Boot 3.x**
    
-   **Spring Security**
    
-   **Spring Data JPA**
    
-   **MySQL**
    
-   **Flyway** (migraciones de base de datos)
    
-   **Lombok**
    
-   **Swagger / OpenAPI (springdoc-openapi)**
    
-   **JWT (JSON Web Tokens)**
    
-   **OAuth2 (Google Login)**
    

----------

## 🔐 Seguridad (Auth & AuthZ)

El proyecto contará con un módulo de seguridad desacoplado y extensible, pensado para **aplicaciones web y móviles**.

### Funcionalidades de Seguridad

1.  **Login con usuario y contraseña**
    
    -   Autenticación mediante JWT
        
    -   Tokens de acceso y refresh token
        
2.  **Login con Google (OAuth2)**
    
    -   Integración con Google Identity Platform
        
    -   Asociación automática con usuarios locales
        
3.  **Registro de usuarios**
    
    -   Alta de usuario con estado `PENDING_VERIFICATION`
        
    -   Encriptación de contraseña con BCrypt
        
4.  **Verificación de cuenta**
    
    -   Envío de email con token de verificación
        
    -   Activación de cuenta mediante endpoint seguro
        
5.  **Recuperación / Restablecimiento de contraseña**
    
    Estrategia recomendada (actual y segura):
    
    -   **Magic Link con token de un solo uso**
        
    -   Token con expiración corta
        
    -   Compatible con Web y Mobile Apps
        
    
    Flujo:
    
    1.  Usuario solicita recuperación
        
    2.  Se genera token temporal
        
    3.  Se envía link por email
        
    4.  Usuario redefine contraseña
        
6.  **Logout**
    
    -   Invalidación de refresh token
        
    -   Soporte para blacklist de tokens (opcional)
        

----------

## 👤 Modelo de Entidades

### 🧩 Entidad Base – Auditoría

Todas las entidades del sistema extenderán de una entidad base de auditoría.

Campos comunes:

-   `createdAt` – fecha de creación
    
-   `createdBy` – usuario creador
    
-   `updatedAt` – fecha de última modificación
    
-   `updatedBy` – usuario modificador
    

Implementación sugerida:

-   `@MappedSuperclass`
    
-   `@EntityListeners(AuditingEntityListener.class)`
    
-   Spring Data JPA Auditing
    

----------

### 👤 Entidad User

Campos:

-   `id`
    
-   `username`
    
-   `firstName`
    
-   `lastName`
    
-   `email`
    
-   `password`
    
-   `enabled`
    
-   `roles`
    

Características:

-   Relación **ManyToMany** con `Role`
    
-   Compatible con Spring Security (`UserDetails`)
    
-   Soporte para autenticación local y OAuth2
    

----------

### 🔑 Entidad Role

Campos:

-   `id`
    
-   `name` (ej: `ROLE_ADMIN`, `ROLE_USER`)
    

Uso:

-   Autorización basada en roles
    
-   Preparado para extender a permisos finos en el futuro
    

----------

## 🗄️ Base de Datos y Migraciones

### MySQL

-   Base de datos relacional principal
    
-   Configuración externa por variables de entorno
    

### Flyway

-   Control de versiones del esquema
    
-   Scripts SQL versionados (`V1__init.sql`, `V2__add_roles.sql`, etc.)
    
-   Migraciones automáticas al iniciar la aplicación
    

> 📌 **Buenas prácticas**:
> 
> -   Flyway gestiona la estructura
>     
> -   JPA gestiona el mapping y la lógica
>     

----------

## 📚 Documentación de la API

### Swagger / OpenAPI

-   Documentación automática de endpoints
    
-   Acceso a UI Swagger
    
-   Soporte para JWT Authorization Header
    

URL típica:

```
http://localhost:8080/swagger-ui.html

```

----------

## 🧰 Arquitectura Propuesta

Estructura base del proyecto:

```
com.fedeherrera.spring-secure-api-starter
│
├── config          # Configuraciones generales
├── security        # JWT, filtros, OAuth2, SecurityConfig
├── auth            # Login, register, tokens, password reset
├── user            # User, Role, repositories, services
├── common          # Auditoría, excepciones, utils
├── controller      # Controllers REST
├── service         # Lógica de negocio
├── repository      # JPA Repositories
└── dto             # DTOs de request/response

```

----------

## 🪜 Roadmap – Construcción Paso a Paso

### Fase 1 – Setup inicial

-   Crear proyecto Spring Boot 3
    
-   Configurar Java 17
    
-   Integrar Lombok, JPA, MySQL
    

### Fase 2 – Flyway

-   Configurar Flyway
    
-   Crear esquema inicial de usuarios y roles
    

### Fase 3 – Seguridad Base

-   Spring Security
    
-   Login con usuario/contraseña
    
-   JWT
    

### Fase 4 – Registro y Verificación

-   Registro de usuarios
    
-   Verificación por email
    

### Fase 5 – Recuperación de contraseña

-   Magic link
    
-   Tokens temporales
    

### Fase 6 – OAuth2 Google

-   Login con Google
    
-   Vinculación de cuentas
    

### Fase 7 – Documentación y Hardening

-   Swagger
    
-   Manejo global de errores
    
-   Buenas prácticas y seguridad
    
