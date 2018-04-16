package org.gbif.pipelines.config;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Defines the types of the interpretations.
 */
public enum InterpretationType {

  ALL, LOCATION, TEMPORAL, TAXONOMY, COMMON;

  public static final List<InterpretationType> ALL_INTERPRETATIONS =
    ImmutableList.of(LOCATION, TEMPORAL, TAXONOMY, COMMON);

}