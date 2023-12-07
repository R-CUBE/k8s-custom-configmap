package com.rcube.configmap;

import org.apache.commons.lang3.tuple.Pair;

public class TestDataUtil {

    public static Pair<String, String> validSchemaData() {
        String schema = """
                 {
                   "type": "object",
                   "properties": {
                     "startNewFlow": {
                       "type": "boolean"
                     }
                   },
                   "required": [
                     "startNewFlow"
                   ]
                 }
                """;

        String data = """
                {
                      "startNewFlow": true
                 }
                """;
        return Pair.of(schema, data);
    }

    public static Pair<String, String> validSchemaInvalidData() {
        String schema = """
                 {
                   "type": "object",
                   "properties": {
                     "startNewFlow": {
                       "type": "boolean"
                     }
                   },
                   "required": [
                     "startNewFlow"
                   ]
                 }
                """;

        String data = """
                {
                      "startNewFlow": "true"
                 }
                """;
        return Pair.of(schema, data);
    }
}
