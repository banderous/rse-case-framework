FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2

WORKDIR /app
COPY build/libs/backend.jar ./
ENTRYPOINT ["java","-jar", "backend.jar"]
