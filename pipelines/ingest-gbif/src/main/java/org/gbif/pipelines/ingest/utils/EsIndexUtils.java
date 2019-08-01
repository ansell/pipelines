package org.gbif.pipelines.ingest.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.gbif.pipelines.estools.EsIndex;
import org.gbif.pipelines.estools.client.EsConfig;
import org.gbif.pipelines.estools.model.IndexParams;
import org.gbif.pipelines.estools.service.EsConstants.Field;
import org.gbif.pipelines.estools.service.EsConstants.Indexing;
import org.gbif.pipelines.ingest.options.EsIndexingPipelineOptions;
import org.gbif.pipelines.parsers.config.LockConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EsIndexUtils {

  /** Connects to Elasticsearch instance and creates an index */
  public static void createIndex(EsIndexingPipelineOptions options) {
    EsConfig config = EsConfig.from(options.getEsHosts());

    String idx = EsIndex.createIndex(config, createIndexParams(options));
    log.info("ES index {} created", idx);

    Optional.ofNullable(idx).ifPresent(options::setEsIndexName);
  }

  /** Connects to Elasticsearch instance and creates an index, if index doesn't exist */
  public static void createIndexIfNotExist(EsIndexingPipelineOptions options) {
    EsConfig config = EsConfig.from(options.getEsHosts());
    Optional<String> idx = EsIndex.createIndexIfNotExists(config, createIndexParams(options));

    if (idx.isPresent()) {
      log.info("ES index {} created", idx.get());
      options.setEsIndexName(idx.get());
    }
  }

  private static IndexParams createIndexParams(EsIndexingPipelineOptions options) {
    Path mappingsPath = Paths.get(options.getEsSchemaPath());

    boolean independentIndex = options.getEsIndexName().startsWith(options.getDatasetId());

    Map<String, String> settings = new HashMap<>();
    settings.put(Field.INDEX_REFRESH_INTERVAL,
        independentIndex ? Indexing.REFRESH_INTERVAL : options.getIndexRefreshInterval());
    settings.put(Field.INDEX_NUMBER_SHARDS, options.getIndexNumberShards().toString());
    settings.put(Field.INDEX_NUMBER_REPLICAS,
        independentIndex ? Indexing.NUMBER_REPLICAS : options.getIndexNumberReplicas().toString());
    settings.put(Field.INDEX_ANALYSIS, Indexing.NORMALIZER);

    return IndexParams.builder()
        .indexName(options.getEsIndexName())
        .datasetKey(options.getDatasetId())
        .attempt(options.getAttempt())
        .pathMappings(mappingsPath)
        .settings(settings)
        .build();
  }

  /** Connects to Elasticsearch instance and swaps an index and an alias */
  public static void swapIndex(EsIndexingPipelineOptions options, LockConfig lockConfig) {
    EsConfig config = EsConfig.from(options.getEsHosts());

    Set<String> aliases = Sets.newHashSet(options.getEsAlias());
    String index = options.getEsIndexName();

    //Lock the index to avoid modifications and stop reads.
    SharedLockUtils.doInWriteLock(lockConfig, () -> {

      EsIndex.swapIndexInAliases(config, aliases, index);
      log.info("ES index {} added to alias {}", index, aliases);
    });
  }

  /** Connects to Elasticsearch instance and swaps an index and an alias, if alias exists */
  public static void swapIndexIfAliasExists(EsIndexingPipelineOptions options, LockConfig lockConfig) {
    String[] aliases = options.getEsAlias();
    if (aliases != null && aliases.length > 0) {
      swapIndex(options, lockConfig);
    }
  }

  /** Connects to Elasticsearch instance and swaps an index and an alias, if alias exists */
  public static void updateAlias(EsIndexingPipelineOptions options, Set<String> existingDatasetIndexes,
      LockConfig lockConfig) {
    Preconditions.checkArgument(options.getEsAlias() != null && options.getEsAlias().length > 0,
        "ES alias is required");
    Preconditions.checkArgument(existingDatasetIndexes != null, "The set with existing datasets cannot be null");

    EsConfig config = EsConfig.from(options.getEsHosts());

    String idxToAdd = options.getEsIndexName().startsWith(options.getDatasetId()) ? options.getEsIndexName() : null;

    Set<String> idxToRemove = existingDatasetIndexes.stream()
        .filter(i -> i.startsWith(options.getDatasetId()))
        .collect(Collectors.toSet());

    // we first check if there are indexes to swap to avoid unnecessary locks
    if (idxToAdd != null || !idxToRemove.isEmpty()) {
      Map<String, String> searchSettings = new HashMap<>();
      searchSettings.put(Field.INDEX_REFRESH_INTERVAL, options.getIndexRefreshInterval());
      searchSettings.put(Field.INDEX_NUMBER_REPLICAS, options.getIndexNumberReplicas().toString());

      SharedLockUtils.doInWriteLock(lockConfig, () ->
          EsIndex.swapIndexInAliases(config, Sets.newHashSet(options.getEsAlias()), idxToAdd, idxToRemove,
              searchSettings)
      );
    }
  }

  /**
   * Connects to Elasticsearch instance and deletes records in an index by datasetId and returns the indexes where the
   * dataset was present
   */
  public static Set<String> deleteRecordsByDatasetId(EsIndexingPipelineOptions options) {
    EsConfig config = EsConfig.from(options.getEsHosts());
    return EsIndex.deleteRecordsByDatasetId(config, options.getEsAlias(), options.getDatasetId(),
        idxName -> idxName.startsWith(options.getDatasetId()));
  }

}
