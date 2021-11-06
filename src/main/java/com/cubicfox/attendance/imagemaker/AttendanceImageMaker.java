package com.cubicfox.attendance.imagemaker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.springframework.stereotype.Component;

import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import com.google.common.collect.Iterators;
import com.google.common.io.CountingOutputStream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class AttendanceImageMaker {
    BufferedImage img = loadImage();

    private BufferedImage loadImage() {
        try (InputStream is = AttendanceImageMaker.class.getResourceAsStream("/jelenleti-iv.jpg")) {
            return ImageIO.read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long write(List<Placement<?>> placements, String MIMEType, OutputStream os) {
        BufferedImage res = bufferedImage();
        {
            Graphics graphics = res.getGraphics();
            graphics.drawImage(img, 0, 0, null);
            graphics.setColor(new Color(0, 0, 0, 255));
            placements.forEach(p -> placeText(graphics, p));
            graphics.dispose();
        }
        try {
            ImageWriter imageWriter = Iterators.get(ImageIO.getImageWritersByMIMEType(MIMEType), 0);
            CountingOutputStream cos = new CountingOutputStream(os);
            imageWriter.setOutput(ImageIO.createImageOutputStream(cos));
            imageWriter.write(res);
            return cos.getCount();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write", e);
        }
    }

    private void placeText(Graphics graphics, Placement<?> placement) {
        Font oldFont = graphics.getFont();
        graphics.setFont(placement.getFont());
        graphics.drawString(String.valueOf(placement.getObject()), placement.getX(), placement.getY());
        graphics.setFont(oldFont);
    }

    private BufferedImage bufferedImage() {
        int height = img.getHeight();
        int width = img.getWidth();
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
}
