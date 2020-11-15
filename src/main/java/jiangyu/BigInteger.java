package jiangyu;


import java.lang.Math;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;


public class BigInteger implements Comparable<BigInteger> {
    static final int BASE_BITS = 4;
    static final long BASE = 1 << BASE_BITS;
    static final long BASE_MASK = (1 << BASE_BITS) - 1;
    static final Random RANDOM = new Random();
    static int millerRabinRepeat = 10;
    static int smallIntegerDLength = bitLengthToLen(64);
    int len = 0;
    long[] d;
    int sign = 1;

    private static final int CACHE_NUMBER = 256;
    private static final BigInteger[] cache = new BigInteger[CACHE_NUMBER];

    static {
        for (int i = 0; i < CACHE_NUMBER; i++) {
            cache[i] = new BigInteger(i);
        }
    }

    BigInteger(long value) {
        if (value < 0) {
            sign = -1;
            value = -value;
        }
        d = new long[smallIntegerDLength];
        do {
            d[len] = value & BASE_MASK;
            value >>= BASE_BITS;
            len++;
        } while (value != 0);
    }

    BigInteger(String hexString) {
        int startPos = 0;
        if (hexString.charAt(0) == '-') {
            startPos = 1;
            sign = -1;
        }
        for (int i = startPos; i < hexString.length(); i++) {
            if (hexString.charAt(i) == '0') {
                startPos++;
            } else {
                break;
            }
        }
        hexString = hexString.substring(startPos);

        int bitLength = (hexString.length() - 1) * 4 + switch (hexString.charAt(0)) {
            case '0' -> 0;
            case '1' -> 1;
            case '2', '3' -> 2;
            case '4', '5', '6', '7' -> 3;
            default -> 4;
        };
        len = bitLengthToLen(bitLength);
        d = new long[len];

        for (int i = 0; i < len; i++) {
            int beginIndex = Math.max(0, hexString.length() - 1 - (BASE_BITS * i + BASE_BITS - 1) / 4);
            int endIndex = Math.max(0, hexString.length() - 1 - (BASE_BITS * i) / 4);
            int rightShiftBits = (BASE_BITS * i) % 4;
            String substring = hexString.substring(beginIndex, endIndex + 1);
            d[i] = (Long.parseLong(substring, 16) >> rightShiftBits) & BASE_MASK;
        }
    }


    private BigInteger(long[] d, int len) {
        this(d, len, 1);
    }

    private BigInteger(long[] d, int len, int sign) {
        this.d = d;
        this.len = len;
        this.sign = sign;
    }

    public static BigInteger of(long value) {
        if (value >= 0 && value < CACHE_NUMBER) {
            return cache[(int) value];
        } else {
            return new BigInteger(value);
        }
    }

    // 总有效bit数
    int bitLength() {
        return (len - 1) * BASE_BITS + (Long.SIZE - Long.numberOfLeadingZeros(d[len - 1]));
    }

    int numberOfTrailingZeros() {
        int s = 0;
        for (int i = 0; i < len; i++) {
            long temp = d[i];
            for (int j = 0; j < BASE_BITS && (temp & 1) == 0; j++) {
                s++;
                temp >>= 1;
            }
            if (temp != 0) {
                break;
            }
        }
        return s;
    }

    // bit数转数组长度
    private static int bitLengthToLen(int bitLength) {
        int l = bitLength / BASE_BITS;
        if (l * BASE_BITS == bitLength) {
            return l;
        } else {
            return l + 1;
        }
    }

    public boolean testBit(int n) {
        int i = n / BASE_BITS;
        return ((d[i] >> (n - i * BASE_BITS)) & 1) == 1;
    }

    // 数组左移
    private BigInteger shiftArrayLeft(int n) {
        long[] array = new long[len + n];
        System.arraycopy(d, 0, array, n, len);
        return new BigInteger(array, array.length);
    }

    // 数组右移
    private BigInteger shiftArrayRight(int n) {
        long[] array = new long[len - n];
        System.arraycopy(d, n, array, 0, array.length);
        return new BigInteger(array, array.length);
    }

    // 右移
    public BigInteger shiftRight(int bitsShift) {
        int shiftArrayNumber = bitsShift / BASE_BITS;
        int bitsShiftRight = bitsShift % BASE_BITS;
        if (bitsShiftRight == 0) {
            return this.shiftArrayRight(shiftArrayNumber);
        } else {
            BigInteger res = this.copy();
            int bitLength = this.bitLength();
            int bitsShiftLeftPadding = BASE_BITS - bitsShiftRight;
            res.len = bitLengthToLen(bitLength - bitsShift);
            for (int i = 0; i < res.len; i++) {
                res.d[i] = (this.d[i + shiftArrayNumber] >> bitsShiftRight);
                if (i + shiftArrayNumber + 1 < len) {
                    res.d[i] = res.d[i] | ((this.d[i + shiftArrayNumber + 1] << bitsShiftLeftPadding) & BASE_MASK);
                }
            }
            return res;
        }
    }

