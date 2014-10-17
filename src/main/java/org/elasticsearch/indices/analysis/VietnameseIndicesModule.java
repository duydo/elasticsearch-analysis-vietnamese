package org.elasticsearch.indices.analysis;

import org.elasticsearch.common.inject.AbstractModule;

/**
 * @author duydo
 */
public class VietnameseIndicesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(VietnameseIndicesAnalysis.class).asEagerSingleton();
    }
}
