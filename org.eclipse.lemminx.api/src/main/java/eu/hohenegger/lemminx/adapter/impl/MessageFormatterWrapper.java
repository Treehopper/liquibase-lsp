package eu.hohenegger.lemminx.adapter.impl;

import java.util.Locale;

import org.apache.xerces.util.MessageFormatter;

public class MessageFormatterWrapper {

    private MessageFormatter messageFormatter;

    public MessageFormatterWrapper(MessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    public String formatMessage(Locale locale, String key, Object[] arguments) {
        return messageFormatter.formatMessage(locale, key, arguments);
    }

}
