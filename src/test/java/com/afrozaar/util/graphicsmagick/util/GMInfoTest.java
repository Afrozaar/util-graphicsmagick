package com.afrozaar.util.graphicsmagick.util;

import org.junit.Test;

import java.io.IOException;

/**
 * @author johan
 */
public class GMInfoTest {

    @Test
    public void getEnvInfo() throws IOException {
        System.out.println("new GMInfo().getEnvironmentInfo() = \n" + GMInfo.getEnvironmentInfo());
    }

}