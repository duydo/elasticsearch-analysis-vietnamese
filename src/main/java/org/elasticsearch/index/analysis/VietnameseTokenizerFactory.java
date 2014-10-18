package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

/**
 * @author duydo
 */
public class VietnameseTokenizerFactory extends AbstractTokenizerFactory {

    private boolean useSentenceDetector = true;
    private boolean useAmbiguitiesResolved = false;

    @Inject
    public VietnameseTokenizerFactory(Index index, @IndexSettings Settings indexSettings, String name, Settings settings) {
        super(index, indexSettings, name, settings);
        useSentenceDetector = settings.getAsBoolean("use_sentence_detector", Boolean.TRUE);
        useAmbiguitiesResolved = settings.getAsBoolean("use_ambiguities_resolved", Boolean.FALSE);
    }

    @Override
    public Tokenizer create(Reader reader) {
        return new VietnameseTokenizer(reader, useSentenceDetector, useAmbiguitiesResolved);
    }
}
