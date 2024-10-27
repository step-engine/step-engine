package dk.ngr.step.engine.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
  private final ObjectMapper mapper = new ObjectMapper();

  public JsonUtil() {
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    mapper.registerModule(new Jdk8Module());
  }

  public String toString(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] toBytes(Object obj) {
    try {
      return mapper.writeValueAsString(obj).getBytes(StandardCharsets.UTF_8);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T toObject(String json, Class<T> cls) {
    try {
      return mapper.readValue(json, cls);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T toObjectFromJsonNode(JsonNode jsonNode, Class<T> cls) {
    try {
      return mapper.treeToValue(jsonNode, cls);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonNode toJsonNodeFromObject(Object obj) {
        return mapper.convertValue(obj, JsonNode.class);
    }

  public JsonNode toJsonNode(String json) {
    try {
      return mapper.readTree(json);
    } catch (IOException e)  {
      throw new RuntimeException(e);
    }
  }
}