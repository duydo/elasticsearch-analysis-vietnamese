package org.elasticsearch.analysis;

import org.elasticsearch.common.settings.Settings;

public class VietnameseConfig {
    public static final String DEFAULT_DICT_PATH = "/usr/local/share/tokenizer/dicts";
    public final String dictPath;
    public final boolean keepPunctuation;
    public final boolean splitHost;
    public final boolean splitURL;


    public VietnameseConfig(Settings settings) {
        dictPath = settings.get("dict_path", DEFAULT_DICT_PATH);
        keepPunctuation = settings.getAsBoolean("keep_punctuation", false);
        splitHost = settings.getAsBoolean("split_host", false);
        splitURL = settings.getAsBoolean("split_url", false);
    }

}
