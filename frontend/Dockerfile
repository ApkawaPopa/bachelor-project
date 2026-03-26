# Используем образ Node.js, совместимый с требованиями проекта (см. package.json)
FROM node:20-alpine

# Устанавливаем рабочий каталог
WORKDIR /app

# Копируем файлы зависимостей
COPY package*.json ./

# Устанавливаем зависимости (включая devDependencies)
RUN npm ci