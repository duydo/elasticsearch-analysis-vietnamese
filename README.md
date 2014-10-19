Vietnamese Analysis for Elasticsearch
=====================================

Vietnamese Analysis plugin integrates Vietnamese language analysis into Elasticsearch.

In order to install the plugin, simply run: 

```sh
bin/plugin --url https://dl.dropboxusercontent.com/u/1598491/elasticsearch-analysis-vietnamese-0.1.zip --install analysis-vietnamese
```

Or to build from source, you need to build it with Maven:

```bash
mvn clean package
bin/plugin --install analysis-vietnamese
       --url file:target/releases/elasticsearch-analysis-vietnamese-0.1.zip
```

*Notes*: To build the plugin you need to clone and build the [vn-nlp-libararies](https://github.com/duydo/vn-nlp-libraries). The plugin uses  [Lê Hồng Phương](http://mim.hus.vnu.edu.vn/phuonglh/) vnTokenizer library. Thanks thầy Lê Hồng Phương for great contribution.

## User guide

The plugin includes the `vi_analyzer` analyzer and `vi_tokenizer` tokenizer.

The `vi_analyzer` is built using the `vi_tokenizer` tokenizer, the `lowercase` and `stop` filter.

 The analyzer analyzes `"công nghệ thông tin Việt Nam"` into `"công nghệ thông tin"` and `"việt nam"` tokens.

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
