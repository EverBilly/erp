# Frontend - React
FROM node:18-alpine AS build

WORKDIR /app
# Copiar package.json e instalar dependencias
COPY package*.json ./
RUN npm install
# Copiar el resto de la aplicación
COPY . .
# Construir la aplicación
RUN npm run build

# Servidor Nginx para servir la aplicación
FROM nginx:alpine
# Copiar los archivos construidos
COPY --from=build /app/build /usr/share/nginx/html
# Copiar configuración de nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]