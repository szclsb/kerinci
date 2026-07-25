package ch.szclsb.kerinci.base.plugin.libc.mapper;


import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcType;
import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LibcStructParser implements LibcObjectParser<StructTemplateDefinition> {
    private final Log logger;
    private final boolean enableBuilder;
    private final String sTypeFieldName;
    private final String sTypeEnum;
    private final String sTypeValuePrefix;

    public LibcStructParser(Log logger, boolean enableBuilder, String sTypeFieldName, String sTypeEnum, String sTypeValuePrefix) {
        this.logger = logger;
        this.enableBuilder = enableBuilder;
        this.sTypeFieldName = sTypeFieldName;
        this.sTypeEnum = sTypeEnum;
        this.sTypeValuePrefix = sTypeValuePrefix;
    }

    private StructTemplateDefinition.FieldDefinition declare(String fieldName, LibcCursor typeCursor, LibcContext context) throws IOException {
        var typeName = typeCursor.getSpelling();
        var decl = context.declare(typeName);
        if (decl == null || decl.isPointer()) {
            return null;  //TODO typeref and function pointer;
        }
        if (decl.isFlag()) {
            return new StructTemplateDefinition.FieldDefinition(
                    StructTemplateDefinition.FieldType.BITMASK,
                    fieldName,
                    "BitMask<%s>".formatted(decl.javaType()),
                    decl.javaLayout(),
                    decl.bytes()
            );
        }
        if (LibcCursor.KIND_ENUM.equals(decl.cursorKind())) {
            return StructTemplateDefinition.FieldDefinition.ofDecl(
                    StructTemplateDefinition.FieldType.ENUM, fieldName, decl);
        }
        if (LibcCursor.KIND_STRUCT.equals(decl.cursorKind())) {
            return StructTemplateDefinition.FieldDefinition.ofDecl(
                    StructTemplateDefinition.FieldType.ELABORATED, fieldName, decl);
        }
        return StructTemplateDefinition.FieldDefinition.ofDecl(
                StructTemplateDefinition.FieldType.PRIMITIVE, fieldName, decl);
    }

    private List<StructTemplateDefinition.Field> offsetAndPadding(Collection<StructTemplateDefinition.FieldDefinition> definitions) {
        var offset = 0L;
        var fields = new ArrayList<StructTemplateDefinition.Field>();
        for (var fieldDefinition : definitions) {
            var m = offset % fieldDefinition.bytes();
            var padding = m > 0
                    ? fieldDefinition.bytes() - m
                    : 0L;
            offset += padding;
            fields.add(new StructTemplateDefinition.Field(fieldDefinition, offset, padding));
            offset += fieldDefinition.bytes();
        }
        return fields;
    }

    private String getSTypeValue(String className) {
            var sb = new StringBuilder(sTypeValuePrefix);
            var charArray = className.substring(2).toCharArray();
            for (var i = 0; i < charArray.length; i++) {
                var c = charArray[i];
                var cp = i - 1 < 0 ? null : charArray[i - 1];
                if (Character.isUpperCase(c) && cp != null && Character.isLowerCase(cp)) {
                    sb.append("_");
                }
                sb.append(Character.toUpperCase(c));
            }
            return sb.toString();
    }

    @Override
    public StructTemplateDefinition parse(String className, LibcCursor structCursor, LibcContext context) throws IOException {
        logger.info("-- declaring struct: %s (%s)".formatted(className, structCursor.getSpelling()));
        var fieldDefinitions = new ArrayList<StructTemplateDefinition.FieldDefinition>();
        for (var fieldCursor : structCursor.getChildren()) {
            if (LibcCursor.KIND_FIELD.equals(fieldCursor.getKind())) {
                var fieldName = fieldCursor.getSpelling();
                logger.debug("---- resolving field: %s".formatted(fieldName));
                var field = switch (fieldCursor.getType().getKind()) {
                    case LibcType.KIND_ELABORATED -> declare(fieldName, fieldCursor.getChildren().getFirst(), context);
                    case LibcType.KIND_POINTER, LibcType.KIND_ARRAY -> StructTemplateDefinition.FieldDefinition.ofAddress(fieldName);
                    case LibcType.KIND_INT -> StructTemplateDefinition.FieldDefinition.ofInt(fieldName);
                    case LibcType.KIND_FLOAT -> StructTemplateDefinition.FieldDefinition.ofFloat(fieldName);
                    default ->
                            throw new IllegalArgumentException("Unexpected field type kind: " + fieldCursor.getType().getKind());
                };
                if (field == null) {
                    logger.warn("---- ignoring field %s, because resolved declaration is null".formatted(fieldName));
                } else {
                    fieldDefinitions.add(field);
                }
            }
        }
        var fields = offsetAndPadding(fieldDefinitions);
        var sType = fields.stream()
                .filter(field -> sTypeFieldName.equals(field.definition().name()))
                .findFirst()
                .map(sTypeField -> new StructTemplateDefinition.SType(
                        sTypeField,
                        sTypeEnum,
                        getSTypeValue(className)))
                .orElse(null);
        return new StructTemplateDefinition(fields, sType, enableBuilder);
    }
}
