/**
 * 
 */
package net.rickcee.scm.devops.util;

import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rickcee
 *
 */
@Converter
@Slf4j
public class JsonAttributeConverter implements AttributeConverter<List<String>, String> {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		if (attribute == null) {
			return null;
		}

		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		try {
			return objectMapper.readValue(dbData, new ListTypeReference<List<String>>());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	class ListTypeReference<T> extends TypeReference<List<String>> {

	}

}
