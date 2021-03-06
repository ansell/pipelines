package org.gbif.converters.parser.xml.parsing.xml.rules;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.gbif.api.vocabulary.OccurrenceSchemaType;
import org.gbif.converters.parser.xml.constants.PrioritizedPropertyNameEnum;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;

public class DwcManisRuleSet extends AbstractDwcRuleSet implements RuleSet {

  private static final String MAPPING_FILE = "mapping/indexMapping_manis_dwc.properties";

  public DwcManisRuleSet() throws IOException {
    mappingProps = new Properties();
    URL url = ClassLoader.getSystemResource(MAPPING_FILE);
    mappingProps.load(url.openStream());
  }

  @Override
  public String getNamespaceURI() {
    return OccurrenceSchemaType.DWC_MANIS.toString();
  }

  @Override
  public void addRuleInstances(Digester digester) {
    super.addRuleInstances(digester);

    addNonNullPrioritizedProperty(
        digester, "catalogueNumber", PrioritizedPropertyNameEnum.CATALOGUE_NUMBER, 2);
    addNonNullPrioritizedProperty(digester, "latitude", PrioritizedPropertyNameEnum.LATITUDE, 2);
    addNonNullPrioritizedProperty(digester, "longitude", PrioritizedPropertyNameEnum.LONGITUDE, 2);

    addNonNullMethod(digester, "continentOrOcean", "setContinentOrOcean", 1);
    addNonNullParam(digester, "continentOrOcean", 0);

    addNonNullMethod(digester, "year", "setYear", 1);
    addNonNullParam(digester, "year", 0);

    addNonNullMethod(digester, "month", "setMonth", 1);
    addNonNullParam(digester, "month", 0);

    addNonNullMethod(digester, "day", "setDay", 1);
    addNonNullParam(digester, "day", 0);

    addNonNullMethod(digester, "yearIdentified", "setYearIdentified", 1);
    addNonNullParam(digester, "yearIdentified", 0);

    addNonNullMethod(digester, "monthIdentified", "setMonthIdentified", 1);
    addNonNullParam(digester, "monthIdentified", 0);

    addNonNullMethod(digester, "dayIdentified", "setDayIdentified", 1);
    addNonNullParam(digester, "dayIdentified", 0);
  }
}
