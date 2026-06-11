// GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
package ${packageName};

import ch.szclsb.kerinci.base.api.Flag;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ${className} implements Flag {
    <#list content as const>
        ${const.name}(${const.value})<#if const_has_next>,<#else>;</#if>
    </#list>

    private final int value;

    private ${className}(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    private static Map<Integer, ${className}> flags = Arrays.stream(${className}.values())
        .collect(Collectors.toMap(Flag::getValue, Function.identity()));
    public static ${className} ofValue(int value) {
        return flags.get(value);
    }
}