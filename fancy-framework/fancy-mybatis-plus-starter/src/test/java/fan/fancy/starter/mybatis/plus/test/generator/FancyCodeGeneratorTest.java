package fan.fancy.starter.mybatis.plus.test.generator;

import fan.fancy.starter.mybatis.plus.generator.FancyCodeGenerator;
import fan.fancy.starter.mybatis.plus.properties.FancyCodeProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/**
 * {@link FancyCodeGenerator} 测试类.
 *
 * @author Fan
 */
class FancyCodeGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void generate_validProperties_executesSuccessfully() {
        FancyCodeProperties properties = FancyCodeProperties.builder()
                .url("jdbc:h2:mem:test_db")
                .username("sa")
                .password("")
                .outputDir(tempDir.toString())
                .parent("com.example")
                .moduleName("test")
                .build();
        Assertions.assertDoesNotThrow(() -> FancyCodeGenerator.generate(properties, "user", "order"));
    }

    @Test
    void generate_nullUrl_throwsException() {
        FancyCodeProperties properties = FancyCodeProperties.builder()
                .username("root")
                .outputDir(tempDir.toString())
                .build();
        Assertions.assertThrows(NullPointerException.class, () -> FancyCodeGenerator.generate(properties, "user"));
    }

    @Test
    void generate_nullUsername_throwsException() {
        FancyCodeProperties properties = FancyCodeProperties.builder()
                .url("jdbc:h2:mem:test_db")
                .username(null)
                .outputDir(tempDir.toString())
                .build();
        Assertions.assertThrows(NullPointerException.class, () -> FancyCodeGenerator.generate(properties, "user"));
    }

    @Test
    void generate_nullOutputDir_throwsException() {
        FancyCodeProperties properties = FancyCodeProperties.builder()
                .url("jdbc:h2:mem:test_db")
                .username("root")
                .build();
        Assertions.assertThrows(NullPointerException.class, () -> FancyCodeGenerator.generate(properties, "user"));
    }

    @Test
    void generateAll_validProperties_executesSuccessfully() {
        FancyCodeProperties properties = FancyCodeProperties.builder()
                .url("jdbc:h2:mem:test_db")
                .username("sa")
                .password("")
                .outputDir(tempDir.toString())
                .parent("com.example")
                .moduleName("test")
                .tablePrefix("t_")
                .tableSuffix("_tab")
                .build();
        Assertions.assertDoesNotThrow(() -> FancyCodeGenerator.generateAll(properties));
    }
}
