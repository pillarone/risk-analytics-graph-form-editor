package org.pillarone.riskanalytics.graph.formeditor.ui.model

import com.ulcjava.base.application.util.ULCIcon
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
public enum EnumGraphElementInfo {

    M {
        public String getDisplayValue() {
            return "Model"
        }

        public String getIcon() {
            return "model.png"
        }
    },
    C {
        public String getDisplayValue() {
            return "Component"
        }

        public String getIcon() {
            return "component.png"
        }
    },
    CC {
        public String getDisplayValue() {
            return "Composed Component"
        }

        public String getIcon() {
            return "composed_component.png"
        }
    },
    IN {
        public String getDisplayValue() {
            return "In -Port - complete"
        }

        public String getIcon() {
            return "in_complete.png"
        }
    },
    IN_MORE_POSSIBLE {
        public String getDisplayValue() {
            return "In-Port - more connections possible"
        }

        public String getIcon() {
            return "in_more_possible.png"
        }
    },
    IN_MORE_NEEDED {
        public String getDisplayValue() {
            return "In-Port - more connections needed"
        }

        public String getIcon() {
            return "in_more_needed.png"
        }
    },
    IN_LESS_NEEDED {
        public String getDisplayValue() {
            return "In-Port - too many connections"
        }

        public String getIcon() {
            return "in_less_needed.png"
        }
    },
    OUT {
        public String getDisplayValue() {
            return "Out-Port"
        }

        public String getIcon() {
            return "out.png"
        }
    },
    UNKNOW {
        public String getDisplayValue() {
            return ""
        }

        public String getIcon() {
            return null
        }
    };

    public static EnumGraphElementInfo getEnumGraphElementInfo(String displayValue) {
        for (EnumGraphElementInfo info: EnumGraphElementInfo.values()) {
            if (info.getDisplayValue() == displayValue)
                return info
        }
        return UNKNOW
    }
}