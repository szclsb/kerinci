package ch.szclsb.kerinci.base.plugin.template;

import java.util.Collections;
import java.util.List;

public class EnumFileModel extends FileModel<List<EnumFileModel.EnumConst>> {
    public record EnumConst(String name, String value) {
    }

    public EnumFileModel(String packageName, String className, List<EnumConst> content) {
        super(packageName, className, Collections.unmodifiableList(content));
    }
}
