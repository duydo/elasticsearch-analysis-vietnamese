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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of Vietnamese Tokenizer.
 * It uses <a href="https://github.com/coccoc/coccoc-tokenizer">C++ Tokenizer</a> written by CocCoc team.
 *
 * @author duydo
 */
final class VietnameseTokenizerImpl {
    private final VietnameseConfig config;
    private final TokenizeOption option;
    private final Tokenizer tokenizer;
    private final List<Token> pending;
    private Reader input;
    private int pos = -1;

    VietnameseTokenizerImpl(VietnameseConfig config, Reader input) {
        this.config = config;
        this.input = input;
        if (config.splitURL) {
            option = TokenizeOption.URL;
        } else if (config.splitHost) {
            option = TokenizeOption.HOST;
        } else {
            option = TokenizeOption.NORMAL;
        }
        tokenizer = AccessController.doPrivileged(
                (PrivilegedAction<Tokenizer>) () -> new Tokenizer(config.dictPath)
        );
        pending = new CopyOnWriteArrayList<>();
    }

    public Token getNextToken() throws IOException {
        while (pending.size() == 0) {
            tokenize();
            if (pending.size() == 0) {
                return null;
            }
        }
        pos++;
        return pos < pending.size() ? pending.get(pos) : null;
    }

    public void reset(Reader input) {
        this.input = input;
        pending.clear();
        pos = -1;
    }


    private void tokenize() throws IOException {
        final List<Token> tokens = tokenize(input);
        if (tokens != null) {
            pending.addAll(tokens);
        }
    }

    private List<Token> tokenize(Reader input) throws IOException {
        return tokenize(CharStreams.toString(input));
    }


    private List<Token> tokenize(String input) {
        return AccessController.doPrivileged(
                (PrivilegedAction<List<Token>>) () -> tokenizer.segment(input, option, config.keepPunctuation)
        );
    }

}
