package org.elasticsearch.plugin.analysis.vi;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.VietnameseAnalysisBinderProcessor;
import org.elasticsearch.indices.analysis.VietnameseIndicesModule;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

/**
 * @author duydo
 */
public class AnalysisVietnamesePlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "analysis-vietnamese";
    }

    @Override
    public String description() {
        return "Vietnamese Analysis plugin";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        return ImmutableList.<Class<? extends Module>>of(VietnameseIndicesModule.class);
    }

    public void onModule(AnalysisModule module){
        module.addProcessor(new VietnameseAnalysisBinderProcessor());
    }
}
