package org.pillarone.riskanalytics.graph.formeditor.ui.model

import com.ulcjava.base.application.util.ULCIcon
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
public enum EnumGraphElementInfo {

    M{
        public String getDisplayValue() {
            return "M"
        }

        public String getIcon() {
            return null
        }
    },
    C {
        public String getDisplayValue() {
            return "C"
        }

        public String getIcon() {
            return "delete-active.png"
        }
    },
    CC {
        public String getDisplayValue() {
            return "CC"
        }

        public String getIcon() {
            return null
        }
    },
    IN {
        public String getDisplayValue() {
            return "IN"
        }

        public String getIcon() {
            return null
        }
    },
    IN_PLUS {
        public String getDisplayValue() {
            return "IN (+)"
        }

        public String getIcon() {
            return null
        }
    },
    IN_PLUS_EX {
        public String getDisplayValue() {
            return "IN +!"
        }

        public String getIcon() {
            return null
        }
    },
    IN_MINUS {
        public String getDisplayValue() {
            return "IN -!"
        }

        public String getIcon() {
            return null
        }
    },
    OUT {
        public String getDisplayValue() {
            return "OUT"
        }

        public String getIcon() {
            return null
        }
    },
    UNKNOW {

    }

    public String getDisplayValue() {
        return ""
    }

    public String getIcon() {
        return null
    }

    public static EnumGraphElementInfo getEnumGraphElementInfo(String displayValue) {
        for (EnumGraphElementInfo info: EnumGraphElementInfo.values()) {
            if (info.getDisplayValue() == displayValue)
                return info
        }
        return UNKNOW
    }

}