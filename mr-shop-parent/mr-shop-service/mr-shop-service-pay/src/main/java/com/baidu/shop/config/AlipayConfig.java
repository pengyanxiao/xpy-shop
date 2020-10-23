package com.baidu.shop.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/22
 * @Version V1.0
 **/
public class AlipayConfig {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102600766691";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCRZacbfitTchr3ZQNBbip5bwEmr4jWDwmzTK7+koPIOlaU2DJJAmm/lYzYbx7I5mGRE7NMmRFCBhyCATriQpgTb2wjAeRf+7b5TKJDqKEkUWHi4L/dLGRkf7CwypFODW/MQqcs2I/aFQhFAUjMcdJZkgLmp6CwZKd75Whs5O6yIbzNAlFiModGVWfoJyXMmtSLRCxsgoo8OXAiIBZngL0D3hW32GyH3RaLSkAoIWBNzZ5iKtdKPWUaGO15WbAEBzSVThjzeH3OnTEGnnalVc7ybWUMyvj10vi86p42/e+9PNw99cWNQ29ruC660iiIIFXbZsw6ey/a9mqjYG1/1a7dAgMBAAECggEAYQc8UF2PXj18f3JajNSO5cDNPNrRb0BT0eKvuVVfOkSsOOJaFFzW4zJS7rChE6Kio6VI9yoC2VOovJ1DhXC5eebWfDosBF2/ini4pHwpJUpmD3i6sUeJq4OHD0PvWe4fmGk2lPfkb72vahVkVoLyxDFrXC+rTWuuVUEPwfO40wgoyGtER6C/FsIsXnYYaZgd7bWGNqYt5UJahmDZqQsyX0aDJ63UgEazOvjcGKyiqlRxzxXKjDyCXXlBOapXKlS831HDZKEXq6LILsetK+ssM3LYfCSUkcPMIZCi+XLRXsG+Q6LdkK99YDG98vNuPprgLM2nvFVxXw9nsza5J5xcwQKBgQDoBRmaGMqre02yxfrAhk+76IuxPplwq+6FZMsjAk94x/cYyuV/Wk74wXTgQvR6GzdD/bkyMoPE4Ks77fu9sRYL+69X9PLJFFZFO6Mv0fUkoMl8cBSzilyD1XMmOvBJd9gSBr8dwfwhoTdCmjkUSWQOPwBOcajyI2ThwkrnbNWwrQKBgQCgbKRZf21JTtMUHQyHvc5Bx8sr8Lvm/MXdIEUv7qBHxcZYwk6GnQna4rRHY71spTbpefsmbJZ+8pkptEX4zkBFvna60olnUUsDgHrXQvcpi6jEcx96BcfR7MJxMzAjlXrQ9mRMoqHHsk+HviKWO2p2McfxKV3k4ePgWW8gPvNM8QKBgB82EGuaSiknPRx/ZWvgpXkxnxkIwrtVZZCsLmdXODmXLLY5FQY8r8Lmw0Inv5ttbdHHn9IQ2YjycBZvvR2XZM/X246JdN93zOMjUqBsLf3vYytKKH6+bhON8x+BbjFz3a0PG0Z1geVEOprLBEKmXL9N2kyzHXb1K+zypgDbsyNBAoGBAILXmUQVlkEQNUBWYP3lZPBpz9/KvLasu4Sa6VMqwTdIq3mBISLRulP4SylexAR0y9CpWJoCPszTePB2xAslzW4U5imBxeeqehAlHgur5oLQEZoIe0e+pzckb4J8SWr8Y+tCypNmoaSKXitMfFEjrLUx7O1Y2+fuBTnaN7SJI09BAoGAet+zakvSet5DBzggGMfhf+4LUdsK3QoWJkLLEPpexmNXAlOSq1rCPiZIetgX/3EQBtwAxjwlXH0bEv2uxXaSukBm64c8e2v0sLZQ33WTtniGTQqXdvVvRmQz5Z3Hi+Gw/CE994o+mLpoIYcUKuk5UjqhLHG24UjpBol3xUHg6wI=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsXvMPJH7YbNnIS1SGMyoYINJy6EnhoIdmK/p0G+tJ0O/UBEciVtBS22l5yLjb70yCgGYUmxlnuPOCl9EAYkL63Wn3yHZfA3RVkSUPUkH1b2gqk+blgz2PZVZxo9J57DXpFvv3lgCzEsntJe7apSnlps30dU5bOdnwfXAYuFtIcfW5qEWot+IFcpWoAvouIb/P+mD9WNTW0P6dHyJPUMUSjjqSAGOuCw/scuxVCd6TcvX0M92h4tlHGRNkLalNncmVeKC1cju5qeqrTRb66+cg6Exg/QQYInDptGdcwVIfN3Aq0dqaPcHv+MDPMlvCHmpZBhQlHw9+ZdDdzb3t+r+QwIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8900/pay/returnNotify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数,内网可以正常访问
    public static String return_url = "http://localhost:8900/pay/returnURL";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
