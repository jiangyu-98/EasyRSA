package jiangyu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BigIntegerTest {
    @Test
    void create() {
        String hexStr = "AF123123123BCDE444213AF1C2323222";
        for (int i = 1; i <= hexStr.length(); i++) {
            String hexSubStr = hexStr.substring(0, i);
            BigInteger n = new BigInteger(hexSubStr);
            assertEquals(hexSubStr, n.toString());
        }
    }

    @Test
    void add() {
        BigInteger n1 = new BigInteger("122");
        BigInteger n2 = new BigInteger("122");
        BigInteger n3 = new BigInteger("244");
        assertEquals(n3, n1.add(n2));

        n1 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        n2 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        assertEquals(n1, n1.add(n2).subtract(n2));
    }

    @Test
    void subtract() {
        BigInteger n1 = new BigInteger("432");
        BigInteger n2 = new BigInteger("1F1");
        BigInteger n3 = new BigInteger("241");
        assertEquals(n3, n1.subtract(n2));

        n1 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        n2 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        assertEquals(n1, n1.subtract(n2).add(n2));

        BigInteger res = BigInteger.of(0).subtract(BigInteger.of(2));
        assertEquals("-2", res.toString());
    }

    @Test
    void multiply() {
        BigInteger n1 = new BigInteger("4D745");
        BigInteger n2 = new BigInteger("65E72");
        BigInteger n3 = new BigInteger("1ED4D132BA");
        assertEquals(n3, n1.multiply(n2));
    }

    @Test
    void divide() {
        BigInteger n1 = new BigInteger("2");
        BigInteger n2 = new BigInteger("3");
        BigInteger n3 = new BigInteger("6");
        assertEquals(n1, n3.divide(n2));

        n1 = new BigInteger("4D745");
        n2 = new BigInteger("65E72");
        n3 = new BigInteger("1ED4D132BA");
        assertEquals(n1, n3.divide(n2));

        n1 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        n2 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        assertEquals(n1, n1.multiply(n2).divide(n1));
    }


    @Test
    void mod() {
        BigInteger n1 = new BigInteger("234432543636327");
        BigInteger n2 = new BigInteger("34444423");
        BigInteger n3 = new BigInteger("1A8798C2");
        assertEquals(n3, n1.mod(n2));

        n1 = BigInteger.of(0x32143143);
        n2 = new BigInteger("144C3B27FE");
        n3 = new BigInteger("144C3B27FF");
        assertEquals(BigInteger.of(1), n1.powMod(n2, n3));
    }

    @Test
    void longBuild() {
        BigInteger n = new BigInteger(0x1023FFFFFFFFF224L);
        assertEquals(n.toString(), "1023FFFFFFFFF224");
    }

    @Test
    void longOperater() {
        BigInteger n1 = BigInteger.of(0x4444);
        BigInteger n2 = BigInteger.of(0x1234);
        assertEquals(n1.subtract(1).toString(), "4443");
        assertEquals(n2.add(0x1234).toString(), "2468");
    }

    @Test
    void random() {
        for (int i = 0; i < 100; i++) {
            BigInteger random = BigInteger.random(1024);
        }
    }

    @Test
    void equalZero() {
        BigInteger n1 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        BigInteger n2 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        assertTrue(n1.subtract(n2).equals(0));
    }

    @Test
    void compareTo() {
        BigInteger n1 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        BigInteger n2 = new BigInteger("AF123123123BCDE444213AF1C2323222");
        assertEquals(0, n1.compareTo(n2));

        n1 = new BigInteger("3123");
        n2 = new BigInteger("123");
        int i = n1.compareTo(n2);
        assertTrue(i > 0);
        assertTrue(n2.compareTo(n1) < 0);
    }

    @Test
    void shiftRight() {
        BigInteger n1 = new BigInteger("144c3b27ff");
        BigInteger n2 = new BigInteger("A261D93FF");

        assertEquals(n2, n1.shiftRight(1));
    }

    @Test
    void multiplyMod() {
        BigInteger n1 = new BigInteger("ff23333333333333332dda");
        BigInteger n2 = new BigInteger("532544543444444");
        BigInteger n3 = new BigInteger("22222222222f1");
        BigInteger res = new BigInteger("1AC1A4FD42D75");
        assertEquals(res, n1.multiplyMod(n2, n3));
    }

    @Test
    void powMod() {
        BigInteger n1 = new BigInteger("ff23333333333333332dda");
        BigInteger n2 = new BigInteger("532544543444444");
        BigInteger n3 = new BigInteger("22222222222f1");
        BigInteger res = new BigInteger("1fb5631eb8cfc");
        assertEquals(res, n1.powMod(n2, n3));
    }

    @Test
    void testBit() {
        BigInteger n1 = new BigInteger("3FF");
        assertTrue(n1.testBit(9));
    }

    @Test
    void equal() {
        BigInteger n2 = new BigInteger("356242E7AD753AB63FCCED44E7CACDA0EDB5B353E59D81AEBAC595A14C4CFD702");
        assertFalse(n2.equals(2));
        n2 = new BigInteger("5");
        assertTrue(n2.equals(5));
    }

    @Test
    void bitLength() {
        assertEquals(4, BigInteger.of(8).bitLength());
        assertEquals(14, (new BigInteger("3FFA")).bitLength());
    }

    @Test
    void inverse() {
        BigInteger n1 = new BigInteger("2");
        BigInteger base = new BigInteger("B");
        BigInteger res = new BigInteger("6");
        assertEquals(res, n1.inverse(base));
    }

    @Test
    void numberOfTrailingZeros() {
        BigInteger n1 = BigInteger.of(0x42342000);
        assertEquals(13, n1.numberOfTrailingZeros());
    }

    @Test
    void probablePrime() {
        BigInteger res = BigInteger.probablePrime(64);
    }

    @Test
    void isProbablePrime() {
        BigInteger n1 = new BigInteger("5");
        assertTrue(n1.isProbablePrime());

        n1 = new BigInteger("144C3B27FF");
        assertTrue(n1.isProbablePrime());
    }

    @Test
    void isProbablePrime2() {
        BigInteger n1 = new BigInteger("F81BBE2D143160D23FF06B3BAE953B659A902382458BBC62930F8AB1C05600C3");
        n1.isProbablePrime();
        BigInteger n2 = new BigInteger("356242E7AD753AB63FCCED44E7CACDA0EDB5B353E59D81AEBAC595A14C4CFD702");
        assertFalse(n2.isProbablePrime());
    }
}