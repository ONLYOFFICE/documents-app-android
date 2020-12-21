package app.editors.manager.managers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.editors.manager.managers.utils.FirebaseUtils;
import lib.toolkit.base.managers.utils.StringUtils;

public class SmsReceiver extends BaseReceiver<String> {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String PATTER_CODE = "(\\d{6})";
    public static final String PATTERN_TEXT_CODE = ".*" + PATTER_CODE + ".*";
    public static final int SMS_CODE_LENGTH = 6;

    private static final String SMS_KEY = "pdus";
    private static final String SMS_KEY_WORD = "ONLYOFFICE";
    private static Pattern SMS_PATTERN = Pattern.compile(PATTERN_TEXT_CODE);

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        try {
            if (StringUtils.equals(intent.getAction(), SMS_RECEIVED)) {
                final Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    final Object[] pdus = (Object[]) bundle.get(SMS_KEY);
                    final SmsMessage[] msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        final String msgFrom = msgs[i].getOriginatingAddress();
                        final String msgBody = msgs[i].getMessageBody();

                        // Check sms on contains code
                        // "Код: 895835\nOnlyOffice"
                        FirebaseUtils.addCrash("From: " + msgFrom + "; Body: " + msgBody);
                        final String code = getCodeFromSms(msgBody);
                        if (!code.isEmpty() && code.length() == SMS_CODE_LENGTH) {
                            if (mOnReceiveListener !=  null) {
                                mOnReceiveListener.onReceive(code);
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            // No need handle
            FirebaseUtils.addCrash(e);
        }
    }

    @Override
    public IntentFilter getFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addAction(SmsReceiver.SMS_RECEIVED);
        return intentFilter;
    }

    public static boolean isSmsCode(@NonNull final String message) {
        return clearMessage(message).matches(PATTERN_TEXT_CODE);
    }

    public static String clearMessage(@NonNull final String message) {
        return message.replace("\n", "")
                .replace("\r", "").toUpperCase();
    }

    public static String getCodeFromSms(@NonNull final String message) {
        String code = "";
        final Matcher matcher = SMS_PATTERN.matcher(clearMessage(message));
        if (matcher.matches() && matcher.groupCount() == 1) {
            code = matcher.group(1);
        }
        return code;
    }

}
