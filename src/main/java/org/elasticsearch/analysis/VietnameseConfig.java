package org.elasticsearch.analysis;

import org.elasticsearch.common.settings.Settings;

public class VietnameseConfig {
    public static final String DEFAULT_DICT_PATH = "/usr/share/tokenizer/dicts";
    public final String dictPath;
    public final Boolean keepPunctuation;
    public final Boolean splitHost;
    public final Boolean splitURL;


    public VietnameseConfig(Settings settings) {
        dictPath = settings.get("dict_path", DEFAULT_DICT_PATH);
        keepPunctuation = settings.getAsBoolean("keep_punctuation", Boolean.FALSE);
        splitHost = settings.getAsBoolean("split_host", Boolean.FALSE);
        splitURL = settings.getAsBoolean("split_url", Boolean.FALSE);
    }

}
