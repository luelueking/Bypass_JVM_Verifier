package org.vidar;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BytecodeVerifierNoper.nop();
        Class<?> payload = new InMemoryClassLoader().findClass("Payload");
        Method m = payload.getDeclaredMethod("hack");
        m.setAccessible(true);
        System.out.println(m);
        m.invoke(null);
    }

    static class InMemoryClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] classData = new byte[0];
            try {
                classData = Files.readAllBytes(Paths.get("Payload.class"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return defineClass(name, classData, 0, classData.length);
        }
    }
}
