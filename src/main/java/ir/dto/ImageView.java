package ir.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder

public class ImageView {

    private String id;           // شناسه‌ی فایل در MongoDB (یا Oracle در حالت بعدی)
    private String fileName;     // نام فایل
    private String contentType;  // نوع فایل (مثل image/jpeg یا application/pdf)
    private String imageBase64;  // محتوای Base64 (فقط برای نمایش در مرورگر)
    private String extractedText;// متن استخراج‌شده با OCR
}
