package ch.szclsb.kerinci.base.plugin.libc.mapper;


import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.template.EnumFileModel;

import java.util.ArrayList;
import java.util.List;

public class LibcEnumParser implements LibcParser<List<EnumFileModel.EnumConst>> {
    @Override
    public List<EnumFileModel.EnumConst> parse(LibcCursor enumCursor) {
        var enumConst = new ArrayList<EnumFileModel.EnumConst>();
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
                enumConst.add(new EnumFileModel.EnumConst(valueName, value));
            }
        }
        return enumConst;
    }
}
