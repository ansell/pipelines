package org.gbif.pipelines.assembling.interpretation;

import org.gbif.pipelines.assembling.interpretation.steps.InterpretationStepSupplier;
import org.gbif.pipelines.assembling.interpretation.steps.PipelineTargetPaths;
import org.gbif.pipelines.assembling.utils.HdfsUtils;
import org.gbif.pipelines.config.DataProcessingPipelineOptions;
import org.gbif.pipelines.config.InterpretationType;
import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.transform.validator.UniqueOccurrenceIdTransform;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;

import static org.gbif.pipelines.assembling.interpretation.steps.InterpretationStepSupplier.commonGbif;
import static org.gbif.pipelines.assembling.interpretation.steps.InterpretationStepSupplier.locationGbif;
import static org.gbif.pipelines.assembling.interpretation.steps.InterpretationStepSupplier.taxonomyGbif;
import static org.gbif.pipelines.assembling.interpretation.steps.InterpretationStepSupplier.temporalGbif;
import static org.gbif.pipelines.config.InterpretationType.COMMON;
import static org.gbif.pipelines.config.InterpretationType.LOCATION;
import static org.gbif.pipelines.config.InterpretationType.TAXONOMY;
import static org.gbif.pipelines.config.InterpretationType.TEMPORAL;

/**
 * Gbif implementation for a {@link InterpretationPipeline}.
 */
public class GbifInterpretationPipeline implements InterpretationPipeline {

  private static final String DATA_FILENAME = "interpreted";
  private static final String ISSUES_FOLDER = "issues";
  private static final String ISSUES_FILENAME = "issues";

  private final EnumMap<InterpretationType, InterpretationStepSupplier> stepsMap =
    new EnumMap<>(InterpretationType.class);
  private final DataProcessingPipelineOptions options;
  private final Function<InterpretationType, PipelineTargetPaths> pathsGenerator;

  private GbifInterpretationPipeline(DataProcessingPipelineOptions options) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(options.getDatasetId()), "datasetId is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(options.getDefaultTargetDirectory()),
                                "defaultTargetDirectory " + "is required");
    Preconditions.checkArgument(options.getHdfsConfiguration() != null && !options.getHdfsConfiguration().isEmpty(),
                                "HDFS configuration is required");
    this.options = options;
    this.pathsGenerator = createPathsGenerator(options);
    initStepsMap();
  }

  /**
   * Creates a new {@link GbifInterpretationPipeline} instance from the {@link DataProcessingPipelineOptions} received.
   */
  public static GbifInterpretationPipeline newInstance(DataProcessingPipelineOptions options) {
    return new GbifInterpretationPipeline(options);
  }

  private void initStepsMap() {
    Objects.requireNonNull(pathsGenerator);
    stepsMap.put(LOCATION, locationGbif(pathsGenerator));
    stepsMap.put(TEMPORAL, temporalGbif(pathsGenerator));
    stepsMap.put(TAXONOMY, taxonomyGbif(pathsGenerator));
    stepsMap.put(COMMON, commonGbif(pathsGenerator));
  }

  @Override
  public Pipeline createPipeline() {
    return InterpretationPipelineAssembler.of(options.getInterpretationTypes())
      .withOptions(options)
      .withInput(options.getInputFile())
      .using(stepsMap)
      .onBeforeInterpretations(createBeforeStep())
      .assemble();
  }

  private BiFunction<PCollection<ExtendedRecord>, Pipeline, PCollection<ExtendedRecord>> createBeforeStep() {
    return (PCollection<ExtendedRecord> verbatimRecords, Pipeline pipeline) -> {
      UniqueOccurrenceIdTransform uniquenessTransform = UniqueOccurrenceIdTransform.create().withAvroCoders(pipeline);
      PCollectionTuple uniqueTuple = verbatimRecords.apply(uniquenessTransform);
      return uniqueTuple.get(uniquenessTransform.getDataTag());
    };

  }

  private static Function<InterpretationType, PipelineTargetPaths> createPathsGenerator(
    DataProcessingPipelineOptions options
  ) {
    return (interpretationType) -> {
      PipelineTargetPaths paths = new PipelineTargetPaths();
      paths.setDataTargetPath(HdfsUtils.getHdfsPath(options.getHdfsConfiguration().get(0),
                                                    options.getDefaultTargetDirectory(),
                                                    options.getDatasetId(),
                                                    interpretationType.name().toLowerCase(),
                                                    DATA_FILENAME));

      paths.setIssuesTargetPath(HdfsUtils.getHdfsPath(options.getHdfsConfiguration().get(0),
                                                      options.getDefaultTargetDirectory(),
                                                      options.getDatasetId(),
                                                      interpretationType.name().toLowerCase(),
                                                      ISSUES_FOLDER,
                                                      ISSUES_FILENAME));

      paths.setTempDir(options.getHdfsTempLocation());

      return paths;
    };
  }

}
