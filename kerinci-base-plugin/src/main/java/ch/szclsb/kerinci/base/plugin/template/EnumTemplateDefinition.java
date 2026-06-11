package ch.szclsb.kerinci.base.plugin.template;

import java.util.List;

public record EnumTemplateDefinition(
        List<EnumTemplateDefinition.EnumConst> consts
) {
    public record EnumConst(String name, String value) {
    }
}

