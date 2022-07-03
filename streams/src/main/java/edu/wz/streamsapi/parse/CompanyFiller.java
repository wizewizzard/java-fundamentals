package edu.wz.streamsapi.parse;

import com.fasterxml.jackson.databind.JsonNode;
import edu.wz.streamsapi.domain.Company;

import java.util.function.BiConsumer;

public class CompanyFiller implements BiConsumer<JsonNode, Company> {
    @Override
    public void accept(JsonNode companyNode, Company company) {
        company.setId(companyNode.get("_id").asText());
        company.setName(companyNode.get("name").asText());
        company.setAddress(companyNode.get("address").asText());
        company.setAbout(companyNode.get("about").asText());
        company.setLatitude(companyNode.get("longitude").asDouble());
        company.setLongitude(companyNode.get("longitude").asDouble());
    }
}
