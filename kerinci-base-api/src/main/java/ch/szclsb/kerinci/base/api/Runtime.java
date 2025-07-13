package ch.szclsb.kerinci.base.api;

import java.lang.foreign.FunctionDescriptor;
import java.lang.invoke.MethodHandle;

public interface Runtime extends AutoCloseable {
    MethodHandle linkMethod(String name, FunctionDescriptor functionDescriptor);
}
