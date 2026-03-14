# ── Stage 1: Build coccoc-tokenizer ──────────────────────────────────
ARG ES_VERSION
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION AS coccoc-builder

USER root
ENV JAVA_HOME=/usr/share/elasticsearch/jdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Build coccoc-tokenizer
RUN echo "Build coccoc-tokenizer..." && \
    microdnf install -y --nodocs \
        gcc gcc-c++ make cmake pkgconf-pkg-config git gzip tar which && \
    microdnf clean all

WORKDIR /tmp
RUN git clone --depth 1 https://github.com/duydo/coccoc-tokenizer.git && \
    mkdir coccoc-tokenizer/build

WORKDIR /tmp/coccoc-tokenizer/build
RUN cmake -DJAVA_HOME=${JAVA_HOME} -DBUILD_JAVA=1 .. && make install

# ── Stage 2: Build analysis-vietnamese plugin ────────────────────────
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION AS plugin-builder

USER root
ENV JAVA_HOME=/usr/share/elasticsearch/jdk
ENV PATH=$JAVA_HOME/bin:$PATH

ARG MVN_VERSION=3.9.9
RUN echo "Build analysis-vietnamese plugin..." && \
    microdnf install -y --nodocs gzip tar && \
    microdnf clean all && \
    curl -fsSL -o /tmp/maven.tar.gz \
        https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MVN_VERSION}/apache-maven-${MVN_VERSION}-bin.tar.gz && \
    tar xf /tmp/maven.tar.gz -C /opt && \
    rm /tmp/maven.tar.gz
ENV MVN_HOME=/opt/apache-maven-${MVN_VERSION}
ENV PATH=$MVN_HOME/bin:$PATH

WORKDIR /tmp/elasticsearch-analysis-vietnamese

# Copy POM first to cache dependency downloads separately from source changes
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode -q

# Copy source and build
COPY src/ src/
ARG ES_VERSION
RUN mvn --batch-mode -Dmaven.test.skip -e package -DprojectVersion=$ES_VERSION

# ── Stage 3: Final runtime image ────────────────────────────────────
FROM docker.elastic.co/elasticsearch/elasticsearch:$ES_VERSION

ARG ES_VERSION
ARG COCCOC_INSTALL_PATH=/usr/local
ARG COCCOC_DICT_PATH=$COCCOC_INSTALL_PATH/share/tokenizer/dicts

USER root
COPY --from=coccoc-builder $COCCOC_INSTALL_PATH/lib/libcoccoc_tokenizer_jni.so /usr/lib/
COPY --from=coccoc-builder $COCCOC_DICT_PATH $COCCOC_DICT_PATH
COPY --from=plugin-builder \
    /tmp/elasticsearch-analysis-vietnamese/target/releases/elasticsearch-analysis-vietnamese-$ES_VERSION.zip /tmp/
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch \
        file:///tmp/elasticsearch-analysis-vietnamese-$ES_VERSION.zip && \
    rm /tmp/elasticsearch-analysis-vietnamese-$ES_VERSION.zip

USER elasticsearch
