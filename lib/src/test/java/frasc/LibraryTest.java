/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package frasc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    @Test
    public void emptySymbolConstructor() {
        Symbol symbol = new Symbol();
        assertTrue(symbol.icl == 0, "symbol icl should be 0");
        assertTrue(symbol.value == 0, "symbol value should be 0");
    }

    @Test
    public void twoIntSymbolConstructor() {
        Symbol symbol = new Symbol(50, 50);
        assertTrue(symbol.icl == 268435456, "symbol icl should be 268435456");
        assertTrue(symbol.value == 3276800, "symbol value should be 3276800");
    }

    @Test
    public void setCodeLengthTest() {
        Symbol symbol = new Symbol();
        symbol.setCodeLength(50, 50);
        assertTrue(symbol.icl == -336, "Wrong icl value");
    }
}
