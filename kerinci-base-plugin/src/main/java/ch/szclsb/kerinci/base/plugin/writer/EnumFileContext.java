package ch.szclsb.kerinci.base.plugin.writer;

import java.util.List;

public record EnumFileContext(
        String packageName,
        String enumName,
        List<EnumConst> consts
) {
    public record EnumConst(String name, String value) {
        @Override
        public String toString() {
            return "%s(%s)".formatted(name, value);
        }
    }
}
