package jiangyu;

import java.lang.Math;
import java.util.Arrays;

public class BigInteger {
    static final long BASE_BITS = 10;
    static final long BASE = 1 << BASE_BITS;// 进制
    static final long BASE_MASK = 1 << (BASE_BITS + 1) - 1;
    int len = 1;// 数字长度,数字0我们认为是1位
    final long[] d;


    BigInteger(String hexStr, int maxBitsBinary) {
        int maxLen = (int) Math.ceil((float) maxBitsBinary / BASE_BITS);
        d = new long[maxLen];
        len = (int) Math.ceil((float) hexStr.length() * 4 / BASE_BITS);

        long temp = 0;
        int bits = 0;
        int p = 0;
        for (int i = 0; i < hexStr.length(); i++) {
            temp = (temp << 4) | (hexStr.charAt(i) > '9' ? hexStr.charAt(i) - '7' : hexStr.charAt(i) - '0');
            bits += 4;
            if (bits >= BASE_BITS) {
                d[p++] = temp & BASE_MASK;
                bits -= BASE_BITS;
                temp >>= BASE_BITS;
            }
        }
        if (bits > 0) {
            d[p] = temp;
        }
    }


    private BigInteger(long[] d, int len) {
        this.d = d;
        this.len = len;
        // long num =
    }


    // 格式化BigNum
    void format() {
        // 格式化每一位
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
        // 确定真实长度
        for (int i = len - 1; i >= 0; i--) {
            if (d[i] > 0) {
                len = i + 1;
                return;
            }
            // 判断是否为负数,用len=0标示其为负数
            if (d[i] < 0) {
                len = 0;
                return;
            }
        }
        // 如果该数所有位数均为0，那么我们认为其长度为1，值为0
        len = 1;
    }

    @Override
    public String toString() {
        long temp = 0;
        int bits = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            temp = (temp << BASE_BITS) | d[i];
            bits += BASE_BITS;
            while (bits >= 4) {
                long num = temp & 0xF;
                if (num > 9) {
                    sb.append(num);
                } else {
                    sb.append((char) (num + 55));
                }
                bits -= 4;
            }
        }
        return sb.toString();
    }

    // 左移
    public BigInteger shiftLeft(int n) {
        BigInteger ans = BigInteger.of(len + n);
        System.arraycopy(d, 0, ans.d, n, len);
        for (int i = 0; i < n; i++) {
            ans.d[i] = 0;
        }
        return ans;
    }

    // 加法
    public BigInteger add(BigInteger val) {
        BigInteger ans = BigInteger.of(val);
        ans.len = Math.max(len, val.len);
        for (int i = 0; i < val.len; i++) {
            ans.d[i] += val.d[i];
        }
        ans.format();
        return ans;
    }

    // 减法
    public BigInteger subtract(BigInteger val) {
        BigInteger ans = BigInteger.of(val);
        ans.len = Math.max(len, val.len);
        for (int i = 0; i < val.len; i++) {
            ans.d[i] -= val.d[i];
        }
        ans.format();
        return ans;
    }

    // 乘法
    public BigInteger multiply(BigInteger val) {
        BigInteger ans = BigInteger.of(len + val.len);
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < val.len; j++) {
                ans.d[i + j] += d[i] * val.d[j];
            }
        }
        ans.format();
        return ans;
    }

    // 除法
    public BigInteger divide(BigInteger val) {
        BigInteger ans = BigInteger.of(len - val.len + 1);
        BigInteger dividend = BigInteger.of(this);
        for (int i = dividend.len - val.len; i >= 0; i--) {
            BigInteger temp = val.shiftLeft(i);//核心
            while (dividend.subtract(temp).len != 0) {//核心
                ans.d[i]++; //核心
                dividend = dividend.subtract(temp); //核心
            }
        }
        ans.format();
        return ans;
    }


    public static BigInteger of(BigInteger val) {
        long[] dNew = Arrays.copyOf(val.d, val.d.length);
        return new BigInteger(dNew, val.len);
    }

    public static BigInteger of(int len) {
        long[] dNew = new long[len];
        return new BigInteger(dNew, len);
    }
}
