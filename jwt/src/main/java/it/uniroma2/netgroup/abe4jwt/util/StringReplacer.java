package it.uniroma2.netgroup.abe4jwt.util;

//workaround to encode valid values into ABE policy
//see also https://github.com/zeutro/openabe/issues/54
public class StringReplacer {
    public static String replace(String s) {
    	StringBuffer replaced=new StringBuffer();
    	char[] c=s.toCharArray();
    	for (int i=0;i<c.length;i++) {
//    		if ((c[i]>=32&&c[i]<=47)
//    			||(c[i]>=58&&c[i]<=64) 
//    			||(c[i]>=91&&c[i]<=95) 
//    			||(c[i]>=123&&c[i]<=126))
    		if (c[i]=='+'
    		  ||c[i]==' ')
    			replaced.append("%"+Integer.toHexString(c[i]).toUpperCase());
    		else 
    			replaced.append(c[i]);
    	}
    	return replaced.toString();
    }

}
