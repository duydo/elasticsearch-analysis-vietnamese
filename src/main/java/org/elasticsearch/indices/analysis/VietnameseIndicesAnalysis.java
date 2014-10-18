package org.elasticsearch.indices.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.analysis.PreBuiltTokenizerFactoryFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;

import java.io.Reader;

/**
 * @author duydo
 */
public class VietnameseIndicesAnalysis extends AbstractComponent {
    @Inject
    public VietnameseIndicesAnalysis(Settings settings, IndicesAnalysisService indicesAnalysisService) {
        super(settings);
        indicesAnalysisService.analyzerProviderFactories().put("vi_analyzer",
                new PreBuiltAnalyzerProviderFactory("vi_analyzer",
                        AnalyzerScope.INDICES, new VietnameseAnalyzer(Lucene.ANALYZER_VERSION)
                )
        );
        indicesAnalysisService.tokenizerFactories().put("vi_tokenizer",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "vi_tokenizer";
                    }

                    @Override
                    public Tokenizer create(Reader reader) {
                        return new VietnameseTokenizer(reader);
                    }
                })
        );
    }
}
