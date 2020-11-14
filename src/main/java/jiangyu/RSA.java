package jiangyu;


import java.util.Random;

public class RSA {
    // private final Key key;
    // private final int BITS;
    // private final int ENCRYPT_BYTES;

    //
    // public RSA(int bits, long e) {
    //     key = generateKey(bits >> 1, e);
    //     this.BITS = bits;
    //     this.ENCRYPT_BYTES = (bits >> 3) - 1;
    // }


    public record Key(BigInteger p, BigInteger q, BigInteger n, BigInteger d, BigInteger e) {
    }


    public static Key generateKey(int bitLen) {
        BigInteger p = null, q = null, n = null, e, d = null;
        e = BigInteger.of(new Random().nextLong());
        while (d == null) {
            p = BigInteger.probablePrime(bitLen);
            q = BigInteger.probablePrime(bitLen);
            n = p.multiply(q);
            BigInteger φn = p.subtract(1).multiply(q.subtract(1));
            d = e.inverse(φn);
        }
        return new Key(p, q, n, e, d);
    }

    // public String encrypt(String plainText) {
    //     if (plainText.length() == 0)
    //         return "";
    //
    //     StringBuilder sb = new StringBuilder();
    //     BigInteger e = key.e;
    //     BigInteger n = key.n;
    //     byte[] bytes = plainText.getBytes();
    //     int offset = bytes.length % ENCRYPT_BYTES;
    //     if (offset != 0) {
    //         BigInteger m = BigInteger.valueOf(bytes, 0, offset - 1);
    //         sb.append(m.powMod(e, n).toString(BITS));
    //     }
    //     while (offset < bytes.length) {
    //         BigInteger m = BigInteger.valueOf(bytes, offset, offset + ENCRYPT_BYTES - 1);
    //         sb.append(m.powMod(e, n).toString(BITS));
    //         offset += ENCRYPT_BYTES;
    //     }
    //     assert offset == bytes.length;
    //     return sb.toString();
    // }
    //
    // public String decrypt(String cipherText) {
    //     if (cipherText.length() == 0)
    //         return "";
    //
    //     int hexBits = BITS >> 2;
    //     BigInteger d = key.d;
    //     BigInteger n = key.n;
    //
    //     BigInteger m = new BigInteger(cipherText.substring(0, hexBits));
    //     BigInteger origin = m.powMod(d, n);
    //     byte[] originBytes = origin.toBytes();
    //     byte[] bytes = new byte[originBytes.length + (cipherText.length() / hexBits - 1) * ENCRYPT_BYTES];
    //     System.arraycopy(originBytes, 0, bytes, 0, originBytes.length);
    //
    //     int count = originBytes.length;
    //
    //     for (int i = hexBits; i < cipherText.length(); i += hexBits) {
    //         m = new BigInteger(cipherText.substring(i, i + hexBits));
    //         origin = m.powMod(d, n);
    //         originBytes = origin.toBytes(ENCRYPT_BYTES << 3);
    //         for (byte originByte : originBytes) {
    //             bytes[count++] = originByte;
    //         }
    //     }
    //     assert count == bytes.length;
    //     return new String(bytes);
    // }
    //
    // public Key getKey() {
    //     return key;
    // }
}
