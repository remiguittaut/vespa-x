FROM eclipse-temurin:17.0.13_11-jdk-noble AS build
LABEL authors="remi guittaut"

WORKDIR /work

COPY . .

RUN ./gradlew jar

FROM eclipse-temurin:17.0.13_11-jre-noble AS runtime

WORKDIR /app

COPY --from=build /work/build/libs/vespax-1.0.jar .

ENTRYPOINT ["java"]

CMD ["-jar", "/app/vespax-1.0.jar"]
