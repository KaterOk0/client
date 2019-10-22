package com.example.client;

import java.util.Random;

public class utils {
    public static Key generate_keypair(int p, int q) throws Exception{
        if(!isPrime(p) || !isPrime(q))
            throw new Exception("Both numbers must be prime!");
        if (p == q)
            throw new Exception("P and Q cannot be equal!");
        Key key = new Key();
        key.setN(p * q);
        int fi = (p - 1) * (q - 1);
        int e = 0;
        while (gcd(e, fi) != 1){
            Random rn = new Random();
            e = rn.nextInt(fi - 2 + 1) + 2;

        }

        //int d = multiplicative_inverse(e,fi);
        return key;
    }

    public static class Key {
        int e;
        int n;

        public int getE() {
            return e;
        }

        public void setE(int e) {
            this.e = e;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }
    }

    // простое ли число х
    public static boolean isPrime(int x) {
        if (x == 2) return true;
        if (x < 2 || x % 2 == 0) return false;
        for (int i = 3; i < Math.sqrt(x); i += 2) {
            if (x % i == 0) return false;
        }
        return true;
    }

    /**
     * greatest common divisor
     * @param a
     * @param b
     * @return
     */
    private static int gcd(int a, int b) {
        if (b == 0) return a;
        else return gcd(b, a % b);
    }
}

