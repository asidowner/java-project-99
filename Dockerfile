FROM eclipse-temurin:21-jdk

ARG GRADLE_VERSION=8.5
ARG PORT
ARG SENTRY_AUTH_TOKEN
ARG SENTRY_DSN
ARG SENTRY_LOG_LEVEL

ENV PORT ${PORT:8080}
ENV SENTRY_AUTH_TOKEN ${SENTRY_AUTH_TOKEN}
ENV SENTRY_DSN ${SENTRY_DSN}
ENV SENTRY_LOG_LEVEL={$SENTRY_LOG_LEVEL:info}

RUN apt-get update && apt-get install -yq make unzip

WORKDIR /backend

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

RUN ./gradlew --no-daemon dependencies

COPY config config
COPY src src
COPY public.pem private.pem src/main/resources/certs/

RUN ./gradlew --no-daemon build

ENV JAVA_OPTS "-Xmx512M -Xms512M"
EXPOSE ${PORT}

CMD java -jar build/libs/app-0.0.1-SNAPSHOT.jar