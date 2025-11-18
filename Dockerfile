FROM openjdk:26-ea-slim-trixie
WORKDIR /app
COPY ./target/vehicleRentalEntretien-0.0.1-SNAPSHOT.jar /app/vehicleRentalEntretien-0.0.1-SNAPSHOT.jar
EXPOSE 8084
CMD ["java", "-jar", "/app/vehicleRentalEntretien-0.0.1-SNAPSHOT.jar"]