package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

/**
 * @author duydo
 */
public class VietnameseTokenizerFactory extends AbstractTokenizerFactory {

    private final boolean sentenceDetectorEnabled;
    private final boolean ambiguitiesResolved;

    @Inject
    public VietnameseTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
                                      @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        sentenceDetectorEnabled = settings.getAsBoolean("sentence_detector", Boolean.TRUE);
        ambiguitiesResolved = settings.getAsBoolean("ambiguities_resolved", Boolean.FALSE);
    }

    @Override
    public Tokenizer create(Reader reader) {
        return new VietnameseTokenizer(reader, sentenceDetectorEnabled, ambiguitiesResolved);
    }
}
