package com.tr.nsergey.uchetKomplektacii;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sergey on 16.11.16.
 */

public class OpNames {
    private static Map<String, String> opNames;
    static {
        opNames = new HashMap<>();
        opNames.put("add", "Добавить на склад");
        opNames.put("remove", "Взять со склада");
        opNames.put("replace", "Ввести остаток");
        opNames.put("checkQuantity", "Проверить количество");
        opNames.put("checkSketchVersion", "Проверить версию чертежа");
    }
    public static String get(String opName){
        return opNames.get(opName);
    }
}
