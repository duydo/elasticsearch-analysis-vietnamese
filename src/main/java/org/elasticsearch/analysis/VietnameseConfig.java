package org.elasticsearch.analysis;

import org.elasticsearch.common.settings.Settings;

public class VietnameseConfig {
    public final String dictPath;
    public final Boolean keepPunctuation;
    public final Boolean keepHost;
    public final Boolean keepURL;


    public VietnameseConfig(Settings settings) {
        dictPath = settings.get("dict_path", "/usr/local/share/tokenizer/dicts");
        keepPunctuation = settings.getAsBoolean("keep_punctuation", Boolean.FALSE);
        keepHost = settings.getAsBoolean("keep_host", Boolean.FALSE);
        keepURL = settings.getAsBoolean("keep_url", Boolean.FALSE);
    }

}
