package ch.szclsb.kerinci.base.plugin.template;

import java.util.List;
import java.util.OptionalLong;

public record EnumTemplateDefinition(
        List<EnumTemplateDefinition.EnumConst> consts
) implements TemplateDefinition {
    public record EnumConst(String name, String value) {
    }

    @Override
    public OptionalLong javaObjectBytes() {
        return OptionalLong.empty();
    }
}

