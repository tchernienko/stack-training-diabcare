FROM node:26-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

FROM node:26-alpine
WORKDIR /app
RUN npm install -g http-server
COPY --from=build /app/dist/frontend-angular/browser ./
EXPOSE 4200
CMD ["http-server", ".", "-p", "4200"]