    public BigInteger add(BigInteger val) {
        BigInteger res;
        if (sign * val.sign < 0) {
            if (this.compareTo(val) >= 0) {
                res = this.subtractRaw(val);
                res.sign = this.sign;
            } else {
                res = val.subtractRaw(this);
                res.sign = val.sign;
            }
        } else {
            res = addRaw(val);
            res.sign = sign;
        }
        return res;
    }

    // 小整数加法
    public BigInteger add(long val) {
        return add(BigInteger.of(val));
    }

    // 加法
    public BigInteger addRaw(BigInteger val) {
        long[] array = new long[Math.max(len, val.len) + 1];
        System.arraycopy(d, 0, array, 0, len);

        for (int i = 0; i < val.len; i++) {
            array[i] += val.d[i];
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] >= BASE) {
                array[i + 1]++;
                array[i] -= BASE;
            }
        }
        if (array[array.length - 1] == 0) {
            return new BigInteger(array, array.length - 1);
        } else {
            return new BigInteger(array, array.length);
        }
    }

    public BigInteger subtract(BigInteger val) {
        BigInteger res;
        if (sign * val.sign > 0) {
            if (this.compareTo(val) >= 0) {
                res = this.subtractRaw(val);
                res.sign = this.sign;
            } else {
                res = val.subtractRaw(this);
                res.sign = -val.sign;
            }
        } else {
            res = addRaw(val);
            res.sign = sign;
        }
        return res;
    }

    // 减法
    public BigInteger subtractRaw(BigInteger val) {
        long[] array = new long[Math.max(len, val.len)];
        System.arraycopy(d, 0, array, 0, len);

        for (int i = 0; i < val.len; i++) {
            array[i] -= val.d[i];
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < 0) {
                array[i + 1]--;
                array[i] += BASE;
            }
        }
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] > 0) {
                return new BigInteger(array, i + 1);
            }
            if (array[i] < 0) {
                return new BigInteger(array, 0);
            }
        }
        return new BigInteger(array, 1);
    }

    // 小整数减法
    public BigInteger subtract(long val) {
        return subtract(BigInteger.of(val));
    }


    // 乘法
    public BigInteger multiply(BigInteger val) {
        if (this.equals(0) || val.equals(0)) {
            return BigInteger.of(0);
        }

        long[] array = new long[len + val.len];

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < val.len; j++) {
                array[i + j] += d[i] * val.d[j];

            }
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] >= BASE) {
                long q = array[i] / BASE;
                array[i + 1] += q;
                array[i] -= q * BASE;
            }
        }
        if (array[array.length - 1] == 0) {
            return new BigInteger(array, array.length - 1, sign * val.sign);
        } else {
            return new BigInteger(array, array.length, sign * val.sign);
        }
    }

    // 乘法取模
    public BigInteger multiplyMod(BigInteger val, BigInteger m) {
        return this.multiply(val).mod(m);
    }

    // 除法
    public BigInteger divide(BigInteger val) {
        if (this.compareTo(val) < 0) {
            return BigInteger.of(0);
        }

        long[] array = new long[len - val.len + 1];
        BigInteger dividend = this.copy();
        for (int i = dividend.len - val.len; i >= 0; i--) {
            BigInteger temp = val.shiftArrayLeft(i);
            while (dividend.subtractRaw(temp).len != 0) {
                array[i]++;
                dividend = dividend.subtractRaw(temp);
            }
        }
        if (array[array.length - 1] == 0) {
            return new BigInteger(array, array.length - 1, sign * val.sign);
        } else {
            return new BigInteger(array, array.length, sign * val.sign);
        }
    }

    public BigInteger mod(BigInteger val) {
        if (this.compareToRaw(val) < 0) {
            return this.copy();
        } else {
            return this.subtract(this.divide(val).multiply(val));
        }
    }

    public BigInteger powMod(BigInteger exponent, BigInteger m) {
        BigInteger ans = BigInteger.of(1);
        BigInteger a = this.copy();
        for (int i = 0, bitLength = exponent.bitLength(); i < bitLength; i++) {
            if (exponent.testBit(i)) {
                ans = ans.multiplyMod(a, m);
            }
            a = a.multiplyMod(a, m);
        }
        return ans;
    }

    public BigInteger inverse(BigInteger n) {
        BigInteger y = gcdExtend(n, this)[2];
        if (y.sign == -1) {
            y = y.add(n);
        }
        if (!y.multiplyMod(this, n).equals(1)) {
            throw new IllegalArgumentException("求逆错误" + "this:\n" + this + "\nn:\n" + n);
        }
        return y;
    }

    public BigInteger[] gcdExtend(BigInteger a, BigInteger b) {
        if (b.equals(0)) {
            BigInteger x1 = BigInteger.of(1);
            BigInteger y1 = BigInteger.of(0);
            return new BigInteger[]{a, x1, y1};
        } else {
            BigInteger[] temp = gcdExtend(b, a.mod(b));
            BigInteger r = temp[0];
            BigInteger x1 = temp[1];
            BigInteger y1 = temp[2];

            BigInteger y = x1.subtract(a.divide(b).multiply(y1));
            return new BigInteger[]{r, y1, y};
        }
    }

    public static BigInteger random(int maxBitsBinary) {
        int maxLen = (int) Math.ceil((float) maxBitsBinary / BASE_BITS);
        long[] d = new long[maxLen];
        for (int i = 0; i < maxLen; i++) {
            d[i] = RANDOM.nextInt() & BASE_MASK;
        }
        return new BigInteger(d, maxLen);
    }


    public static BigInteger probablePrime(int maxBitsBinary) {
        while (true) {
            BigInteger randomPrime = BigInteger.random(maxBitsBinary);
            randomPrime.d[0] |= 1;
            if (randomPrime.isProbablePrime()) {
                return randomPrime;
            }
        }
    }

    private static final int[] smallPrimes = {3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};

    public boolean isProbablePrime() {
        for (int prime : smallPrimes) {
            if (this.mod(new BigInteger(prime)).equals(0)) {
                return this.equals(prime);
            }
        }
        BigInteger nSubtract1 = this.subtract(1);
        int s = nSubtract1.numberOfTrailingZeros();
        BigInteger d = this.shiftRight(s);
        for (int i = 0; i < millerRabinRepeat; i++) {
            BigInteger a = random(this.bitLength() - 1);
            BigInteger x = a.powMod(d, this);
            BigInteger y = BigInteger.of(0);
            for (int j = 0; j < s; j++) {
                y = x.multiplyMod(x, this);
                if (y.equals(1) && !x.equals(1) && !x.equals(nSubtract1)) {
                    return false;
                } else {
                    x = y;
                }
            }
            if (!y.equals(1)) {
                return false;
            }
        }
        return true;
    }

    public BigInteger copy() {
        return new BigInteger(Arrays.copyOf(d, d.length), len);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigInteger that = (BigInteger) o;
        if (len != that.len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (d[i] != that.d[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(int o) {
        if (o == 0) {
            return len == 1 && d[0] == 0;
        }
        int i = 0;
        for (; o != 0; i++) {
            if ((o & BASE_MASK) != d[i]) {
                return false;
            }
            o >>= BASE_BITS;
        }

        return i == len;
    }

    @Override
    public int compareTo(BigInteger o) {
        if (sign * o.sign == 1) {
            return sign * this.compareToRaw(o);
        } else {
            return sign;
        }
    }

    public int compareToRaw(BigInteger o) {
        if (len > o.len) {
            return 1;
        }
        if (len < o.len) {
            return -1;
        }
        return (int) (d[len - 1] - o.d[len - 1]);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(len);
        result = 31 * result + Arrays.hashCode(d);
        return result;
    }

    @Override
    public String toString() {
        if (len == 1 && d[0] == 0) {
            return "0";
        }

        LinkedList<Integer> out = new LinkedList<>();
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < BASE_BITS; j++) {
                out.add((int) ((d[i] >> j) & 1));
            }
        }
        long temp = d[len - 1];
        while (temp != 0) {
            out.add((int) (temp & 1));
            temp >>= 1;
        }
        StringBuilder sb = new StringBuilder();
        if (sign == -1) {
            sb.append('-');
        }
        int hexNum = 0, rem = out.size() % 4;
        for (int i = 0; i < rem; i++) {
            hexNum = hexNum << 1 | out.pollLast();
        }
        if (rem != 0) {
            sb.append((char) (hexNum + (hexNum <= 9 ? 48 : 55)));
        }

        while (!out.isEmpty()) {
            hexNum = out.pollLast() << 3 | out.pollLast() << 2 | out.pollLast() << 1 | out.pollLast();
            sb.append((char) (hexNum + (hexNum <= 9 ? 48 : 55)));
        }

        return sb.toString();
    }

    public String toString(int specilen) {
        String str = this.toString();
        if (str.length() > specilen) {
            throw new IllegalArgumentException("toString");
        }
        if (str.length() == specilen) {
            return str;
        } else {
            return "0".repeat(specilen - str.length()) + str;
        }
    }
}
