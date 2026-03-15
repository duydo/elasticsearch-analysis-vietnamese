# Testing Guidelines

This document explains how to build and run the test suite for the **Elasticsearch Vietnamese Analysis Plugin**.

---

## Prerequisites

### 1. Java 21+

The project requires Java 21. Confirm with:

```sh
java -version
```

### 2. Maven 3.8+

```sh
mvn -version
```

### 3. CocCoc Tokenizer Native Library (required for tokenization tests)

Tests that exercise actual Vietnamese tokenization (`VietnameseAnalysisTests`, `VietnameseAnalysisIntegrationTests`) need the CocCoc native library on the system.

**Build and install:**

```sh
git clone https://github.com/duydo/coccoc-tokenizer.git
cd coccoc-tokenizer && mkdir build && cd build
cmake -DBUILD_JAVA=1 ..
make install

# Make the shared library discoverable by the JVM
sudo ln -sf /usr/local/lib/libcoccoc_tokenizer_jni.* /usr/lib/
```

**Verify the library is installed:**

```sh
/usr/local/bin/tokenizer "Cộng hòa Xã hội chủ nghĩa Việt Nam"
# Expected output: cộng hòa    xã hội    chủ nghĩa    việt nam
```

> **Note:** The two unit test classes (`TestVietnameseAnalyzer`, `VietnameseConfigTest`) do **not** need the native library and can run anywhere.

---

## Running Tests

### All tests (unit + integration)

```sh
mvn test
```

The build uses `maven-surefire-plugin` 3.5.2, which supports Java 21 and runs JUnit 4 tests via the `RandomizedRunner` that is already on the test classpath (pulled in by `elasticsearch-test:framework`).

### Skip tests (build the ZIP artifact only)

```sh
mvn package -DskipTests
```

### Run a single test class

```sh
mvn test -Dtests.class=org.elasticsearch.index.analysis.VietnameseAnalysisTests
```

### Run a single test method

```sh
mvn test \
  -Dtests.class=org.elasticsearch.index.analysis.VietnameseAnalysisTests \
  -Dtests.method=testVietnameseAnalyzer
```

### Reproduce a specific randomized failure

Each test run prints a seed, e.g. `SEED: 1A2B3C4D5E6F`. Pass it back to replay the same execution order:

```sh
mvn test -Dtests.seed=1A2B3C4D5E6F
```

---

## Test Classes

| Class | Type | Needs native lib |
| --- | --- | :---: |
| `o.a.l.analysis.vi.TestVietnameseAnalyzer` | Unit — stop-set loading & analyzer construction | No |
| `o.e.analysis.VietnameseConfigTest` | Unit — settings parsing | No |
| `o.e.index.analysis.VietnameseAnalysisTests` | ES single-node — tokenization & factory wiring | Yes |
| `o.e.index.analysis.VietnameseAnalysisIntegrationTests` | ES integration — full cluster, search, analyze API | Yes |

### Unit tests (no native library)

These run fast and are safe to execute in any CI environment without the CocCoc library:

- **`TestVietnameseAnalyzer`** — verifies that `WordlistLoader.getWordSet` correctly loads `stopwords.txt` (the replacement for the Lucene 10 / ES 9.x incompatible `loadStopwordSet` overload), checks known stop words are present, and confirms the analyzer constructs cleanly with default and custom stop-word sets.

- **`VietnameseConfigTest`** — verifies that `VietnameseConfig` reads all four settings (`dict_path`, `keep_punctuation`, `split_url`, `split_host`) and applies correct defaults when settings are absent.

### Single-node tests (native library required)

`VietnameseAnalysisTests` wires up a single Elasticsearch node via `ESSingleNodeTestCase` and exercises:

- Plugin registration (analyzer/tokenizer/filter names resolve correctly).
- End-to-end tokenization of Vietnamese text.
- Custom analyzer configuration via `Settings`.

### Integration tests (native library required)

`VietnameseAnalysisIntegrationTests` spins up a mini Elasticsearch cluster via `ESIntegTestCase` and exercises:

- Plugin loading verification via the Nodes Info API.
- The `_analyze` API with the `vi_analyzer`.
- Full index/search round-trip with a Vietnamese text field.

---

## Writing New Tests

### Unit tests (no native library)

Extend `org.elasticsearch.test.ESTestCase`:

```java
public class MyTest extends ESTestCase {
    public void testSomething() {
        // standard JUnit-style assertions via ESTestCase helpers
        assertEquals("expected", "actual");
        assertTrue(someCondition);
    }
}
```

### Tokenization tests (native library required)

Extend `org.elasticsearch.test.ESSingleNodeTestCase` and use `AnalysisTestsHelper.createTestAnalysisFromSettings` to get a fully-wired `TestAnalysis` object. Use Lucene's `BaseTokenStreamTestCase.assertTokenStreamContents` for token-level assertions.

```java
public class MyTokenizerTest extends ESSingleNodeTestCase {
    public void testTokenization() throws IOException {
        TestAnalysis analysis = AnalysisTestsHelper.createTestAnalysisFromSettings(
                Settings.builder()
                        .put(IndexMetadata.SETTING_VERSION_CREATED, IndexVersion.current())
                        .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
                        .build(),
                new AnalysisVietnamesePlugin()
        );
        Tokenizer tokenizer = analysis.tokenizer.get("vi_tokenizer").create();
        tokenizer.setReader(new StringReader("công nghệ thông tin"));
        assertTokenStreamContents(tokenizer, new String[]{"công nghệ", "thông tin"});
    }
}
```

### Integration tests (native library + cluster)

Extend `org.elasticsearch.test.ESIntegTestCase`, annotate with `@ClusterScope`, and override `nodePlugins()`:

```java
@ClusterScope(supportsDedicatedMasters = false, numDataNodes = 1, numClientNodes = 0)
public class MyIntegTest extends ESIntegTestCase {
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singleton(AnalysisVietnamesePlugin.class);
    }

    public void testSomething() {
        // use client() to interact with the cluster
    }
}
```

---

## Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| `UnsatisfiedLinkError: no coccoc_tokenizer_jni` / tests skipped with "Requires the CocCoc native library" | Native library not on JVM's library path | Follow the [install steps](#3-coccoc-tokenizer-native-library-required-for-tokenization-tests) above; `VietnameseAnalysisTests` and `VietnameseAnalysisIntegrationTests` skip automatically when the library is absent |
| `RuntimeException: Unable to load default stopword set` | `stopwords.txt` missing from classpath | Ensure `src/main/resources/org/apache/lucene/analysis/vi/stopwords.txt` exists and run `mvn compile` |
| Tests not discovered / 0 tests run | Test class names don't match surefire's default pattern | Ensure test classes end with `Test`, `Tests`, or `TestCase` |
