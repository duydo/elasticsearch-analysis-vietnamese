## Changes:
Lowercase characters before going to tokenize step.

Before: 
`Công Nghệ Thông Tin Việt Nam` => analyze => `[công, nghệ, thông, tin, việt, nam]`

After: 
`Công Nghệ Thông Tin Việt Nam` => analyze => `[công nghệ thông tin, việt nam]`

Vietnamese Analysis Plugin for Elasticsearch
========================================

Vietnamese Analysis plugin integrates Vietnamese language analysis into Elasticsearch.

The plugin provides the `vi_analyzer` analyzer and `vi_tokenizer` tokenizer. The `vi_analyzer` is composed of the `vi_tokenizer` tokenizer, the `lowercase` and `stop` filter.

## Installation on Elasticsearch 5.x

In order to install the plugin, choose a version in [releases](https://github.com/duydo/elasticsearch-analysis-vietnamese/releases) page then run:

```sh
bin/elasticsearch-plugin install link/to/binary/version
```
## Build from Source
Check this post: [How to build Elasticsearch Vietnamese Analysis Plugin](http://duydo.me/how-to-build-elasticsearch-vietnamese-analysis-plugin/)

## Compatible Versions
| Vietnamese Analysis Plugin | Elasticsearch |
| -------------------------- | ------------- |
| master                     | 7.3.1         |
| 7.3.1                      | 7.3.1         |   
| 5.6.5                      | 5.6.5         |
| 5.4.1                      | 5.4.1         |
| 5.3.1                      | 5.3.1         |
| 5.2.1                      | 5.2.1         |
| 2.4.1                      | 2.4.1         |
| 2.4.0                      | 2.4.0         |
| 2.3.5                      | 2.3.5         |
| 2.3.4                      | 2.3.4         |
| 2.3.3                      | 2.3.3         |
| 2.3.2                      | 2.3.2         |
| 2.3.1                      | 2.3.1         |
| 2.3.0                      | 2.3.0         |
| 0.2.2                      | 2.2.0         |
| 0.2.1.1                    | 2.1.1         |
| 0.2.1                      | 2.1.0         |
| 0.2                        | 2.0.0         |
| 0.1.7                      | 1.7+          |
| 0.1.6                      | 1.6+          |
| 0.1.5                      | 1.5+          |
| 0.1.1                      | 1.4+          |
| 0.1                        | 1.3           |

## Thanks to
- [Lê Hồng Phương](http://mim.hus.vnu.edu.vn/phuonglh/) for his VnTokenizer library
- [JetBrains](https://www.jetbrains.com) has provided a free license for their great tool: [IntelliJ IDEA](https://www.jetbrains.com/idea/)

## License
    
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
