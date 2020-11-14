package jiangyu;

import java.lang.Math;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;


public class BigInteger implements Comparable<BigInteger> {
    static final int BASE_BITS = 14;
    static final long BASE = 1 << BASE_BITS;
    static final long BASE_MASK = (1 << BASE_BITS) - 1;
    static final Random RANDOM = new Random();
    static int millerRabinRepeat = 50;
    int len = 1;
    final long[] d;
    int maxBitsBinary;

    BigInteger(String hexString, int maxBitsBinary) {
        int maxLen = (int) Math.ceil((float) maxBitsBinary / BASE_BITS);
        d = new long[maxLen];
        int bitsNum = (hexString.length() - 1) * 4;
        for (int highestChar = Integer.parseInt(hexString.substring(0, 1), 16); highestChar != 0; highestChar >>= 1) {
            bitsNum++;
        }
        len = (int) Math.ceil((float) bitsNum / BASE_BITS);
        for (int i = 0; i < len; i++) {
            int beginIndex = Math.max(0, hexString.length() - 1 - (BASE_BITS * i + BASE_BITS - 1) / 4);
            int endIndex = Math.max(0, hexString.length() - 1 - (BASE_BITS * i) / 4);
            int rightShiftBits = (BASE_BITS * i) % 4;
            String substring = hexString.substring(beginIndex, endIndex + 1);
            d[i] = (Long.parseLong(substring, 16) >> rightShiftBits) & BASE_MASK;
        }
    }


    BigInteger(long value, int maxBitsBinary) {
        int maxLen = (int) Math.ceil((float) maxBitsBinary / BASE_BITS);
        d = new long[maxLen];
        len = 0;
        this.maxBitsBinary = maxBitsBinary;
        while (value != 0) {
            d[len] = value & BASE_MASK;
            value >>= BASE_BITS;
            len++;
        }
    }

    public BigInteger(short maxLen) {
        this.d = new long[maxLen];
    }

