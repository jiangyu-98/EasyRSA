package jiangyu;


import java.util.Arrays;
import java.util.Random;

public class RSA {
    private final Key key;

    public record Key(BigInteger p, BigInteger q, BigInteger n, BigInteger d, BigInteger e) {
    }

    public RSA(Key key) {
        this.key = key;
    }

    public static Key generateKey(int bitLen) {
        BigInteger p = null, q = null, n = null, e, d = null;
        e = BigInteger.of(Math.abs(new Random().nextLong()));
        while (d == null) {
            p = BigInteger.probablePrime(bitLen);
            q = BigInteger.probablePrime(bitLen);
            n = p.multiply(q);
            BigInteger φn = p.subtract(1).multiply(q.subtract(1));
            d = e.inverse(φn);
        }
        return new Key(p, q, n, d, e);
    }

    public String encrypt(String plainText) {
        byte[] bytes = plainText.getBytes();
        int nBitLen = key.n.bitLength();
        int bytesEncodedEachGroup = (nBitLen - 1) / 8;
        int byteNumber = bytes.length + 4;
        if (byteNumber % bytesEncodedEachGroup != 0) {
            byteNumber = (byteNumber / bytesEncodedEachGroup + 1) * bytesEncodedEachGroup;
        }
        byte[] data = new byte[byteNumber];
        for (int i = 0, l = bytes.length; i < 4; i++) {
            data[i] = (byte) (l & 0xff);
            l >>= 8;
        }
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        int groupNumber = byteNumber / bytesEncodedEachGroup;


        int ciphertextLenEachGroup = nBitLen / 4 + (nBitLen % 4 == 0 ? 0 : 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < groupNumber; i++) {
            byte[] bs = Arrays.copyOfRange(data, i * bytesEncodedEachGroup, (i + 1) * bytesEncodedEachGroup);
            String st = byteToString(bs);
            BigInteger num = new BigInteger(st);
            BigInteger eNum = num.powMod(key.e, key.n);
            sb.append(eNum.toString(ciphertextLenEachGroup));
        }
        return sb.toString();
    }

    public String decrypt(String cipherText) {
        int nBitLen = key.n.bitLength();
        int ciphertextLenEachGroup = nBitLen / 4 + (nBitLen % 4 == 0 ? 0 : 1);
        int bytesEncodedEachGroup = (key.n.bitLength() - 1) / 8;
        int groupNumber = cipherText.length() / ciphertextLenEachGroup;
        byte[] all = new byte[bytesEncodedEachGroup * groupNumber];
        for (int i = 0; i < groupNumber; i++) {
            String bs = cipherText.substring(i * ciphertextLenEachGroup, (i + 1) * ciphertextLenEachGroup);
            BigInteger num = new BigInteger(bs);
            BigInteger eNum = num.powMod(key.d, key.n);
            String s = eNum.toString(bytesEncodedEachGroup * 2);
            byte[] b = stringToByte(s);
            System.arraycopy(b, 0, all, i * bytesEncodedEachGroup, b.length);
        }
        int allLen = 0;
        for (int i = 3; i >= 0; i--) {
            allLen = (allLen << 8) | (all[i] & 0xff);
        }
        byte[] data = Arrays.copyOfRange(all, 4, 4 + allLen);

        return new String(data);
    }

    public static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String byteToString(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }
        return new String(buf);
    }

    static byte[] stringToByte(String s) {
        assert s.length() % 2 == 0;
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * (i + 1)), 16);
        }
        return buf;
    }
}
