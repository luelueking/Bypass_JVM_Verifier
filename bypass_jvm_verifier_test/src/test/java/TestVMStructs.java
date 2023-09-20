import org.vidar.BytecodeVerifierNoper;
import sun.misc.Unsafe;


public class TestVMStructs {
    public static void main(String[] args) {
        Long vmStructs = BytecodeVerifierNoper.findNative("gHotSpotVMStructs", null);
        Unsafe unsafe = BytecodeVerifierNoper.getUnsafe();
        System.out.printf(Long.toHexString(vmStructs)+
                ", value: "+ Long.toHexString(unsafe.getLong(vmStructs)));
    }
}
