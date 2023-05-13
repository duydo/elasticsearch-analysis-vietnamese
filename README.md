# Vietnamese Analysis Plugin for Elasticsearch

[![Test](https://github.com/duydo/elasticsearch-analysis-vietnamese/actions/workflows/test.yml/badge.svg)](https://github.com/duydo/elasticsearch-analysis-vietnamese/actions/workflows/test.yml)

Vietnamese Analysis plugin integrates Vietnamese language analysis into Elasticsearch. It uses [C++ tokenizer for Vietnamese](https://github.com/coccoc/coccoc-tokenizer) library developed by
CocCoc team for their Search Engine and Ads systems.

The plugin provides `vi_analyzer` analyzer, `vi_tokenizer` tokenizer and `vi_stop` stop filter. The `vi_analyzer` is composed of the `vi_tokenizer` tokenizer, `stop` and `lowercase` filter.

## Example output

```
GET _analyze
{
  "analyzer": "vi_analyzer",
  "text": "Cộng hòa Xã hội chủ nghĩa Việt Nam"
}
```

The above sentence would produce the following terms:
```
["cộng hòa", "xã hội", "chủ nghĩa" ,"việt nam"]

```

## Configuration

The `vi_analyzer` analyzer accepts the following parameters:

- `dict_path` The path to tokenizer dictionary on system. Defaults to `/usr/local/share/tokenizer/dicts`.
- `keep_punctuation` Keep punctuation marks as tokens. Defaults to `false`.
- `split_url` If it's enabled (`true`), a domain `duydo.me` is split into  `["duy", "do", "me"]`.
  If it's disabled (`false`) `duydo.me` is split into `["duydo", "me"]`. Defaults to `false`.
  
- `stopwords` A pre-defined stop words list like `_vi_` or an array containing a list of stop words. Defaults to [stopwords.txt](src/main/resources/org/apache/lucene/analysis/vi/stopwords.txt) file. 
- `stopwords_path` The path to a file containing stop words.


### Example configuration
In this example, we configure the `vi_analyzer` analyzer to keep punctuation marks and to use the custom list of stop words:

```
PUT my-vi-index-00001
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_vi_analyzer": {
          "type": "vi_analyzer",
          "keep_punctuation": true,
          "stopwords": ["rất", "những"]
        }
      }
    }
  }
}

GET my-vi-index-00001/_analyze
{
  "analyzer": "my_vi_analyzer",
  "text": "Công nghệ thông tin Việt Nam rất phát triển trong những năm gần đây."
}
```

The above example produces the following terms:
```
["công nghệ", "thông tin", "việt nam", "phát triển", "trong", "năm", "gần đây", "."]

```

We can also create a custom analyzer with the `vi_tokenizer`. In following example, we create `my_vi_analyzer` to produce
both diacritic and no diacritic tokens in lowercase:

```
PUT my-vi-index-00002
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_vi_analyzer": {
          "tokenizer": "vi_tokenizer",
          "filter": [
            "lowercase",
            "ascii_folding"
          ]
        }
      },
      "filter": {
        "ascii_folding": {
          "type": "asciifolding",
          "preserve_original": true
        }
      }
    }
  }
}

GET my-vi-index-00002/_analyze
{
  "analyzer": "my_vi_analyzer",
  "text": "Cộng hòa Xã hội chủ nghĩa Việt Nam"
}
```

The above example produces the following terms:
```
["cong hoa", "cộng hòa", "xa hoi", "xã hội", "chu nghia", "chủ nghĩa", "viet nam", "việt nam"]

```

## Use Docker

Make sure you have installed both Docker & docker-compose

### Build the image with Docker Compose

```sh
# Copy, edit ES version and password for user elastic in file .env. Default password: changeme
cp .env.sample .env
docker compose build
docker compose up
```
### Verify
```sh
curl -k http://elastic:changeme@localhost:9200/_analyze -H 'Content-Type: application/json' -d '
{
  "analyzer": "vi_analyzer",
  "text": "Cộng hòa Xã hội chủ nghĩa Việt Nam"
}'

# Output
{"tokens":[{"token":"cộng hòa","start_offset":0,"end_offset":8,"type":"<WORD>","position":0},{"token":"xã hội","start_offset":9,"end_offset":15,"type":"<WORD>","position":1},{"token":"chủ nghĩa","start_offset":16,"end_offset":25,"type":"<WORD>","position":2},{"token":"việt nam","start_offset":26,"end_offset":34,"type":"<WORD>","position":3}]}                                                                                     
```

## Build from Source
### Step 1: Build C++ tokenizer for Vietnamese library
```sh
git clone https://github.com/duydo/coccoc-tokenizer.git
cd coccoc-tokenizer && mkdir build && cd build
cmake -DBUILD_JAVA=1 ..
make install
# Link the coccoc shared lib to /usr/lib
sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.* /usr/lib/
```
By default, the `make install` installs:
- The lib commands `tokenizer`, `dict_compiler` and `vn_lang_tool` under `/usr/local/bin`
- The dynamic lib `libcoccoc_tokenizer_jni.so` under `/usr/local/lib/`. The plugin uses this lib directly.
- The dictionary files under `/usr/local/share/tokenizer/dicts`. The plugin uses this path for `dict_path` by default.

Verify
```sh
/usr/local/bin/tokenizer "Cộng hòa Xã hội chủ nghĩa Việt Nam"
# cộng hòa	xã hội	chủ nghĩa	việt nam
```

Refer [the repo](https://github.com/duydo/coccoc-tokenizer) for more information to build the library.


### Step 2: Build the plugin

Clone the plugin’s source code:

```sh
git clone https://github.com/duydo/elasticsearch-analysis-vietnamese.git
cd elasticsearch-analysis-vietnamese
git checkout 8.4.0
```

Optionally, edit the `pom.xml` to change the version of Elasticsearch (same as plugin version) you want to build the plugin with:

```xml
...
<version>8.4.0</version>
...
 ```
Build the plugin:
```sh
mvn package
```

### Step 3: Installation the plugin on Elasticsearch

```sh
bin/elasticsearch-plugin install file://target/releases/elasticsearch-analysis-vietnamese-8.4.0.zip
```

## Compatible Versions
From v7.12.11, the plugin uses CocCoc C++ tokenizer instead of the VnTokenizer by Lê Hồng Phương,
I don't maintain the plugin with the VnTokenizer anymore, if you want to continue developing with it, refer [the branch vntokenizer](https://github.com/duydo/elasticsearch-analysis-vietnamese/tree/vntokenizer).  

| Vietnamese Analysis Plugin | Elasticsearch   |
|----------------------------|-----------------|
| 8.4.0                      | 8.4.0 ~ 8.7.1   |
| 7.16.1                     | 7.16 ~ 7.17.1   |
| 7.12.1                     | 7.12.1 ~ 7.15.x |     
| 7.3.1                      | 7.3.1           |   
| 5.6.5                      | 5.6.5           |
| 5.4.1                      | 5.4.1           |
| 5.3.1                      | 5.3.1           |
| 5.2.1                      | 5.2.1           |
| 2.4.1                      | 2.4.1           |
| 2.4.0                      | 2.4.0           |
| 2.3.5                      | 2.3.5           |
| 2.3.4                      | 2.3.4           |
| 2.3.3                      | 2.3.3           |
| 2.3.2                      | 2.3.2           |
| 2.3.1                      | 2.3.1           |
| 2.3.0                      | 2.3.0           |
| 0.2.2                      | 2.2.0           |
| 0.2.1.1                    | 2.1.1           |
| 0.2.1                      | 2.1.0           |
| 0.2                        | 2.0.0           |
| 0.1.7                      | 1.7+            |
| 0.1.6                      | 1.6+            |
| 0.1.5                      | 1.5+            |
| 0.1.1                      | 1.4+            |
| 0.1                        | 1.3             |


## Issues

You might get errors during starting Elasticsearch with the plugin

**1. Error: java.lang.UnsatisfiedLinkError: no libcoccoc_tokenizer_jni in java.library.path ...** (reported in [102](https://github.com/duydo/elasticsearch-analysis-vietnamese/issues/102))

It happens because of your JVM cannot find the dynamic lib `libcoccoc_tokenizer_jni` in `java.library.path`, try to resolve by doing one of following options:
- Appending `/usr/local/lib` into environment variable  `LD_LIBRARY_PATH`:
```sh
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
```
- Making a symbolic link or copying the file `/usr/local/lib/libcoccoc_tokenizer_jni.so` to `/usr/lib` :
```sh
# Make link
ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.so /usr/lib/libcoccoc_tokenizer_jni.so

# Copy 
cp /usr/local/lib/libcoccoc_tokenizer_jni.so /usr/lib
```

**2. Error: Cannot initialize Tokenizer: /usr/local/share/tokenizer/dicts** (reported in [106](https://github.com/duydo/elasticsearch-analysis-vietnamese/issues/106))

It happens because of the tokenizer cannot find the dictionary files under `/usr/local/share/tokenizer/dicts`. 
Ensure the path `/usr/local/share/tokenizer/dicts` existed and includes those files: alphabetic, i_and_y.txt, nontone_pair_freq_map.dump, syllable_trie.dump
d_and_gi.txt, multiterm_trie.dump, numeric. If not, try to build the C++ tokenizer (Step 1) again.


## Thanks to
- [JetBrains](https://www.jetbrains.com) has provided a free license for [IntelliJ IDEA](https://www.jetbrains.com/idea).
- [CocCoc team](https://coccoc.com) has provided their C++ Vietnamese tokenizer library as open source.

## License
    
    This software is licensed under the Apache 2 license, quoted below.

    Copyright by Duy Do

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
