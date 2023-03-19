FROM nvidia/cuda:11.2.2-cudnn8-devel-ubuntu20.04

ARG SCALA_VERSION=2.13.10

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get -y dist-upgrade && \
  apt-get install -y --no-install-recommends \
    nvidia-driver-515 nvidia-utils-515 openssh-server openjdk-11-jdk 

RUN apt-get update && apt-get -y dist-upgrade && apt-get install -y --no-install-recommends gnupg2 curl && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
  curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
  apt-get update && apt-get install -y --no-install-recommends sbt

RUN mkdir -p /usr/local/scala && \
  curl -s https://downloads.lightbend.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz | tar xzvf - --strip-components=1 -C /usr/local/scala && \
  echo "export PATH=\$PATH:/usr/local/scala/bin" >> /etc/bash.bashrc 
  
EXPOSE 22 

CMD mkdir -p /root/.ssh && \
  echo "$PUB_KEY" >> /root/.ssh/authorized_keys && \
  echo "$PUB_KEY" >> /root/.ssh/id_rsa.pub && \
  echo "$PRIVATE_KEY" >> /root/.ssh/id_rsa && \
  chmod 700 /root/.ssh && chmod 600 /root/.ssh/* && \
  service ssh start && \
  tail -f /dev/null