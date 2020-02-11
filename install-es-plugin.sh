#!/bin/bash
# setting up prerequisites

cd /usr/share/elasticsearch

yes | ./bin/elasticsearch-plugin install file:./plugin/elasticsearch-analysis-vietnamese-$ES_VERSION.zip

/usr/local/bin/docker-entrypoint.sh eswrapper