    private BigInteger(long[] d, int len) {
        this.d = d;
        this.len = len;
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

    // 左移
    private BigInteger shiftArrayLeft(int n) {
        BigInteger ans = new BigInteger((short) d.length);
        ans.len = len + n;
        System.arraycopy(d, 0, ans.d, n, len);
        for (int i = 0; i < n; i++) {
            ans.d[i] = 0;
        }
        return ans;
    }

    // 右移
    public BigInteger shiftRight(int bitsShift) {
        BigInteger res = this.copy();
        int shiftArrayNumber = bitsShift / BASE_BITS;
        int bitsShiftRight = bitsShift % BASE_BITS;
        if (bitsShiftRight == 0) {
            res.shiftArrayLeft(shiftArrayNumber);
        } else {
            int bitLength = this.bitLength();
            int bitsShiftLeftPadding = BASE_BITS - bitsShiftRight;
            res.len = bitLengthToLen(bitLength - bitsShift);
            for (int i = 0; i < res.len - 1; i++) {
                res.d[i] = (this.d[i + shiftArrayNumber] >> bitsShiftRight) |
                        ((this.d[i + shiftArrayNumber + 1] << bitsShiftLeftPadding) & BASE_MASK);
            }
            res.d[res.len - 1] = this.d[res.len -1 + shiftArrayNumber] >> bitsShiftRight;
        }
        return res;
    }

    // 加法
    public BigInteger add(BigInteger val) {
        BigInteger ans = this.copy();
        ans.len = Math.max(len, val.len);
        for (int i = 0; i < val.len; i++) {
            ans.d[i] += val.d[i];
        }
        ans.format();
        return ans;
    }

    // 小整数加法
    public BigInteger add(long val) {
        BigInteger res = this.copy();
        for (int i = 0; i < len && val != 0; i++) {
            res.d[i] += val & BASE_MASK;
            val >>= BASE_BITS;
        }
        res.format();
        return res;
    }

    // 原地小整数减法
    public BigInteger addEqual(long val) {
        for (int i = 0; i < len && val != 0; i++) {
            d[i] += val & BASE_MASK;
            val >>= BASE_BITS;
        }
        format();
        return this;
    }

    // 减法
    public BigInteger subtract(BigInteger val) {
        BigInteger ans = this.copy();
        ans.len = Math.max(len, val.len);
        for (int i = 0; i < val.len; i++) {
            ans.d[i] -= val.d[i];
        }
        ans.format();
        return ans;
    }

    // 小整数减法
    public BigInteger subtract(long val) {
        BigInteger res = this.copy();
        for (int i = 0; i < len && val != 0; i++) {
            res.d[i] -= val & BASE_MASK;
            val >>= BASE_BITS;
        }
        res.format();
        return res;
    }

    // 原地小整数减法
    public BigInteger subtractEqual(long val) {
        for (int i = 0; i < len && val != 0; i++) {
            d[i] -= val & BASE_MASK;
            val >>= BASE_BITS;
        }
        format();
        return this;
    }

    // 乘法
    public BigInteger multiply(BigInteger val) {
        BigInteger ans = new BigInteger((short) d.length);
        ans.len = len + val.len;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < val.len; j++) {
                if (i + j < d.length) {
                    ans.d[i + j] += d[i] * val.d[j];
                }
            }
        }
        ans.format();
        return ans;
    }

    // 乘法
    public BigInteger multiplyMod(BigInteger val, BigInteger m) {
        BigInteger ans = new BigInteger((short) Math.max(d.length, len + val.len));
        ans.len = len + val.len;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < val.len; j++) {
                if (i + j < d.length) {
                    ans.d[i + j] += d[i] * val.d[j];
                }
            }
        }
        ans = ans.mod(m);
        ans.format();
        return ans;
    }

    // 除法
    public BigInteger divide(BigInteger val) {
        BigInteger ans = new BigInteger((short) d.length);
        ans.len = len - val.len + 1;
        BigInteger dividend = this.copy();
        for (int i = dividend.len - val.len; i >= 0; i--) {
            BigInteger temp = val.shiftArrayLeft(i);//核心
            while (dividend.subtract(temp).len != 0) {//核心
                ans.d[i]++; //核心
                dividend = dividend.subtract(temp); //核心
            }
        }
        ans.format();
        return ans;
    }

    public BigInteger powMod(BigInteger exponent, BigInteger m) {
        BigInteger ans = new BigInteger(1, 1024);
        BigInteger a = this.copy();
        while (!exponent.equals(0)) {
            if ((exponent.d[0] & 1) == 1) {//末位是1
                ans = ans.multiplyMod(a, m);
            }
            a = a.multiplyMod(a, m);
            exponent.shiftRight(1);
        }
        return ans;
    }

    public BigInteger mod(BigInteger val) {
        return this.subtract(this.divide(val).multiply(val));
    }

    public BigInteger mod(int val) {
        BigInteger valBig = new BigInteger(val, maxBitsBinary);
        return this.subtract(this.divide(valBig).multiply(valBig));
    }

    public BigInteger inverse(BigInteger n) {
        gcdExtend(n, this);
        if (signY == -1) {
            y = n.subtract(y);
        }
        return y;
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

    public static BigInteger random(BigInteger maxVal, int maxBitsBinary) {

        return new BigInteger(1, maxBitsBinary);
    }


    private BigInteger x, y;
    private int signY;

    void gcdExtend(BigInteger a, BigInteger b) {
        if (b.equals(0)) {
            x = new BigInteger(1, maxBitsBinary);
            signY = 1;
            y = new BigInteger(0, maxBitsBinary);
            return;
        }
        gcdExtend(b, a.mod(b));
        BigInteger temp = a.divide(b).multiply(x);
        if (temp.compareTo(y) > 0) {
            signY = -1;
            y = temp.subtract(y);
        } else {
            y = y.subtract(temp);
        }
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
            if (this.mod(prime).equals(0)) {
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
            BigInteger a = random(this.subtract(3), 1024).add(2);
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

    // 格式化
    private void format() {
        for (int i = 0; i < len; i++) {
            if (d[i] < 0) {
                d[i] += BASE;
                d[i + 1]--;
            }
            if (d[i] >= BASE) {
                long q = d[i] / BASE;
                d[i + 1] += q;
                d[i] -= q * BASE;
            }
        }
        for (int i = len; i >= 0; i--) {
            if (d[i] > 0) {
                len = i + 1;
                return;
            }
            if (d[i] < 0) {
                len = 0;
                return;
            }
        }
        len = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigInteger that = (BigInteger) o;
        return len == that.len &&
                Arrays.equals(d, that.d);
    }

    public boolean equals(int o) {
        int i = 0;
        while (o != 0) {
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
