package ch.szclsb.kerinci.base.plugin.template.ftl;

import ch.szclsb.kerinci.base.plugin.template.EnumTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.FunctionsTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import ch.szclsb.kerinci.base.plugin.template.ftl.methods.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.ext.beans.GenericObjectModel;
import freemarker.template.*;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FtlTemplateFactory {
    public static final String templatePath = "templates/freemarker";
    public static final String templateFileNameEnum = "enum.ftl";
    public static final String templateFileNameStruct = "struct.ftl";
    public static final String templateFileNameLibrary = "library.ftl";

    // todo use enum instead of string
    private static final Map<String, FtlTemplateMethodStructField> templateMethodStructFieldMap = Map.of(
            StructTemplateDefinition.FieldType.PRIMITIVE.name(), new FtlTemplateMethodStructFieldPrimitive(),
            StructTemplateDefinition.FieldType.ENUM.name(), new FtlTemplateMethodStructFieldEnum(),
            StructTemplateDefinition.FieldType.BITMASK.name(), new FtlTemplateMethodStructFieldBitMask(),
            StructTemplateDefinition.FieldType.ELABORATED.name(), new FtlTemplateMethodStructFieldElaborated()
    );

    private final Log logger;
    private final Configuration configuration;

    public FtlTemplateFactory(Log logger) {
        this.logger = logger;
        this.configuration = configure();
    }

    public static Configuration configure() {
        try {
            var cfg = new Configuration(Configuration.VERSION_2_3_34);
            cfg.setTemplateLoader(new ClassTemplateLoader(FtlTemplateFactory.class.getClassLoader(), templatePath));
            cfg.setDefaultEncoding("UTF-8");
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> TemplateWriter<T> createFileTemplateWriter(String templateFile) {
        return createFileTemplateWriter(templateFile, null);
    }

    private <T> TemplateWriter<T> createFileTemplateWriter(String templateFile, Map<String, ?> additions) {
        return (packageName, className, definition, writer) -> {
            Map<String, Object> fileModel = additions != null ? new HashMap<>(additions) : new HashMap<>();
            fileModel.put("packageName", packageName);
            fileModel.put("className", className);
            fileModel.put("definition", definition);
            try {
                var template = configuration.getTemplate(templateFile);
                template.process(fileModel, writer);
            } catch (TemplateException e) {
                throw new IOException(e);
            }
        };
    }

    public TemplateWriter<EnumTemplateDefinition> createEnumTemplateWriter() {
        return createFileTemplateWriter(templateFileNameEnum);
    }

    // TODO simplify
    private TemplateMethodModelEx createGetterTemplateMethod() {
        return arguments -> {
            if (arguments.get(0) instanceof GenericObjectModel dType
                    && arguments.get(1) instanceof TemplateScalarModel javaType
                    && arguments.get(2) instanceof TemplateScalarModel memoryLayout
                    && arguments.get(3) instanceof TemplateNumberModel offset
                    && arguments.get(4) instanceof TemplateScalarModel name) {
                var templateMethodStructField = templateMethodStructFieldMap.get(dType.getAsString());
                if (templateMethodStructField != null) {
                    return templateMethodStructField.getterMethod(
                            javaType.getAsString(),
                            memoryLayout.getAsString(),
                            offset.getAsNumber().longValue(),
                            name.getAsString()
                    );
                } else {
                    throw new TemplateModelException("unknown dtype " + dType);
                }
            } else {
                throw new TemplateModelException("illagal arguments " + arguments);
            }
        };
    }

    // TODO simplify
    private TemplateMethodModelEx createSetterTemplateMethod() {
        return arguments -> {
            if (arguments.get(0) instanceof GenericObjectModel dType
                    && arguments.get(1) instanceof TemplateScalarModel javaType
                    && arguments.get(2) instanceof TemplateScalarModel memoryLayout
                    && arguments.get(3) instanceof TemplateNumberModel offset
                    && arguments.get(4) instanceof TemplateScalarModel name) {
                var templateMethodStructField = templateMethodStructFieldMap.get(dType.getAsString());
                if (templateMethodStructField != null) {
                    return templateMethodStructField.setterMethod(
                            javaType.getAsString(),
                            memoryLayout.getAsString(),
                            offset.getAsNumber().longValue(),
                            name.getAsString()
                    );
                } else {
                    throw new TemplateModelException("unknown dtype " + dType);
                }
            } else {
                throw new TemplateModelException("illagal arguments " + arguments);
            }
        };
    }

    // TODO simplify
    private TemplateMethodModelEx createBuilderTemplateMethod() {
        return arguments -> {
            if (arguments.get(0) instanceof GenericObjectModel dType
                    && arguments.get(1) instanceof TemplateScalarModel javaType
                    && arguments.get(2) instanceof TemplateScalarModel memoryLayout
                    && arguments.get(3) instanceof TemplateNumberModel offset
                    && arguments.get(4) instanceof TemplateScalarModel name) {
                var templateMethodStructField = templateMethodStructFieldMap.get(dType.getAsString());
                if (templateMethodStructField != null) {
                    return templateMethodStructField.builderMethod(
                            javaType.getAsString(),
                            memoryLayout.getAsString(),
                            offset.getAsNumber().longValue(),
                            name.getAsString()
                    );
                } else {
                    throw new TemplateModelException("unknown dtype " + dType);
                }
            } else {
                throw new TemplateModelException("illagal arguments " + arguments);
            }
        };
    }

    public TemplateWriter<StructTemplateDefinition> createStrcutTemplateWriter() {
        return createFileTemplateWriter(templateFileNameStruct, Map.of(
//            "getter_method_name", arguments -> "get",
//            "setter_method_name", arguments -> "set",
                "getter_method", createGetterTemplateMethod(),
                "setter_method", createSetterTemplateMethod(),
                "builder_method", createBuilderTemplateMethod()
        ));
    }

    public TemplateWriter<FunctionsTemplateDefinition> createLibraryTemplateWriter() {
        return createFileTemplateWriter(templateFileNameLibrary);
    }
}
