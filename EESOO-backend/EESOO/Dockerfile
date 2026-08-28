FROM eclipse-temurin:21.0.12_8-jre-jammy

RUN groupadd --system appgroup && \
    useradd --system --gid appgroup appuser

WORKDIR /app

COPY target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 9090

ENTRYPOINT [ "java" , "-jar" , "app.jar" ]