# # Dockerfile
# FROM eclipse-temurin:21-jdk
#
# WORKDIR /app
#
# # Copiar o JAR gerado
# COPY target/*.jar app.jar
#
# # Criar usuário não-root para segurança
# RUN addgroup --system appgroup && \
#     adduser --system --ingroup appgroup appuser
#
# USER appuser
#
# # Expor a porta
# EXPOSE 8080
#
# # Executar a aplicação
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Estágio 1: Build com Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar pom.xml e baixar dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiar o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Criar usuário não-root para segurança
RUN addgroup --system appgroup && \
    adduser --system --ingroup appgroup appuser

USER appuser

# Expor a porta
EXPOSE 8080

# Executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]