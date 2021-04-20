FROM dvmarques/openjdk-14-jdk-alpine-with-timezone

ADD target/money_transfer_service-0.0.1-SNAPSHOT.jar money_transfer_service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/money_transfer_service.jar"]