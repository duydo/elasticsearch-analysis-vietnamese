package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

/**
 * @author duydo
 */
public class VietnameseTokenizerFactory extends AbstractTokenizerFactory {

    @Inject
    public VietnameseTokenizerFactory(Index index, @IndexSettings Settings indexSettings, String name, Settings settings) {
        super(index, indexSettings, name, settings);
    }

    @Override
    public Tokenizer create(Reader reader) {
        return new VietnameseTokenizer(reader);
    }
}
