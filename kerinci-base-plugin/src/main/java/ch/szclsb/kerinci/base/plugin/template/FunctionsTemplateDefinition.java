package ch.szclsb.kerinci.base.plugin.template;

import java.util.List;
import java.util.OptionalLong;

public record FunctionsTemplateDefinition(
    List<Function> functions
) implements TemplateDefinition {
    public record Function(
            String nativeMethod,
            String handleName,
            String returnType,
            String methodName,
            List<Param> methodParams
    ) {
        public record Param(String type, String name) {
        }
    }

    @Override
    public OptionalLong javaObjectBytes() {
        return OptionalLong.empty();
    }
}
