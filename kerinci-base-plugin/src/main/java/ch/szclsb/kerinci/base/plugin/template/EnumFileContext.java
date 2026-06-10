package ch.szclsb.kerinci.base.plugin.template;

import java.util.List;

public record EnumFileContext(
        String packageName,
        String enumName,
        List<EnumConst> consts
) {
    public record EnumConst(String name, String value) {
    }
}
