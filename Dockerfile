FROM eclipse-temurin:17-jdk

COPY build/libs/*.jar /service.jar

CMD java $JAVA_OPTS -jar /service.jar
