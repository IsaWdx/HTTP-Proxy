import java.security.MessageDigest;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Coder {
    public Coder() {
    }
    public static String Digest(String myinfo) {
        String newUri = "";
        try {
            java.security.MessageDigest a=java.security.MessageDigest.getInstance("MD5");
            //java.security.MessageDigest a = java.security.MessageDigest.getInstance("SHA-1");
            a.update(myinfo.getBytes());
            byte[] digesta = a.digest();
            newUri = byte2hex(digesta);
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("exception:"+e);
        }
        return newUri;
    }
    public static String byte2hex(byte[] b)
    {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1){
                hs = hs + "0" + stmp;
            }else{
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

}

