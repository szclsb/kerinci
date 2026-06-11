<#macro formart_args args><#list args as arg>${arg.type} ${arg.name}<#if arg_has_next>, </#if></#list></#macro>
// GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
package ${packageName};

import ch.szclsb.kerinci.base.api.BitMask;
import ch.szclsb.kerinci.base.api.Runtime;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.lang.foreign.FunctionDescriptor;
import java.lang.invoke.MethodHandle;
import java.util.stream.Stream;

public class ${className} {
    private final Runtime runtime;
<#list definition.functions as function>
    private final MethodHandle ${function.handleName};
</#list>

    public ${className}(Runtime runtime) {
        this.runtime = runtime;
<#list definition.functions as function>
        this.${function.handleName} = runtime.linkMethod("${function.nativeMethod}", FunctionDescriptor.ofVoid());  // FIXME return args
</#list>
    }
<#list definition.functions as function>

    /**
    * invokes native method ${function.nativeMethod}
    */
    public ${function.returnType} ${function.methodName}(<@formart_args function.methodArgs/>) {
        try {
            // FIXME return args
            ${function.handleName}.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
</#list>
}