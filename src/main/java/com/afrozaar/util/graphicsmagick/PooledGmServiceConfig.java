package com.afrozaar.util.graphicsmagick;

import com.afrozaar.util.graphicsmagick.util.RuntimeLimits;

import com.google.common.base.MoreObjects;

import org.gm4java.engine.GMService;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;

public class PooledGmServiceConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PooledGmServiceConfig.class);

    public static GMService getGraphicsMagickService() {

        GMConnectionPoolConfig config = new GMConnectionPoolConfig();

        if (RuntimeLimits.applyLimits()) {
            config.setMaxIdle(4);
            config.setMaxActive(4);
        }

        LOG.info("Initialised GraphicsMagickImageIO with config: {}", MoreObjects.toStringHelper(config)
                .add("maxIdle", config.getMaxIdle())
                .add("maxActive", config.getMaxActive())
                .toString());

        return new PooledGMService(config);
    }

}
