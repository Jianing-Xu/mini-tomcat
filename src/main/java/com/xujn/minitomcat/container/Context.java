package com.xujn.minitomcat.container;

import com.xujn.minitomcat.mapper.Mapper;

/**
 * Web-application container responsible for choosing the target Wrapper.
 */
public interface Context extends Container {

    String getPath();

    Mapper getMapper();
}
