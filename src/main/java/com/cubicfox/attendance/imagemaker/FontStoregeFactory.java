package com.cubicfox.attendance.imagemaker;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class FontStoregeFactory implements FactoryBean<FontStorege> {
    private static final String defaultFontResourcePath = "/Inconsolata-Regular.ttf";

    @Getter
    Class<FontStorege> objectType = FontStorege.class;

    @Override
    public FontStorege getObject() throws Exception {
        Font defaultFont = loadResourceFont(defaultFontResourcePath);
        return new FontStorege(defaultFont.deriveFont(82f), defaultFont.deriveFont(72f), defaultFont.deriveFont(180f),
                defaultFont.deriveFont(40f));
    }

    private Font loadResourceFont(@NonNull String resourceName) {
        try (java.io.InputStream is = AttendanceProfile.class.getResourceAsStream(resourceName)) {
            return Font.createFont(Font.PLAIN, is);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
