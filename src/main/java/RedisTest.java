import org.junit.jupiter.api.*;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static redis.clients.jedis.ListPosition.AFTER;
import static redis.clients.jedis.ListPosition.BEFORE;
import static redis.clients.jedis.ZParams.Aggregate.*;
import static redis.clients.jedis.args.FlushMode.ASYNC;
import static redis.clients.jedis.args.FlushMode.SYNC;

/**
 * @author 1fragment
 */
public class RedisTest {
    /**ENV*/
    String hostDynomiteA = "Modify to your sync source";
    int portDynomiteA =50000;
    String A =hostDynomiteA+":"+portDynomiteA+"> ";
    String hostDynomiteB = "Modify to your sync target";
    int portDynomiteB =51000;
    String B =hostDynomiteB+":"+portDynomiteB+"> ";

    Jedis jedisA;
    Jedis jedisB;
    JedisPool jedisPoolA;
    JedisPool jedisPoolB;

    @BeforeEach
    void initEach(){
        JedisPoolConfig config=new JedisPoolConfig();
        jedisPoolA= new JedisPool(config,hostDynomiteA,portDynomiteA);
        jedisPoolB = new JedisPool(hostDynomiteB,portDynomiteB);
        jedisA= jedisPoolA.getResource();
        jedisB = jedisPoolB.getResource();
    }

    /*Redis Commands Supported
     *https://github.com/Netflix/dynomite/blob/dev/notes/redis.md */

