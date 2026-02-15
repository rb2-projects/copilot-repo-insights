package com.rb.repoinsight.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ExternalDependency model class.
 */
class ExternalDependencyTest {

    @Test
    void testExternalDependencyCreationWithStringCategory() {
        ExternalDependency dep = new ExternalDependency("Spring Boot", "Framework", "pom.xml");
        
        assertEquals("Spring Boot", dep.getName());
        assertNotNull(dep.getCategory());
        assertEquals("pom.xml", dep.getEvidence());
    }

    @Test
    void testExternalDependencyCreationWithEnumCategory() {
        ExternalDependency dep = new ExternalDependency("PostgreSQL", DependencyCategory.PERSISTENCE, "pom.xml");
        
        assertEquals("PostgreSQL", dep.getName());
        assertNotNull(dep.getCategory());
        assertEquals("pom.xml", dep.getEvidence());
        assertEquals(DependencyCategory.PERSISTENCE, dep.getCategoryEnum());
    }

    @Test
    void testExternalDependencyWithDifferentCategories() {
        ExternalDependency db = new ExternalDependency("MySQL", "Persistence", "pom.xml");
        ExternalDependency cache = new ExternalDependency("Redis", "Caching", "pom.xml");
        
        assertNotNull(db.getCategory());
        assertNotNull(cache.getCategory());
    }

    @Test
    void testExternalDependencyEvidenceField() {
        String evidence = "dependency found in pom.xml with name 'spring-boot-starter'";
        ExternalDependency dep = new ExternalDependency("Spring Boot", "Framework", evidence);
        
        assertEquals(evidence, dep.getEvidence());
    }

    @Test
    void testExternalDependencyFromStringCategory() {
        ExternalDependency messaging = new ExternalDependency("RabbitMQ", "Messaging", "pom.xml");
        assertEquals("Messaging", messaging.getCategory());
        assertEquals(DependencyCategory.MESSAGING, messaging.getCategoryEnum());
    }

    @Test
    void testExternalDependencyUnknownCategory() {
        ExternalDependency unknown = new ExternalDependency("CustomLib", "UnknownCategory", "pom.xml");
        assertEquals("Unknown", unknown.getCategory());
        assertEquals(DependencyCategory.UNKNOWN, unknown.getCategoryEnum());
    }
}
