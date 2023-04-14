ARG ES_VERSION=8.3.3
ARG DEBIAN_FRONTEND=noninteractive

# thanks to https://github.com/cpfriend1721994/docker-es-cococ-tokenizer
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION as builder
ARG ES_VERSION
ARG DEBIAN_FRONTEND
USER root

RUN apt-get update -y && apt-get install -y software-properties-common build-essential
RUN gcc --version
RUN apt-get update -y && \
    apt-get install -y make cmake pkg-config wget git openjdk-17-jdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN cd /tmp && wget https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz && \
tar xvf apache-maven-3.8.8-bin.tar.gz && \
mkdir -p /usr/share/maven && cd /usr/share/maven && \
cp -r /tmp/apache-maven-3.8.8/* .
ENV PATH=/usr/share/maven/bin:$PATH

WORKDIR /

COPY pom.xml .
RUN mvn verify clean --fail-never

RUN git clone https://github.com/coccoc/coccoc-tokenizer.git

RUN mkdir /coccoc-tokenizer/build
WORKDIR /coccoc-tokenizer/build
RUN cmake -DBUILD_JAVA=1 ..
RUN make install

COPY . /elasticsearch-analysis-vietnamese
WORKDIR /elasticsearch-analysis-vietnamese
RUN mvn package -Dmaven.test.skip -e

FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION
ARG ES_VERSION

COPY --from=builder /coccoc-tokenizer/dicts/tokenizer /usr/local/share/tokenizer/dicts
COPY --from=builder /coccoc-tokenizer/dicts/vn_lang_tool /usr/local/share/tokenizer/dicts
COPY --from=builder /coccoc-tokenizer/build/libcoccoc_tokenizer_jni.so /usr/lib
COPY --from=builder /coccoc-tokenizer/build/multiterm_trie.dump /usr/local/share/tokenizer/dicts
COPY --from=builder /coccoc-tokenizer/build/nontone_pair_freq_map.dump /usr/local/share/tokenizer/dicts
COPY --from=builder /coccoc-tokenizer/build/syllable_trie.dump /usr/local/share/tokenizer/dicts
COPY --from=builder /elasticsearch-analysis-vietnamese/target/releases/elasticsearch-analysis-vietnamese-$ES_VERSION.zip /
RUN echo "Y" | /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch file:///elasticsearch-analysis-vietnamese-$ES_VERSION.zip
