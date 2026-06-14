<#macro format_field_memory_layout field>${field.definition.memoryLayout}.withName("${field.definition.name}")<#if field.padding gt 0>,
    MemoryLayout.paddingLayout(${field.padding})</#if></#macro>
// GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
package ${packageName};

import ch.szclsb.kerinci.base.api.BitMask;
import ch.szclsb.kerinci.base.api.ForeignObject;

import ch.szclsb.kerinci.internal.KerinciBitMask;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_FLOAT;

public class ${className} implements ForeignObject {
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
<#list definition.fields as field>
    <@format_field_memory_layout field/><#if field_has_next>,</#if>
</#list>
    ).withName("${className}");

    private final MemorySegment pSegment;
    private final int index;

    public ${className}(MemorySegment pSegment) {
        this(pSegment, 0);
    }

    public ${className}(MemorySegment pSegment, int index) {
        this.pSegment = pSegment;
        this.index = index;
    }

    @Override
    public MemorySegment getSegment() {
        return pSegment.asReadOnly();
    }
<#list definition.fields as field>

    /**
    * getter for ${field.definition.name}
    */
${getter_method(field.definition.dType, field.definition.javaType, field.definition.memoryLayout, field.offset, field.definition.name)}

    /**
    * setter for ${field.definition.name}
    */
${setter_method(field.definition.dType, field.definition.javaType, field.definition.memoryLayout, field.offset, field.definition.name)}
</#list>
<#if definition.enableBuilder>

    public static Builder builder(SegmentAllocator allocator) {
        return new Builder(allocator);
    };

    public static class Builder {
        private final ${className} instance;

        private Builder(SegmentAllocator allocator) {
            this.instance = new ${className}(allocator.allocate(LAYOUT));
            // TODO sType
        }
<#list definition.fields as field>
${builder_method(field.definition.dType, field.definition.javaType, field.definition.memoryLayout, field.offset, field.definition.name)}
</#list>

        public ${className} build() {
            return instance;
        }
    }
</#if>
}