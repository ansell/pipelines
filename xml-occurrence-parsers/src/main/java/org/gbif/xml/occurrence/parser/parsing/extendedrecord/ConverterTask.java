package org.gbif.xml.occurrence.parser.parsing.extendedrecord;

import org.gbif.xml.occurrence.parser.OccurrenceParser;
import org.gbif.xml.occurrence.parser.ParsingException;
import org.gbif.xml.occurrence.parser.parsing.validators.UniquenessValidator;
import org.gbif.xml.occurrence.parser.parsing.xml.XmlFragmentParser;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The task for CompletableFuture which reads a xml response file, parses and converts to ExtendedRecord avro file
 */
public class ConverterTask implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ConverterTask.class);

  private final File inputFile;
  private final SyncDataFileWriter dataFileWriter;
  private final UniquenessValidator validator;

  public ConverterTask(File inputFile, SyncDataFileWriter dataFileWriter, UniquenessValidator validator) {
    this.inputFile = inputFile;
    this.dataFileWriter = dataFileWriter;
    this.validator = validator;
  }

  @Override
  public void run() {
    new OccurrenceParser().parseFile(inputFile).stream()
      .map(XmlFragmentParser::parseRecord)
      .forEach(rawRecords -> rawRecords.stream()
        .filter(rawOccurrenceRecord -> validator.isUnique(rawOccurrenceRecord.getId()))
        .forEach(rawRecord -> {
          try {
            dataFileWriter.append(ExtendedRecordConverter.from(rawRecord));
          } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ParsingException("Parsing failed", ex);
          }
        })
      );
  }

}
