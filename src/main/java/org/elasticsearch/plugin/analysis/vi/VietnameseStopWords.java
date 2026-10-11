/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.elasticsearch.plugin.analysis.vi;

import org.apache.lucene.analysis.CharArraySet;
import org.elasticsearch.plugin.analysis.vi.lucene.VietnameseAnalyzer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Resolves the {@code stopwords}, {@code stopwords_path} and {@code ignore_case} settings shared by
 * {@code vi_analyzer} and {@code vi_stop}, following the semantics of Elasticsearch's built-in stop filter:
 * {@code stopwords} wins over {@code stopwords_path}, which wins over the built-in list. The named list
 * {@code _vi_} (alias {@code _vietnamese_}) refers to the built-in list and {@code _none_} disables stop words.
 *
 * <p>Stop words are resolved when an analyzer or filter is created rather than in the factory constructors:
 * Elasticsearch wraps constructor exceptions into an HTTP 500 error, while exceptions thrown on creation are
 * reported to the user as invalid settings.
 */
final class VietnameseStopWords {

    static final String NONE = "_none_";
    private static final Set<String> NAMED_DEFAULT = Set.of("_vi_", "_vietnamese_");

    static CharArraySet resolve(VietnameseAnalysisSettings settings) {
        CharArraySet stopWords = resolveWords(settings);
        // The built-in list is case-sensitive, so it has to be copied to honour ignore_case
        return settings.ignoreCase() ? CharArraySet.unmodifiableSet(new CharArraySet(stopWords, true)) : stopWords;
    }

    private static CharArraySet resolveWords(VietnameseAnalysisSettings settings) {
        List<String> words = settings.stopwords();
        if (words.isEmpty() == false) {
            return words.size() == 1 && NONE.equals(words.get(0)) ? CharArraySet.EMPTY_SET : expandNamedLists(words);
        }
        if (settings.stopwordsPath().isEmpty() == false) {
            return expandNamedLists(readWordList(settings.stopwordsPath()));
        }
        return VietnameseAnalyzer.getDefaultStopSet();
    }

    private static CharArraySet expandNamedLists(List<String> words) {
        CharArraySet set = new CharArraySet(words.size(), false);
        for (String word : words) {
            if (NAMED_DEFAULT.contains(word)) {
                set.addAll(VietnameseAnalyzer.getDefaultStopSet());
            } else {
                set.add(word);
            }
        }
        return CharArraySet.unmodifiableSet(set);
    }

    /** The plugin is only entitled to read files in the config directory, so reject other paths with a clear message. */
    private static Path resolveInConfigDir(String path) {
        Path configDir = configDir().toAbsolutePath().normalize();
        Path file = configDir.resolve(path).normalize();
        if (file.startsWith(configDir) == false) {
            throw new IllegalArgumentException(
                "stopwords_path [" + path + "] must be inside the Elasticsearch config directory [" + configDir + "]");
        }
        return file;
    }

    /** Reads one word per line, skipping blank lines and {@code #} comments, like Elasticsearch's word list files. */
    private static List<String> readWordList(String path) {
        Path file = resolveInConfigDir(path);
        try {
            List<String> words = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String word = line.strip();
                if (word.isEmpty() == false && word.startsWith("#") == false) {
                    words.add(word);
                }
            }
            return words;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read stopwords_path [" + path + "] at [" + file + "]", e);
        }
    }

    /**
     * The stable plugin API does not expose the node environment, so the config directory is located the same way the
     * Elasticsearch launcher does: {@code ES_PATH_CONF}, falling back to {@code $ES_HOME/config}.
     */
    static Path configDir() {
        String pathConf = System.getenv("ES_PATH_CONF");
        if (pathConf != null && pathConf.isEmpty() == false) {
            return Path.of(pathConf);
        }
        String home = System.getProperty("es.path.home");
        return home != null ? Path.of(home, "config") : Path.of("config");
    }

    private VietnameseStopWords() {
    }
}
