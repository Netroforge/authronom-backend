FROM public.ecr.aws/docker/library/maven:3.9.9-amazoncorretto-23-al2023 AS build
WORKDIR /code

COPY . .
RUN mvn clean package -DskipTests --also-make --batch-mode

FROM build AS tests

RUN mvn test --batch-mode

FROM public.ecr.aws/docker/library/maven:3.9.9-amazoncorretto-23-al2023 AS development

WORKDIR /authronom-backend

# Install development dependencies
RUN yum install -y git

ENTRYPOINT []
CMD []