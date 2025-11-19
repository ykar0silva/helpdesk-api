# --- Estágio 1: Build (Compilar o código) ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e faz o build
COPY src ./src
RUN mvn clean package -DskipTests

# --- Estágio 2: RUNTIME (Execução) ---
# MUDANÇA AQUI: Trocamos a imagem antiga pela Eclipse Temurin (estável)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copia apenas o arquivo .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Cria a pasta de uploads
RUN mkdir -p /app/uploads

# Expõe a porta 8080
EXPOSE 8080

# Comando para rodar a API
ENTRYPOINT ["java", "-jar", "app.jar"]
