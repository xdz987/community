package com.nowcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "E:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com H:/code/testProject/java/data/community/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
