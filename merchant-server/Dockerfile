FROM davidcaste/alpine-java-unlimited-jce:jdk8

MAINTAINER toney <toney@gmail.com>

RUN echo "http://mirrors.aliyun.com/alpine/latest-stable/main/" > /etc/apk/repositories
RUN echo '@community http://mirrors.aliyun.com/alpine/latest-stable/community' >> /etc/apk/repositories
RUN echo '@testing http://mirrors.aliyun.com/alpine/edge/testing' >> /etc/apk/repositories
RUN apk upgrade --update

#时区配置
ENV TIMEZONE Asia/Shanghai
RUN apk add tzdata
RUN ln -snf /usr/share/zoneinfo/$TIMEZONE /etc/localtime
RUN echo $TIMEZONE > /etc/timezone

COPY  merchant-service/target/merchant-service-0.0.1-SNAPSHOT.jar /opt/merchant-service-0.0.1-SNAPSHOT.jar

#EXPOSE 9119
#EXPOSE 8200

ENTRYPOINT ["java","-jar","/opt/merchant-service-0.0.1-SNAPSHOT.jar"]