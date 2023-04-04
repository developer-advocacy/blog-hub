package com.joshlong.blogs.contentful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public record BlogPost(Author author, String title, String category, String body) {

    public String slug() {
        var stringBuffer = new StringBuffer();
        var replacements = new HashMap<Predicate<Character>, Function<Character, String>>();
        replacements.put(Character::isWhitespace, c -> "-");
        replacements.put(c -> Character.isAlphabetic(c) || Character.isDigit(c), c -> Character.toString(c).toLowerCase(Locale.getDefault()));
        for (var c : this.title().toCharArray()) {
            for (var test : replacements.keySet())
                if (test.test(c)) stringBuffer.append(replacements.get(test).apply(c));
        }
        return stringBuffer.toString();
    }

    @SneakyThrows
    public JsonNode toJsonNode(ObjectMapper objectMapper) {
        var ow = objectMapper.createObjectNode();
        ow.put("category", singleFieldObjectNode(objectMapper, this.category));
        ow.put("title", singleFieldObjectNode(objectMapper, this.title));
        ow.put("title", singleFieldObjectNode(objectMapper, this.title));
        ow.put("slug", singleFieldObjectNode(objectMapper, this.slug()));
        ow.put("body", singleFieldObjectNode(objectMapper, this.body));
        var sys = Map.of("sys", Map.of("type", "Link", "linkType", "Entry", "id", this.author.id()));
        var enUs = Map.of("en-US", sys);
        var jsonForAuthor = objectMapper.writeValueAsString(enUs);
        ow.put("author", objectMapper.readTree(jsonForAuthor));
        var fields = objectMapper.createObjectNode();
        fields.put("fields", ow);
        return fields;
    }

    private ObjectNode singleFieldObjectNode(ObjectMapper objectMapper, String value) {
        var categoryON = objectMapper.createObjectNode();
        categoryON.put("en-US", value);
        return categoryON;
    }
}
