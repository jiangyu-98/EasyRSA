package jiangyu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RSATest {

    @Test
    void generateKey() {
        RSA.Key key = RSA.generateKey(1024);
        System.out.println(key);
    }
}