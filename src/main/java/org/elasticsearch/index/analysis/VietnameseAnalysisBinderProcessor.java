package org.elasticsearch.index.analysis;

/**
 * @author duydo
 */
public class VietnameseAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {
    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("vi_analyzer", VietnameseAnalyzerProvider.class);
    }

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("vi_tokenizer", VietnameseTokenizerFactory.class);
    }
}
