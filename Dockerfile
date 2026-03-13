# ── Stage 1: Build coccoc-tokenizer ──────────────────────────────────
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION AS coccoc-builder

USER root
ARG ES_VERSION
ENV DEBIAN_FRONTEND=noninteractive

# Build coccoc-tokenizer
RUN echo "Build coccoc-tokenizer..." && \
    apt-get update -y && \
    apt-get install -y --no-install-recommends \
        build-essential cmake pkg-config git && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /tmp
RUN git clone --depth 1 https://github.com/duydo/coccoc-tokenizer.git && \
    mkdir coccoc-tokenizer/build

WORKDIR /tmp/coccoc-tokenizer/build
RUN cmake -DBUILD_JAVA=1 .. && make install

# ── Stage 2: Build analysis-vietnamese plugin ────────────────────────
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION AS plugin-builder

USER root
ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/share/elasticsearch/jdk
ENV PATH=$JAVA_HOME/bin:$PATH

ARG MVN_VERSION=3.9.9
RUN echo "Build analysis-vietnamese plugin..." && \
    apt-get update -y && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/* && \
    wget -q -O /tmp/maven.tar.gz \
        https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/apache-maven-${MVN_VERSION}-bin.tar.gz && \
    tar xf /tmp/maven.tar.gz -C /opt && \
    rm /tmp/maven.tar.gz
ENV MVN_HOME=/opt/apache-maven-${MVN_VERSION}
ENV PATH=$MVN_HOME/bin:$PATH

WORKDIR /tmp/elasticsearch-analysis-vietnamese

# Copy POM first to cache dependency downloads separately from source changes
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode -q || true

# Copy source and build
COPY src/ src/
ARG ES_VERSION
RUN mvn --batch-mode -Dmaven.test.skip -e package -DprojectVersion=$ES_VERSION

# ── Stage 3: Final runtime image ────────────────────────────────────
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION

ARG ES_VERSION
ARG COCCOC_INSTALL_PATH=/usr/local
ARG COCCOC_DICT_PATH=$COCCOC_INSTALL_PATH/share/tokenizer/dicts

COPY --from=coccoc-builder $COCCOC_INSTALL_PATH/lib/libcoccoc_tokenizer_jni.so /usr/lib/
COPY --from=coccoc-builder $COCCOC_DICT_PATH $COCCOC_DICT_PATH
COPY --from=plugin-builder /tmp/elasticsearch-analysis-vietnamese/target/releases/elasticsearch-analysis-vietnamese-$ES_VERSION.zip /tmp/
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch file:///tmp/elasticsearch-analysis-vietnamese-$ES_VERSION.zip && \
    rm /tmp/elasticsearch-analysis-vietnamese-$ES_VERSION.zip
