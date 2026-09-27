# Elasticsearch Vietnamese Analysis Plugin

[![Build Status](https://github.com/duydo/elasticsearch-analysis-vietnamese/actions/workflows/test.yml/badge.svg)](https://github.com/duydo/elasticsearch-analysis-vietnamese/actions/workflows/test.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.3.0-005571?logo=elasticsearch)](https://www.elastic.co/elasticsearch/)

Vietnamese text analysis for Elasticsearch, powered by the [CocCoc tokenizer](https://github.com/coccoc/coccoc-tokenizer), the C++ word segmenter behind the CocCoc search engine.

Vietnamese words often span several syllables separated by spaces (`công nghệ`, `thông tin`), so splitting on whitespace breaks them apart. This plugin segments text into real words:

```
"Cộng hòa Xã hội chủ nghĩa Việt Nam"  →  ["cộng hòa", "xã hội", "chủ nghĩa", "việt nam"]
```

It provides:

| Name | Kind | Description |
| :--- | :--- | :--- |
| `vi_analyzer` | analyzer | `vi_tokenizer` + `lowercase` + `vi_stop` |
| `vi_tokenizer` | tokenizer | Vietnamese word segmentation |
| `vi_stop` | token filter | Stop word removal with the built-in Vietnamese list |

---

## 🛠 Installation

The plugin calls a native library, so **every Elasticsearch node** needs:

1. the CocCoc tokenizer native library (`libcoccoc_tokenizer_jni`) and its dictionaries, and
2. the plugin itself.

The plugin version must match your Elasticsearch version exactly.

### Option 1: Docker (easiest)

The [Dockerfile](Dockerfile) builds the native library and the plugin, then installs both into the official Elasticsearch image:

```bash
git clone https://github.com/duydo/elasticsearch-analysis-vietnamese.git
cd elasticsearch-analysis-vietnamese

cp .env.sample .env          # set ELASTIC_PASSWORD, and ES_VERSION (any 9.x release, default 9.3.0)
mkdir -p data && chmod a+rw data

docker compose up --build
```

Check that it works:

```bash
curl -u "elastic:$ELASTIC_PASSWORD" -H 'Content-Type: application/json' \
  localhost:9200/_analyze -d '{"analyzer": "vi_analyzer", "text": "Công nghệ thông tin Việt Nam"}'
```

### Option 2: Existing installation

**Step 1. Install the native library and dictionaries** (requires `gcc`, `g++`, `cmake`, `make` and a JDK):

```bash
git clone https://github.com/duydo/coccoc-tokenizer.git
cd coccoc-tokenizer && mkdir build && cd build
cmake -DBUILD_JAVA=1 ..
sudo make install
```

This installs the library to `/usr/local/lib` and the dictionaries to `/usr/local/share/tokenizer/dicts`. The JVM does not search `/usr/local/lib` by default, so make the library visible to Elasticsearch in one of these ways:

```bash
# Either link it into a default library directory…
sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.so /usr/lib/
# …or add it to the library path of the Elasticsearch process
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
```

**Step 2. Install the plugin:**

```bash
bin/elasticsearch-plugin install --batch \
  https://github.com/duydo/elasticsearch-analysis-vietnamese/releases/download/v9.3.0/elasticsearch-analysis-vietnamese-9.3.0.zip
```

`--batch` accepts the `load_native_libraries` entitlement that the plugin needs to load the tokenizer. Restart the node afterwards.

---

## ⚙️ Configuration

### `vi_tokenizer`

| Parameter | Description | Default |
| :--- | :--- | :--- |
| `dict_path` | Directory containing the tokenizer dictionaries. | `/usr/local/share/tokenizer/dicts` |
| `keep_punctuation` | Emit punctuation marks as tokens. | `false` |
| `split_url` | Also segment the words inside domain names (`duydo.me` → `duy`, `do`, `me`). Takes precedence over `split_host`. | `false` |
| `split_host` | Keep host names as whole words (`vnexpress.net` → `vnexpress`, `net`). Intended for fields that contain only host names. | `false` |

How the URL options change the output:

| Input | default | `split_url: true` | `split_host: true` |
| :--- | :--- | :--- | :--- |
| `duydo.me` | `duydo`, `me` | `duy`, `do`, `me` | `duydo`, `me` |
| `vnexpress.net` | `vn`, `express`, `net` | `vn`, `express`, `net` | `vnexpress`, `net` |
| `https://duydo.me/blog/tieng-viet` | `https`, `duydo`, `me`, `blog`, `tieng`, `viet` | `duy`, `do`, `me`, `blog`, `tieng-viet` | not recommended |

Each token has a type: `<WORD>`, `<NUMBER>` or `<PUNCT>`. Decimal commas are normalized, so `3,5` becomes `3.5`.

> **Note:** The native tokenizer is shared by the whole node and loads one dictionary directory. All indices on a node must use the same `dict_path`. An index that asks for a different one is rejected with HTTP 400.

### `vi_stop`

| Parameter | Description | Default |
| :--- | :--- | :--- |
| `stopwords` | `_vi_` (alias `_vietnamese_`) for the [built-in list](src/main/resources/org/elasticsearch/plugin/analysis/vi/lucene/stopwords.txt), `_none_` for no stop words, or an array of words. | `_vi_` |
| `stopwords_path` | Path to a stop word file (one word per line, UTF-8), absolute or relative to the Elasticsearch `config` directory. | – |
| `ignore_case` | Match stop words case-insensitively. | `false` |

The built-in list is lowercase, so put `vi_stop` after `lowercase` or set `ignore_case: true`.

### `vi_analyzer`

Accepts all `vi_tokenizer` and `vi_stop` parameters.

---

## 🔍 Usage

### Built-in analyzer

```json
POST /_analyze
{
  "analyzer": "vi_analyzer",
  "text": "Công nghệ thông tin của Việt Nam"
}
```

Tokens: `công nghệ`, `thông tin`, `việt nam` (`của` is a stop word).

### Configured analyzer

```json
PUT /my-index
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
  },
  "mappings": {
    "properties": {
      "content": { "type": "text", "analyzer": "my_vi_analyzer" }
    }
  }
}
```

### Custom analyzer: matching with or without diacritics

Combine `vi_tokenizer` with `asciifolding` so that searching `tieng viet` also finds `tiếng Việt`:

```json
PUT /search-index
{
  "settings": {
    "analysis": {
      "analyzer": {
        "vi_folding": {
          "tokenizer": "vi_tokenizer",
          "filter": ["lowercase", "vi_stop", "ascii_folding_keep"]
        }
      },
      "filter": {
        "ascii_folding_keep": { "type": "asciifolding", "preserve_original": true }
      }
    }
  }
}
```

`Tiếng Việt` is indexed as both `tiếng việt` and `tieng viet`.

### HTML content

Offsets always point into the original text, so highlighting works with character filters such as `html_strip`:

```json
POST /_analyze
{
  "tokenizer": "vi_tokenizer",
  "char_filter": ["html_strip"],
  "filter": ["vi_stop"],
  "text": "<b>Việt Nam</b> của tôi"
}
```

Tokens: `Việt Nam` (offsets 3–15), `tôi` (offsets 20–23).

---

## 🏗 Building from Source

Requirements:

- JDK 21+ (build with JDK 22+ to include the Foreign Function & Memory code path used on newer JVMs)
- Maven 3.8+
- The native library from [Installation, step 1](#option-2-existing-installation), needed to run the tokenization tests

```bash
mvn clean package
```

The plugin ZIP is written to `target/releases/`. See [TESTING.md](TESTING.md) for running the tests.

To build for another Elasticsearch 9.x release, set the project version to that release first. The Elasticsearch dependency and the plugin descriptor follow it:

```bash
mvn versions:set -DnewVersion=9.2.0 -DgenerateBackupPoms=false
mvn clean package
```

---

## 📋 Compatibility

Each plugin release is built for exactly one Elasticsearch version. The current code builds and passes its tests against Elasticsearch 9.0.0, 9.1.0, 9.2.0 and 9.3.0; see [Building from Source](#-building-from-source) to build for your version.

| Plugin | Elasticsearch | Java |
| :--- | :--- | :--- |
| 9.3.0 | 9.3.0 | 21+ |
| 8.7.0 | 8.7.x | 17+ |
| 7.16.1 | 7.16.x – 7.17.x | 11+ |

---

## ❓ Troubleshooting

**`Cannot load native library [coccoc_tokenizer_jni] from java.library.path [...]`**
Elasticsearch cannot find `libcoccoc_tokenizer_jni`. Make sure the library is in one of the listed directories, or set `LD_LIBRARY_PATH` as shown in [Installation, step 1](#option-2-existing-installation).

**`Cannot initialize tokenizer with dict_path [...]`**
The dictionary directory is missing or incomplete. It should contain `alphabetic`, `numeric`, `multiterm_trie.dump`, `syllable_trie.dump` and related files. Re-run `sudo make install` for the tokenizer, or point `dict_path` to the right directory.

**`Tokenizer already initialized with dict_path [...], cannot use [...]`**
Two indices on the same node use different `dict_path` values. Use the same dictionary directory everywhere (see the note under [`vi_tokenizer`](#vi_tokenizer)).

---

## ❤️ Acknowledgments

- [CocCoc](https://coccoc.com) for open-sourcing their tokenizer.
- [JetBrains](https://www.jetbrains.com) for providing development tools.

## 📜 License

Licensed under the [Apache License 2.0](LICENSE.txt).
