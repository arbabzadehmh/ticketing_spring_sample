FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-fas \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

# بهتر: بدون نسخه hardcode
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY ticketing_spring_sample-0.0.1-SNAPSHOT.jar ticketing_spring_sample.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "ticketing_spring_sample.jar"]