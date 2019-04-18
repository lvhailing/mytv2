package com.jbtm.parentschool.early.edu.aes;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lvhailing on 2018/12/18.
 */

public class AES256 {
    static {
        /*AES 加密默认128位的key，这里改成256位的（类在下面粘出来了）*/
        UnlimitedKeyStrengthJurisdictionPolicy.ensure();
    }

    /*加密算法*/
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * @param srcData 要加密的数组（String 需要base64 编码）
     * @param key     公钥，32位byte数组
     * @param iv      私钥，16位byte数组
     * @return 加密后的byte数组
     * @throws Exception 找不到加密算法等
     */
    public static byte[] AES_cbc_encrypt(byte[] srcData, byte[] key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {

        Cipher.getMaxAllowedKeyLength(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

        return cipher.doFinal(srcData);
    }

    /**
     * @param encData 要解密的数组
     * @param key     公钥
     * @param iv      私钥
     * @return 解密后的byte数组
     * @throws Exception 找不到解密算法等
     */
    private static byte[] AES_cbc_decrypt(byte[] encData, byte[] key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {

        Cipher.getMaxAllowedKeyLength(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

        return cipher.doFinal(encData);
    }
}
