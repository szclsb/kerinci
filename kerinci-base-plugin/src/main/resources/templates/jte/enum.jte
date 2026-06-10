@import gg.jte.support.ForSupport
@import ch.szclsb.kerinci.base.plugin.writer.EnumFileContext
@import ch.szclsb.kerinci.base.plugin.writer.EnumFileContext.EnumConst
@param EnumFileContext context
// GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
package ${context.packageName()};

import ch.szclsb.kerinci.base.api.Flag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ${context.enumName()} implements Flag {
    TEST;

    private final int value;

    private %1$s(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    private static Map<Integer, ${context.enumName()}> flags = Arrays.stream(${context.enumName()}.values())
        .collect(Collectors.toMap(Flag::getValue, Function.identity()));
    public static ${context.enumName()} ofValue(int value) {
        return flags.get(value);
    }
}