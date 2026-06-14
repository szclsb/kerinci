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
    private static final Map<StructTemplateDefinition.FieldType, FtlTemplateMethodStructField> templateMethodStructFieldMap = Map.of(
            StructTemplateDefinition.FieldType.PRIMITIVE, new FtlTemplateMethodStructFieldPrimitive(),
            StructTemplateDefinition.FieldType.ENUM, new FtlTemplateMethodStructFieldEnum(),
            StructTemplateDefinition.FieldType.BITMASK, new FtlTemplateMethodStructFieldBitMask(),
            StructTemplateDefinition.FieldType.ELABORATED, new FtlTemplateMethodStructFieldElaborated()
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

    private static String methodName(String prefix, String fieldName) {
        return prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private static TemplateMethodModelEx createTemplateMethodStructField(FtlTemplateMethodStructFieldFunction<?> function) {
        return arguments -> {
            if (arguments.get(0) instanceof GenericObjectModel fieldModel
                    && fieldModel.getWrappedObject() instanceof StructTemplateDefinition.Field field) {
                var fieldType = field.definition().dType();
                var templateMethodStructField = templateMethodStructFieldMap.get(fieldType);
                if (templateMethodStructField != null) {
                    var argParser = new FtlTemplateMethodExtraArgParser(arguments);
                    return function.apply(field, templateMethodStructField, argParser);
                } else {
                    throw new TemplateModelException("unknown dtype " + fieldType);
                }
            } else {
                throw new TemplateModelException("illagal arguments " + arguments);
            }
        };
    }

    public TemplateWriter<StructTemplateDefinition> createStrcutTemplateWriter() {
        return createFileTemplateWriter(templateFileNameStruct, Map.of(
                "getter_method_name", createTemplateMethodStructField((field, _, _) ->
                        methodName("get", field.definition().name())),
                "setter_method_name", createTemplateMethodStructField((field, _, _) ->
                        methodName("set", field.definition().name())),
                "read_field", createTemplateMethodStructField((field, templateMethodStructField, _) ->
                        templateMethodStructField.readField(field)),
                "write_field", createTemplateMethodStructField(((field, templateMethodStructField, extraArgParser) -> {
                    var varName = extraArgParser.readString(1);
                    return templateMethodStructField.writeField(field, varName);
                }))
        ));
    }

    public TemplateWriter<FunctionsTemplateDefinition> createLibraryTemplateWriter() {
        return createFileTemplateWriter(templateFileNameLibrary);
    }
}
