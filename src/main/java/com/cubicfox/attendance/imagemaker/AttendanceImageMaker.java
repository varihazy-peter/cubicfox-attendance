package com.cubicfox.attendance.imagemaker;

import com.cubicfox.attendance.CountableWritableByteChannel;
import com.cubicfox.attendance.imagemaker.AttendanceProfile.Placement;
import com.google.common.collect.Iterators;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

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

    public long write(List<Placement<?>> placements, String MIMEType, WritableByteChannel os) {
        BufferedImage res = bufferedImage();
        {
            Graphics graphics = res.getGraphics();
            graphics.drawImage(img, 0, 0, null);
            graphics.setColor(new Color(0, 0, 0, 255));
            placements.forEach(p -> placeText(graphics, p));
            graphics.dispose();
        }
        try (CountableWritableByteChannel cwbs = CountableWritableByteChannel.of(os)) {
            ImageWriter imageWriter = Iterators.get(ImageIO.getImageWritersByMIMEType(MIMEType), 0);
            imageWriter.setOutput(ImageIO.createImageOutputStream(Channels.newOutputStream(cwbs)));
            imageWriter.write(res);
            return cwbs.getCount();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write", e);
        }
    }

    private void placeText(Graphics graphics, Placement<?> placement) {
        Font oldFont = graphics.getFont();
        graphics.setFont(placement.getFont());
        graphics.drawString(placement.text(), placement.getX(), placement.getY());
        graphics.setFont(oldFont);
    }

    private BufferedImage bufferedImage() {
        int height = img.getHeight();
        int width = img.getWidth();
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
}
