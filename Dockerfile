# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/cadenceAPI-0.0.1-SNAPSHOT.jar /app/cadenceAPI-0.0.1-SNAPSHOT.jar

# Copy React build files
COPY /client/build/ /app/static

# Expose the port the app runs on
EXPOSE 8080

# Define the environment variables
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV CLIENT_ID=${CLIENT_ID}
ENV CLIENT_SECRET=${CLIENT_SECRET}
ENV CLIENT_URL=${CLIENT_URL}
ENV COOKIE_SECRET=${COOKIE_SECRET}
ENV JWT_SECRET=${JWT_SECRET}
ENV SESSION_SECRET=${SESSION_SECRET}
ENV MONGO_URI=${MONGO_URI}
ENV REDIRECT_URI=${REDIRECT_URI}
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV VITE_API_URL=${VITE_API_URL}
ENV ENV=${ENV}

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/cadenceAPI-0.0.1-SNAPSHOT.jar"]
