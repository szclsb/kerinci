package ch.szclsb.kerinci.base.plugin.libc.mapper;


import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.template.EnumTemplateDefinition;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;

public class LibcEnumParser implements LibcObjectParser<EnumTemplateDefinition> {
    private final Log logger;

    public LibcEnumParser(Log logger) {
        this.logger = logger;
    }

    @Override
    public EnumTemplateDefinition parse(String className, LibcCursor enumCursor) {
        logger.info("-- declaring enum: %s (%s)".formatted(className, enumCursor.getSpelling()));
        var enumConst = new ArrayList<EnumTemplateDefinition.EnumConst>();
        for (var enumValue : enumCursor.getChildren()) {
            if (LibcCursor.KIND_ENUM_CONST.equals(enumValue.getKind())) {
                var valueName = enumValue.getSpelling();
                var value = enumValue.getChildren().stream()
                        .filter(c -> LibcCursor.KIND_INT_LITERAL.equals(c.getKind()))
                        .findFirst()
                        .map(LibcCursor::getSpelling)
                        .or(() -> enumValue.getChildren().stream()
                                .filter(c -> LibcCursor.KIND_REF_EXPR.equals(c.getKind()))
                                .findFirst()
                                .map(c -> c.getSpelling() + ".value"))
                        .orElse("");
                enumConst.add(new EnumTemplateDefinition.EnumConst(valueName, value));
            }
        }
        return new EnumTemplateDefinition(enumConst);
    }
}