    /*Redis Commands Supported-Keys Command*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_DEL")
    void delTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Boolean hasnotdeleted=jedisB.exists(key);
        System.out.println(B+"EXISTS "+key+" => "+hasnotdeleted);
        jedisA.del(key);
        System.out.println(A+"DEL "+key);
        Boolean hasdeleted=jedisB.exists(key);
        System.out.println(B+"EXISTS "+key+" => "+hasdeleted);
        assertEquals(true,hasnotdeleted,"PASS");
        assertEquals(false,hasdeleted,"PASS");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_DUMP")
    void dumpTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value="测试dump命令"+timeStampstr;
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        String value_before= jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value_before);
        byte[] dump_a =jedisA.dump(key);
        System.out.println(A+"DUMP "+key+" => "+dump_a);
        jedisA.del(key);
        System.out.println(A+"DEL "+key);
        Boolean hasdeleted= jedisB.exists(key);
        System.out.println(B+"EXISTS "+key+" => "+hasdeleted);
        jedisB.restore(key,0,dump_a);
        System.out.println(B+"RESTORE "+key+" 0 "+dump_a);
        String value_berestore= jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value_berestore);
        assertEquals(value_before,value,"PASS");
        assertEquals(hasdeleted,false,"PASS");
        assertEquals(value_berestore,value,"PASS");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_RESTORE")
    void restoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value="测试restore命令"+timeStampstr;
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        String value_before= jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value_before);
        byte[] dump_a =jedisA.dump(key);
        System.out.println(A+"DUMP "+key+" => "+dump_a);
        jedisA.del(key);
        System.out.println(A+"DEL "+key);
        Boolean hasdeleted= jedisB.exists(key);
        System.out.println(B+"EXISTS "+key+" => "+hasdeleted);
        jedisA.restore(key,0,dump_a);
        System.out.println(A+"RESTORE "+key+" 0 "+dump_a);
        Boolean hasrestoreed= jedisB.exists(key);
        System.out.println(B+"EXISTS "+key+" => "+hasrestoreed);
        String value_berestore= jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value_berestore);
        assertEquals(value_before,value,"PASS");
        assertEquals(hasdeleted,false,"PASS");
        assertEquals(hasrestoreed,true,"PASS");
        assertEquals(value_berestore,value,"PASS");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_EXISTS")
    void existsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Boolean existsa=jedisA.exists(key);
        System.out.println(A+"EXISTS "+key+" => "+existsa);
        Boolean existsb=jedisB.exists(key);
        System.out.println(B+"EXISTS "+key+" => "+existsb);
        assertTrue(existsa,"Pass");
        assertEquals(existsa,existsb,"PASS");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_EXPIRE")
    void expireTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Long ttlabeforeexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlabeforeexpire);
        Long ttlbbeforeexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbbeforeexpire);
        assertEquals(ttlabeforeexpire,ttlbbeforeexpire,"PASS");
        assertEquals(ttlabeforeexpire,-1,"PASS");
        long ex=jedisA.expire(key,60);
        System.out.println(A+"EXPIRE "+key+" => "+ex);
        Long ttlaafterexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlaafterexpire);
        Long ttlbafterexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbafterexpire);
        assertEquals(ttlaafterexpire,ttlbafterexpire,"PASS");
        assertTrue(60>=ttlaafterexpire && ttlaafterexpire>0);
        assertTrue(60>=ttlbafterexpire && ttlbafterexpire>0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_PEXPIRE")
    void pexpireTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int second=60;
        long millisecond=second*1000;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Long ttlabeforepexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlabeforepexpire);
        Long ttlbbeforepexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbbeforepexpire);
        assertEquals(ttlabeforepexpire,ttlbbeforepexpire,"PASS");
        assertEquals(ttlabeforepexpire,-1,"PASS");
        long pex=jedisA.pexpire(key,millisecond);
        System.out.println(A+"PEXPIRE "+key+" => "+pex);
        Long ttlaafterpexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlaafterpexpire);
        Long ttlbafterpexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbafterpexpire);
        assertEquals(ttlaafterpexpire,ttlbafterpexpire,"PASS");
        assertTrue(second>=ttlaafterpexpire && ttlaafterpexpire>0);
        assertTrue(second>=ttlbafterpexpire && ttlbafterpexpire>0);
        Long pttlaafterpexpire=jedisA.pttl(key);
        System.out.println(A+"PTTL "+key+" => "+pttlaafterpexpire);
        Long pttlbafterpexpire=jedisB.pttl(key);
        System.out.println(B+"PTTL "+key+" => "+pttlbafterpexpire);
        assertEquals(ttlaafterpexpire,ttlbafterpexpire,"PASS");
        assertTrue(millisecond>=pttlaafterpexpire && pttlaafterpexpire>0);
        assertTrue(millisecond>=pttlbafterpexpire && pttlbafterpexpire>0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_EXPIREAT")
    void expireatTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Long ttlabeforeexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlabeforeexpire);
        Long ttlbbeforeexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbbeforeexpire);
        assertEquals(ttlabeforeexpire,ttlbbeforeexpire,"PASS");
        assertEquals(ttlabeforeexpire,-1,"PASS");
        int second_now=Integer.valueOf(String.valueOf(System.currentTimeMillis()/1000));
        int second=60;
        int second_ex=second_now+second;
        long exat=jedisA.expireAt(key,second_ex);
        System.out.println(A+"EXPIREAT "+key+exat+" => "+exat);
        assertEquals(exat,1,"PASS");
        Long ttlaafterexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlaafterexpire);
        Long ttlbafterexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbafterexpire);
        assertEquals(ttlaafterexpire,ttlbafterexpire,"PASS");
        assertTrue(second>=ttlaafterexpire && ttlaafterexpire>second-2);
        assertTrue(second>=ttlbafterexpire && ttlbafterexpire>second-2);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_PEXPIREAT")
    void pexpireatTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Long ttlabeforeexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlabeforeexpire);
        Long ttlbbeforeexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbbeforeexpire);
        assertEquals(ttlabeforeexpire,ttlbbeforeexpire,"PASS");
        assertEquals(ttlabeforeexpire,-1,"PASS");
        long millisecond_now=System.currentTimeMillis();
        int second=60;
        long millisecond=second*1000;
        long millisecond_pex=millisecond_now+millisecond;
        long pexat=jedisA.pexpireAt(key,millisecond_pex);
        System.out.println(A+"PEXPIREAT "+key+millisecond_pex+" => "+pexat);
        assertEquals(pexat,1,"PASS");
        long ttlaafterpexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlaafterpexpire);
        long ttlbafterpexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbafterpexpire);
        long pttlaafterpexpire=jedisA.pttl(key);
        System.out.println(A+"PTTL "+key+" => "+pttlaafterpexpire);
        long pttlbafterpexpire=jedisB.pttl(key);
        System.out.println(B+"PTTL "+key+" => "+pttlbafterpexpire);
        assertTrue(ttlaafterpexpire-ttlbafterpexpire>=0 );
        assertTrue( ttlaafterpexpire-ttlbafterpexpire<2);
        assertTrue(millisecond>pttlaafterpexpire);
        assertTrue(pttlaafterpexpire>(millisecond-1000));
        assertTrue(millisecond>pttlbafterpexpire);
        assertTrue(pttlbafterpexpire>(millisecond-1000));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_KETS AB")
    void keysTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="TESTkey"+timeStampstr;
        String pattern="*"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            jedisB.keys(pattern);
            throw new IllegalArgumentException("未正确禁用该命令");
        });
        System.out.println(B+"KEYS "+pattern+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("ERR unknown command"));
        jedisA.del(key);
        System.out.println(A+"DEL "+key);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_KETS AA")
    void keysbyonesideTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="TESTkey"+timeStampstr;
        String pattern="*"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            jedisA.keys(pattern);
            throw new IllegalArgumentException("未正确禁用该命令");
        });
        System.out.println(A+"KEYS "+pattern+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("ERR unknown command"));
        jedisA.del(key);
        System.out.println(A+"DEL "+key);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_PERSIST")
    void persisttest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        long ex=jedisA.expire(key,600);
        System.out.println(A+"EXPIRE "+key+" => "+ex);
        Long ttlaafterexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlaafterexpire);
        long haspersist=jedisA.persist(key);
        System.out.println(A+"PERSIST "+key+" => "+haspersist);
        Long ttlbafterpersist=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbafterpersist);
        assertEquals(ttlbafterpersist,-1,"PASS");
        jedisA.del(key);
        System.out.println(A+"DEL "+key);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_TTL")
    void ttlTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        Long ttlabeforeexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlabeforeexpire);
        Long ttlbbeforeexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbbeforeexpire);
        assertEquals(ttlabeforeexpire,ttlbbeforeexpire,"PASS");
        assertEquals(ttlabeforeexpire,-1,"PASS");
        int second=60;
        long ex=jedisA.expire(key,second);
        System.out.println(A+"EXPIRE "+key+second+" => "+ex);
        assertEquals(ex,1,"PASS");
        Long ttlaafterexpire=jedisA.ttl(key);
        System.out.println(A+"TTL "+key+" => "+ttlaafterexpire);
        Long ttlbafterexpire=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttlbafterexpire);
        assertEquals(ttlaafterexpire,ttlbafterexpire,"PASS");
        assertTrue(second>=ttlaafterexpire && ttlaafterexpire>second-1);
        assertTrue(second>=ttlbafterexpire && ttlbafterexpire>second-1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_PTTL")
    void pttlTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int second=60;
        long millisecond=second*1000;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        long pex=jedisA.pexpire(key,millisecond);
        System.out.println(A+"PEXPIRE "+key+" => "+pex);
        Long pttlaafterpexpire=jedisA.pttl(key);
        System.out.println(A+"PTTL "+key+" => "+pttlaafterpexpire);
        Long pttlbafterpexpire=jedisB.pttl(key);
        System.out.println(B+"PTTL "+key+" => "+pttlbafterpexpire);
        assertTrue(millisecond>=pttlaafterpexpire && pttlaafterpexpire>pttlaafterpexpire-10);
        assertTrue(millisecond>=pttlbafterpexpire && pttlbafterpexpire>pttlaafterpexpire-10);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_TYPE")
    void typeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        jedisA.set(key_s,timeStampstr);
        System.out.println(A+"SET "+key_s+" "+timeStampstr);
        String type_s=jedisB.type(key_s);
        System.out.println(B+"TYPE "+key_s+" => "+type_s);
        assertEquals(type_s,"string","Strings");}
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Keys Command_SCAN_AA")
    void scanbyonesideTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_scan1="1scan"+key_s;
        String key_scan2="2scan"+key_s;
        String key_scan3="3scan"+key_s;
        String key_scan4="4scan"+key_s;
        String value_s1="fish"+key_s;
        String value_s2="foobar"+key_s;
        String value_s3="feelsgood"+key_s;
        String value_s4="ohno"+key_s;
        String set1=jedisA.set(key_scan1,value_s1);
        System.out.println(A+"SET "+key_scan1+" "+value_s1+" => "+set1);
        String set2=jedisA.set(key_scan1,value_s1);
        System.out.println(A+"SET "+key_scan2+" "+value_s2+" => "+set2);
        String set3=jedisA.set(key_scan1,value_s1);
        System.out.println(A+"SET "+key_scan3+" "+value_s3+" => "+set3);
        String set4=jedisA.set(key_scan1,value_s1);
        System.out.println(A+"SET "+key_scan4+" "+value_s4+" => "+set4);
        ScanParams scanParams=new ScanParams();scanParams.match("*redistest*");String cursor=String.valueOf(0);
        ScanResult<String> scanResult_a=jedisA.scan(cursor,scanParams);
        String cursor_a=scanResult_a.getCursor();
        List<String> resultlist_a=scanResult_a.getResult();
        int size_a=resultlist_a.size();
        System.out.println(A+"SCAN "+"0 match *redistest*"+" => "+cursor_a+resultlist_a);
    }

    /*Redis Commands Supported-Strings Command*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_APPEND")
    void appendTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String s1="Hello";
        String s2=" World";
        long l1=jedisA.append(key,s1);
        long l2=jedisA.append(key,s2);
        System.out.println(A+"APPEND "+key+" "+s1+" => "+l1);
        System.out.println(A+"APPEND "+key+" "+s2+" => "+l2);
        String s=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+s);
        assertEquals(s,s1+s2);
        assertEquals(s.length(),l2);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_BITCOUNT")
    void bitcountTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value="a1?";
        //仅支持ASCII对应字符 其他字符校验不通过
        char[] chars=value.toCharArray();
        int countchar=chars.length;
        int ret=0;
        for (int i=0;i<countchar;i++){
            char c=chars[i];
//            char[] charsbin=Integer.toBinaryString(c).toCharArray();
//            for (int n=0;n< charsbin.length;n++){
//                if(charsbin[n] =='1'){ ret++; }
//            }
            int intbin=Integer.valueOf(c);
            for (int n = 0; n < (1<<(2^7)); n++) {
                if ((intbin & (1 << n)) != 0) { ret++;}
            }
        }
        System.out.println(ret);
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        long bitcout=jedisB.bitcount(key);
        System.out.println(B+"BITCOUNT "+key+" => "+bitcout);
        assertEquals(ret,bitcout);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_DECR")
    void decrTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int value=100;
        String value_s=String.valueOf(value);
        jedisA.set(key,value_s);
        System.out.println(A+"SET "+key+" "+value_s);
        long result=jedisA.decr(key);
        System.out.println(A+"DECR "+key+" => "+result);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(result_b,Long.toString(result));
        assertEquals(Long.toString(result),Integer.toString(value-1));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_DECRBY")
    void decrByTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int value=100;
        int dec=30;
        String value_s=String.valueOf(value);
        jedisA.set(key,value_s);
        System.out.println(A+"SET "+key+" "+value_s);
        long result=jedisA.decrBy(key,dec);
        System.out.println(A+"DECRBY "+key+" "+dec+" => "+result);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(result_b,Long.toString(result));
        assertEquals(Long.toString(result),Integer.toString(value-dec));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_GET")
    void getTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        String result_a=jedisA.get(key);
        System.out.println(A+"GET "+key+" => "+result_a);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(result_b,result_a);
        assertEquals(timeStampstr,result_a);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_GETBIT")
    void getbitTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value="a1?";
        //仅支持ASCII对应字符 其他字符未处理
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        char[] strChar=value.toCharArray();
        System.out.println(strChar);
        String strbin="";
        for(int i=0;i<strChar.length;i++){
            char c=strChar[i];
            int c_int=Integer.valueOf(c);
            String c_bits=Integer.toBinaryString(c_int);
            int countbit= c_bits.toCharArray().length;
            int count0=8-countbit;
//            System.out.println("countbit="+countbit+" 需高位补0 需补位数"+count0);
            String str0="0";
            String highstr0=String.join("",Collections.nCopies(count0,str0));
            String str8=highstr0+c_bits;
            strbin +=str8;
            System.out.println(c+"->"+c_int+"->"+c_bits+"->"+str8);
        }
        char[] bins=strbin.toCharArray();
        System.out.println(bins);
        for (int i=0;i<bins.length;i++){
            Boolean ret=false;
            System.out.println("value["+i+"]"+" "+bins[i]);
            if (bins[i]=='1') { ret=true;} System.out.println(ret);
            int offset=i;
            Boolean result_a=jedisA.getbit(key,offset);
            System.out.println(A+"GETBIT "+key+" "+offset+" => "+result_a);
            Boolean result_b=jedisB.getbit(key,offset);
            System.out.println(B+"GETBIT "+key+" "+offset+" => "+result_b);
            assertEquals(result_b,result_a);
            assertEquals(ret,result_a);
        }
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_GETRANGE")
    void getrangeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value="This is a string redis.com.cn";
        int start=6;
        int end=17;
        String str=value.substring(start,end+1);
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        String result_a=jedisA.getrange(key,start,end);
        System.out.println(A+"GETRANGE "+key+" "+start+" "+end+" => "+result_a);
        String result_b=jedisB.getrange(key,start,end);
        System.out.println(B+"GETRANGE "+key+" "+start+" "+end+" => "+result_b);
        assertEquals(result_b,result_a);
        assertEquals(str,result_a);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_GETSET")
    void getsetTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value_old="old";
        String value_new="new";
        jedisA.set(key,value_old);
        System.out.println(A+"SET "+key+" "+value_old);
        String result_a=jedisA.getSet(key,value_new);
        System.out.println(A+"GETSET "+key+" "+value_new+" => "+result_a);
        String result_b=jedisB.getSet(key,value_new);
        System.out.println(B+"GETSET "+key+" "+value_new+" => "+result_b);
        String result_az=jedisA.get(key);
        System.out.println(A+"GET "+key+" => "+result_az);
        String result_bz=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_bz);
//        assertEquals(result_b,result_a);
        assertEquals(value_old,result_a);
        assertEquals(result_az,result_bz);
        assertEquals(value_new,result_bz);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_INCR")
    void incrTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int value=100;
        String value_s=String.valueOf(value);
        jedisA.set(key,value_s);
        System.out.println(A+"SET "+key+" "+value_s);
        long result=jedisA.incr(key);
        System.out.println(A+"INCR "+key+" => "+result);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(result_b,Long.toString(result));
        assertEquals(Long.toString(result),Integer.toString(value+1));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_INCRBY")
    void incrByTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int value=100;
        int inc=30;
        String value_s=String.valueOf(value);
        jedisA.set(key,value_s);
        System.out.println(A+"SET "+key+" "+value_s);
        long result=jedisA.incrBy(key,inc);
        System.out.println(A+"INCRBY "+key+" "+inc+" => "+result);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(result_b,Long.toString(result));
        assertEquals(Long.toString(result),Integer.toString(value+inc));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_INCRBYFLOAT")
    void incrByFloatTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        int value=100;
        double inc=1.001;
        String value_s=String.valueOf(value);
        jedisA.set(key,value_s);
        System.out.println(A+"SET "+key+" "+value_s);
        double result=jedisA.incrByFloat(key,inc);
        System.out.println(A+"INCRBY "+key+" "+inc+" => "+result);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(result_b,Double.toString(result));
        assertEquals(Double.toString(result),Double.toString(value+inc));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_MGET")
    void mgetTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key1="1redistest"+timeStampstr;
        String key2="2redistest"+timeStampstr;
        String value1="1value"+timeStampstr;
        String value2="2value"+timeStampstr;
        jedisA.set(key1,value1);
        System.out.println(A+"SET "+key1+" "+value1);
        jedisA.set(key2,value2);
        System.out.println(A+"SET "+key2+" "+value2);
        List<String> result_a=jedisA.mget(key1,key2);
        System.out.println(A+"MGET "+key1+" "+key2+" => "+result_a.toString());
        List<String> result_b=jedisB.mget(key1,key2);
        System.out.println(B+"MGET "+key1+" "+key2+" => "+result_b.toString());
        assertEquals(result_a,result_b);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_PSETEX")
    void psetexTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistestkey"+timeStampstr;
        String value="redistestvalue"+timeStampstr;
        int milliseconds=1000;
        jedisA.psetex(key,milliseconds,value);
        System.out.println(A+"PSETEX "+key+" "+milliseconds+" "+value);
        long ttl=jedisB.pttl(key);
        System.out.println(B+"PTTL "+key+" => "+ttl);
        String result_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result_b);
        assertEquals(value,result_b);
        assertTrue(ttl<=milliseconds && ttl>0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_SET")
    void setTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_ex="redistestkeyex"+timeStampstr;
        String key_px="redistestkeypx"+timeStampstr;
        String key="redistestkeyttl"+timeStampstr;
        String value="redistestvalue"+timeStampstr;
        int seconds=60;
        int milliseconds=2000;
        SetParams params1=new SetParams();
        params1.ex(seconds);
        SetParams params2=new SetParams();
        params2.px(milliseconds);
        SetParams params3=new SetParams();
        params3.keepttl();
        jedisA.set(key_ex,value,params1);
        System.out.println(A+"SET "+key_ex+" "+value+" EX "+seconds);
        String value_b=jedisB.get(key_ex);
        System.out.println(B+"GET "+key_ex+" => "+value_b);
        long ttl_b=jedisB.ttl(key_ex);
        System.out.println(B+"TTL "+key_ex+" => "+ttl_b);
        assertEquals(value_b,value);
        assertTrue(ttl_b<=seconds && ttl_b>0);
        jedisA.set(key_px,value,params2);
        System.out.println(A+"SET "+key_px+" "+value+" PX "+milliseconds);
        String value_bp=jedisB.get(key_px);
        System.out.println(B+"GET "+key_px+" => "+value_bp);
        long ttl_bp=jedisB.pttl(key_px);
        System.out.println(B+"PTTL "+key_px+" => "+ttl_bp);
        assertEquals(value_bp,value);
        assertTrue(ttl_bp<=milliseconds && ttl_bp>0);
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            jedisB.set(key,value,params3);
            throw new IllegalArgumentException("支持redis>=6.0");
        });
        System.out.println(B+"SET "+key+" "+value+" KEEPTTL"+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("ERR syntax error"));
        //redis >= 2.6.12: Added the EX, PX, NX and XX options.
        //redis>= 6.0: Added the KEEPTTL option.
        //redis>= 6.2: Added the GET option.
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_SETBIT")
    void setbitTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String value="A";//偏移位[2]设置值为1，效果等同与 大小写字母转化
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        int offset=2;
        Boolean result_a=jedisA.getbit(key,offset);
        System.out.println(A+"GETBIT "+key+" "+offset+" => "+result_a);
        Boolean result_b=jedisA.setbit(key,offset,true);
        System.out.println(A+"SETBIT "+key+" "+offset+" 1");
        String result=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+result);
        assertFalse(result_a);
        assertEquals(result,value.toLowerCase());
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_SETEX")
    void setexTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistestkeyex"+timeStampstr;
        String value="redistestvalue"+timeStampstr;
        int seconds=60;
        jedisA.setex(key,seconds,value);
        System.out.println(A+"SET "+key+" "+seconds+" "+value);
        String value_b=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value_b);
        long ttl_b=jedisB.ttl(key);
        System.out.println(B+"TTL "+key+" => "+ttl_b);
        assertEquals(value_b,value);
        assertTrue(ttl_b<=seconds && ttl_b>0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_SETNX")
    void setnxTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_exist="existkeyredistestkey"+timeStampstr;
        String key_noexist="noexistkeyredistestkey"+timeStampstr;
        String value1="1redistest"+timeStampstr;
        String value2="2redistest"+timeStampstr;
        jedisA.set(key_exist,value1);
        System.out.println(A+"SET "+key_exist+" "+value1);
        long changeexistkey=jedisB.setnx(key_exist,value2);
        System.out.println(B+"SETNX "+key_exist+" "+value2+" => "+changeexistkey);
        String resultb=jedisB.get(key_exist);
        System.out.println(B+"GET "+key_exist+" => "+resultb);
        assertEquals(changeexistkey,0);
        assertEquals(resultb,value1,"don't set if exists");
        long changenoexistkey=jedisA.setnx(key_noexist,value2);
        System.out.println(A+"SETNX "+key_noexist+" "+value2+" => "+changenoexistkey);
        String resultbz=jedisB.get(key_noexist);
        System.out.println(B+"GET "+key_noexist+" => "+resultbz);
        assertEquals(changenoexistkey,1);
        assertEquals(resultbz,value2,"set if not exists");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_SETRANGE")
    void setrangeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistestkey"+timeStampstr;
        String value="redistest"+timeStampstr;
        String value_set="setrange";
        int offset=12;
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        jedisA.setrange(key,offset,value_set);
        System.out.println(A+"SETRANGE "+key+" "+offset+" => "+value_set);
        String resultb=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+resultb);
        String value1=value;
        String value2=value;
        String berepalce=value1.substring(offset,offset+value_set.toCharArray().length);
        String replace=value2.replace(berepalce,value_set);
        System.out.println(value+"—>"+" -"+berepalce+"-> +"+value_set+"->"+replace);
        assertEquals(replace,resultb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Strings Command_STRLEN")
    void strlenTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistestkey"+timeStampstr;
        String value="redistest"+timeStampstr;
        jedisA.set(key,value);
        System.out.println(A+"SET "+key+" "+value);
        long lena=jedisA.strlen(key);
        System.out.println(A+"STRANGE "+key+" => "+lena);
        long lenb=jedisB.strlen(key);
        System.out.println(B+"STRANGE "+key+" => "+lenb);
        assertEquals(lena,value.length());
        assertEquals(lenb,lena);
    }

    /*Redis Commands Supported-Hashes*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HDEL")
    void hdelTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        String filedvalue=value_list.get(index).toString();
        String fileddata0=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+fileddata0);
        jedisA.hdel(key_hashes,filed);
        System.out.println(A+"HDEL "+key_hashes+" "+filed);
        String fileddata=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+fileddata);
        assertEquals(fileddata0,filedvalue);
        assertNull(fileddata);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HDELs")
    void hdelsTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        int index1=1;
        String filed1=key_list.get(index1).toString();
        jedisA.hdel(key_hashes,filed,filed1);
        System.out.println(A+"HDEL "+key_hashes+" "+filed+" "+filed1);
        String fileddata=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+fileddata);
        String fileddata1=jedisB.hget(key_hashes,filed1);
        System.out.println(B+"HGET "+key_hashes+" "+filed1+" => "+fileddata1);
        Map dataz=jedisB.hgetAll(key_hashes);
        System.out.println(B+"HGETALL "+key_hashes+" => "+dataz);
        assertNull(fileddata);
        assertNull(fileddata1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HEXISTS")
    void hexistsTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        int index1=1;
        String filed1=key_list.get(index1).toString();
        jedisA.hdel(key_hashes,filed);
        System.out.println(A+"HDEL "+key_hashes+" "+filed);
        Boolean  noexistsb=jedisB.hexists(key_hashes,filed);
        System.out.println(B+"HEXISTS "+key_hashes+" "+filed+" => "+noexistsb);
        Boolean  existsb=jedisB.hexists(key_hashes,filed1);
        System.out.println(B+"HEXISTS "+key_hashes+" "+filed1+" => "+existsb);
        Map dataz=jedisB.hgetAll(key_hashes);
        System.out.println(B+"HGETALL "+key_hashes+" => "+dataz);
        assertTrue(existsb);
        assertFalse(noexistsb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HGET")
    void hgetTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        int index1=1;
        String filed1=key_list.get(index1).toString();
        String filedvaluea=jedisA.hget(key_hashes,filed);
        System.out.println(A+"HGET "+key_hashes+" "+filed+" => "+filedvaluea);
        String filedvalue1a=jedisA.hget(key_hashes,filed1);
        System.out.println(A+"HEXISTS "+key_hashes+" "+filed1+" => "+filedvalue1a);
        String filedvalue=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+filedvalue);
        String filedvalue1=jedisB.hget(key_hashes,filed1);
        System.out.println(B+"HEXISTS "+key_hashes+" "+filed1+" => "+filedvalue1);
        assertEquals(filedvaluea,filedvalue);
        assertEquals(filedvalue1a,filedvalue1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HGETALL")
    void hgetallTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        Map filedvaluea=jedisA.hgetAll(key_hashes);
        System.out.println(A+"HGETALL "+key_hashes+" => "+filedvaluea);
        Map filedvalue=jedisB.hgetAll(key_hashes);
        System.out.println(B+"HGETALL "+key_hashes+" => "+filedvalue);
        assertEquals(filedvaluea,filedvalue);
        assertEquals(data,filedvalue);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HINCRBY_notaninteger")
    void hincrbynotanintTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        int incrementup=100;
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            jedisA.hincrBy(key_hashes,filed,incrementup);;
            throw new IllegalArgumentException("未限制存储字符串值的域进行值操作");
        });
        System.out.println(A+"HINCRBY "+key_hashes+" "+filed+" "+incrementup+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("ERR hash value is not an integer"));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HINCRBY")
    void hincrbyTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHashint(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        String value=value_list.get(index).toString();
        int index1=2;
        String filed1=key_list.get(index1).toString();
        String value1=value_list.get(index1).toString();
        int incrementup=100;
        int incrementdown=-100;
        jedisA.hincrBy(key_hashes,filed,incrementup);
        System.out.println(A+"HINCRBY "+key_hashes+" "+filed+" "+incrementup);
        jedisA.hincrBy(key_hashes,filed1,incrementdown);
        System.out.println(A+"HINCRBY "+key_hashes+" "+filed1+" "+incrementdown);
        String filedvalue=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+filedvalue);
        String filedvalue1=jedisB.hget(key_hashes,filed1);
        System.out.println(B+"HGET "+key_hashes+" "+filed1+" => "+filedvalue1);
        assertEquals(Integer.valueOf(value)+incrementup,Integer.valueOf(filedvalue));
        assertEquals(Integer.valueOf(value1)+incrementdown,Integer.valueOf(filedvalue1));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HINCRBYFLOAT")
    void hincrbyfloatTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHashint(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        String value=value_list.get(index).toString();
        int index1=2;
        String filed1=key_list.get(index1).toString();
        String value1=value_list.get(index1).toString();
        double incrementup=100.1;
        double incrementdown=-100.1;
        jedisA.hincrByFloat(key_hashes,filed,incrementup);
        System.out.println(A+"HINCRBYFLOAT "+key_hashes+" "+filed+" "+incrementup);
        jedisA.hincrByFloat(key_hashes,filed1,incrementdown);
        System.out.println(A+"HINCRBYFLOAT "+key_hashes+" "+filed1+" "+incrementdown);
        String filedvalue=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+filedvalue);
        String filedvalue1=jedisB.hget(key_hashes,filed1);
        System.out.println(B+"HGET "+key_hashes+" "+filed1+" => "+filedvalue1);
        assertEquals(Double.valueOf(value)+incrementup,Double.valueOf(filedvalue));
        assertEquals(Double.valueOf(value1)+incrementdown,Double.valueOf(filedvalue1));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HKEYS")
    void hkeysTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        Set keysa=jedisA.hkeys(key_hashes);
        System.out.println(A+"HKEYS "+key_hashes+" => "+keysa);
        Set keysb=jedisB.hkeys(key_hashes);
        System.out.println(B+"HKEYS "+key_hashes+" => "+keysb);
        assertEquals(keysa,keysb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HLEN")
    void hlenTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        long fieldsa=jedisA.hlen(key_hashes);
        System.out.println(A+"HLEN "+key_hashes+" => "+fieldsa);
        long fieldsb=jedisB.hlen(key_hashes);
        System.out.println(B+"HLEN "+key_hashes+" => "+fieldsb);
        assertEquals(fieldsa,fieldsb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HMGET")
    void hmgetTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        int index1=1;
        String filed1=key_list.get(index1).toString();
        List filedvaluea=jedisA.hmget(key_hashes,filed,filed1);
        System.out.println(A+"HMGET "+key_hashes+" "+filed+" "+filed1+" => "+filedvaluea);
        List filedvalue=jedisB.hmget(key_hashes,filed,filed1);
        System.out.println(B+"HMGET "+key_hashes+" "+filed+" "+filed1+" => "+filedvalue);
        assertEquals(filedvaluea,filedvalue);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HMSET")
    void hmsetTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        Map datanew=initHash(key_hashes);
        Map filedvaluea=jedisA.hgetAll(key_hashes);
        System.out.println(A+"HGETALL "+key_hashes+" => "+filedvaluea);
        Map filedvalue=jedisB.hgetAll(key_hashes);
        System.out.println(B+"HGETALL "+key_hashes+" => "+filedvalue);
        assertEquals(filedvaluea,filedvalue);
        assertEquals(filedvaluea,datanew);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HSET")
    void hsetTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        jedisA.hset(key_hashes,filed,timeStampstr);
        System.out.println(A+"HSET "+key_hashes+" "+filed+" "+timeStampstr);
        String filedvaluea=jedisA.hget(key_hashes,filed);
        System.out.println(A+"HGET "+key_hashes+" "+filed+" => "+filedvaluea);
        String filedvalue=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+filedvalue);
        assertEquals(filedvaluea,filedvalue);
        assertEquals(filedvaluea,timeStampstr);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HSETNX")
    void hsetnxTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List key_list= Arrays.asList(data.keySet().toArray());
        int index=0;
        String filed=key_list.get(index).toString();
        String value=value_list.get(index).toString();
        long noset=jedisA.hsetnx(key_hashes,filed,timeStampstr);
        System.out.println(A+"HSETNX "+key_hashes+" "+filed+" "+timeStampstr+" => "+noset);
        assertEquals(noset,0);
        String filednx=filed+"noexist";
        long set=jedisA.hsetnx(key_hashes,filednx,timeStampstr);
        System.out.println(A+"HSETNX "+key_hashes+" "+filednx+" "+timeStampstr+" => "+set);
        assertEquals(set,1);
        String filedvaluea=jedisB.hget(key_hashes,filed);
        System.out.println(B+"HGET "+key_hashes+" "+filed+" => "+filedvaluea);
        String filedvalue=jedisB.hget(key_hashes,filednx);
        System.out.println(B+"HGET "+key_hashes+" "+filednx+" => "+filedvalue);
        assertEquals(filedvaluea,value);
        assertEquals(filedvalue,timeStampstr);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Hash Command_HVALS")
    void hvalsTest() {
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        Map data=initHash(key_hashes);
        List value_list= Arrays.asList(data.values().toArray());
        List valuea=jedisA.hvals(key_hashes);
        System.out.println(A+"HVALS "+key_hashes+" => "+valuea);
        List valueb=jedisB.hvals(key_hashes);
        System.out.println(B+"HVALS "+key_hashes+" => "+valueb);
        assertTrue(valuea.containsAll(valueb));
        assertTrue(valuea.containsAll(value_list));
    }

    /*Redis Commands Supported-Lists*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LINDEX")
    void lindexTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        for (int i=0;i<value_list.size();i++){
            String value_i=jedisB.lindex(key_list,i);
            System.out.println(B+"LINDEX "+key_list+" "+i+"=> "+value_i);
            assertEquals(value_i,value_list.get(i));}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LINSERT_BEFORE")
    void linsertbeforeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int index=2;
        String pivot=value_list.get(index).toString();
        String element="before[2]";
        jedisA.linsert(key_list,BEFORE,pivot,element);
        System.out.println(A+"INSERT "+key_list+" BEFORE "+pivot+" "+element);
        String value_i=jedisB.lindex(key_list,index);
        System.out.println(B+"LINDEX "+key_list+" "+index+"=> "+value_i);
        assertEquals(value_i,element);
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LINSERT_AFTER")
    void linsertafterTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int index=1;int indexafter=index+1;
        String pivot=value_list.get(index).toString();
        String element="after[1]";
        jedisA.linsert(key_list,AFTER,pivot,element);
        System.out.println(A+"INSERT "+key_list+" AFTER "+pivot+" "+element);
        String value_i=jedisB.lindex(key_list,indexafter);
        System.out.println(B+"LINDEX "+key_list+" "+indexafter+"=> "+value_i);
        assertEquals(value_i,element);
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LLEN")
    void llenTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        long llen=jedisB.llen(key_list);
        System.out.println(B+"LLEN "+key_list+" => "+llen);
        assertEquals(llen,value_list.size());
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LPOP")
    void lpopTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        String lpop=jedisA.lpop(key_list);
        System.out.println(A+"LPOP "+key_list+"=> "+lpop);
        assertEquals(lpop,value_list.get(0));
        String value_i=jedisB.lindex(key_list,0);
        System.out.println(B+"LINDEX "+key_list+" "+0+"=> "+value_i);
        assertEquals(value_i,value_list.get(1));
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LPUSH")
    void lpushTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        String lpush1="a";
        String lpush2="b";
        long lpush=jedisA.lpush(key_list,lpush1,lpush2);
        System.out.println(A+"LPUSH "+key_list+" "+lpush1+" "+lpush2+" => "+lpush);
        assertEquals(lpush,value_list.size()+2);
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
        String value_i1=jedisB.lindex(key_list,0);
        System.out.println(B+"LINDEX "+key_list+" "+0+"=> "+value_i1);
        assertEquals(value_i1,lpush2);
        String value_i2=jedisB.lindex(key_list,1);
        System.out.println(B+"LINDEX "+key_list+" "+1+"=> "+value_i2);
        assertEquals(value_i2,lpush1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LPUSHX")
    void lpushxTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        String lpush1="a";
        long lpushx=jedisA.lpushx(key_list,lpush1);
        System.out.println(A+"LPUSHX "+key_list+" "+lpush1+" => "+lpushx);
        assertEquals(lpushx,value_list.size()+1);
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
        String value_i1=jedisB.lindex(key_list,0);
        System.out.println(B+"LINDEX "+key_list+" "+0+"=> "+value_i1);
        assertEquals(value_i1,lpush1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LPUSHX_elements")
    void lpushxbyelementsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        String lpush1="a";
        String lpush2="b";
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            long lpushx=jedisA.lpushx(key_list,lpush1,lpush2);
            throw new IllegalArgumentException("版本支持该命令 >= 4.0: 支持一次插入多个值");
        });
        System.out.println(A+"LPUSHX "+key_list+" "+lpush1+" "+lpush2+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        assertEquals(lpushx,value_list.size()+2);
//        List result_list=jedisB.lrange(key_list,0,-1);
//        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
//        String value_i1=jedisB.lindex(key_list,0);
//        System.out.println(B+"LINDEX "+key_list+" "+0+"=> "+value_i1);
//        assertEquals(value_i1,lpush2);
//        String value_i2=jedisB.lindex(key_list,1);
//        System.out.println(B+"LINDEX "+key_list+" "+1+"=> "+value_i2);
//        assertEquals(value_i2,lpush1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LPUSHX_noexist")
    void lpushxnoexistTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        String lpush1="a";
        long lpushx=jedisA.lpushx(key_list,lpush1);
        System.out.println(A+"LPUSHX "+key_list+" "+lpush1+" => "+lpushx);
        assertEquals(lpushx,0);
        long llen=jedisB.llen(key_list);
        System.out.println(B+"LLEN "+key_list+" => "+llen);
        assertEquals(llen,0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LRANGE")
    void lrangeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        List value_a=jedisA.lrange(key_list,1,2);
        System.out.println(A+"LRANGE "+key_list+"1 2 => "+value_a);
        List value_b=jedisB.lrange(key_list,1,2);
        System.out.println(B+"LRANGE "+key_list+"1 2 => "+value_b);
        assertEquals(value_a,value_b);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LREM")
    void lremTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        String element="element";
        String list_head=value_list.get(0).toString();
        String list_tail=value_list.get(2).toString();
        long listsize=jedisA.linsert(key_list,BEFORE,list_head,element);
        System.out.println(A+"LINSERT "+key_list+"BEFORE "+list_head+" "+element+" => "+listsize);
        long listsizez=jedisA.linsert(key_list,AFTER,list_tail,element);
        System.out.println(A+"LINSERT "+key_list+"AFTER "+list_tail+" "+element+" => "+listsizez);
        List value_insert=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+"0 -1 => "+value_insert);
        int delete=1;
        long delete_a=jedisA.lrem(key_list,delete,element);
        System.out.println(A+"LREM "+key_list+" "+delete+" "+element+" => "+delete_a);
        assertEquals(delete_a,1);
        List value_b_deleted=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+"0 -1 => "+value_b_deleted);
        long delete_az=jedisA.lrem(key_list,-delete,element);
        System.out.println(A+"LREM "+key_list+" "+delete+" "+element+" => "+delete_az);
        assertEquals(delete_a,1);
        List value_b_deletedz=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+"0 -1 => "+value_b_deletedz);
        assertEquals(value_b_deletedz,value_list);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LSET")
    void lsetTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int index=1;String element="element";
        String lset=jedisA.lset(key_list,index,element);
        System.out.println(A+"LSET "+key_list+" "+index+" "+element+" => "+lset);
        String valueb=jedisB.lindex(key_list,index);
        System.out.println(B+"LINDEX "+key_list+" "+index+" => "+valueb);
        assertEquals(valueb,element);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_LTRIM")
    void ltrimTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        String element="element";
        long listsize=jedisA.lpush(key_list,element);
        System.out.println(A+"LPUSH "+key_list+" "+element+" => "+listsize);
        List valuepush=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+valuepush);
        int start=1;int stop=2;
        String ltrim=jedisA.ltrim(key_list,start,stop);
        System.out.println(A+"LTRIM "+key_list+" "+start+" "+stop+" => "+ltrim);
        List valueb=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+valueb);
        assertEquals(valueb,valuepush.subList(start,stop+1));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPOP")
    void rpopTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int count=value_list.size()-1;
        String rpop=jedisA.rpop(key_list);
        System.out.println(A+"RPOP "+key_list+" => "+rpop);
        List value=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+value);
        assertEquals(rpop,value_list.get(count));
        assertEquals(value,value_list.subList(0,count));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPOPLPUSH ")
    void rpoplpushTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        String destination="1list"+key_s;
        List value_list=initList(key_list);
        int count=value_list.size()-1;
        String rpoplpush=jedisA.rpoplpush(key_list,destination);
        System.out.println(A+"RPOPLPUSH "+key_list+" "+destination+" => "+rpoplpush);
        List value=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+value);
        List valuede=jedisB.lrange(destination,0,-1);
        System.out.println(B+"LRANGE "+destination+" 0 -1"+" => "+valuede);
        assertEquals(rpoplpush,value_list.get(count));
        assertEquals(value,value_list.subList(0,count));
        assertTrue(valuede.contains(rpoplpush));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPUSH")
    void rpushTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int size=value_list.size();
        String element="element";
        long rpush=jedisA.rpush(key_list,element);
        System.out.println(A+"RPUSH "+key_list+" "+element+" => "+rpush);
        assertEquals(rpush,size+1);
        List value=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+value);
        String valuez=jedisB.lindex(key_list,-1);
        System.out.println(B+"LINDEX "+key_list+" -1"+" => "+valuez);
        assertEquals(valuez,element);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPUSHs")
    void rpushsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int size=value_list.size();
        String element="element";
        String element1="1element";
        long rpush=jedisA.rpush(key_list,element,element1);
        System.out.println(A+"RPUSH "+key_list+" "+element+" "+element1+" => "+rpush);
        assertEquals(rpush,size+2);
        List value=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+value);
        String valuez=jedisB.lindex(key_list,-1);
        System.out.println(B+"LINDEX "+key_list+" -1"+" => "+valuez);
        assertEquals(valuez,element1);
        String valuez1=jedisB.lindex(key_list,-2);
        System.out.println(B+"LINDEX "+key_list+" -2"+" => "+valuez1);
        assertEquals(valuez1,element);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPUSHX_exist")
    void rpushxexistTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int size=value_list.size();
        String element="element";
        long rpush=jedisA.rpushx(key_list,element);
        System.out.println(A+"RPUSHX "+key_list+" "+element+" => "+rpush);
        assertEquals(rpush,size+1);
        List value=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+value);
        String valuez=jedisB.lindex(key_list,-1);
        System.out.println(B+"LINDEX "+key_list+" -1"+" => "+valuez);
        assertEquals(valuez,element);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPUSHX_noexist")
    void rpushxnoexistTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        String element="element";
        long rpush=jedisA.rpushx(key_list,element);
        System.out.println(A+"RPUSHX "+key_list+" "+element+" => "+rpush);
        assertEquals(rpush,0);
        long value=jedisB.llen(key_list);
        System.out.println(B+"LLEN "+key_list+" 0 -1"+" => "+value);
        assertEquals(value,0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Lists Command_RPUSHXs")
    void rpushxsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List value_list=initList(key_list);
        int size=value_list.size();
        String element="element";
        String element1="1element";
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            long rpushx=jedisA.rpushx(key_list,element,element1);
            throw new IllegalArgumentException("支持redis>= 2.4");
        });
        System.out.println(A+"RPUSHX "+key_list+" "+element+" "+element1+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        assertEquals(rpush,size+2);
//        List value=jedisB.lrange(key_list,0,-1);
//        System.out.println(B+"LRANGE "+key_list+" 0 -1"+" => "+value);
//        String valuez=jedisB.lindex(key_list,-1);
//        System.out.println(B+"LINDEX "+key_list+" -1"+" => "+valuez);
//        assertEquals(valuez,element1);
//        String valuez1=jedisB.lindex(key_list,-2);
//        System.out.println(B+"LINDEX "+key_list+" -2"+" => "+valuez1);
//        assertEquals(valuez1,element);
    }

    /*Redis Commands Supported-Sets*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SADD")
    void saddTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set value_sets=initSets(key_sets);
        String newmember="newmember";
        String oldmenber=value_sets.iterator().next().toString();
        long saddold=jedisA.sadd(key_sets,oldmenber);
        System.out.println(A+"SADD "+key_sets+" "+oldmenber+"=> "+saddold);
        assertEquals(saddold,0);
        Set setsb=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+setsb);
        assertEquals(setsb,value_sets);
        long saddnew=jedisA.sadd(key_sets,newmember);
        System.out.println(A+"SADD "+key_sets+" "+newmember+"=> "+saddnew);
        assertEquals(saddnew,1);
        Set setsbz=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+setsbz);
        assertTrue(setsbz.containsAll(value_sets));
        assertTrue(setsbz.contains(newmember));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SADDs")
    void saddsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set value_sets=initSets(key_sets);
        String newmember="newmember";String newmember1="newmember1";
        long saddnew=jedisA.sadd(key_sets,newmember,newmember1);
        System.out.println(A+"SADD "+key_sets+" "+newmember+" "+newmember1+"=> "+saddnew);
        assertEquals(saddnew,2);
        Set setsbz=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+setsbz);
        assertTrue(setsbz.containsAll(value_sets));
        assertTrue(setsbz.contains(newmember));
        assertTrue(setsbz.contains(newmember1));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SCARD")
    void scardTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set value_sets=initSets(key_sets);
        long scard=jedisA.scard(key_sets);
        System.out.println(A+"SCARD "+key_sets+"=> "+scard);
        long scardb=jedisB.scard(key_sets);
        System.out.println(B+"SCARD "+key_sets+"=> "+scard);
        assertEquals(scardb,value_sets.size());
        assertEquals(scardb,scard);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SDIFF")
    void sdiffTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String key_sets1="1sets"+key_s;
        Set value_sets=initSets(key_sets);
        long sinter=jedisA.sinterstore(key_sets1,key_sets);
        System.out.println(A+"SINTERSTORE "+key_sets1+" "+key_sets+"=> "+sinter);
        Set setsbz1=jedisB.smembers(key_sets1);
        System.out.println(B+"SEMEBERS "+key_sets1+" => "+setsbz1);
        String newmember="newmember";
        long saddnew=jedisA.sadd(key_sets,newmember);
        System.out.println(A+"SADD "+key_sets+" "+newmember+"=> "+saddnew);
        assertEquals(saddnew,1);
        Set setsbz=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+setsbz);
        Set sdiffa=jedisA.sdiff(key_sets,key_sets1);
        System.out.println(A+"SDIFF "+key_sets+" "+key_sets1+"=> "+sdiffa);
        Set sdiff=jedisB.sdiff(key_sets,key_sets1);
        System.out.println(B+"SDIFF "+key_sets+" "+key_sets1+"=> "+sdiff);
        assertTrue(sdiffa.contains(newmember));
        assertTrue(sdiff.contains(newmember));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SDIFFSTORE")
    void sdiffstoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String key_sets1="1sets"+key_s;
        String destination="destination"+key_s;
        Set value_sets=initSets(key_sets);
        long sinter=jedisA.sinterstore(key_sets1,key_sets);
        System.out.println(A+"SINTERSTORE "+key_sets1+" "+key_sets+"=> "+sinter);
        Set setsbz1=jedisB.smembers(key_sets1);
        System.out.println(B+"SEMEBERS "+key_sets1+" => "+setsbz1);
        String newmember="newmember";
        long saddnew=jedisA.sadd(key_sets,newmember);
        System.out.println(A+"SADD "+key_sets+" "+newmember+"=> "+saddnew);
        Set setsbz=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+setsbz);
        String value=value_sets.iterator().next().toString();
        long sdiffa=jedisA.sdiffstore(destination,key_sets,key_sets1);
        System.out.println(A+"SDIFFSTORE "+destination+" "+key_sets+" "+key_sets1+"=> "+sdiffa);
        assertEquals(sdiffa,1);
        Set sdiff=jedisB.smembers(destination);
        System.out.println(B+"SEMEBERS "+destination+"=> "+sdiff);
        assertTrue(sdiff.contains(newmember));
        assertEquals(sdiff.size(),1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SINTER")
    void sinterTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String key_sets1="1sets"+key_s;
        Set value_sets=initSetswithstatble(key_sets);
        Set value_sets1=initSetswithstatble(key_sets1);
        Set sinterall=jedisB.sinter(key_sets);
        System.out.println(B+"SINTER "+key_sets+"=> "+sinterall);
        assertEquals(sinterall,value_sets);
        Set sintera=jedisA.sinter(key_sets,key_sets1);
        System.out.println(A+"SINTER "+key_sets+" "+key_sets1+" => "+sintera);
        Set sinterb=jedisB.sinter(key_sets,key_sets1);
        System.out.println(B+"SINTER "+key_sets+" "+key_sets1+" => "+sinterb);
        assertTrue(sintera.containsAll(sinterb));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SINTERSTORE")
    void sinterstoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String key_sets1="1sets"+key_s;
        String destination="destination"+key_s;
        Set value_sets=initSetswithstatble(key_sets);
        Set value_sets1=initSetswithstatble(key_sets1);
        Set<String> result = new HashSet<String>();
        result.clear();result.addAll(value_sets);result.retainAll(value_sets1);
        long sintera=jedisA.sinterstore(destination,key_sets,key_sets1);
        System.out.println(A+"SINTERSTORE "+destination+" "+key_sets+" "+key_sets1+" => "+sintera);
        assertEquals(sintera,1);
        Set sinterb=jedisB.smembers(destination);
        System.out.println(B+"SEMMBERS "+destination+" => "+sinterb);
        assertTrue(sinterb.containsAll(result));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SISMEMBER")
    void sismemberTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set value_sets=initSets(key_sets);
        String member=value_sets.iterator().next().toString();
        Boolean ismember=jedisA.sismember(key_sets,member);
        System.out.println(A+"SISMEMBER "+key_sets+" "+member+" => "+ismember);
        assertEquals(ismember,true);
        Boolean ismemberb=jedisB.sismember(key_sets,member);
        System.out.println(B+"SISMEMBER "+key_sets+" "+member+" => "+ismember);
        assertEquals(ismemberb,true);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SMEMBERS")
    void smembersTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set value_sets=initSets(key_sets);
        Set smembersa=jedisA.smembers(key_sets);
        System.out.println(A+"SMEMBERS "+key_sets+" => "+smembersa);
        Set smembersb=jedisB.smembers(key_sets);
        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembersb);
        assertTrue(smembersa.containsAll(value_sets));
        assertTrue(smembersa.containsAll(smembersb));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SMOVE")
    void smoveTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String source="sourcesets"+key_s;
        String destination="destinationsets"+key_s;
        Set source_sets=initSets(source);
        Set destination_sets=initSets(destination);
        String member=source_sets.iterator().next().toString();
        long hasmove=jedisA.smove(source,destination,member);
        System.out.println(A+"SMOVE "+source+" "+destination+" "+member+" => "+hasmove);
        assertEquals(hasmove,1);
        Set smemberssource=jedisB.smembers(source);
        System.out.println(B+"SMEMBERS "+source+" => "+smemberssource);
        assertFalse(smemberssource.contains(member));
        Set smembersdestination=jedisB.smembers(destination);
        System.out.println(B+"SMEMBERS "+destination+" => "+smembersdestination);
        assertTrue(smembersdestination.contains(member));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SPOP")
    void spopTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        int count=2;
        Set sets=initSets(key_sets);
        Set result=new HashSet();
        String spop=jedisA.spop(key_sets);
        System.out.println(A+"SPOP "+key_sets+" => "+spop);
        result.clear();result.addAll(sets);result.remove(spop);
        Set smembers=jedisB.smembers(key_sets);
        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembers);
        assertFalse(smembers.contains(spop));
        assertTrue(smembers.containsAll(result));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SPOPs")
    void spopsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        int count=2;
        Set sets=initSets(key_sets);
        Set result=new HashSet();
        Set spops=jedisA.spop(key_sets,count);
        System.out.println(A+"SPOP "+key_sets+" "+count+" => "+spops);
        result.clear();result.addAll(sets);result.remove(spops);
        Set smembersa=jedisA.smembers(key_sets);
        System.out.println(A+"SMEMBERS "+key_sets+" => "+smembersa);
        Set smembersb=jedisB.smembers(key_sets);
        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembersb);
        assertFalse(smembersa.containsAll(spops));
        assertFalse(smembersb.containsAll(spops));
        assertTrue(smembersa.containsAll(result));
        //Redis>3.2 支持参数count
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SRANDMEMBER")
    void srandmenberTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        int count=2;
        Set sets=initSets(key_sets);
        Set result=new HashSet();
        String srandmembera=jedisA.srandmember(key_sets);
        System.out.println(A+"SRANDMEMBER "+key_sets+" => "+srandmembera);
        String srandmemberb=jedisB.srandmember(key_sets);
        System.out.println(B+"SRANDMEMBER "+key_sets+" => "+srandmemberb);
        Set smembersa=jedisA.smembers(key_sets);
        System.out.println(A+"SMEMBERS "+key_sets+" => "+smembersa);
        Set smembersb=jedisB.smembers(key_sets);
        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembersb);
        assertTrue(smembersb.containsAll(smembersa));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SRANDMEMBERs")
    void srandmenbersTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        int count=2;
        Set sets=initSets(key_sets);
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            List srandmembera=jedisA.srandmember(key_sets,count);
            throw new IllegalArgumentException("Redis>2.6 支持count参数");
        });
        System.out.println(A+"SRANDMEMBER "+key_sets+" "+count+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        List srandmembera=jedisA.srandmember(key_sets,count);
//        System.out.println(A+"SRANDMEMBER "+key_sets+" "+count+" => "+srandmembera);
//        List srandmemberb=jedisB.srandmember(key_sets,count);
//        System.out.println(B+"SRANDMEMBER "+key_sets+" "+count+" => "+srandmemberb);
//        Set smembersa=jedisA.smembers(key_sets);
//        System.out.println(A+"SMEMBERS "+key_sets+" => "+smembersa);
//        Set smembersb=jedisB.smembers(key_sets);
//        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembersb);
//        assertTrue(smembersb.containsAll(smembersa));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SREM")
    void sremTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set sets=initSets(key_sets);
        String member=sets.iterator().next().toString();
        long srem=jedisA.srem(key_sets,member);
        System.out.println(A+"SREM "+key_sets+" "+member+" => "+srem);
        assertEquals(srem,1);
        Set result=new HashSet();result.clear();result.addAll(sets);result.remove(member);
        Set smembersb=jedisB.smembers(key_sets);
        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembersb);
        assertTrue(smembersb.containsAll(result));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SREMs")
    void sremsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set sets=initSets(key_sets);
        List list=new ArrayList<String>();
        Iterator iter=sets.iterator();
        while (iter.hasNext()){String x=iter.next().toString();list.add(x);}
        String member1=list.get(0).toString();
        String member2=list.get(1).toString();
        long srem=jedisA.srem(key_sets,member1,member2);
        System.out.println(A+"SREM "+key_sets+" "+member1+" "+member2+" => "+srem);
        assertEquals(srem,2);
        Set result=new HashSet();result.clear();result.addAll(sets);result.remove(member1);result.remove(member2);
        Set smembersb=jedisB.smembers(key_sets);
        System.out.println(B+"SMEMBERS "+key_sets+" => "+smembersb);
        assertTrue(smembersb.containsAll(result));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SUNION")
    void sunionTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String key_sets1="1sets"+key_s;
        String key_sets2="2sets"+key_s;
        Set sets=initSets(key_sets);
        Set sets1=initSets(key_sets1);
        Set sets2=initSets(key_sets2);
        Set suniona=jedisA.sunion(key_sets,key_sets1,key_sets2);
        System.out.println(A+"SUNION "+key_sets+" "+key_sets1+" "+key_sets2+" => "+suniona);
        Set sunionb=jedisB.sunion(key_sets,key_sets1,key_sets2);
        System.out.println(B+"SUNION "+key_sets+" "+key_sets1+" "+key_sets2+" => "+sunionb);
        Set result=new HashSet();result.clear();result.addAll(sets);result.addAll(sets1);result.addAll(sets2);
        assertTrue(suniona.containsAll(result));
        assertTrue(sunionb.containsAll(result));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SUNIONSTORE")
    void sunionstoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String key_sets1="1sets"+key_s;
        String destination="destinationsets"+key_s;
        Set sets=initSets(key_sets);
        Set sets1=initSets(key_sets1);
        Set result=new HashSet();result.clear();result.addAll(sets);result.addAll(sets1);
        long suniona=jedisA.sunionstore(destination,key_sets,key_sets1);
        System.out.println(A+"SUNIONSTORE "+destination+" "+key_sets+" "+key_sets1+" => "+suniona);
        assertEquals(suniona,result.size());
        Set sunionaa=jedisA.smembers(destination);
        System.out.println(A+"SMEMBERS "+destination+" => "+sunionaa);
        Set sunionb=jedisB.smembers(destination);
        System.out.println(B+"SMEMBERS "+destination+" => "+sunionb);
        assertTrue(sunionaa.containsAll(result));
        assertTrue(sunionb.containsAll(result));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SSCAN_AA")
    void sscanbyonesideTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set sets=initSets(key_sets);
        ScanParams scanParams=new ScanParams();scanParams.match("A*");
        ScanResult<String> scanResult_a=jedisA.sscan(key_sets, String.valueOf(0),scanParams);
        String cursor_a=scanResult_a.getCursor();
        List<String> resultlist_a=scanResult_a.getResult();
        int size_a=resultlist_a.size();
        System.out.println(A+"SSCAN "+key_sets+"0 match A* => "+cursor_a+resultlist_a);
        assertEquals(size_a,1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sets Command_SSCAN_AB")
    void sscanTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        Set sets=initSets(key_sets);
        ScanParams scanParams=new ScanParams();scanParams.match("A*");
        ScanResult<String> scanResult_a=jedisA.sscan(key_sets, String.valueOf(0),scanParams);
        String cursor_a=scanResult_a.getCursor();
        List<String> resultlist_a=scanResult_a.getResult();
        int size_a=resultlist_a.size();
        System.out.println(A+"SSCAN "+key_sets+"0 match A* => "+cursor_a+resultlist_a);
        ScanResult<String> scanResult_b=jedisB.sscan(key_sets, String.valueOf(0),scanParams);
        String cursor_b=scanResult_b.getCursor();
        List<String> resultlist_b=scanResult_b.getResult();
        int size_b=resultlist_b.size();
        System.out.println(B+"SSCAN "+key_sets+"0 match A* => "+cursor_b+resultlist_b);
        assertEquals(size_a,size_b);
        assertTrue(resultlist_a.containsAll(resultlist_b));
    }

    /*Redis Commands Supported-Sorted Sets*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADD")
    void zaddTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;
        String member="C";
        long zaddnum=jedisA.zadd(key_sortedsets, score,member);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+member+" => "+zaddnum);
        assertEquals(zaddnum,1);
        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
        assertEquals(zrangea,zrangeb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADDs")
    void zaddsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;String member="C";
        int score1=5;String member1="E";
        Map<String, Double> scoreMembers=new HashMap<>();
        scoreMembers.put(member,Double.valueOf(score));
        scoreMembers.put(member1,Double.valueOf(score1));
        long zaddnum=jedisA.zadd(key_sortedsets, scoreMembers);
        System.out.println(A+"ZADD "+key_sortedsets+" "+scoreMembers+" => "+zaddnum);
        assertEquals(zaddnum,2);
        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
        assertEquals(zrangea,zrangeb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADD_XX")
    void zaddxxTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;
        String member="C";
        ZAddParams zAddParams=new ZAddParams();
        long zaddnum=jedisA.zadd(key_sortedsets, score,member,zAddParams.xx());
        System.out.println(A+"ZADD "+key_sortedsets+" XX "+score+" "+member+" => "+zaddnum);
        assertEquals(zaddnum,0);
        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
        assertEquals(zrangea,zrangeb);
        String member1=sets.iterator().next().toString();
        System.out.println(member1);
        int scorexx=5;
        String memberxx=member1;
        long zaddnumxx=jedisA.zadd(key_sortedsets, scorexx,memberxx,zAddParams.xx());
        System.out.println(A+"ZADD "+key_sortedsets+" XX "+scorexx+" "+memberxx+" => "+zaddnumxx);
        assertEquals(zaddnumxx,0);
        Set zrangeaxxz=jedisA.zrange(key_sortedsets,-1,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" -1 -1 => "+zrangeaxxz);
        Set zrangebxxz=jedisB.zrange(key_sortedsets,-1,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" -1 -1 => "+zrangebxxz);
        assertEquals(zrangeaxxz,zrangebxxz);
        assertTrue(zrangebxxz.contains(memberxx));
        Set zrangeaxx=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeaxx);
        Set zrangebxx=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangebxx);
        assertEquals(zrangea,zrangeb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADD_NX")
    void zaddnxTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;
        String member="C";
        ZAddParams zAddParams=new ZAddParams();
        long zaddnum=jedisA.zadd(key_sortedsets, score,member,zAddParams.nx());
        System.out.println(A+"ZADD "+key_sortedsets+" NX "+score+" "+member+" => "+zaddnum);
        assertEquals(zaddnum,1);
        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
        assertEquals(zrangea,zrangeb);
        String member1=sets.iterator().next().toString();
        System.out.println(member1);
        int scorenx=5;
        String membernx=member1;
        long zaddnumnx=jedisA.zadd(key_sortedsets, scorenx,membernx,zAddParams.nx());
        System.out.println(A+"ZADD "+key_sortedsets+" NX "+scorenx+" "+membernx+" => "+zaddnumnx);
        assertEquals(zaddnumnx,0);
        Set zrangeanx=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeanx);
        Set zrangebnx=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangebnx);
        assertEquals(zrangea,zrangeb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADD_CH")
    void zaddchTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;
        String member="C";
        ZAddParams zAddParams=new ZAddParams();
        zAddParams.ch();
        long zaddchnum=jedisA.zadd(key_sortedsets, score,member,zAddParams);
        System.out.println(A+"ZADD "+key_sortedsets+" CH "+score+" "+member+" => "+zaddchnum);
        assertEquals(zaddchnum,1);
        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
        assertEquals(zrangea,zrangeb);
        String member1=sets.iterator().next().toString();
        System.out.println(member1);
        int scorexx=5;
        String memberxx=member1;
        ZAddParams zAddParams1=new ZAddParams();
        zAddParams1.ch();zAddParams1.xx();
        long zaddnumxxch=jedisA.zadd(key_sortedsets, scorexx,memberxx,zAddParams1);
        System.out.println(A+"ZADD "+key_sortedsets+" XX CH "+scorexx+" "+memberxx+" => "+zaddnumxxch);
        assertEquals(zaddnumxxch,1);
        Set zrangeaxxchz=jedisA.zrange(key_sortedsets,-1,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" -1 -1 => "+zrangeaxxchz);
        Set zrangebxxchz=jedisB.zrange(key_sortedsets,-1,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" -1 -1 => "+zrangebxxchz);
        assertEquals(zrangeaxxchz,zrangebxxchz);
        assertTrue(zrangebxxchz.contains(memberxx));
        Set zrangeaxx=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeaxx);
        Set zrangebxx=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangebxx);
        assertEquals(zrangea,zrangeb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADDINCR")
    void zaddincrTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set zrangeahead=jedisA.zrange(key_sortedsets,0,0);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 0 => "+zrangeahead);
        int score=1;
        String memberhead=zrangeahead.iterator().next().toString();
        ZAddParams zAddParams=new ZAddParams();
        double zaddIncrnum=jedisA.zaddIncr(key_sortedsets, score,memberhead,zAddParams);
        System.out.println(A+"ZADD "+key_sortedsets+" INCR "+score+" "+memberhead+" => "+zaddIncrnum);
        assertEquals(zaddIncrnum,score+1);
        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
        assertEquals(zrangea,zrangeb);
        double scoreincra=jedisA.zscore(key_sortedsets,memberhead);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+memberhead+" => "+scoreincra);
        double scoreincrb=jedisB.zscore(key_sortedsets,memberhead);
        System.out.println(B+"ZSCORE "+key_sortedsets+" "+memberhead+" => "+scoreincrb);
        assertEquals(scoreincra,scoreincrb);
        assertEquals(scoreincra,score+1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADDLT")
    void zaddltTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;
        String member="lt";
        ZAddParams zAddParams=new ZAddParams();zAddParams.lt();zAddParams.ch();
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            double zaddltnum=jedisA.zadd(key_sortedsets, score,member,zAddParams);
            throw new IllegalArgumentException("redis>=6.2: 增加 GT 和 LT 选项");
        });
        System.out.println(A+"ZADD "+key_sortedsets+" LT CH "+score+" "+member+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("ERR syntax error"));
//        double zaddltnum=jedisA.zadd(key_sortedsets, score,member,zAddParams);
//        System.out.println(A+"ZADD "+key_sortedsets+" LT CH "+score+" "+member+" => "+zaddltnum);
//        assertEquals(zaddltnum,2);
//        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
//        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
//        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
//        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
//        assertEquals(zrangea,zrangeb);
//        Set scorelta=jedisA.zrangeByScore(key_sortedsets,score,score);
//        System.out.println(A+"ZRANGEBYSCORE "+key_sortedsets+" "+score+" "+score+" => "+scorelta);
//        Set scoreltb=jedisB.zrangeByScore(key_sortedsets,score,score);
//        System.out.println(B+"ZRANGEBYSCORE "+key_sortedsets+" "+score+" "+score+" => "+scoreltb);
//        assertTrue(scorelta.containsAll(scoreltb));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZADDGT")
    void zaddgtTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int score=3;
        String member="gt";
        ZAddParams zAddParams=new ZAddParams();zAddParams.gt();zAddParams.ch();
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            double zaddltnum=jedisA.zadd(key_sortedsets, score,member,zAddParams);
            throw new IllegalArgumentException("redis>=6.2: 增加 GT 和 LT 选项");
        });
        System.out.println(A+"ZADD "+key_sortedsets+" GT CH "+score+" "+member+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("ERR syntax error"));
//        double zaddgtnum=jedisA.zadd(key_sortedsets, score,member,zAddParams);
//        System.out.println(A+"ZADD "+key_sortedsets+" LT CH "+score+" "+member+" => "+zaddgtnum);
//        assertEquals(zaddgtnum,1);
//        Set zrangea=jedisA.zrange(key_sortedsets,0,-1);
//        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangea);
//        Set zrangeb=jedisB.zrange(key_sortedsets,0,-1);
//        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+zrangeb);
//        assertEquals(zrangea,zrangeb);
//        Set scoregta=jedisA.zrangeByScore(key_sortedsets,score,score);
//        System.out.println(A+"ZRANGEBYSCORE "+key_sortedsets+" "+score+" "+score+" => "+scoregta);
//        Set scoregtb=jedisB.zrangeByScore(key_sortedsets,score,score);
//        System.out.println(B+"ZRANGEBYSCORE "+key_sortedsets+" "+score+" "+score+" => "+scoregtb);
//        assertTrue(scoregta.containsAll(scoregtb));
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZCARD")
    void zcardTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        long numa=jedisA.zcard(key_sortedsets);
        System.out.println(A+"ZCARD "+key_sortedsets+" => "+numa);
        long numb=jedisB.zcard(key_sortedsets);
        System.out.println(B+"ZCARD "+key_sortedsets+" => "+numb);
        assertEquals(numa,numb);
        assertEquals(numa,3);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZCOUNT")
    void zcountTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int min=1;
        int max=3;
        long numa=jedisA.zcount(key_sortedsets,min,max);
        System.out.println(A+"ZCOUNT "+key_sortedsets+" "+min+" "+max+" => "+numa);
        long numb=jedisB.zcount(key_sortedsets,min,max);
        System.out.println(B+"ZCOUNT "+key_sortedsets+" "+min+" "+max+" => "+numb);
        assertEquals(numa,numb);
        assertEquals(numa,2);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZINCRBY")
    void zincrbyTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        int increment=3;
        String member=sets.iterator().next().toString();
        double score=jedisA.zscore(key_sortedsets,member);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+member+" => "+score);
        double scorea=jedisA.zincrby(key_sortedsets,increment,member);
        System.out.println(A+"ZINCRBY "+key_sortedsets+" "+increment+" "+member+" => "+scorea);
        double scoreb=jedisB.zscore(key_sortedsets,member);
        System.out.println(B+"ZSCORE "+key_sortedsets+" "+member+" => "+score);
        assertEquals(scorea,scoreb);
        assertEquals(scorea,score+increment);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZINTERSTORE")
    void zinterstoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score+" "+intermember+" => "+zadd1);
        long numa=jedisA.zinterstore(destination,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZINTERSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+" => "+numa);
        assertEquals(numa,1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        double scorea=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scorea);
        assertEquals(scorea,score+score);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoreb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoreb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoreb,score+score);
        }
        catch (Exception e){
            System.out.println("ZINTERSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZINTERSTORE_WEIGHTS")
    void zinterstoreweightTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";int weight=2;int weight1=3;
        double expected=weight*score+weight1*score;
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score+" "+intermember+" => "+zadd1);
        ZParams zParams=new ZParams();zParams.weights(weight,weight1);
        long numa=jedisA.zinterstore(destination,zParams,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZINTERSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+" "+zParams.getParams().toString()+" => "+numa);
        assertEquals(numa,1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        double scorea=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scorea);
        assertEquals(scorea,expected);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoreb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoreb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoreb,expected);
        }
        catch (Exception e){
            System.out.println("ZINTERSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZINTERSTORE_AGGREGATE_SUM")
    void zinterstoreaggregatesumTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        int score1=4;
        double expected_sum=score+score1;
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score1,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score1+" "+intermember+" => "+zadd1);
        ZParams zParams_sum=new ZParams();zParams_sum.aggregate(SUM);
        long numa=jedisA.zinterstore(destination,zParams_sum,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZINTERSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+" "+"AGGREGATE SUM => "+numa);
        assertEquals(numa,1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        double scoresuma=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresuma);
        assertEquals(scoresuma,expected_sum);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoresumb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresumb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoresumb,expected_sum);
        }
        catch (Exception e){
            System.out.println("ZINTERSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZINTERSTORE_AGGREGATE_MIN")
    void zinterstoreaggregateminTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        int score1=4;
        double expected_min=Math.min(score,score1);
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score1,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score1+" "+intermember+" => "+zadd1);
        ZParams zParams_min=new ZParams();zParams_min.aggregate(MIN);
        long numa=jedisA.zinterstore(destination,zParams_min,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZINTERSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+"AGGREGATE MIN => "+numa);
        assertEquals(numa,1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        double scoresuma=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresuma);
        assertEquals(scoresuma,expected_min);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoresumb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresumb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoresumb,expected_min);
        }
        catch (Exception e){
            System.out.println("ZINTERSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZINTERSTORE_AGGREGATE_MAX")
    void zinterstoreaggregatemaxTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        int score1=4;
        double expected_max=Math.max(score,score1);
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score1,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score1+" "+intermember+" => "+zadd1);
        ZParams zParams_max=new ZParams();zParams_max.aggregate(MAX);
        long numa=jedisA.zinterstore(destination,zParams_max,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZINTERSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+"AGGREGATE MAX=> "+numa);
        assertEquals(numa,1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        double scoresuma=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresuma);
        assertEquals(scoresuma,expected_max);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoresumb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresumb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoresumb,expected_max);
        }
        catch (Exception e){
            System.out.println("ZINTERSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZLEXCOUNT")
    void zlexcountTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsetssamescore(key_sortedsets);
        String menber=set.iterator().next().toString();
        String min="-";String max="["+menber;
        long numa=jedisA.zlexcount(key_sortedsets,min,max);
        System.out.println(A+"ZLEXCOUNT "+key_sortedsets+" "+min+" "+max+" => "+numa);
        long numb=jedisB.zlexcount(key_sortedsets,min,max);
        System.out.println(B+"ZLEXCOUNT "+key_sortedsets+" "+min+" "+max+" => "+numb);
        assertEquals(numa,numb);
        String max1="("+menber;
        long numaz=jedisA.zlexcount(key_sortedsets,min,max1);
        System.out.println(A+"ZLEXCOUNT "+key_sortedsets+" "+min+" "+max1+" => "+numaz);
        long numbz=jedisB.zlexcount(key_sortedsets,min,max1);
        System.out.println(B+"ZLEXCOUNT "+key_sortedsets+" "+min+" "+max1+" => "+numbz);
        assertEquals(numaz,numbz);
        assertEquals(numaz,numa-1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZRANGE")
    void zrangeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        int start=0;int stop=-1;
        Set aresult_sortedsets=jedisA.zrange(key_sortedsets,start,stop);
        System.out.println(A+"ZRANGE "+key_sortedsets+" "+start+" "+stop+" => "+aresult_sortedsets);
        Set bresult_sortedsets=jedisB.zrange(key_sortedsets,start,stop);
        System.out.println(B+"ZRANGE "+key_sortedsets+" "+start+" "+stop+" => "+bresult_sortedsets);
        assertEquals(aresult_sortedsets,bresult_sortedsets);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZRANGEWITHSCORES")
    void zrangeWithScoresTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        int start=0;int stop=-1;
        Set aresult_sortedsets1=jedisA.zrangeWithScores(key_sortedsets,start,stop);
        System.out.println(A+"ZRANGE "+key_sortedsets+" "+start+" "+stop+" WITHSCORES => "+aresult_sortedsets1);
        Set bresult_sortedsets1=jedisB.zrangeWithScores(key_sortedsets,start,stop);
        System.out.println(B+"ZRANGE "+key_sortedsets+" "+start+" "+stop+" WITHSCORES => "+bresult_sortedsets1);
        assertEquals(aresult_sortedsets1,bresult_sortedsets1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZRANGEBYLEX")
    void zrangebylexTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsetssamescore(key_sortedsets);
        String menber=set.iterator().next().toString();
        String min="-";String max="["+menber;
        Set numa=jedisA.zrangeByLex(key_sortedsets,min,max);
        System.out.println(A+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numa);
        Set numb=jedisB.zrangeByLex(key_sortedsets,min,max);
        System.out.println(B+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numb);
        assertEquals(numa,numb);
        String max1="("+menber;
        Set numaz=jedisA.zrangeByLex(key_sortedsets,min,max1);
        System.out.println(A+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max1+" => "+numaz);
        Set numbz=jedisB.zrangeByLex(key_sortedsets,min,max1);
        System.out.println(B+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max1+" => "+numbz);
        assertEquals(numaz,numbz);
        assertEquals(numaz.size(),numa.size()-1);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZRANGEBYLEX_LIMIT")
    void zrangebylexlimitTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsetssamescore(key_sortedsets);
        String member=set.iterator().next().toString();
        String min="-";String max="["+member;
        Set numa0=jedisA.zrangeByLex(key_sortedsets,min,max);
        System.out.println(A+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numa0);
        Set numb0=jedisB.zrangeByLex(key_sortedsets,min,max);
        System.out.println(B+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numb0);
        int offset=2;int count =1;
        Set numa=jedisA.zrangeByLex(key_sortedsets,min,max,offset,count);
        System.out.println(A+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numa);
        Set numb=jedisB.zrangeByLex(key_sortedsets,min,max,offset,count);
        System.out.println(B+"ZRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numb);
        assertEquals(numa,numb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZRANGEBYSCORE")
    void zrangebyscoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        int min=0;int max=3;
        Set numa0=jedisA.zrangeByScore(key_sortedsets,min,max);
        System.out.println(A+"ZRANGEBYSCORE "+key_sortedsets+" "+min+" "+max+" => "+numa0);
        Set numb0=jedisB.zrangeByScore(key_sortedsets,min,max);
        System.out.println(B+"ZRANGEBYSCORE "+key_sortedsets+" "+min+" "+max+" => "+numb0);
        assertEquals(numa0,numb0);
        int offset=1;int count =1;
        Set numa=jedisA.zrangeByScore(key_sortedsets,min,max,offset,count);
        System.out.println(A+"ZRANGEBYSCORE "+key_sortedsets+" "+min+" "+max+" "+offset+" "+count+" => "+numa);
        Set numb=jedisB.zrangeByScore(key_sortedsets,min,max,offset,count);
        System.out.println(B+"ZRANGEBYSCORE "+key_sortedsets+" "+min+" "+max+" "+offset+" "+count+" => "+numb);
        assertEquals(numa,numb);
        Set numazz=jedisA.zrangeByScoreWithScores(key_sortedsets,min,max);
        System.out.println(A+"ZRANGEBYSCOREWITHSCORES "+key_sortedsets+" "+min+" "+max+" WITHSCORES => "+numazz);
        Set numbzz=jedisB.zrangeByScoreWithScores(key_sortedsets,min,max);
        System.out.println(B+"ZRANGEBYSCOREWITHSCORES "+key_sortedsets+" "+min+" "+max+" WITHSCORES => "+numbzz);
        assertEquals(numazz,numbzz);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZRANK")
    void zrankTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        String member=set.iterator().next().toString();
        long numa0=jedisA.zrank(key_sortedsets,member);
        System.out.println(A+"ZRANK "+key_sortedsets+" "+member+" => "+numa0);
        long numb0=jedisB.zrank(key_sortedsets,member);
        System.out.println(B+"ZRANK "+key_sortedsets+" "+member+" => "+numb0);
        assertEquals(numa0,numb0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREM")
    void zremTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        String member=set.iterator().next().toString();
        long numa0=jedisA.zrem(key_sortedsets,member);
        System.out.println(A+"ZREM "+key_sortedsets+" "+member+" => "+numa0);
        Set numb0=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+numb0);
        assertEquals(numa0,1);
        assertEquals(numb0.size(),set.size()-numa0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREMRANGEBYLEX")
    void zremrangebylexTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsetssamescore(key_sortedsets);
        String member=set.iterator().next().toString();
        String min="-";String max="["+member;
        long numa0=jedisA.zremrangeByLex(key_sortedsets,min,max);
        System.out.println(A+"ZREMRANGEBYLEX "+key_sortedsets+" "+min+" "+max+" => "+numa0);
        Set numb0=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+numb0);
        assertEquals(numb0.size(),set.size()-numa0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREMRANGEBYRANK")
    void zremrangebyrankTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        int start=0;int stop=1;
        long numa0=jedisA.zremrangeByRank(key_sortedsets,start,stop);
        System.out.println(A+"ZREMRANGEBYRANK "+key_sortedsets+" "+start+" "+stop+" => "+numa0);
        Set numb0=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+numb0);
        assertEquals(numb0.size(),set.size()-numa0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREMRANGEBYSCORE")
    void zremrangebyscoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        double min=1;double max=3;
        long numa0=jedisA.zremrangeByScore(key_sortedsets,min,max);
        System.out.println(A+"ZREMRANGEBYSCORE "+key_sortedsets+" "+min+" "+max+" => "+numa0);
        Set numb0=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1 => "+numb0);
        assertEquals(numb0.size(),set.size()-numa0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREVRANGE")
    void zrevrangeTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        int start=0;int stop=1;
        Set numa0=jedisA.zrevrange(key_sortedsets,start,stop);
        System.out.println(A+"ZREVRANGE "+key_sortedsets+" "+start+" "+stop+" => "+numa0);
        Set numb0=jedisB.zrevrange(key_sortedsets,start,stop);;
        System.out.println(B+"ZREVRANGE "+key_sortedsets+" "+start+" "+stop+" => "+numb0);
        assertEquals(numb0,numa0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREVRANGEBYLEX")
    void zrevrangebylexTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsetssamescore(key_sortedsets);
        String member=set.iterator().next().toString();
        String min="-";String max="["+member;
        Set numa0=jedisA.zrevrangeByLex(key_sortedsets,max,min);
        System.out.println(A+"ZREMRANGEBYLEX "+key_sortedsets+" "+max+" "+min+" => "+numa0);
        Set numb0=jedisB.zrevrangeByLex(key_sortedsets,max,min);
        System.out.println(B+"ZREMRANGEBYLEX "+key_sortedsets+" "+max+" "+min+" => "+numa0);
        assertEquals(numb0,numb0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREVRANGEBYSCORE")
    void zrevrangebyscoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        int min=2;int max=5;
        Set numa0=jedisA.zrevrangeByScore(key_sortedsets,max,min);
        System.out.println(A+"ZREVRANGEBYSCORE "+key_sortedsets+" "+max+" "+min+" => "+numa0);
        Set numb0=jedisB.zrevrangeByScore(key_sortedsets,max,min);
        System.out.println(B+"ZREVRANGEBYSCORE "+key_sortedsets+" "+max+" "+min+" => "+numb0);
        assertEquals(numa0,numb0);
        int offset=1;int count =2;
        Set numa=jedisA.zrevrangeByScore(key_sortedsets,max,min,offset,count);
        System.out.println(A+"ZREVRANGEBYSCORE "+key_sortedsets+" "+max+" "+min+" "+offset+" "+count+" => "+numa);
        Set numb=jedisB.zrevrangeByScore(key_sortedsets,max,min,offset,count);
        System.out.println(B+"ZREVRANGEBYSCORE "+key_sortedsets+" "+max+" "+min+" "+offset+" "+count+" => "+numb);
        assertEquals(numa,numb);
        Set numazz=jedisA.zrevrangeByScoreWithScores(key_sortedsets,max,min);
        System.out.println(A+"ZREVRANGEBYSCOREWithScores "+key_sortedsets+" "+max+" "+min+" WITHSCORES => "+numazz);
        Set numbzz=jedisB.zrevrangeByScoreWithScores(key_sortedsets,max,min);
        System.out.println(B+"ZREVRANGEBYSCOREWithScores "+key_sortedsets+" "+max+" "+min+" WITHSCORES => "+numbzz);
        assertEquals(numazz,numbzz);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZREVRANK")
    void zrevrankTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        String member=set.iterator().next().toString();
        long numa0=jedisA.zrevrank(key_sortedsets,member);
        System.out.println(A+"ZREVRANK "+key_sortedsets+" "+member+" => "+numa0);
        long numb0=jedisB.zrevrank(key_sortedsets,member);
        System.out.println(B+"ZREVRANK "+key_sortedsets+" "+member+" => "+numb0);
        assertEquals(numa0,numb0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZSCORE")
    void zscoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        String member=set.iterator().next().toString();
        double numa0=jedisA.zscore(key_sortedsets,member);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+member+" => "+numa0);
        double numb0=jedisB.zscore(key_sortedsets,member);
        System.out.println(B+"ZSCORE "+key_sortedsets+" "+member+" => "+numb0);
        assertEquals(numa0,numb0);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZUNIONSTORE")
    void zunionstoreTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score+" "+intermember+" => "+zadd1);
        long numa=jedisA.zunionstore(destination,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZUNIONSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+" => "+numa);
        assertEquals(numa,sets.size()+sets1.size()+1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        double scorea=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scorea);
        assertEquals(scorea,score+score);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoreb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoreb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoreb,score+score);
        }
        catch (Exception e){
            System.out.println("ZUNIONSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZUNIONSTORE_WEIGHTS")
    void zunionstoreweightTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";int weight=2;int weight1=3;
        double expected=weight*score+weight1*score;
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score+" "+intermember+" => "+zadd1);
        ZParams zParams=new ZParams();zParams.weights(weight,weight1);
        long numa=jedisA.zunionstore(destination,zParams,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZUNIONSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+" "+zParams.getParams().toString()+" => "+numa);
        assertEquals(numa,sets.size()+sets1.size()+1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        assertTrue(setdestinationa.containsAll(sets1));
        assertTrue(setdestinationa.containsAll(sets));
        double scorea=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scorea);
        assertEquals(scorea,expected);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoreb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoreb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoreb,expected);
        }
        catch (Exception e){
            System.out.println("ZUNIONSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZUNIONSTORE_AGGREGATE_SUM")
    void zunionstoreaggregatesumTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        int score1=4;
        double expected_sum=score+score1;
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score1,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score1+" "+intermember+" => "+zadd1);
        ZParams zParams_sum=new ZParams();zParams_sum.aggregate(SUM);
        long numa=jedisA.zunionstore(destination,zParams_sum,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZUNIONSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+" "+"AGGREGATE SUM => "+numa);
        assertEquals(numa,sets.size()+sets1.size()+1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        assertTrue(setdestinationa.containsAll(sets));
        assertTrue(setdestinationa.containsAll(sets1));
        double scoresuma=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresuma);
        assertEquals(scoresuma,expected_sum);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoresumb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresumb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoresumb,expected_sum);
        }
        catch (Exception e){
            System.out.println("ZUNIONSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZUNIONSTORE_AGGREGATE_MIN")
    void zunionstoreaggregateminTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        int score1=4;
        double expected_min=Math.min(score,score1);
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score1,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score1+" "+intermember+" => "+zadd1);
        ZParams zParams_min=new ZParams();zParams_min.aggregate(MIN);
        long numa=jedisA.zunionstore(destination,zParams_min,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZUNIONSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+"AGGREGATE MIN => "+numa);
        assertEquals(numa,sets.size()+sets1.size()+1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        assertTrue(setdestinationa.containsAll(sets));
        assertTrue(setdestinationa.containsAll(sets1));
        double scoresuma=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresuma);
        assertEquals(scoresuma,expected_min);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoresumb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresumb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoresumb,expected_min);
        }
        catch (Exception e){
            System.out.println("ZUNIONSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZUNIONSTORE_AGGREGATE_MAX")
    void zunionstoreaggregatemaxTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String key_sortedsets1="1sortedsets"+key_s;
        Set sets=initSortedsets(key_sortedsets);
        Set sets1=initSortedsets(key_sortedsets1);
        String destination="destinationsortedsets"+key_s;
        int numkeys=2;
        int score=3;String intermember="C";
        int score1=4;
        double expected_max=Math.max(score,score1);
        long zadd=jedisA.zadd(key_sortedsets,score,intermember);
        System.out.println(A+"ZADD "+key_sortedsets+" "+score+" "+intermember+" => "+zadd);
        long zadd1=jedisA.zadd(key_sortedsets1,score1,intermember);
        System.out.println(A+"ZADD "+key_sortedsets1+" "+score1+" "+intermember+" => "+zadd1);
        ZParams zParams_max=new ZParams();zParams_max.aggregate(MAX);
        long numa=jedisA.zunionstore(destination,zParams_max,key_sortedsets,key_sortedsets1);
        System.out.println(A+"ZUNIONSTORE "+destination+" "+numkeys+" "+key_sortedsets+" "+key_sortedsets1+"AGGREGATE MAX=> "+numa);
        assertEquals(numa,sets.size()+sets1.size()+1);
        Set setdestinationa=jedisA.zrange(destination,0,-1);
        System.out.println(A+"ZRANGE "+destination+" 0 -1"+" => "+setdestinationa);
        assertTrue(setdestinationa.contains(intermember));
        assertTrue(setdestinationa.containsAll(sets));
        assertTrue(setdestinationa.containsAll(sets1));
        double scoresuma=jedisA.zscore(destination,intermember);
        System.out.println(A+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresuma);
        assertEquals(scoresuma,expected_max);
        try{
            Set setdestination=jedisB.zrange(destination,0,-1);
            System.out.println(B+"ZRANGE "+destination+" 0 -1"+" => "+setdestination);
            double scoresumb=jedisB.zscore(destination,intermember);
            System.out.println(B+"ZSCORE "+key_sortedsets+" "+key_sortedsets+" "+intermember+" => "+scoresumb);
            assertTrue(setdestination.contains(intermember));
            assertEquals(scoresumb,expected_max);
        }
        catch (Exception e){
            System.out.println("ZUNIONSTORE support requires that the supplied keys hash to the same server.");}
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Sorted Sets Command_ZSCAN")
    void zscanTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        Set set=initSortedsets(key_sortedsets);
        ScanParams scanParams=new ScanParams();scanParams.match("A*");
        ScanResult<Tuple> scanResult_a=jedisA.zscan(key_sortedsets, String.valueOf(0),scanParams);
        String cursor_a=scanResult_a.getCursor();
        List<Tuple> resultlist_a=scanResult_a.getResult();
        int size_a=resultlist_a.size();
        System.out.println(A+"ZSCAN "+key_sortedsets+"0 match A* => "+cursor_a+resultlist_a);
        ScanResult<Tuple> scanResult_b=jedisB.zscan(key_sortedsets, String.valueOf(0),scanParams);
        String cursor_b=scanResult_b.getCursor();
        List<Tuple> resultlist_b=scanResult_b.getResult();
        int size_b=resultlist_b.size();
        System.out.println(B+"ZSCAN "+key_sortedsets+"0 match A* => "+cursor_b+resultlist_b);
        assertEquals(size_a,size_b);
        assertTrue(resultlist_a.containsAll(resultlist_b));
    }
    /*Redis Commands Supported-HyperLogLog*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("HyperLogLog Command_PFADD")
    void pfaddTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_hll="hll"+key;
        jedisA.pfadd(key_hll,"a","b","c","d");
        System.out.println(A+"PFADD "+key_hll+" a b c d");
        long counta=jedisA.pfcount(key_hll);
        System.out.println(A+"PFCOUNT "+key_hll+" => "+counta);
        long count=jedisB.pfcount(key_hll);
        System.out.println(B+"PFCOUNT "+key_hll+" => "+count);
        assertEquals(counta,count);}
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("HyperLogLog Command_PFCOUNT")
    void pfcountTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_hll="hll"+key;
        jedisA.pfadd(key_hll,"a","b","c","d");
        System.out.println(A+"PFADD "+key_hll+" a b c d");
        jedisA.pfadd(key_hll,"e");
        System.out.println(A+"PFADD "+key_hll+" e");
        long counta=jedisA.pfcount(key_hll);
        System.out.println(A+"PFCOUNT "+key_hll+" => "+counta);
        long count=jedisB.pfcount(key_hll);
        System.out.println(B+"PFCOUNT "+key_hll+" => "+count);
        assertEquals(counta,count);}
    /*Redis Commands Supported-Pub/Sub*/
    /*Redis Commands Supported-Transactions*/
    /*Redis Commands Supported-Scripting*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_EVAL")
    void evalTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_eval1="eval1"+key;
        String key_eval2="eval2"+key;
        String script="return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}";
        int numkeys=2;
        String arg1="arg1"+key;
        String arg2="arg2"+key;
        Object objecta=jedisA.eval(script,numkeys,key_eval1,key_eval2,arg1,arg2);
        System.out.println(A+"EVAL "+script+" "+numkeys+" "+key_eval1+" "+key_eval2+" "+arg1+" "+arg2+" => "+objecta);
        Object objectb=jedisB.eval(script,numkeys,key_eval1,key_eval2,arg1,arg2);
        System.out.println(B+"EVAL "+script+" "+numkeys+" "+key_eval1+" "+key_eval2+" "+arg1+" "+arg2+" => "+objectb);
        assertEquals(objecta,objectb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_EVAL_string")
    void evalstringTest() {
        String script="return type(KEYS[1])";int numkeys=1;
        String key_eval1=String.valueOf(10);
        Object objecta=jedisA.eval(script,numkeys,key_eval1);
        System.out.println(A+"EVAL "+script+" "+numkeys+" "+key_eval1+" => "+objecta);
        Object objectb=jedisB.eval(script,numkeys,key_eval1);
        System.out.println(B+"EVAL "+script+" "+numkeys+" "+key_eval1+" => "+objectb);
        assertEquals(objecta,objectb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_EVAL_withoutkey")
    void evalwithoutkeyTest() {
        String script="return 1";
        int numkeys=0;
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            Object objecta=jedisA.eval(script,numkeys);
            throw new IllegalArgumentException("support is limited to scripts that take without key");
        });
        System.out.println(A+"EVAL "+script+" "+numkeys+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
        // dynomite support is limited to scripts that take at least 1 key
//        Object objectb=jedisB.eval(script,numkeys);
//        System.out.println(B+"EVAL "+script+" "+numkeys+" => "+objectb);
//        assertEquals(objecta,objectb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_EVALSHA")
    void evalshaTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_eval1="eval1"+key;
        String script="return {KEYS[1],ARGV[1]}";
        int numkeys=1;
        String arg1="arg1"+key;
        String sha1=jedisA.scriptLoad(script);
        System.out.println(A+"SCRIPT LOAD "+script+" => "+sha1);
        Object evalshaobjecta=jedisA.evalsha(sha1,numkeys,key_eval1,arg1);
        System.out.println(A+"EVALSHA "+sha1+" "+numkeys+" "+key_eval1+" "+arg1+" => "+evalshaobjecta);
        Object evalobjecta=jedisA.eval(script,numkeys,key_eval1,arg1);
        System.out.println(A+"EVAL "+script+" "+numkeys+" "+key_eval1+" "+arg1+" => "+evalobjecta);
        assertEquals(evalshaobjecta,evalobjecta);
        Object evalshaobjectb=jedisB.evalsha(sha1,numkeys,key_eval1,arg1);
        System.out.println(B+"EVALSHA "+sha1+" "+numkeys+" "+key_eval1+" "+arg1+" => "+evalshaobjectb);
        assertEquals(evalshaobjecta,evalshaobjectb);
        boolean existsa=jedisA.scriptExists(sha1);
        System.out.println(A+"SCRIPT EXISTS "+sha1+" => "+existsa);
        boolean existsb=jedisB.scriptExists(sha1);
        System.out.println(B+"SCRIPT EXISTS "+sha1+" => "+existsb);
        assertEquals(existsa,existsb);
        String flush=jedisA.scriptFlush();
        System.out.println(A+"SCRIPT FLUSH "+" => "+flush);

    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_EVALSHAs")
    void evalshasTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_eval1="eval1"+key;
        String key_eval2="eval1"+key;
        String script="return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}";
        int numkeys=2;
        String arg1="arg1"+key;
        String arg2="arg2"+key;
        String sha1=jedisA.scriptLoad(script);
        System.out.println(A+"SCRIPT LOAD "+script+" => "+sha1);
        Object evalshaobjecta=jedisA.evalsha(sha1,numkeys,key_eval1,key_eval2,arg1,arg2);
        System.out.println(A+"EVALSHA "+sha1+" "+numkeys+" "+key_eval1+" "+key_eval2+" "+arg1+" "+arg2+" => "+evalshaobjecta);
        Object evalobjecta=jedisA.eval(script,numkeys,key_eval1,key_eval2,arg1,arg2);
        System.out.println(A+"EVAL "+script+" "+numkeys+" "+key_eval1+" "+key_eval2+" "+arg1+" "+arg2+" => "+evalobjecta);
        assertEquals(evalshaobjecta,evalobjecta);
        Object evalshaobjectb=jedisB.evalsha(sha1,numkeys,key_eval1,key_eval2,arg1,arg2);
        System.out.println(B+"EVALSHA "+sha1+" "+numkeys+" "+key_eval1+" "+key_eval2+" "+arg1+" "+arg2+" => "+evalshaobjectb);
        assertEquals(evalshaobjecta,evalshaobjectb);
        boolean existsa=jedisA.scriptExists(sha1);
        System.out.println(A+"SCRIPT EXISTS "+sha1+" => "+existsa);
        boolean existsb=jedisB.scriptExists(sha1);
        System.out.println(B+"SCRIPT EXISTS "+sha1+" => "+existsb);
        assertEquals(existsa,existsb);
        String flush=jedisA.scriptFlush();
        System.out.println(A+"SCRIPT FLUSH "+" => "+flush);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_EVALSHA_withoutkey")
    void evalshawithoutkeyTest() {
        String script="return 10";
        int numkeys=0;
        String sha1=jedisA.scriptLoad(script);
        System.out.println(A+"SCRIPT LOAD "+script+" => "+sha1);
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            Object evalshaobjecta=jedisA.evalsha(sha1,numkeys);
            throw new IllegalArgumentException("support is limited to scripts that take without key");
        });
        System.out.println(A+"EVALSHA "+sha1+" "+numkeys+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        Object evalshaobjectb=jedisB.evalsha(sha1,numkeys);
//        System.out.println(B+"EVALSHA "+sha1+" "+numkeys+" "+" => "+evalshaobjectb);
//        assertEquals(evalshaobjecta,evalshaobjectb);
//        boolean existsa=jedisA.scriptExists(sha1);
//        System.out.println(A+"SCRIPT EXISTS "+sha1+" => "+existsa);
//        boolean existsb=jedisB.scriptExists(sha1);
//        System.out.println(B+"SCRIPT EXISTS "+sha1+" => "+existsb);
//        assertEquals(existsa,existsb);
//        String flush=jedisA.scriptFlush();
//        System.out.println(A+"SCRIPT FLUSH "+" => "+flush);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_SCRIPT EXISTS")
    void scriptexistsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_eval1="eval1"+key;
        String script="return {KEYS[1],ARGV[1]}";
        int numkeys=1;
        String arg1="arg1"+key;
        String sha1=jedisA.scriptLoad(script);
        System.out.println(A+"SCRIPT LOAD "+script+" => "+sha1);
        boolean existsa=jedisA.scriptExists(sha1);
        System.out.println(A+"SCRIPT EXISTS "+sha1+" => "+existsa);
        boolean existsb=jedisB.scriptExists(sha1);
        System.out.println(B+"SCRIPT EXISTS "+sha1+" => "+existsb);
        assertEquals(existsa,existsb);
        assertTrue(existsa);
        String flush=jedisA.scriptFlush();
        System.out.println(A+"SCRIPT FLUSH "+" => "+flush);
        boolean existsaafterflush=jedisA.scriptExists(sha1);
        System.out.println(A+"SCRIPT EXISTS "+sha1+" => "+existsaafterflush);
        boolean existsbafterflush=jedisB.scriptExists(sha1);
        System.out.println(B+"SCRIPT EXISTS "+sha1+" => "+existsbafterflush);
        assertEquals(existsaafterflush,existsbafterflush);
        assertFalse(existsbafterflush);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_SCRIPT FLUSH")
    void scripflushTest() {
        String flusha=jedisA.scriptFlush();
        System.out.println(A+"SCRIPT FLUSH "+" => "+flusha);
        String flushb=jedisB.scriptFlush();
        System.out.println(B+"SCRIPT FLUSH "+" => "+flushb);
        assertEquals(flusha,flushb);
        assertEquals(flusha,"OK");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_SCRIPT FLUSH SYNC")
    void scripflushsyncTest() {
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            String flusha=jedisA.scriptFlush(SYNC);
            throw new IllegalArgumentException("support FlushMode");
        });
        System.out.println(A+"SCRIPT FLUSH SYNC"+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        System.out.println(A+"SCRIPT FLUSH SYNC"+" => "+flusha);
//        String flushb=jedisB.scriptFlush(SYNC);
//        System.out.println(B+"SCRIPT FLUSH SYNC"+" => "+flushb);
//        assertEquals(flusha,flushb);
//        assertEquals(flusha,"OK");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_SCRIPT FLUSH ASYNC")
    void scripflushasyncTest() {
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            String flusha=jedisA.scriptFlush(ASYNC);
            throw new IllegalArgumentException("support FlushMode");
        });
        System.out.println(A+"SCRIPT FLUSH ASYNC"+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        System.out.println(A+"SCRIPT FLUSH ASYNC"+" => "+flusha);
//        String flushb=jedisB.scriptFlush(ASYNC);
//        System.out.println(B+"SCRIPT FLUSH ASYNC"+" => "+flushb);
//        assertEquals(flusha,flushb);
//        assertEquals(flusha,"OK");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_SCRIPT KILL")
    void scripkillTest() {
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            String killa=jedisA.scriptKill();
            throw new IllegalArgumentException("will kil");
        });
        System.out.println(A+"SCRIPT KILL "+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("NOTBUSY No scripts in execution right now"));
//        System.out.println(A+"SCRIPT KILL "+" => "+killa);
//        String killb=jedisB.scriptKill();
//        System.out.println(B+"SCRIPT KILL "+" => "+killB);
//        assertEquals(killa,killb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Scripting Command_SCRIPT LOAD")
    void scriptloadTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_eval1="eval1"+key;
        String script="return {KEYS[1],ARGV[1]}";
        int numkeys=1;
        String arg1="arg1"+key;
        String sha1=jedisA.scriptLoad(script);
        System.out.println(A+"SCRIPT LOAD "+script+" => "+sha1);
        boolean existsa=jedisA.scriptExists(sha1);
        System.out.println(A+"SCRIPT EXISTS "+sha1+" => "+existsa);
        boolean existsb=jedisB.scriptExists(sha1);
        System.out.println(B+"SCRIPT EXISTS "+sha1+" => "+existsb);
        assertEquals(existsa,existsb);
        String flush=jedisA.scriptFlush();
        System.out.println(A+"SCRIPT FLUSH "+" => "+flush);
    }
    /*Redis Commands Supported-Connection*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Connection PING")
    void pingTest() {
        String pinga0=jedisA.ping();
        System.out.println(A+"PING "+" => "+pinga0);
        String pingb0=jedisB.ping();
        System.out.println(B+"PING "+" => "+pingb0);
        assertEquals(pinga0,pingb0);
        assertEquals(pinga0,"PONG");
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Connection PING message")
    void pingmessageTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String message="ping"+key;
        String pinga=jedisA.ping(message);
        System.out.println(A+"PING "+message+" => "+pinga);
        String pingb=jedisB.ping(message);
        System.out.println(B+"PING "+message+" => "+pingb);
        assertEquals(pinga,pingb);
        assertEquals(pinga,message);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Connection QUIT")
    void quitTest() {
        String quita=jedisA.quit();
        System.out.println(A+"QUIT "+" => "+quita);
        String quitb=jedisB.quit();
        System.out.println(B+"QUIT "+" => "+quitb);
        assertEquals(quita,quitb);assertEquals(quita,"OK");
    }
    /*Redis Commands Supported-Server*/
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Server_CONFIG GET")
    void configgetTest() {
        String parameter="*";
        List configa=jedisA.configGet(parameter);
        System.out.println(A+"CONFIG GET "+parameter+" => "+configa);
        List configb=jedisB.configGet(parameter);
        System.out.println(B+"CONFIG GET "+parameter+" => "+configb);
        assertEquals(configa,configb);
    }
    @Test
    @Tag("Redis Commands Supported")
    @DisplayName("Server_INFO")
    void infoTest() {
        String infoa=jedisA.info();
        System.out.println(A+"INFO "+" => "+infoa);
        String infob=jedisB.info();
        System.out.println(B+"INFO "+" => "+infob);
        assertEquals(infoa,infob,"INFO reads only the local node");
    }

