FROM public.ecr.aws/docker/library/maven:3.9.9-amazoncorretto-23-al2023 AS build

WORKDIR /code

COPY pom.xml .
# Create .m2 directory to avoid permission issues
RUN mkdir -p /root/.m2
# Download dependencies first (this layer will be cached)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

# Build the application
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests --also-make --batch-mode

FROM build AS tests

ENV SPRING_DATASOURCE_PRIMARY_URL=jdbc:postgresql://authronom-backend-postgres:5432/test_authronom_backend
ENV SPRING_DATA_REDIS_HOST=authronom-backend-redis

RUN mvn test --batch-mode

FROM public.ecr.aws/docker/library/maven:3.9.9-amazoncorretto-23-al2023 AS development

WORKDIR /authronom-backend

# Install development dependencies
RUN yum install -y git

ENTRYPOINT []
CMD []

FROM public.ecr.aws/amazoncorretto/amazoncorretto:23-al2023 AS application

WORKDIR /authronom-backend

COPY --from=build /code/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
