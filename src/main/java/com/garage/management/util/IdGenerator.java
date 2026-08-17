package com.garage.management.util;

import java.util.UUID;

/**
 * Utility for generating unique identifiers for Firestore documents.
 */
public final class IdGenerator {

    private IdGenerator() {}

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generate(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
