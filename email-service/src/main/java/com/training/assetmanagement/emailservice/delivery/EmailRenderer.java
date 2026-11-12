package com.training.assetmanagement.emailservice.delivery;

import com.training.assetmanagement.emailservice.messaging.EmailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class EmailRenderer {

    private static final String TEMPLATE_DIR = "email/";
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

    private final ITemplateEngine templateEngine;

    public String render(EmailMessage message) {
        Context context = new Context(VIETNAMESE);
        if (message.templateVars() != null) {
            context.setVariables(message.templateVars());
        }
        context.setVariable("recipientName", message.recipientName());
        context.setVariable("subject", message.subject());
        context.setVariable("link", message.link());
        // Chọn tệp mẫu theo enum, không theo chuỗi tự do trong message
        return templateEngine.process(TEMPLATE_DIR + message.eventType().templateName(), context);
    }
}
