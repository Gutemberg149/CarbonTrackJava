# Estágio 1: Build com Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Baixar dependências (cache)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar o projeto
RUN mvn clean package -DskipTests

# Estágio 2: Runtime
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup --system appgroup && \
    adduser --system --ingroup appgroup appuser

# Copiar o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Trocar para usuário não-root
USER appuser

# Expor a porta
EXPOSE 8080

# Executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]