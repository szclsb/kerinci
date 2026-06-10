// GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
package ${packageName};

import ch.szclsb.kerinci.base.api.Flag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ${enumName} implements Flag {
    <#list consts as const>
        ${const.name}(${const.value})<#if const_has_next>,<#else>;</#if>
    </#list>

    private final int value;

    private ${enumName}(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    private static Map<Integer, ${enumName}> flags = Arrays.stream(${enumName}.values())
        .collect(Collectors.toMap(Flag::getValue, Function.identity()));
    public static ${enumName} ofValue(int value) {
        return flags.get(value);
    }
}