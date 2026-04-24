FROM eclipse-temurin:17

WORKDIR /app

COPY . .

RUN javac NumberGuessingServer.java

EXPOSE 8080

CMD ["java", "NumberGuessingServer"]