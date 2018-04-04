package id.bonabrian.scious.libraryservice.model;

import id.bonabrian.scious.libraryservice.device.IconID;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public enum NotificationType {
    UNKNOWN(IconID.NOTIFICATION_GENERIC),

    AMAZON(IconID.NOTIFICATION_AMAZON),
    BBM(IconID.NOTIFICATION_BLACKBERRY_MESSENGER),
    CONVERSATIONS(IconID.NOTIFICATION_HIPCHAT),
    FACEBOOK(IconID.NOTIFICATION_FACEBOOK),
    FACEBOOK_MESSENGER(IconID.NOTIFICATION_FACEBOOK_MESSENGER),
    GENERIC_ALARM_CLOCK(IconID.ALARM_CLOCK),
    GENERIC_CALENDAR(IconID.TIMELINE_CALENDAR),
    GENERIC_EMAIL(IconID.GENERIC_EMAIL),
    GENERIC_NAVIGATION(IconID.LOCATION),
    GENERIC_PHONE(IconID.DURING_PHONE_CALL),
    GENERIC_SMS(IconID.GENERIC_SMS),
    GMAIL(IconID.NOTIFICATION_GMAIL),
    GOOGLE_HANGOUTS(IconID.NOTIFICATION_GOOGLE_HANGOUTS),
    GOOGLE_INBOX(IconID.NOTIFICATION_GOOGLE_INBOX),
    GOOGLE_MAPS(IconID.NOTIFICATION_GOOGLE_MAPS),
    GOOGLE_MESSENGER(IconID.NOTIFICATION_GOOGLE_MESSENGER),
    GOOGLE_PHOTOS(IconID.NOTIFICATION_GOOGLE_PHOTOS),
    HIPCHAT(IconID.NOTIFICATION_HIPCHAT),
    INSTAGRAM(IconID.NOTIFICATION_INSTAGRAM),
    KAKAO_TALK(IconID.NOTIFICATION_KAKAOTALK),
    KIK(IconID.NOTIFICATION_KIK),
    LIGHTHOUSE(IconID.NOTIFICATION_LIGHTHOUSE),
    LINE(IconID.NOTIFICATION_LINE),
    LINKEDIN(IconID.NOTIFICATION_LINKEDIN),
    MAILBOX(IconID.NOTIFICATION_MAILBOX),
    OUTLOOK(IconID.NOTIFICATION_OUTLOOK),
    RIOT(IconID.NOTIFICATION_HIPCHAT),
    SIGNAL(IconID.NOTIFICATION_HIPCHAT),
    SKYPE(IconID.NOTIFICATION_SKYPE),
    SLACK(IconID.NOTIFICATION_SLACK),
    SNAPCHAT(IconID.NOTIFICATION_SNAPCHAT),
    TELEGRAM(IconID.NOTIFICATION_TELEGRAM),
    TRANSIT(IconID.LOCATION),
    TWITTER(IconID.NOTIFICATION_TWITTER),
    VIBER(IconID.NOTIFICATION_VIBER),
    WECHAT(IconID.NOTIFICATION_WECHAT),
    WHATSAPP(IconID.NOTIFICATION_WHATSAPP),
    YAHOO_MAIL(IconID.NOTIFICATION_YAHOO_MAIL);

    public final int icon;

    NotificationType(int icon) {
        this.icon = icon;
    }

    public String getFixedValue() {
        return name().toLowerCase();
    }

    public String getGenericType() {
        switch (this) {
            case GENERIC_EMAIL:
            case GENERIC_NAVIGATION:
            case GENERIC_SMS:
            case GENERIC_ALARM_CLOCK:
                return getFixedValue();
            case FACEBOOK:
            case TWITTER:
            case SNAPCHAT:
            case INSTAGRAM:
            case LINKEDIN:
                return "generic_social";
            case CONVERSATIONS:
            case FACEBOOK_MESSENGER:
            case RIOT:
            case SIGNAL:
            case TELEGRAM:
            case WHATSAPP:
            case GOOGLE_MESSENGER:
            case GOOGLE_HANGOUTS:
            case HIPCHAT:
            case SKYPE:
            case WECHAT:
            case KIK:
            case KAKAO_TALK:
            case SLACK:
                return "generic_chat";
            case GMAIL:
            case GOOGLE_INBOX:
            case MAILBOX:
            case OUTLOOK:
            case YAHOO_MAIL:
                return "generic_email";
            case LINE:
            case VIBER:
                return "generic_phone";
            case UNKNOWN:
            default:
                return "generic";
        }
    }
}
