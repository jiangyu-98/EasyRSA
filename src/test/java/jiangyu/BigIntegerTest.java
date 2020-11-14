package jiangyu;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class BigIntegerTest {
    @Test
    void create() {
        String hexStr = "AF123123123BCDE444213AF1C2323222";
        for (int i = 1; i <= hexStr.length(); i++) {
            String hexSubStr = hexStr.substring(0, i);
            BigInteger n = new BigInteger(hexSubStr, 1024);
            assertEquals(hexSubStr, n.toString());
        }
    }

    @Test
    void add() {
        BigInteger n1 = new BigInteger("122", 1024);
        BigInteger n2 = new BigInteger("122", 1024);
        BigInteger n3 = new BigInteger("244", 1024);
        assertEquals(n3, n1.add(n2));


        n1 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        n2 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        assertEquals(n1, n1.add(n2).subtract(n2));
    }

    @Test
    void subtract() {
        BigInteger n1 = new BigInteger("432", 1024);
        BigInteger n2 = new BigInteger("1F1", 1024);
        BigInteger n3 = new BigInteger("241", 1024);
        assertEquals(n3, n1.subtract(n2));

        n1 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        n2 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        assertEquals(n1, n1.subtract(n2).add(n2));
    }

    @Test
    void multiply() {
        BigInteger n1 = new BigInteger("4D745", 1024);
        BigInteger n2 = new BigInteger("65E72", 1024);
        BigInteger n3 = new BigInteger("1ED4D132BA", 1024);
        assertEquals(n3, n1.multiply(n2));
    }

    @Test
    void divide() {
        BigInteger n1 = new BigInteger("2", 1024);
        BigInteger n2 = new BigInteger("3", 1024);
        BigInteger n3 = new BigInteger("6", 1024);
        assertEquals(n1, n3.divide(n2));

        n1 = new BigInteger("4D745", 1024);
        n2 = new BigInteger("65E72", 1024);
        n3 = new BigInteger("1ED4D132BA", 1024);
        assertEquals(n1, n3.divide(n2));

        n1 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        n2 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        assertEquals(n1, n1.multiply(n2).divide(n1));
    }

    @Test
    void longBuild() {
        BigInteger n = new BigInteger(0x1023FFFFFFFFF224L);
        assertEquals(n.toString(), "1023FFFFFFFFF224");
    }

    @Test
    void longOperater() {
        BigInteger n1 = new BigInteger(0x4444, 1024);
        BigInteger n2 = new BigInteger(0x1234, 1024);
        assertEquals(n1.subtractEqual(1).toString(), "4443");
        assertEquals(n2.addEqual(0x1234).toString(), "2468");
    }

    @Test
    void random() {
        for (int i = 0; i < 100; i++) {
            BigInteger random = BigInteger.random(1024);
        }
    }

    @Test
    void mod() {
        BigInteger n1 = new BigInteger("234432543636327", 1024);
        BigInteger n2 = new BigInteger("34444423", 1024);
        BigInteger n3 = new BigInteger("1A8798C2", 1024);
        assertEquals(n3, n1.mod(n2));
    }

    @Test
    void equalZero() {
        BigInteger n1 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        BigInteger n2 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        assertTrue(n1.subtract(n2).equals(0));
    }

    @Test
    void compareTo() {
        BigInteger n1 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        BigInteger n2 = new BigInteger("AF123123123BCDE444213AF1C2323222", 1024);
        assertEquals(0, n1.compareTo(n2));

        n1 = new BigInteger("3123", 1024);
        n2 = new BigInteger("123", 1024);
        int i = n1.compareTo(n2);
        assertTrue(i > 0);
        assertTrue(n2.compareTo(n1) < 0);
    }

    @Test
    void shiftRight() {
        BigInteger n1 = new BigInteger("3", 1024);
        assertTrue(n1.shiftRight(1).equals(1));
    }

    @Test
    void modMultiply() {
        BigInteger n1 = new BigInteger("ff23333333333333332dda", 1024);
        BigInteger n2 = new BigInteger("532544543444444", 1024);
        BigInteger n3 = new BigInteger("22222222222f1", 1024);
        BigInteger res = new BigInteger("26d7da76586627d1601b639a3", 1024);
        assertEquals(res, n1.multiplyMod(n2, n3));
    }

    @Test
    void modPow() {
        BigInteger n1 = new BigInteger("ff23333333333333332dda", 1024);
        BigInteger n2 = new BigInteger("532544543444444", 1024);
        BigInteger n3 = new BigInteger("22222222222f1", 1024);
        System.out.println(n1.powMod(n2, n3));
    }

    @Test
    void testBit() {
        BigInteger n1 = new BigInteger("3FF", 1024);
        assertTrue(n1.testBit(9));
    }


    @Test
    void bitLength() {
        assertEquals(4, (new BigInteger(8, 1024)).bitLength());
        assertEquals(14, (new BigInteger("3FFA", 1024)).bitLength());
    }
}