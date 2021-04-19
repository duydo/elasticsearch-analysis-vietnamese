package org.apache.lucene.analysis.vi;

import com.coccoc.Token;
import com.coccoc.Tokenizer;
import com.coccoc.Tokenizer.TokenizeOption;

import com.google.common.io.CharStreams;
import org.elasticsearch.analysis.VietnameseConfig;

import java.io.IOException;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

class VietnameseTokenizerImpl {
    private final Tokenizer tokenizer;
    private final VietnameseConfig config;
    private final TokenizeOption option;

    public VietnameseTokenizerImpl(VietnameseConfig config) {
        this.config = config;
        if (config.keepURL) {
            option = TokenizeOption.URL;
        } else if (config.keepHost) {
            option = TokenizeOption.HOST;
        } else {
            option = TokenizeOption.NORMAL;
        }
        tokenizer = AccessController.doPrivileged(
                (PrivilegedAction<Tokenizer>) () -> new Tokenizer(config.dictPath)
        );
    }


    public List<Token> tokenize(Reader input) throws IOException {
        return tokenize(CharStreams.toString(input));
    }


    public List<Token> tokenize(String input) throws IOException {
        return AccessController.doPrivileged(
                (PrivilegedAction<List<Token>>) () -> tokenizer.segment(input, option, config.keepPunctuation)
        );
    }
}
