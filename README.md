Vietnamese Analysis Plugin for Elasticsearch
========================================

Vietnamese Analysis plugin integrates Vietnamese language analysis into Elasticsearch.

The plugin provides the `vi_analyzer` analyzer and `vi_tokenizer` tokenizer. The `vi_analyzer` is composed of the `vi_tokenizer` tokenizer, the `lowercase` and `stop` filter.


## Installation on Elasticsearch 5.x

In order to install the plugin, choose a version in [releases](https://github.com/duydo/elasticsearch-analysis-vietnamese/releases) page then run:

```sh
bin/elasticsearch-plugin install link/to/binary/version
```

Or to build from source, you need to build it with Maven:

```bash
mvn clean package
bin/elasticsearch-plugin install file:target/releases/elasticsearch-analysis-vietnamese-5.2.1.zip
```

*In order to build the plugin you need to build the [vn-nlp-libararies](https://github.com/duydo/vn-nlp-libraries) first. Thanks to thầy [Lê Hồng Phương](http://mim.hus.vnu.edu.vn/phuonglh/) for his VnTokenizer library.*



## Example
```sh
curl "http://localhost:9200/_analyze?pretty" -d'
{
  "analyzer": "vi_analyzer",
  "text": "Công nghệ thông tin Việt Nam"
}'
```

Result
```json
{
  "tokens" : [
    {
      "token" : "công nghệ thông tin",
      "start_offset" : 0,
      "end_offset" : 19,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "việt nam",
      "start_offset" : 20,
      "end_offset" : 28,
      "type" : "name2",
      "position" : 1
    }
  ]
}
```

|Vietnamese Analysis Plugin|Elasticsearch|
|---|---|
| master|5.2.1|
| 5.2.1|5.2.1|
| 2.4.1|2.4.1|
| 2.4.0|2.4.0|
| 2.3.5|2.3.5|
| 2.3.4|2.3.4|
| 2.3.3|2.3.3|
| 2.3.2|2.3.2|
| 2.3.1|2.3.1|
| 2.3.0|2.3.0|
| 0.2.2|2.2.0|
| 0.2.1.1|2.1.1|
| 0.2.1|2.1.0|
| 0.2|2.0.0|
| 0.1.7|1.7+|
| 0.1.6|1.6+|
| 0.1.5|1.5+|
| 0.1.1|1.4+|
| 0.1|1.3|

License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