    /*Redis data types
     * https://redis.io/topics/data-types-intro*/
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_String")
    void typeStringsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        String value=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value);
        assertEquals(timeStampstr,value,"PASS");
    }
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_lists")
    void typelistsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_list="list"+key_s;
        List<String> value_list=new ArrayList<>();
        String value_s1="A"+key_list;
        String value_s2="B"+key_list;
        String value_s3="C"+key_list;
        value_list.add(value_s1);
        value_list.add(value_s2);
        value_list.add(value_s3);
        long listsize=jedisA.rpush(key_list,value_s2);
        System.out.println(A+"RPUSH "+key_list+" "+value_s2+" => "+listsize);
        listsize=jedisA.rpush(key_list,value_s3);
        System.out.println(A+"RPUSH "+key_list+" "+value_s3+" => "+listsize);
        listsize=jedisA.lpush(key_list,value_s1);
        System.out.println(A+"LPUSH "+key_list+" "+value_s1+" => "+listsize);
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
        String type_list=jedisB.type(key_list);
        System.out.println(B+"TYPE "+key_list+" => "+type_list);
        assertEquals(value_list,result_list);
        assertEquals(type_list,"list","lists");}
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_Sets")
    void typeSetsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sets="sets"+key_s;
        String value_s1="A"+key_sets;
        String value_s2="B"+key_sets;
        String value_s3="C"+key_sets;
        Set<String> value_sets=new HashSet<>();
        value_sets.add(value_s1);
        value_sets.add(value_s2);
        value_sets.add(value_s3);
        long setssize=jedisA.sadd(key_sets,value_s1,value_s2,value_s3);
        System.out.println(A+"SADD "+key_sets+" "+value_s1+" "+value_s2+" "+value_s3+" => "+setssize);
        Set aresult_sets=jedisA.smembers(key_sets);
        System.out.println(A+"SEMEBERS "+key_sets+" => "+aresult_sets);
        Set result_sets=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+result_sets);
        String type_sets=jedisB.type(key_sets);
        System.out.println(B+"TYPE "+key_sets+" => "+type_sets);
        assertTrue(result_sets.containsAll(value_sets));
        assertEquals(type_sets,"set","Sets");}
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_Sorted sets")
    void typeSortedsetsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_sortedsets="sortedsets"+key_s;
        String value_s1="A"+key_sortedsets;
        String value_s2="B"+key_sortedsets;
        String value_s3="C"+key_sortedsets;
        Set<String> value_sets=new HashSet<>();
        value_sets.add(value_s1);
        value_sets.add(value_s2);
        value_sets.add(value_s3);
        long haszadd=jedisA.zadd(key_sortedsets,10,value_s1);
        System.out.println(A+"ZADD "+key_sortedsets+" "+10+" "+value_s1+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,20,value_s2);
        System.out.println(A+"ZADD "+key_sortedsets+" "+20+" "+value_s2+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,30,value_s3);
        System.out.println(A+"ZADD "+key_sortedsets+" "+30+" "+value_s3+" => "+haszadd);
        Set aresult_sortedsets=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1"+" => "+aresult_sortedsets);
        Set result_sortedsets=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1"+" => "+result_sortedsets);
        String type_sortedsets=jedisB.type(key_sortedsets);
        System.out.println(B+"TYPE "+key_sortedsets+" => "+type_sortedsets);
        assertEquals(aresult_sortedsets,value_sets);
        assertEquals(aresult_sortedsets,result_sortedsets);
        assertEquals(type_sortedsets,"zset","Sorted sets");}
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_Hashes")
    void typeHashesTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key_s="redistest"+timeStampstr;
        String key_hashes="hashes"+key_s;
        String key_s1="A";
        String key_s2="B";
        String key_s3="C";
        String value_s1=key_s1+key_hashes;
        String value_s2=key_s2+key_hashes;
        String value_s3=key_s3+key_hashes;
        Map value_hashes=new HashMap<String,String>();
        value_hashes.put(key_s1,value_s1);
        value_hashes.put(key_s2,value_s2);
        value_hashes.put(key_s3,value_s3);
        List value_list= Arrays.asList(value_hashes.values().toArray());
        List key_list= Arrays.asList(value_hashes.keySet().toArray());
        System.out.println("MAP: "+value_hashes.entrySet());
        jedisA.hmset(key_hashes,value_hashes);
        System.out.println(A+"HMSET "+key_hashes+" "+key_s1+" "+value_s1+" "+key_s2+" "+value_s2+" "+key_s3+" "+value_s3);
        List<String> value_listz=jedisB.hmget(key_hashes,key_s1,key_s2,key_s3);
        System.out.println(B+"HMGET "+key_hashes+" "+key_s1+" "+key_s2+" "+key_s3+" => "+value_listz.toString());
        Map value_mapz=jedisB.hgetAll(key_hashes);
        System.out.println(B+"HGETALL "+key_hashes+" => "+Arrays.asList(value_mapz.entrySet()));
        String type_hashes=jedisB.type(key_hashes);
        System.out.println(B+"TYPE "+key_hashes+" => "+type_hashes);
        assertEquals(value_listz,value_list);
        assertEquals(value_mapz,value_hashes);
        assertEquals(type_hashes,"hash","Hashes");
    }
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_Bit arrays")
    void typeBitarraysTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_bitmaps="bitmaps"+key;
        int value=1;
        String value_bins=Integer.toBinaryString(value);
        System.out.println(value+" 二进制转化为 "+value_bins);
        int valuez=value|4;
        System.out.println(value+"与00000100位或运算后为"+valuez);
        int offset=5;
        jedisA.set(key_bitmaps,String.valueOf(value));
        System.out.println(A+"SET "+key_bitmaps+" "+value);
        boolean hassetbit=jedisA.setbit(key_bitmaps,offset,true);
        System.out.println(A+"SETBIT "+key_bitmaps+" "+offset+" 1"+" => "+hassetbit);
        String valuezz=jedisA.get(key_bitmaps);
        System.out.println(A+"GET "+key_bitmaps+" => "+valuezz);
        String type_bitmaps=jedisA.type(key_bitmaps);
        System.out.println(A+"TYPE "+key_bitmaps+" => "+type_bitmaps);
        String valuezzz=jedisB.get(key_bitmaps);
        System.out.println(B+"GET "+key_bitmaps+" => "+valuezzz);
        boolean getbit=jedisB.getbit(key_bitmaps,offset);
        System.out.println(B+"GETBIT "+key_bitmaps+" "+offset+" => "+getbit);
        assertEquals(type_bitmaps,"string","Bit arrays");
        assertEquals(valuezzz,valuezz);
        assertTrue(getbit);
        assertEquals(valuezz,Integer.toString(valuez));
    }
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_HyperLogLogs")
    void typeHyperLogLogsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_hll="hll"+key;
        jedisA.pfadd(key_hll,"a","b","c","d");
        System.out.println(A+"PFADD "+key_hll+" a b c d");
        long count=jedisB.pfcount(key_hll);
        System.out.println(B+"PFCOUNT "+key_hll+" => "+count);
        String type_hhl=jedisB.type(key_hll);
        System.out.println(B+"TYPE "+key_hll+" => "+type_hhl);
        assertEquals(count,4);
        assertEquals(type_hhl,"string","HyperLogLogs");}
    @Test
    @Tag("Redis data types")
    @DisplayName("DateType_Streams")
    void typeStreamsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        String key_streams="streams"+key;
        String field_s1="A";
        String field_s2="B";
        String field_s3="C";
        String value_s1=field_s1+key_streams;
        String value_s2=field_s2+key_streams;
        String value_s3=field_s3+key_streams;
        Map<String,String> value_streams=new HashMap<>();
        value_streams.put(field_s1,value_s1);
        value_streams.put(field_s2,value_s2);
        value_streams.put(field_s3,value_s3);
        Throwable exception = Assertions.assertThrows(JedisException.class, () -> {
            jedisA.xadd(key_streams,null, value_streams);
            throw new IllegalArgumentException("未正确禁用该命令");
        });
        System.out.println(A+"XADD "+key_streams+" * "+value_streams.entrySet()+" => "+exception.getMessage());
        assertTrue(exception.getMessage().contains("Unexpected end of stream"));
