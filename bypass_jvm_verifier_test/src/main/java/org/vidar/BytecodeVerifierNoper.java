package org.vidar;

import org.vidar.entity.Fld;
import org.vidar.entity.JVMFlag;
import org.vidar.entity.JVMStruct;
import org.vidar.entity.JVMType;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeVerifierNoper {
    private static Unsafe unsafe = getUnsafe();
    private static Method findNativeMethod = getFindNativeMethod();

    public static void nop() {
        Map<String, JVMStruct> structs = getStructs();
        System.out.println("structs size:" + structs.size());
        Map<String, JVMType> types = getTypes(structs);
        System.out.println("types size:" + types.size());
        List<JVMFlag> flags = getFlags(types);
        for (JVMFlag flag : flags) {
            if (flag.getName().equals("BytecodeVerificationLocal")
                    || flag.getName().equals("BytecodeVerificationRemote"))  {
                unsafe.putByte(flag.getAddress(), (byte) 0);
            }
        }
    }

    public static List<JVMFlag> getFlags(Map<String, JVMType> types) {
        List<JVMFlag> jvmFlags = new ArrayList<>();

        JVMType flagType = types.get("Flag");
        if (flagType == null) {
            flagType = types.get("JVMFlag");
            if (flagType == null) {
                throw new RuntimeException("Could not resolve type 'Flag'");
            }
        }

        Fld flagsField = flagType.getFields().get("flags");
        if (flagsField == null) {
            throw new RuntimeException("Could not resolve field 'Flag.flags'");
        }
        long flags = unsafe.getAddress(flagsField.getOffset());

        Fld numFlagsField = flagType.getFields().get("numFlags");
        if (numFlagsField == null) {
            throw new RuntimeException("Could not resolve field 'Flag.numFlags'");
        }
        int numFlags = unsafe.getInt(numFlagsField.getOffset());

        Fld nameField = flagType.getFields().get("_name");
        if (nameField == null) {
            throw new RuntimeException("Could not resolve field 'Flag._name'");
        }

        Fld addrField = flagType.getFields().get("_addr");
        if (addrField == null) {
            throw new RuntimeException("Could not resolve field 'Flag._addr'");
        }

        for (int i = 0; i < numFlags; i++) {
            long flagAddress = flags + (i * flagType.getSize());
            long flagNameAddress = unsafe.getAddress(flagAddress + nameField.getOffset());
            long flagValueAddress = unsafe.getAddress(flagAddress + addrField.getOffset());

            String flagName = getString(flagNameAddress);
            if (flagName != null) {
                JVMFlag flag = new JVMFlag(flagName, flagValueAddress);
                jvmFlags.add(flag);
            }
        }

        return jvmFlags;
    }



    public static Map<String, JVMType> getTypes(Map<String, JVMStruct> structs) {
        Map<String, JVMType> types = new HashMap<>();

        long entry = symbol("gHotSpotVMTypes");
        long arrayStride = symbol("gHotSpotVMTypeEntryArrayStride");

        while (true) {
            String typeName = derefReadString(entry + offsetTypeSymbol("TypeName"));
            if (typeName == null) {
                break;
            }

            String superClassName = derefReadString(entry + offsetTypeSymbol("SuperclassName"));

            int size = unsafe.getInt(entry + offsetTypeSymbol("Size"));
            boolean oop = unsafe.getInt(entry + offsetTypeSymbol("IsOopType")) != 0;
            boolean intType = unsafe.getInt(entry + offsetTypeSymbol("IsIntegerType")) != 0;
            boolean unsigned = unsafe.getInt(entry + offsetTypeSymbol("IsUnsigned")) != 0;

            Map<String, Fld> structFields = null;
            JVMStruct struct = structs.get(typeName);
            if (struct != null) {
                structFields = struct.getFields();
            }
//            Map<String, Fld> structFields = structs.get(typeName).getFields();
            JVMType jvmType = new JVMType(typeName, superClassName, size, oop, intType, unsigned);
            if (structFields != null) {
                jvmType.getFields().putAll(structFields);
            }

            types.put(typeName, jvmType);

            entry += arrayStride;
        }

        return types;
    }


    public static Map<String, JVMStruct> getStructs() {
        Map<String, JVMStruct> structs = new HashMap<>();

        long currentEntry = symbol("gHotSpotVMStructs");
        long arrayStride = symbol("gHotSpotVMStructEntryArrayStride");

        while (true) {
            String typeName = derefReadString(currentEntry + offsetStructSymbol("TypeName"));
            String fieldName = derefReadString(currentEntry + offsetStructSymbol("FieldName"));
            if (typeName == null || fieldName == null) {
                break;
            }

            String typeString = derefReadString(currentEntry + offsetStructSymbol("TypeString"));
            boolean staticField = unsafe.getInt(currentEntry + offsetStructSymbol("IsStatic")) != 0;

            long offsetOffset = staticField ? offsetStructSymbol("Address") : offsetStructSymbol("Offset");
            long offset = unsafe.getLong(currentEntry + offsetOffset);

            JVMStruct struct = structs.computeIfAbsent(typeName, JVMStruct::new);
            struct.setField(fieldName, new Fld(fieldName, typeString, offset, staticField));

            currentEntry += arrayStride;
        }

        return structs;
    }

    public static long symbol(String name) {
        return unsafe.getLong(findNative(name,null));
    }

    public static long offsetStructSymbol(String name) {
        return symbol("gHotSpotVMStructEntry" + name + "Offset");
    }

    public static long offsetTypeSymbol(String name) {
        return symbol("gHotSpotVMTypeEntry" + name + "Offset");
    }

    public static String derefReadString(long addr) {
        return getString(unsafe.getLong(addr));
    }

    public static String getString(long addr) {
        if (addr == 0L) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int offset = 0;

        while (true) {
            byte b = unsafe.getByte(addr + offset);
            char ch = (char) b;
            if (ch == '\u0000') {
                break;
            }
            stringBuilder.append(ch);
            offset++;
        }
        return stringBuilder.toString();
    }

    public static Long findNative(String name,ClassLoader classLoader) {
        try {
            return (Long) findNativeMethod.invoke(null,classLoader,name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    private static Method getFindNativeMethod() {
        try {
            Method findNative = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);
            findNative.setAccessible(true);
            return findNative;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public static Unsafe getUnsafe() {
        try {
            Constructor constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Unsafe) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
