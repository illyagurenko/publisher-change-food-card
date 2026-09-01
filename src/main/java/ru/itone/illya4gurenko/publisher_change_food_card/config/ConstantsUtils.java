package ru.itone.illya4gurenko.publisher_change_food_card.config;

public class ConstantsUtils {
    // типы строк
    public static final String POM_TYPE_HEADER = "101";
    public static final String POM_TYPE_BODY = "106";
    public static final String POM_TYPE_TRAILER = "108";

    // папки и расширения
    public static final String DIR_IN_PROGRESS = "in_progress";
    public static final String DIR_SUCCESS = "success";
    public static final String DIR_ERROR = "error";

    public static final String POINT_IN_PROGRESS = ".in_progress";
    public static final String POINT_SUCCESS = ".success";
    public static final String POINT_ERROR = ".error";

    // GruVistaTab
    public static final String GRU_CURRENCY = "222";
    public static final String GRU_FOC_STATUS_WAIT = "WAIT";
    public static final String GRU_FOC_TYPE_IMMEDIATE = "IMMEDIATE";
    public static final String GRU_FOC_TYPE_INTIME = "IN-TIME";

    public static final String ADD_INFO_ZR = "Обнуление счёта";
    public static final String ADD_INFO_CR = "Списание счёта";
    public static final String ADD_INFO_DR = "Зачисление насчёт";

    // PomUnitError коды ошибок
    public static final String ERR_CODE_HEADER = "H01";
    public static final String ERR_CODE_TRAILER = "T01";
    public static final String ERR_CODE_AUTO_INVALID = "B00";
    public static final String ERR_CODE_BODY_LEN = "B01";
    public static final String ERR_CODE_EMPTY_NAME = "B02";
    public static final String ERR_CODE_EMPTY_ACCOUNT = "B03";
    public static final String ERR_CODE_INVALID_TYPE = "B04";
    public static final String ERR_CODE_NEGATIVE_AMOUNT = "B05";
    public static final String ERR_CODE_INVALID_AMOUNT = "B06";

    // тексты ошибок
    public static final String MSG_INVALID_HEADER = "invalid header";
    public static final String MSG_INVALID_TRAILER = "trailer invalid";
    public static final String MSG_INVALID_COUNT_ROWS = "count rows invalid";
    public static final String MSG_INVALID_FILENAME = "file name invalid";
    public static final String MSG_AUTO_INVALID = "auto invalid";
    public static final String MSG_BODY_INVALID = "body invalid";
}
