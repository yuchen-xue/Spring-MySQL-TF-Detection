FROM openjdk:21

# Copy the JAR package into the image
ARG PROJECT_NAME
ARG PROJECT_VERSION
ARG JAR_FILE=build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar
COPY ${JAR_FILE} /app.jar

# Expose the application port
ARG APP_PORT
EXPOSE ${APP_PORT}

# Run the App
# ENTRYPOINT ["/bin/bash"]
ENTRYPOINT ["java", "-jar", "/app.jar"]
