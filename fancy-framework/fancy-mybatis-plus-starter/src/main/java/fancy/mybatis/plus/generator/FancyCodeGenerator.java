package fancy.mybatis.plus.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

/**
 * 代码生成器.
 *
 * @author Fan
 * @since 2025/3/6 9:15
 */
public class FancyCodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/fancy_blog?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&connectTimeout=5000&socketTimeout=10000", "username", "password")
                // 全局配置.
                .globalConfig(builder -> builder
                        // 设置作者.
                        .author("Fan")
                        // 开启 Swagger 模式.
                        .enableSwagger()
                        // 指定输出目录.
                        .outputDir("D://generator"))
                // 数据源配置.
                .dataSourceConfig(builder -> builder
                        .typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (Types.SMALLINT == typeCode) {
                                // 自定义类型转换.
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        }))
                // 包配置.
                .packageConfig(builder -> builder
                        // 设置父包名.
                        .parent("fancy")
                        // 设置父包模块名.
                        .moduleName("generator")
                        // 设置 Mapper Xml 生成路径.
                        .pathInfo(Collections.singletonMap(OutputFile.xml, "D://generator")))
                // 策略配置.
                .strategyConfig(builder -> builder
                        // 设置需要生成的表名.
                        .addInclude("article", "category")
                        // 设置过滤表前缀.
                        .addTablePrefix("t_", "c_"))
                // 模板引擎配置, 默认是 Velocity.
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行.
                .execute();
    }
}
