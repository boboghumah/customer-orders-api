FROM jenkins/jenkins:lts-jdk17

USER root

ARG DOCKER_GID=20

RUN apt-get update \
    && apt-get install -y docker.io \
    && if getent group ${DOCKER_GID}; then \
         group_name=$(getent group ${DOCKER_GID} | cut -d: -f1); \
         usermod -aG "${group_name}" jenkins; \
       else \
         groupadd -g ${DOCKER_GID} dockerhost; \
         usermod -aG dockerhost jenkins; \
       fi \
    && rm -rf /var/lib/apt/lists/*

USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
