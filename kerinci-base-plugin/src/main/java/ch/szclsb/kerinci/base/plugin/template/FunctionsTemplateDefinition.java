package ch.szclsb.kerinci.base.plugin.template;

import java.util.List;

public record FunctionsTemplateDefinition(
    List<Function> functions
) {
    public record Function(
            String nativeMethod,
            String handleName,
            String returnType,
            String methodName,
            List<Arg> methodArgs
    ) {
        public record Arg(String type, String name) {
        }
    }
}
