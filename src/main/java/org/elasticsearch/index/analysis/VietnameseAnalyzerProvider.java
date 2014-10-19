package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * @author duydo
 */
public class VietnameseAnalyzerProvider extends AbstractIndexAnalyzerProvider<VietnameseAnalyzer> {
    private final VietnameseAnalyzer analyzer;

    @Inject
    public VietnameseAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
                                      @Assisted String name,
                                      @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new VietnameseAnalyzer(version,
                Analysis.parseStopWords(env, settings, VietnameseAnalyzer.getDefaultStopSet(), version)
        );
    }

    @Override
    public VietnameseAnalyzer get() {
        return analyzer;
    }
}
