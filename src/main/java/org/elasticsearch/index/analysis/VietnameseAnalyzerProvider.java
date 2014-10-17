package org.elasticsearch.index.analysis;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.lucene.analysis.vi.VietnameseAnalyzer;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * @author duydo
 */
public class VietnameseAnalyzerProvider extends AbstractIndexAnalyzerProvider<VietnameseAnalyzer> {
    private final VietnameseAnalyzer analyzer;

    @Inject
    public VietnameseAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, String name, Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new VietnameseAnalyzer(version);
    }
    @Override
    public VietnameseAnalyzer get() {
        return analyzer;
    }
}
