package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

import java.util.List;

public class FtlTemplateMethodExtraArgParser {
    private final List arguments;

    public FtlTemplateMethodExtraArgParser(List arguments) {
        this.arguments = arguments;
    }

    public String readString(int index) throws TemplateModelException {
        if (index > 0 && arguments.get(index) instanceof TemplateScalarModel templateModel) {
            return templateModel.getAsString();
        }
        throw new IllegalArgumentException();
    }

    public long readLong(int index) throws TemplateModelException {
        if (index > 0 && arguments.get(index) instanceof TemplateNumberModel templateModel) {
            var number = templateModel.getAsNumber();
            return number.longValue();
        }
        throw new IllegalArgumentException();
    }
}