//        System.out.println(A+"XADD "+key_streams+" "+NEW_ENTRY.toString()
//                +field_s1+" "+value_s1+" "+field_s2+" "+value_s2+" "+field_s3+" "+value_s3+" => "+streamEntryID);
//        StreamInfo streamInfo_a= jedisA.xinfoStream(key_streams);
//        System.out.println(A+"XINFO STREAM"+key_streams+" => "+streamInfo_a.toString());
//        StreamInfo streamInfo_b= jedisB.xinfoStream(key_streams);
//        System.out.println(B+"XINFO STREAM"+key_streams+" => "+streamInfo_b.toString());
//        String type_streams=jedisB.type(key_streams);
//        System.out.println(B+"TYPE "+key_streams+" => "+type_streams);
//        assertEquals(type_streams,"stream","Streams");
//        assertEquals(streamInfo_a,streamInfo_b);
    }

    /*Scene Testcase*/
    @Tag("Scene")
    @DisplayName("Repeated SET")
    @RepeatedTest(value = 10,name=RepeatedTest.LONG_DISPLAY_NAME)
    void repeatedsetsTest() {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String key="redistest"+timeStampstr;
        jedisA.set(key,timeStampstr);
        System.out.println(A+"SET "+key+" "+timeStampstr);
        String value=jedisB.get(key);
        System.out.println(B+"GET "+key+" => "+value);
        Assertions.assertTrue(timeStampstr.equals(value),"redis同步正常");
    }

    @Test
    @Disabled("Do not run")
    void noTest() {
        String key="redistest";
        System.out.println(B+"SET "+key+" => "+jedisB.get(key));
    }

    @AfterEach
    void tearDownEach(){
        jedisA.close();
        jedisB.close();
        jedisPoolA.close();
        jedisPoolB.close();
    }

    Map initHash(String key_hashes){
        String key_s1="A";
        String key_s2="B";
        String key_s3="C";
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String value_s1=key_s1+key_hashes+timeStampstr;
        String value_s2=key_s2+key_hashes+timeStampstr;
        String value_s3=key_s3+key_hashes+timeStampstr;
        Map value_hashes=new HashMap<String,String>();
        value_hashes.put(key_s1,value_s1);
        value_hashes.put(key_s2,value_s2);
        value_hashes.put(key_s3,value_s3);
        System.out.println("MAP: "+value_hashes.entrySet());
        jedisA.hmset(key_hashes,value_hashes);
        System.out.println(A+"HMSET "+key_hashes+" "+key_s1+" "+value_s1+" "+key_s2+" "+value_s2+" "+key_s3+" "+value_s3);
        return value_hashes;
    }
    Map initHashint(String key_hashes){
        String key_s1="A";
        String key_s2="B";
        String key_s3="C";
        String value_s1=Integer.toString(100);
        String value_s2=Integer.toString(200);
        String value_s3=Integer.toString(300);
        Map value_hashes=new HashMap<String,String>();
        value_hashes.put(key_s1,value_s1);
        value_hashes.put(key_s2,value_s2);
        value_hashes.put(key_s3,value_s3);
        System.out.println("MAP: "+value_hashes.entrySet());
        jedisA.hmset(key_hashes,value_hashes);
        System.out.println(A+"HMSET "+key_hashes+" "+key_s1+" "+value_s1+" "+key_s2+" "+value_s2+" "+key_s3+" "+value_s3);
        return value_hashes;
    }
    List initList(String key_list){
        long timeStamp =System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        List<String> value_list=new ArrayList<>();
        String value_s1="A"+key_list+timeStampstr;
        String value_s2="B"+key_list+timeStampstr;
        String value_s3="C"+key_list+timeStampstr;
        value_list.add(value_s1);
        value_list.add(value_s2);
        value_list.add(value_s3);
        long listsize=jedisA.rpush(key_list,value_s2);
        System.out.println(A+"RPUSH "+key_list+" "+value_s2+" => "+listsize);
        listsize=jedisA.rpush(key_list,value_s3);
        System.out.println(A+"RPUSH "+key_list+" "+value_s3+" => "+listsize);
        listsize=jedisA.lpush(key_list,value_s1);
        System.out.println(A+"LPUSH "+key_list+" "+value_s1+" => "+listsize);
        List result_list=jedisB.lrange(key_list,0,-1);
        System.out.println(B+"LRANGE "+key_list+" 0 -1 => "+result_list);
        assertEquals(value_list,result_list);
        return value_list;
    }
    Set initSets(String key_sets) {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String value_s1="A"+key_sets+timeStampstr;
        String value_s2="B"+key_sets+timeStampstr;
        String value_s3="C"+key_sets+timeStampstr;
        Set<String> value_sets=new HashSet<>();
        value_sets.add(value_s1);
        value_sets.add(value_s2);
        value_sets.add(value_s3);
        long setssize=jedisA.sadd(key_sets,value_s1,value_s2,value_s3);
        System.out.println(A+"SADD "+key_sets+" "+value_s1+" "+value_s2+" "+value_s3+" => "+setssize);
        Set aresult_sets=jedisA.smembers(key_sets);
        System.out.println(A+"SEMEBERS "+key_sets+" => "+aresult_sets);
        Set result_sets=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+result_sets);
        assertTrue(result_sets.containsAll(value_sets));
        return value_sets;
    }
    Set initSetswithstatble(String key_sets) {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String value_s1="A"+key_sets+timeStampstr;
        String value_s2="B";
        String value_s3="C"+key_sets+timeStampstr;
        Set<String> value_sets=new HashSet<>();
        value_sets.add(value_s1);
        value_sets.add(value_s2);
        value_sets.add(value_s3);
        long setssize=jedisA.sadd(key_sets,value_s1,value_s2,value_s3);
        System.out.println(A+"SADD "+key_sets+" "+value_s1+" "+value_s2+" "+value_s3+" => "+setssize);
        Set aresult_sets=jedisA.smembers(key_sets);
        System.out.println(A+"SEMEBERS "+key_sets+" => "+aresult_sets);
        Set result_sets=jedisB.smembers(key_sets);
        System.out.println(B+"SEMEBERS "+key_sets+" => "+result_sets);
        assertTrue(result_sets.containsAll(value_sets));
        return value_sets;
    }
    Set initSortedsets(String key_sortedsets) {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String value_s1="A"+key_sortedsets+timeStampstr;
        String value_s2="B"+key_sortedsets+timeStampstr;
        String value_s3="D"+key_sortedsets+timeStampstr;
        Set<String> value_sets=new HashSet<>();
        value_sets.add(value_s1);
        value_sets.add(value_s2);
        value_sets.add(value_s3);
        long haszadd=jedisA.zadd(key_sortedsets,1,value_s1);
        System.out.println(A+"ZADD "+key_sortedsets+" "+1+" "+value_s1+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,2,value_s2);
        System.out.println(A+"ZADD "+key_sortedsets+" "+2+" "+value_s2+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,4,value_s3);
        System.out.println(A+"ZADD "+key_sortedsets+" "+4+" "+value_s3+" => "+haszadd);
        Set aresult_sortedsets=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1"+" => "+aresult_sortedsets);
        Set result_sortedsets=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1"+" => "+result_sortedsets);
        assertEquals(aresult_sortedsets,value_sets);
        assertEquals(aresult_sortedsets,result_sortedsets);
        return value_sets;
    }
    Set initSortedsetssamescore(String key_sortedsets) {
        long timeStamp=System.currentTimeMillis();
        String timeStampstr = String.format("%s", timeStamp);
        String value_s1="A"+key_sortedsets;
        String value_s2="B"+key_sortedsets;
        String value_s3="C"+key_sortedsets;
        String value_s4="D"+key_sortedsets;
        String value_s5="E"+key_sortedsets;
        Set<String> value_sets=new HashSet<>();
        value_sets.add(value_s1);
        value_sets.add(value_s2);
        value_sets.add(value_s3);
        value_sets.add(value_s4);
        value_sets.add(value_s5);
        int samescore=0;
        long haszadd=jedisA.zadd(key_sortedsets,samescore,value_s1);
        System.out.println(A+"ZADD "+key_sortedsets+" "+samescore+" "+value_s1+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,samescore,value_s2);
        System.out.println(A+"ZADD "+key_sortedsets+" "+samescore+" "+value_s2+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,samescore,value_s3);
        System.out.println(A+"ZADD "+key_sortedsets+" "+samescore+" "+value_s3+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,samescore,value_s4);
        System.out.println(A+"ZADD "+key_sortedsets+" "+samescore+" "+value_s4+" => "+haszadd);
        haszadd=jedisA.zadd(key_sortedsets,samescore,value_s5);
        System.out.println(A+"ZADD "+key_sortedsets+" "+samescore+" "+value_s5+" => "+haszadd);
        Set aresult_sortedsets=jedisA.zrange(key_sortedsets,0,-1);
        System.out.println(A+"ZRANGE "+key_sortedsets+" 0 -1"+" => "+aresult_sortedsets);
        Set result_sortedsets=jedisB.zrange(key_sortedsets,0,-1);
        System.out.println(B+"ZRANGE "+key_sortedsets+" 0 -1"+" => "+result_sortedsets);
        assertEquals(aresult_sortedsets,value_sets);
        assertEquals(aresult_sortedsets,result_sortedsets);
        return value_sets;
    }
}
