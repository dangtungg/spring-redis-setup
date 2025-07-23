package com.example.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@Slf4j
public final class JsonUtils {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * <code>JsonUtils</code> should not normally be instantiated.
     */
    private JsonUtils() {
    }

    public static ArrayNode createArrayNode(Collection<String> collection) {
        var node = MAPPER.createArrayNode();
        collection.forEach(item -> node.add(TextNode.valueOf(item)));
        return node;
    }

    public static <O> String toJson(O o) {
        return toJson(o, false);
    }

    public static <O> String toJson(O o, boolean defaultError) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            log.warn("[JsonUtils#toJson] :: {}", e.getMessage(), e);
            if (defaultError) {
                return "{\"error\":\"Error serializing response\"}";
            }
            return "{}";
        }
    }

    public static <T> T toObject(String jsonStr, final Class<T> clazz) {
        try {
            return MAPPER.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.warn("[JsonUtils#toObject] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T toObject(JsonNode jsonNode, final Class<T> clazz) {
        try {
            return MAPPER.convertValue(jsonNode, clazz);
        } catch (Exception e) {
            log.warn("[JsonUtils#toObject] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T toObject(String jsonStr, final TypeReference<T> reference) {
        try {
            return MAPPER.readValue(jsonStr, reference);
        } catch (Exception e) {
            log.warn("[JsonUtils#toObject] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T toObject(Object obj, final TypeReference<T> reference) {
        try {
            return MAPPER.convertValue(obj, reference);
        } catch (Exception e) {
            log.warn("[JsonUtils#toObject] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T toObject(JsonNode jsonNode, final TypeReference<T> reference) {
        try {
            return MAPPER.convertValue(jsonNode, reference);
        } catch (Exception e) {
            log.warn("[JsonUtils#toObject] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T toObject(Object source, Class<?> mixinTarget, Class<?> mixinSource, final TypeReference<T> reference) {
        try {
            MAPPER.addMixIn(mixinTarget, mixinSource);
            return MAPPER.convertValue(source, reference);
        } catch (Exception e) {
            log.warn("[JsonUtils#toObject] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T toObjectFromJsonResource(String filePath, final Class<T> clazz) {
        try {
            InputStream inJson = clazz.getResourceAsStream(filePath);
            return MAPPER.readValue(inJson, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load json resource [%s]".formatted(filePath));
        }
    }

    public static <T> T toObjectFromJsonResource(Resource resource, final Class<T> clazz) {
        try {
            InputStream inJson = resource.getInputStream();
            return MAPPER.readValue(inJson, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load json resource [%s]".formatted(resource.getFilename()));
        }
    }

    public static <T> T toObjectFromJsonResource(String filePath, final TypeReference<T> reference) {
        try {
            InputStream inJson = reference.getClass().getResourceAsStream(filePath);
            return MAPPER.readValue(inJson, reference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load json resource [%s]".formatted(filePath));
        }
    }

    public static <T> T toObjectFromJsonResource(Resource resource, final TypeReference<T> reference) {
        try {
            InputStream inJson = resource.getInputStream();
            return MAPPER.readValue(inJson, reference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load json resource [%s]".formatted(resource.getFilename()));
        }
    }

    public static JsonNode toJsonNode(String jsonStr) {
        try {
            return MAPPER.readTree(jsonStr);
        } catch (Exception e) {
            log.warn("[JsonUtils#toJsonNode] :: {}", e.getMessage(), e);
            return null;
        }
    }

    public static JsonNode toJsonNode(Object obj) {
        try {
            return MAPPER.valueToTree(obj);
        } catch (Exception e) {
            log.warn("[JsonUtils#toJsonNode] :: {}", e.getMessage(), e);
            return null;
        }
    }

}
