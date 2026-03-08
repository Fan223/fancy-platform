package fan.fancy.starter.mybatis.plus.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import fan.fancy.starter.mybatis.plus.properties.FancyCodeProperties;
import lombok.experimental.UtilityClass;

import java.sql.Types;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 代码生成器.
 *
 * @author Fan
 */
@UtilityClass
public class FancyCodeGenerator {

    public static void generate(FancyCodeProperties properties, String... tables) {
        generateInternal(properties, builder -> builder.addInclude(tables));
    }

    private static void generateInternal(FancyCodeProperties properties, Consumer<StrategyConfig.Builder> strategyCustomizer) {
        validate(properties);

        FastAutoGenerator.create(properties.getUrl(), properties.getUsername(), properties.getPassword())
                .globalConfig(builder -> builder
                        .author(properties.getAuthor())
                        .enableSwagger()
                        .outputDir(properties.getOutputDir())
                        .commentDate("yyyy-MM-dd HH:mm:ss")
                )
                .dataSourceConfig(builder -> builder
                        .typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder -> builder
                        .parent(properties.getParent())
                        .moduleName(properties.getModuleName())
                        .entity("entity")
                        .mapper("mapper")
                        .xml("mapper.xml")
                        .service("service")
                        .serviceImpl("service.impl")
                )
                .strategyConfig(builder -> {
                    // 公共策略
                    builder.entityBuilder().enableLombok();
                    // 差异策略
                    if (strategyCustomizer != null) {
                        strategyCustomizer.accept(builder);
                    }
                })
                // 模板引擎配置, 默认是 Velocity.
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    public static void generateAll(FancyCodeProperties properties) {
        generateInternal(properties, builder ->
                builder.addTablePrefix(properties.getTablePrefix())
                        .addTableSuffix(properties.getTableSuffix()));
    }

    private static void validate(FancyCodeProperties properties) {
        Objects.requireNonNull(properties.getUrl(), "jdbc url must not be null.");
        Objects.requireNonNull(properties.getUsername(), "username must not be null.");
        Objects.requireNonNull(properties.getOutputDir(), "outputDir must not be null.");
    }
}
