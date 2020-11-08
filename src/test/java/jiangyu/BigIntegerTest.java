package jiangyu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BigIntegerTest {
    @Test
    void create() {
        BigInteger n = new BigInteger("AF", 1024);
        System.out.println(n);
    }
}