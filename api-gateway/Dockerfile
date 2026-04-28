FROM node:24-alpine

# Define o diretório do contentor
WORKDIR /app

# Copia dependências
COPY package*.json ./

# Instala as dependências 
RUN npm install 

# Copia o restante código da pasta api-gateway para o contentor
COPY . .

# Expõe a porta que o index.js está utilizando
EXPOSE 40004

# Comando para iniciar a aplicação
CMD ["node", "src/routes/index.js"]