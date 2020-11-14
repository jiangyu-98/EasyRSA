package jiangyu;

import java.lang.Math;
import java.lang.invoke.VolatileCallSite;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;


public class BigInteger implements Comparable<BigInteger> {
    static final int BASE_BITS = 6;
    static final long BASE = 1 << BASE_BITS;
    static final long BASE_MASK = (1 << BASE_BITS) - 1;
    static final Random RANDOM = new Random();
    static int millerRabinRepeat = 50;
    static int smallIntegerDLength = bitLengthToLen(64);
    int len = 0;
    long[] d;
    int bitLength;
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
        if (hexString.charAt(0) == '-') {
            hexString = hexString.substring(1, hexString.length());
            sign = -1;
        }

        bitLength = (hexString.length() - 1) * 4 + switch (hexString.charAt(0)) {
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

    // bit数转数组长度
    private static int bitLengthToLen(int bitLength) {
        int l = bitLength / BASE_BITS;
        if (l * BASE_BITS == bitLength) {
            return l;
        } else {
            return l + 1;
        }
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
            for (int i = 0; i < res.len - 1; i++) {
                res.d[i] = (this.d[i + shiftArrayNumber] >> bitsShiftRight) |
                        ((this.d[i + shiftArrayNumber + 1] << bitsShiftLeftPadding) & BASE_MASK);
            }
            res.d[res.len - 1] = this.d[res.len - 1 + shiftArrayNumber] >> bitsShiftRight;
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
        return this.subtract(this.divide(val).multiply(val));
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
        gcdExtend(n, this);
        if (y.sign == -1) {
            y = y.add(n);
        }
        return y;
    }

    BigInteger x, y;

    void gcdExtend(BigInteger a, BigInteger b) {
        System.out.println("a = "  + a +", b = " + b);
        if (!b.equals(0)) {
            gcdExtend(b, a.mod(b));
        }
        if (b.equals(0)) {
            x = BigInteger.of(1);
            y = BigInteger.of(0);
        } else {
            BigInteger temp = x;
            x = y;
            y = temp.subtract(a.divide(b).multiply(y));
        }

        System.out.println("x = "  + x +", y = " + y);
    }

    public boolean testBit(int n) {
        int i = n / BASE_BITS;
        return ((d[i] >> (n - i * BASE_BITS)) & 1) == 1;
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
            if (randomPrime.isProbablePrime()) {
                return randomPrime;
            }
        }
    }

    private static final int[] smallPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};

    public boolean isProbablePrime() {
        for (int prime : smallPrimes) {
            if (this.mod(new BigInteger(prime)).equals(0)) {
                return false;
            }
        }
        BigInteger nSubtract1 = this.subtract(1);
        int s = 0;
        for (int i = 0; i < nSubtract1.len; i++) {
            long temp = nSubtract1.d[i];
            while ((temp & 1) == 0) {
                s++;
                temp >>= 1;
            }
            if (temp != 0) {
                break;
            }
        }
        BigInteger d = this.shiftRight(s);
        for (int i = 0; i < millerRabinRepeat; i++) {
            BigInteger a = random(this.bitLength() - 1);
            BigInteger x = a.powMod(d, this);
            for (int j = 0; j < s; j++) {
                BigInteger y = x.multiplyMod(x, this);
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

    public BigInteger set(BigInteger val) {
        d = val.d;
        len = val.len;
        return this;
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
        for (int i = 0; o != 0; i++) {
            if ((o & BASE_MASK) != d[i]) {
                return false;
            }
            o >>= BASE_BITS;
            i++;
        }
        return true;
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
}
