package org.gbif.pipelines.core.interpreters.core;

import java.util.HashMap;
import java.util.Map;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.pipelines.io.avro.BasicRecord;
import org.gbif.pipelines.io.avro.ExtendedRecord;

import org.junit.Assert;
import org.junit.Test;

public class BasicInterpreterTest {

  private static final String ID = "777";

  @Test
  public void interpretSampleSizeValueTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.sampleSizeValue.qualifiedName(), " value ");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretSampleSizeValue(er, br);

    // Should
    Assert.assertEquals("value", br.getSampleSizeValue());
  }

  @Test
  public void interpretSampleSizeUnitTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.sampleSizeUnit.qualifiedName(), " value ");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretSampleSizeUnit(er, br);

    // Should
    Assert.assertEquals("value", br.getSampleSizeUnit());
  }

  @Test
  public void interpretOrganismQuantityTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.organismQuantity.qualifiedName(), " value ");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantity(er, br);

    // Should
    Assert.assertEquals("value", br.getOrganismQuantity());
  }

  @Test
  public void interpretOrganismQuantityTypeTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.organismQuantityType.qualifiedName(), " value ");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantityType(er, br);

    // Should
    Assert.assertEquals("value", br.getOrganismQuantityType());
  }

  @Test
  public void interpretRelativeOrganismQuantityTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.sampleSizeValue.qualifiedName(), "2");
    coreMap.put(DwcTerm.sampleSizeUnit.qualifiedName(), "some type ");
    coreMap.put(DwcTerm.organismQuantity.qualifiedName(), "10");
    coreMap.put(DwcTerm.organismQuantityType.qualifiedName(), " Some Type");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantityType(er, br);
    BasicInterpreter.interpretOrganismQuantity(er, br);
    BasicInterpreter.interpretSampleSizeUnit(er, br);
    BasicInterpreter.interpretSampleSizeValue(er, br);

    BasicInterpreter.interpretRelativeOrganismQuantity(er, br);

    // Should
    Assert.assertEquals(Double.valueOf(5d), br.getRelativeOrganismQuantity());
  }

  @Test
  public void interpretRelativeOrganismQuantityEmptySamplingTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.organismQuantity.qualifiedName(), "10");
    coreMap.put(DwcTerm.organismQuantityType.qualifiedName(), " Some Type");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantityType(er, br);
    BasicInterpreter.interpretOrganismQuantity(er, br);
    BasicInterpreter.interpretSampleSizeUnit(er, br);
    BasicInterpreter.interpretSampleSizeValue(er, br);

    BasicInterpreter.interpretRelativeOrganismQuantity(er, br);

    // Should
    Assert.assertNull(br.getRelativeOrganismQuantity());
  }

  @Test
  public void interpretRelativeOrganismQuantityEmptyOrganismTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.sampleSizeValue.qualifiedName(), "2");
    coreMap.put(DwcTerm.sampleSizeUnit.qualifiedName(), "some type ");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantityType(er, br);
    BasicInterpreter.interpretOrganismQuantity(er, br);
    BasicInterpreter.interpretSampleSizeUnit(er, br);
    BasicInterpreter.interpretSampleSizeValue(er, br);

    BasicInterpreter.interpretRelativeOrganismQuantity(er, br);

    // Should
    Assert.assertNull(br.getRelativeOrganismQuantity());
  }


  @Test
  public void interpretRelativeOrganismQuantityDiffTypesTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.sampleSizeValue.qualifiedName(), "2");
    coreMap.put(DwcTerm.sampleSizeUnit.qualifiedName(), "another type ");
    coreMap.put(DwcTerm.organismQuantity.qualifiedName(), "10");
    coreMap.put(DwcTerm.organismQuantityType.qualifiedName(), " Some Type");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantityType(er, br);
    BasicInterpreter.interpretOrganismQuantity(er, br);
    BasicInterpreter.interpretSampleSizeUnit(er, br);
    BasicInterpreter.interpretSampleSizeValue(er, br);

    BasicInterpreter.interpretRelativeOrganismQuantity(er, br);

    // Should
    Assert.assertNull(br.getRelativeOrganismQuantity());
  }

  @Test
  public void interpretRelativeOrganismQuantityNanTest() {

    // State
    Map<String, String> coreMap = new HashMap<>();
    coreMap.put(DwcTerm.sampleSizeValue.qualifiedName(), Double.valueOf("9E99999999").toString());
    coreMap.put(DwcTerm.sampleSizeUnit.qualifiedName(), "another type ");
    coreMap.put(DwcTerm.organismQuantity.qualifiedName(), "10");
    coreMap.put(DwcTerm.organismQuantityType.qualifiedName(), " Some Type");
    ExtendedRecord er = ExtendedRecord.newBuilder().setId(ID).setCoreTerms(coreMap).build();

    BasicRecord br = BasicRecord.newBuilder().setId(ID).build();

    // When
    BasicInterpreter.interpretOrganismQuantityType(er, br);
    BasicInterpreter.interpretOrganismQuantity(er, br);
    BasicInterpreter.interpretSampleSizeUnit(er, br);
    BasicInterpreter.interpretSampleSizeValue(er, br);

    BasicInterpreter.interpretRelativeOrganismQuantity(er, br);

    // Should
    Assert.assertNull(br.getRelativeOrganismQuantity());
  }

}