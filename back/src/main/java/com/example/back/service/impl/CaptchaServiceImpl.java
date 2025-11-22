package com.example.back.service.impl;

import com.example.back.dto.response.CaptchaResponse;
import com.example.back.service.CaptchaService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除容易混淆的字符
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    // 存储验证码的缓存（key -> code）
    private final ConcurrentHashMap<String, CaptchaInfo> captchaCache = new ConcurrentHashMap<>();

    // 定时清理过期验证码
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CaptchaServiceImpl() {
        // 每5分钟清理一次过期验证码
        scheduler.scheduleAtFixedRate(this::cleanExpiredCaptcha, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public CaptchaResponse generateCaptcha() {
        // 生成验证码Key
        String key = UUID.randomUUID().toString().replace("-", "");

        // 生成验证码文本
        String code = generateCode();

        // 生成验证码图片
        BufferedImage image = generateImage(code);

        // 转换为Base64
        String imageBase64 = imageToBase64(image);

        // 存储验证码（不区分大小写）
        captchaCache.put(key, new CaptchaInfo(code.toUpperCase(), System.currentTimeMillis()));

        return new CaptchaResponse(imageBase64, key);
    }

    @Override
    public boolean verifyCaptcha(String key, String answer) {
        if (key == null || answer == null) {
            return false;
        }

        CaptchaInfo info = captchaCache.remove(key);
        if (info == null) {
            return false; // 验证码不存在或已过期
        }

        // 检查是否过期
        long now = System.currentTimeMillis();
        if (now - info.timestamp > CAPTCHA_EXPIRE_MINUTES * 60 * 1000) {
            return false; // 验证码已过期
        }

        // 验证码不区分大小写
        return info.code.equalsIgnoreCase(answer.trim());
    }

    /**
     * 生成随机验证码文本
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片
     */
    private BufferedImage generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制干扰线
        Random random = new Random();
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        int x = 20;
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(new Color(
                    random.nextInt(100) + 50,
                    random.nextInt(100) + 50,
                    random.nextInt(100) + 50
            ));
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.3;
            g.rotate(angle, x, HEIGHT / 2);
            g.drawString(String.valueOf(code.charAt(i)), x, HEIGHT / 2 + 10);
            g.rotate(-angle, x, HEIGHT / 2);
            x += 25;
        }

        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            int px = random.nextInt(WIDTH);
            int py = random.nextInt(HEIGHT);
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillOval(px, py, 2, 2);
        }

        g.dispose();
        return image;
    }

    /**
     * 将图片转换为Base64字符串
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 清理过期的验证码
     */
    private void cleanExpiredCaptcha() {
        long now = System.currentTimeMillis();
        captchaCache.entrySet().removeIf(entry -> {
            CaptchaInfo info = entry.getValue();
            return now - info.timestamp > CAPTCHA_EXPIRE_MINUTES * 60 * 1000;
        });
    }

    /**
     * 验证码信息
     */
    private static class CaptchaInfo {
        final String code;
        final long timestamp;

        CaptchaInfo(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}

